package total;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//Overarching class to manage painters and allow for painting over socketed connections
public class Hub {
	// global variables for node stuff+interaction with hub
	static ArrayList<Socket> nodes;
	static ArrayList<PaintingPrimitive> shapes;
	static ArrayList<Thread> threads;
	static int socketCounter;

	/*
	 * class to handle painter input; receiving things from other painters and
	 * updating their boards accordingly
	 * 
	 * Socket s; - the socket in which this instance of the input handler will
	 * operate
	 */
	class painterHandler implements Runnable {
		Socket s;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		int socknum;
		Hub h;

		public painterHandler(Socket s, int snum, Hub h) {
			this.s = s;
			socknum = snum;
			this.h = h;
		}

		@Override
		public void run() {
			// default objects for class comparison
			Object obj;
			Line l = new Line(null, null, null);
			Circle c = new Circle(null, null, null);
			String tem = "";
			while (true) {
				try {
					// listen for things
					ois = new ObjectInputStream(s.getInputStream());
					obj = ois.readObject();
					// listen for shapes
					if (obj.getClass().equals(c.getClass()) || obj.getClass().equals(l.getClass())) {
						PaintingPrimitive pp = (PaintingPrimitive) obj;
						shapes.add(pp);
						for (int i = 0; i < h.nodes.size(); i++) {
							oos = new ObjectOutputStream(h.nodes.get(i).getOutputStream());
							oos.writeObject(pp);
						}
					}
					// listen for messages and send them to all sockets accordingly
					else if (obj.getClass().equals(tem.getClass())) {
						String mes = (String) obj;
						for (int i = 0; i < h.nodes.size(); i++) {
							oos = new ObjectOutputStream(h.nodes.get(i).getOutputStream());
							oos.writeObject(obj);
						}
					}
				} catch (Exception e) {
					// if we get an error (namely connection reset) then close the socket and remove
					// it from the ones we need to update and end the threadF
					try {
						s.close();
						System.out.println("Painter " + socknum + " has disconnected");
					} catch (IOException e1) {
						System.out.println("error closing socket");
					}
					nodes.remove(s);
					return;
				}
			}
		}
	}

	/*
	 * 
	 * Class to handle painter output (shapes and messages and stuff) and help
	 * distribute it across all connected painters
	 * 
	 * Socket s; the socket in which this instance of the input handler will operate
	 */
	class painterOutputHandler implements Runnable {
		Socket s;

		public painterOutputHandler(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			while (true) {

			}
		}

	}

	public static void main(String[] args) {
		System.out.println("The purpose of this message is to let me close the server from the console window");

		// set currently connected socket count to 0
		socketCounter = 0;

		// initialize arraylist of threads
		threads = new ArrayList<Thread>();

		// I don't really have any idea why java wants this to happen, but if it lets me
		// use the other class in here I don't really care
		Hub h = new Hub();

		// Initialize global variable; doesn't need to be in try+catch
		nodes = new ArrayList<Socket>();

		// large try+catch to make server stuff happy
		try {
			// setup server on port 7000
			ServerSocket ss = new ServerSocket(7000);

			// initialize the array of shapes drawn on the canvas to avoid null things when
			// passing to the first painter
			shapes = new ArrayList<PaintingPrimitive>();

			while (true) {
				/*
				 * Wait until painter connects to hub and accept connection and add to list of
				 * things connected
				 */
				Socket s = ss.accept();
				nodes.add(s);

				// one socket connected immediately give it the current list of things that have
				// been drawn so it can draw them to the new user, if the array is not currently
				// empty
				ObjectOutputStream oiss = new ObjectOutputStream(s.getOutputStream());
				oiss.writeObject(shapes);

				// creates a new thread that manages the new painter
				Thread tt = new Thread(h.new painterHandler(s, socketCounter, h));
				socketCounter++;
				tt.start();
				threads.add(tt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
