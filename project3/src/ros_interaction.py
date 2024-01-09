#!/usr/bin/env python3

from gui import GUI
import rospy
from geometry_msgs.msg import Twist, Pose
from nav_msgs.msg import Odometry
from threading import Thread

# ActionLib
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib


class ROSInteraction:
    gui = None

    def __init__(self):
        self.init_node()
        self.init_publisher()
        self.init_subscriber()
        self.init_action_client()
        self.gui = GUI(
            direction_key_callback=self.direction_key_callback,
            coordinate_callback=self.coordinate_callback,
        )
        self.gui.run()

    def init_node(self):
        ###
        ### TODO: Initialize the ROS node here
        ###
        pass

    def init_publisher(self):
        ###
        ### TODO: Initialize the publisher here
        ###
        pass

    def init_subscriber(self):
        ###
        ### TODO: Initialize the subscriber here
        ###
        pass

    def init_action_client(self):
        ###
        ### TODO: Initialize the action client here
        ###
        pass

    def direction_key_callback(self, direction):
        ###
        ### TODO: Use the publisher to send the direction to the robot
        ###
        pass

    def coordinate_callback(self, x, y):
        thread = Thread(target=self.coordinate_callback_thread, args=(x, y))
        thread.start()

    def coordinate_callback_thread(self, x, y):
        ###
        ### TODO: Use the action client to send the coordinate to the robot
        ###
        pass

    def subscription_callback(self, odometry):
        if self.gui is not None:
            pose = odometry.pose.pose
            self.gui.update_position(pose.position.x, pose.position.y, pose.position.z)
