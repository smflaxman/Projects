package total;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class Painter extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	// local variables for Painter stuff
	PaintingPanel p;
	boolean isLine = false;
	Point mousePos;
	private Color objClr;
	String name;
	JTextField jta;
	JTextArea jtat;

	// other variables for server stuff
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public Painter() {
		// get the user's name for their chat thing
		name = JOptionPane.showInputDialog("Enter your name");

		// Holds entire GUI thing
		JPanel holder = new JPanel();
		holder.setLayout(new BorderLayout());

		// Holds color selectors on left side
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(3, 1));

		// Red paint button
		JButton redPaint = new JButton();
		redPaint.addActionListener(this);
		redPaint.setActionCommand("red");
		redPaint.setBackground(Color.RED);
		redPaint.setOpaque(true);
		redPaint.setBorderPainted(false);
		leftPanel.add(redPaint);
		// Green paint button
		JButton greenPaint = new JButton();
		greenPaint.addActionListener(this);
		greenPaint.setActionCommand("green");
		greenPaint.setBackground(Color.GREEN);
		greenPaint.setOpaque(true);
		greenPaint.setBorderPainted(false);
		leftPanel.add(greenPaint);
		// Blue paint button
		JButton bluePaint = new JButton();
		bluePaint.addActionListener(this);
		bluePaint.setActionCommand("blue");
		bluePaint.setBackground(Color.BLUE);
		bluePaint.setOpaque(true);
		bluePaint.setBorderPainted(false);
		leftPanel.add(bluePaint);
		// Add color button panel to GUI holder
		holder.add(leftPanel, BorderLayout.WEST);

		// avoid errors when user does not press button before drawing by setting
		// default color to red
		objClr = Color.RED;

		// Holds 'line' and 'circle' option buttons on upper part of gui
		JPanel upPanel = new JPanel();
		upPanel.setLayout(new FlowLayout());

		// Line button
		JButton lineBut = new JButton("Line");
		lineBut.addActionListener(this);
		lineBut.setActionCommand("line");
		upPanel.add(lineBut);
		// Circle button
		JButton circleBut = new JButton("Circle");
		circleBut.addActionListener(this);
		circleBut.setActionCommand("circle");
		upPanel.add(circleBut);
		// Add shape button panel to holder
		holder.add(upPanel, BorderLayout.NORTH);

		// Center panel for PaintingPanel
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(1, 1));
		p = new PaintingPanel();
		centerPanel.add(p);
		holder.add(centerPanel, BorderLayout.CENTER);

		// chat window
		JPanel southPanel = new JPanel();
		// setting up layout to make it look nice
		southPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		// input section for chat
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		jta = new JTextField("Input your message here");
		southPanel.add(jta, gbc);
		// button to send chat message
		gbc.gridx = 1;
		gbc.gridy = 0;
		JButton chatBut = new JButton("Send");
		chatBut.addActionListener(this);
		chatBut.setActionCommand("Message");
		southPanel.add(chatBut, gbc);
		// window where chat is displayed
		jtat = new JTextArea("");
		jtat.setEditable(false);
		JScrollPane jsp = new JScrollPane(jtat);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.ipady = 40;
		gbc.ipadx = 500;
		// gbc.gridwidth = 600;
		southPanel.add(jsp, gbc);

		holder.add(southPanel, BorderLayout.SOUTH);

		addMouseListener(this);

		// Connect holder to JFrame
		setContentPane(holder);
		setResizable(false);

		// Adjust size so window opens at correct size
		setSize(600, 600);

		// Make JFrame content visible on screen
		setVisible(true);

		/*
		 * After window correctly displays attempt to connect to server
		 */
		try {
			System.out.println("About to connect");
			s = new Socket("localhost", 7000);
			System.out.println("Connected");
		} catch (Exception e) {
			System.out.println("Error connecting to server");
			System.exit(0);
		}

		// get everything already displayed on painting by other users and draw it to
		// this painter once connected
		try {
			ois = new ObjectInputStream(s.getInputStream());
			ArrayList<PaintingPrimitive> painted = (ArrayList<PaintingPrimitive>) ois.readObject();
			for (PaintingPrimitive pp : painted) {
				p.addPrimitive(pp);
			}
		} catch (Exception e1) {
			System.out.println("Error drawing shapes received from server");
		}

		/*
		 * listen for incoming objects and draw them to panel when received or write
		 * them in chat window if applicable
		 */
		while (true) {
			Circle c = new Circle(null, null, null);
			Line l = new Line(null, null, null);
			String tem = "";
			try {
				ois = new ObjectInputStream(s.getInputStream());
				Object obj = ois.readObject();
				if (obj.getClass().equals(c.getClass()) || obj.getClass().equals(l.getClass())) {
					PaintingPrimitive pp = (PaintingPrimitive) obj;
					p.addPrimitive(pp);
				} else if (obj.getClass().equals(tem.getClass())) {
					jtat.append((String) obj + "\n");
				}
			} catch (Exception e) {
				System.out.println("server has closed. exiting painter.");
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {
		new Painter();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePos = new Point((int) e.getPoint().getX() - 40, (int) e.getPoint().getY() - 65);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// needs to be in try+catch to make server things happy
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			if (isLine) {
				Line l = new Line(mousePos, new Point((int) e.getPoint().getX() - 40, (int) e.getPoint().getY() - 65),
						objClr);
				if (s != null) {
					oos.writeObject(l);
				} else {
					p.addPrimitive(l);
				}
			} else {
				Circle c = new Circle(mousePos,
						new Point((int) e.getPoint().getX() - 40, (int) e.getPoint().getY() - 65), objClr);
				if (s != null) {
					oos.writeObject(c);
				} else {
					p.addPrimitive(c);
				}
			}
		} catch (Exception ex) {
			System.out.println("Error writing shape to server");
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("red")) {
			objClr = Color.RED;
		} else if (e.getActionCommand().equals("blue")) {
			objClr = Color.BLUE;
		} else if (e.getActionCommand().equals("green")) {
			objClr = Color.GREEN;
		} else if (e.getActionCommand().equals("line")) {
			isLine = true;
		} else if (e.getActionCommand().equals("circle")) {
			isLine = false;
		} else if (e.getActionCommand().equals("Message")) {
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(name + ": " + jta.getText());
				jta.setText("");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
