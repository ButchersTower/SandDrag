package holyBowlyRavioli;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Panel extends JPanel implements Runnable, KeyListener,
		MouseListener, MouseMotionListener {

	int width = 700;
	int height = 450;

	Image[] imageAr;

	Thread thread;
	Image image;
	static Graphics g;

	// Vars for gLoop Below
	public int tps = 20;
	public int milps = 1000 / tps;
	long lastTick = 0;
	int sleepTime = 0;
	long lastSec = 0;
	int ticks = 0;
	long startTime;
	long runTime;
	private long nextTick = 0;
	private boolean running = false;

	// Vars for gLoop Above

	boolean mouseP;
	int[] mouseLoc;

	float[][][] houseShape = { { { 80, -30 }, { 80, -70 } },
			{ { 80, -70 }, { 0, -130 } }, { { 0, -130 }, { -90, -80 } },
			{ { -90, -80 }, { -90, 80 } }, { { -90, 80 }, { 0, 130 } },
			{ { 0, 130 }, { 80, 70 } }, { { 80, 70 }, { 80, 30 } } };

	float[][][] allWalls = { { { 80, -30 }, { 80, -70 } },
			{ { 80, -70 }, { 0, -130 } }, { { 0, -130 }, { -90, -80 } },
			{ { -90, -80 }, { -90, 80 } }, { { -90, 80 }, { 0, 130 } },
			{ { 0, 130 }, { 80, 70 } }, { { 80, 70 }, { 80, 30 } } };

	// float[][][] allWalls = { { { 360, 140 }, { 280, 60 } },
	// { { 360, 140 }, { 360, 220 } }, { { 364, 140 }, { 340, 220 } } };

	public Panel() {
		super();

		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		requestFocus();
	}

	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void run() {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		this.setSize(new Dimension(width, height));

		startTime = System.currentTimeMillis();

		gStart();
	}

	/**
	 * Methods go below here.
	 * 
	 */

	Player player;

	public void gStart() {
		imageInit();

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		player = new Player(new float[] { 60, -60 });
		float[] add = { 400, 200 };
		for (int al = 0; al < allWalls.length; al++) {
			allWalls[al][0] = VeMa.vectAdd(allWalls[al][0], add);
			allWalls[al][1] = VeMa.vectAdd(allWalls[al][1], add);
		}
		player.setWalls(allWalls);

		running = true;
		gLoop();
	}

	public void gLoop() {
		while (running) {
			// Do the things you want the gLoop to do below here

			if (mouseP) {
				System.out.println("**tick**");
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, width, height);
				for (int i = 0; i < allWalls.length; i++) {
					drawLine(Color.RED, allWalls[i][0], allWalls[i][1]);
				}
				int redCol = 0;
				for (int i = 0; i < allWalls.length; i++) {
					// System.out.println("i: " + i);
					redCol += 30;
					drawCircle(new Color(255 - redCol, redCol, 0),
							allWalls[i][0][0], allWalls[i][0][1], 6);
				}
				float[] tempML = { mouseLoc[0], mouseLoc[1] };
				player.VeToWa(player.moveSpeed, tempML, -1, new int[] {},
						false, tempML, false);
				drawPlayer();
			}

			// And above here.
			drwGm();

			ticks++;
			// Runs once a second and keeps track of ticks;
			// 1000 ms since last output
			if (timer() - lastSec > 1000) {
				if (ticks < tps - 1 || ticks > tps + 1) {
					if (timer() - startTime < 2000) {
						System.out.println("Ticks this second: " + ticks);
						System.out.println("timer(): " + timer());
						System.out.println("nextTick: " + nextTick);
					}
				}

				ticks = 0;
				lastSec = (System.currentTimeMillis() - startTime);
			}

			// Used to protect the game from falling beind.
			if (nextTick < timer()) {
				nextTick = timer() + milps;
			}

			// Limits the ticks per second
			if (timer() - nextTick < 0) {
				sleepTime = (int) (nextTick - timer());
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					System.out.println("----Sleep Error----");
					e.printStackTrace();
				}

				nextTick += milps;
			}
		}
	}

	void drawPlayer() {
		drawCircle(Color.BLACK, player.playLoc[0], player.playLoc[1],
				player.radius);
	}

	static void drawCircle(Color color, float[] point, float radius) {
		g.setColor(color);
		g.drawOval((int) (point[0] - radius), (int) (point[1] - radius),
				(int) (2 * radius), (int) (2 * radius));
	}

	static void drawCircle(Color color, float x, float y, float radius) {
		g.setColor(color);
		g.drawOval((int) (x - radius), (int) (y - radius), (int) (2 * radius),
				(int) (2 * radius));
	}

	static void drawLine(Color color, float[] point0, float[] point1) {
		g.setColor(color);
		g.drawLine((int) point0[0], (int) point0[1], (int) point1[0],
				(int) point1[1]);
	}

	static void drawLine(Color color, float p1x, float p1y, float p2x, float p2y) {
		g.setColor(color);
		g.drawLine((int) p1x, (int) p1y, (int) p2x, (int) p2y);
	}

	/**
	 * Methods go above here.
	 * 
	 */

	public long timer() {
		return System.currentTimeMillis() - startTime;

	}

	public void drwGm() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	public void imageInit() {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		mouseLoc = new int[] { me.getX(), me.getY() };
		drwGm();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
		// drawMost();
		System.out.println("***CLICK***");
		if (me.getButton() == MouseEvent.BUTTON1) {
			mouseLoc = new int[] { me.getX(), me.getY() };
			mouseP = true;
		} else if (me.getButton() == MouseEvent.BUTTON3) {
			System.out.println(player.isLeft(10, 10, 30, 20, 30, 10));
		}
		drwGm();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (me.getButton() == MouseEvent.BUTTON1) {
			mouseP = false;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
