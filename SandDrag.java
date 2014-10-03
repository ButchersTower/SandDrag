package SandDrag;

import javax.swing.JFrame;

public class SandDrag {
	public SandDrag() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Panel());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		// frame.setLocationRelativeTo();
		frame.setTitle("HolyBowlyRavioli");
	}

	public static void main(String[] args) {
		new SandDrag();
	}
}
