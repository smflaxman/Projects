#!/usr/bin/env python

#Collaborated with Lukas Rodwin

#import statements
import rospy
import random
import time
import pickle
import os
import matplotlib.pyplot as plt
import numpy as np
import math
from std_srvs.srv import Empty
from geometry_msgs.msg import Twist
from sensor_msgs.msg import LaserScan
from gazebo_msgs.msg import ModelState
from gazebo_msgs.srv import SetModelState
from statistics import mode

#q table states and actions
#            -      -
#            -    -
#            -  -
#            R  -  -  -
#            
#       (R = robot)     
#state components:
#f1 = front close, f2 = front mid, f3 = front far, fr1 = front right 1, fr2 front right mid
#fr3 = front right far, r1 = right close, r2 = right mid, r3 = right far
#states:
#f fr r
#111, 112, 113, 121, 122, 123, 131, 132, 133
#211, 212, 213, 221, 222, 223, 231, 232, 233
#311, 312, 313, 321, 322, 323, 331, 332, 333
#l = 0, r = 1, f = 2

rewards = np.full(27, -2)
qtable = np.zeros((27, 3))
stateindex = [111, 112, 113, 121, 122, 123, 131, 132, 133, 211, 212, 213, 221, 222, 223, 231, 232, 233, 311, 312, 313, 321, 322, 323, 331, 332, 333]

#laserscan subscriber callback function
def callback(msg):
    q_setup(msg)
    #rospy.loginfo("callback finished")

#setup what happens based on train variable state
def q_setup(msg):
    #if training, then update qtable, otherwise just read qtable from file and act on it
    if (train): 
        qlearn(msg)
    else:
        qmove(msg)

#get the robot's current state
def get_state(msg):
    #ranges which the robot should try to stay within
    close_range = .4
    far_range = .6
    #0 = right, 1 = right front, 2 = front
    lasers = [[],[],[]]
    #get the substate of each wall with respect to the robot
    rmin = 10
    rfmin = 10
    fmin = 10

    for i in range (len(msg.ranges)):
        if(not math.isinf(msg.ranges[i]) and not msg.ranges[i] == 0):
            #right
            if(i > 255 and i < 300):
                if(msg.ranges[i] < rmin):
                    rmin = msg.ranges[i]
            #right front
            elif(i > 300 and i < 345):
                if(msg.ranges[i] < rfmin):
                    rfmin = msg.ranges[i]
            #front
            elif(i > 345 or i < 30):
                if(msg.ranges[i] < fmin):
                    fmin = msg.ranges[i]

    #right
    if(rmin < close_range):
        rmin = 1
    elif(rmin > close_range and rmin < far_range):
        rmin = 2
    else:
        rmin = 3
    #right front
    if(rfmin < close_range):
        rfmin = 1
    elif(rfmin > close_range and rfmin < far_range):
        rfmin = 2
    else:
        rfmin = 3
    #front
    if(fmin < close_range):
        fmin = 1
    elif(fmin > close_range and fmin < far_range):
        fmin = 2
    else:
        fmin = 3
    
    #combine the substates together to get the final state
    state = int(str(fmin)+str(rfmin)+str(rmin))

    return state

#just moved based off the created qtable
def qmove(msg):
    global action
    #open qtable file and read qtable from it
    #qtable itself created and stored in ~/root/.ros folder
    qfile = open("qtable.pkl", "rb")
    fileqtable = pickle.load(qfile)
    qtable = fileqtable
    qfile.close() 
    
    state = get_state(msg)
    qindex = stateindex.index(state)
    action = np.argmax(qtable[qindex])
    rospy.loginfo(state)
    #rospy.loginfo(action)

