import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.ServerSocket;

public class Head {
	static int[] primelist = new int[4];

	class nodeWork implements Runnable {
		int[] range;
		Socket s;
		int listpos;

		public nodeWork(int[] rng, Socket s, int listpos) {
			range = rng;
			this.s = s;
			this.listpos = listpos;
		}

		@Override
		public void run() {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				oos.writeObject(range);
				primelist[listpos] = (int) ois.readObject();
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Head d = new Head();
		try {
			System.out.println("test");
			ServerSocket ss = new ServerSocket(7000);
			int expectedNodes = 4;
			int currentNodes = 0;
			Socket[] slist = new Socket[expectedNodes];
			int primes = 0;

			while (true) {
				System.out.println("Waiting for connection");
				Socket s = ss.accept();
				System.out.println("Connection accepted");
				slist[currentNodes] = s;
				currentNodes++;
				System.out.println("Current connected Nodes: " + currentNodes);

				if (currentNodes == expectedNodes) {

					int start = 1000;
					int end = 1000000;

					Thread[] ths = new Thread[currentNodes];

					for (int i = 0; i < currentNodes; i++) {
						int[] ar = new int[] { (start + ((end-start) / currentNodes) * i), (start + ((end-start) / currentNodes) * (i+1))};
						System.out.println(ar[0] + "-" + ar[1]);
						
						
						nodeWork nw = d.new nodeWork(ar, slist[i], i);

						Thread th = new Thread(nw);
						ths[i] = th;
						th.start();
					}

					for (int i = 0; i < currentNodes; i++) {
						try {
							ths[i].join();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					for (int i = 0; i < currentNodes; i++) {
						primes += d.primelist[i];
					}

					System.out.println(primes);

					ss.close();
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
