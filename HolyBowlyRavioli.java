package holyBowlyRavioli;

import javax.swing.JFrame;

public class HolyBowlyRavioli {
	public HolyBowlyRavioli() {
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
		new HolyBowlyRavioli();
	}
}