#qlearning algorithm
def qlearn(msg):
    #number of episodes that should occur
    global episodes
    #counts number of times in consecutive bad state
    global wallcounter
    #count how many states have been read this episode
    global timer
    #increments after every correct chosen action
    global correctactions
    #increments after every action
    global totalactions
    #list of ratio values comparing correct actions to total actions in a single episode
    global ratios
    #list of number of episodes, for graphing
    global iterations
    #in order to tell the persistent moving function what to do
    global action

    #when finished training, show graph
    if(episodecount == episodes):
        rospy.loginfo("finished training for specified number of episodes!")
        plt.plot(iterations, ratios)
        plt.autoscale()
        #pause physics
        rospy.wait_for_service('/gazebo/pause_physics')
        pause_physics = rospy.ServiceProxy('/gazebo/pause_physics', Empty)
        pause_physics()
        plt.show()

    #variables to use while learning
    epsilon = 0.9
    discount = 0.8
    rate = 0.2
    decay = .001

    #open qtable file and read qtable from it
    qfile = open("qtable.pkl", "rb")
    fileqtable = pickle.load(qfile)
    qtable = fileqtable
    qfile.close() 

    #get state of the robot
    state = get_state(msg)
    #rospy.loginfo(state)

    #if against wall for too long, then end episode
    if(state == 333 or state == 113 or state == 112 or state == 111):
        wallcounter = wallcounter + 1
        if(wallcounter >= 20):
            if(state == 333):
                if(wallcounter >= 40):
                    wallcounter = 0
                    timer = 0
                    newepisode()
            else:
                wallcounter = 0
                timer = 0
                newepisode()
    else:
        wallcounter = 0
    #if the episode has gone on for too long, then end the episode
    if(timer < 300):
        timer += 1
    else:
        timer = 0
        wallcounter = 0
        newepisode()

    #get index of state in qtable
    qindex = stateindex.index(state)

    #epsilon greedy
    if(random.random() > epsilon):
        action = np.argmax(qtable[qindex])
    else:
        action = np.random.randint(3)
    epsilon = epsilon - decay

    #calculate temporal difference
    reward = rewards[qindex]
    oldqval = qtable[qindex][action]
    temporaldif = reward + (discount * (np.max(qtable[qindex]))) - oldqval

    #actually change the q value in the table
    newqval = oldqval + (rate * temporaldif)
    qtable[qindex][action] = newqval

    #collect data about actions taken and compare them to 'correct' actions for those states
    if (state == 322):
        if (action == 1):
            totalactions += 1
            correctactions += 1
        else:
            totalactions += 1
    if (state == 222):
        if(action == 0):
            totalactions += 1
            correctactions += 1
        else:
            totalactions += 1
    if (state == 333):
        if(action == 1):
            totalactions += 1
            correctactions += 1
        else:
            totalactions += 1

    #write qtable to file
    writefile = open('qtable.pkl', 'wb')
    pickle.dump(qtable, writefile)
    writefile.close()

def move(move_to):
    #how long commands will execute for before checking state again
    how_long = 0
    #how quickly the robot moves
    intensity = 0.2
    #unpause_phys = rospy.ServiceProxy('/gazebo/unpause_physics', Empty)

    if(move_to == 0):
        #move left
        twist(.05, .4)
        rospy.sleep(how_long)
        #twist(0,0)
    if(move_to == 1):
        #move right
        twist(.05, -.4)
        rospy.sleep(how_long)
        #twist(0,0)
    if(move_to == 2):
        #move forward
        twist(.1, 0.0)
        rospy.sleep(how_long)
        #twist(0,0)

