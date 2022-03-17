import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Node {

	public static void main(String[] args) {
		try {
			System.out.println("About to connect");
			Socket s = new Socket("localhost", 7000);
			System.out.println("Connected");

			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

			int[] x = (int[]) ois.readObject();

			
			
			int g = getPrimes(x);

			
			oos.writeObject(g);

			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getPrimes(int[] x) {
		int ret = 0;
		System.out.println(x[0] + " " + x[1]);
		for (int i = x[0]; i < x[1]; i++) {
			if (isPrime(i)) {
				ret++;
			}
		}
		System.out.println(ret);
		return ret;
	}

	public static boolean isPrime(int num) {
		for (int i = 2; i < num / 2; i++) {
			if (num % i == 0) {
				return true;
			}
		}
		return false;
	}
}