#puts the robot into a new position when the end of an episode is reached or at the total start of the algorithm
def newepisode():
    #total actions taken this episode
    global totalactions
    #correct actions taken this episode
    global correctactions
    #list of ratios of correct actions to total actions in each episode, for graphing
    global ratios
    #list of all episode counts, for graphing
    global iterations
    #current episodes completed
    global episodecount

    ratios = np.append(ratios, (correctactions/totalactions))
    iterations = np.append(iterations, episodecount)

    episodecount = episodecount + 1

    #randomly choose from a set number of set starting training states
    startstate = random.randint(0,2)
    #

    #two different starting states
    state_msg = ModelState()
    state_msg.model_name = 'turtlebot3_waffle_pi'
    #left side facing downwards
    if(startstate == 0):
        state_msg.pose.position.x = -0.7
        state_msg.pose.position.y = 1.9
        state_msg.pose.position.z = 0
        state_msg.pose.orientation.x = 0
        state_msg.pose.orientation.y = 0
        state_msg.pose.orientation.z = 1
        state_msg.pose.orientation.w = 0
    #top part facing left, other side of the middle wall
    if(startstate == 1):
        state_msg.pose.position.x = 0.5
        state_msg.pose.position.y = -2.0
        state_msg.pose.position.z = 0
        state_msg.pose.orientation.x = 0
        state_msg.pose.orientation.y = 0
        state_msg.pose.orientation.z = 1
        state_msg.pose.orientation.w = 20
    #handling outward pointing corner
    if(startstate == 2):
        state_msg.pose.position.x = -0.6
        state_msg.pose.position.y = -2.0
        state_msg.pose.position.z = 0
        state_msg.pose.orientation.x = 0
        state_msg.pose.orientation.y = 0
        state_msg.pose.orientation.z = 1
        state_msg.pose.orientation.w = 1

    #actually set the robot to this state
    rospy.wait_for_service('/gazebo/set_model_state')
    try:
        set_state = rospy.ServiceProxy('/gazebo/set_model_state', SetModelState)
    except(rospy.ServiceException) as e: 
        rospy.loginfo("call failed")
    set_state(state_msg)

    #logging progress of episodes
    rospy.loginfo("Episode %d/%d", episodecount, episodes)
       
#function for moving robot
def twist(linx, angz):
    twist = Twist()
    rate = rospy.Rate(10)
    twist.linear.x = linx
    twist.angular.z = angz
    pub.publish(twist)
    rate.sleep()
               
def initialize():
    #publisher for robot movement, used in twist function
    global pub    
    global train
    #train mode = 1, not train mode = 0
    train = 1
    global episodes
    #number of episodes to perform (change this number)
    episodes = 800
    global episodecount
    #counter for current episode
    episodecount = 0
    global wallcounter 
    #counter for the number of times the wall hitting states persist
    wallcounter = 0
    #count until a set number of iterations for an episode to occur
    global timer
    timer = 0
    #variables used for gathering learning data
    global correctactions
    correctactions = 0
    global totalactions
    totalactions = 1
    global iterations
    iterations = np.array([episodecount])
    global ratios
    ratios = np.array([0])
    #global varable of the current action 
    global action
    action = 333
   
    #initialize the node itself and all subscribers and publishers
    rospy.wait_for_service('/gazebo/reset_simulation')
    reset_sim = rospy.ServiceProxy('gazebo/reset_simulation', Empty)
    reset_sim()

    #initialize itself
    rospy.init_node('wallfollow')

    #put the robot in a starting position
    newepisode()
    episodecount = 0

    #if a qtable file does not already exist then make one and give it an empty table
    #stored in root/.ros/
    if(os.path.isfile('qtable.pkl')):
        rospy.loginfo("existing qtable file found!")
    else:
        rospy.loginfo("No qtable file found! creating new file...")
        qtbl = open("qtable.pkl", 'wb')
        pickle.dump(qtable, qtbl)
        qtbl.close()
        rospy.loginfo("new qtable successfully created")

    #finish setting up the state vs reward array
    # 322 good
    rewards[21] = 0
    # 332 good
    rewards[25] = 0
    # 222 good
    #rewards[13] = 0
    # 221 good
    #rewards[12] = 0
    # 311 good
    #rewards[18] = 0
    # 233 not that bad
    #rewards[17] = -1

    #initialize laser data receiving and movement publisher
    rospy.Subscriber('/scan', LaserScan, callback)
    pub = rospy.Publisher('/cmd_vel', Twist, queue_size = 10)

    #movement loop
    while (not rospy.is_shutdown()):
        #unpause - don't do this with the actual robot!
        rospy.wait_for_service('/gazebo/unpause_physics')
        unpause_physics = rospy.ServiceProxy('/gazebo/unpause_physics', Empty)
        unpause_physics()

        #move
        move(action)

        #pause - don't do this with the actual robot!
        rospy.wait_for_service('/gazebo/pause_physics')
        pause_physics = rospy.ServiceProxy('/gazebo/pause_physics', Empty)
        pause_physics()   
    rospy.spin()

if __name__ == '__main__':
    initialize()