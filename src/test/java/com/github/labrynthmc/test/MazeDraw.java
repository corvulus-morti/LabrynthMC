package com.github.labrynthmc.test;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Cell;
import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.mazegen.Grid;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static com.github.labrynthmc.util.MazeDrawUpdateHandler.PORT;

public class MazeDraw extends JFrame {
	public static void main(String[] args) {
		new MazeDraw();
	}

	/** Whether  an image of the maze should be saved on generation */
	private static boolean SAVE_IMAGE = false;

	private MazeCanvas mazeCanvas;
	private PlayerPosition playerPosition = new PlayerPosition();
	private JTextField seedTextField;

	private MazeDraw() {

		seedTextField = new JTextField();
		seedTextField.setPreferredSize(new Dimension(500, 32));
		seedTextField.setToolTipText("Seed");
		seedTextField.setText(new Random().nextLong() + "");
		JTextField paths = new JTextField();
		paths.setPreferredSize(new Dimension(50, 32));
		paths.setToolTipText("Max paths");
		paths.setText(Labrynth.MAX_PATHS + "");

		JPanel panel = new JPanel();
		JPanel mazePanel = new JPanel();

		JButton drawButton = new JButton();
		drawButton.setText("Draw");
		drawButton.addActionListener(e -> {
			mazeCanvas.setSeed(Long.parseLong(seedTextField.getText()));
			mazeCanvas.setMaxPaths(Integer.parseInt(paths.getText()));
			mazeCanvas.regenMaze();
		});

		BorderLayout borderLayout = new BorderLayout();
		mazePanel.setLayout(borderLayout);

		panel.add(seedTextField);
		panel.add(paths);
		panel.add(drawButton);

		mazePanel.add(panel, BorderLayout.NORTH);
		mazeCanvas = new MazeCanvas(Long.parseLong(seedTextField.getText()), Integer.parseInt(paths.getText()));
		JScrollPane scrollPane = new JScrollPane(mazeCanvas);
		mazePanel.add(scrollPane, BorderLayout.CENTER);

		add(mazePanel);

		new PlayerPositionListener(playerPosition, mazeCanvas);

		setVisible(true);
		pack();
		setSize(getWidth(), 750);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	}

	private class MazeCanvas extends JPanel {

		Grid grid;

		int leftx = Integer.MAX_VALUE;
		int rightx = Integer.MIN_VALUE;
		int topy = Integer.MAX_VALUE;
		int bottomy = Integer.MIN_VALUE;

		int width = rightx - leftx;
		int height = bottomy - topy;

		public long getSeed() {
			return seed;
		}

		public void setSeed(long seed) {
			this.seed = seed;
		}

		public int getMaxPaths() {
			return maxPaths;
		}

		public void setMaxPaths(int maxPaths) {
			this.maxPaths = maxPaths;
		}

		long seed;
		int maxPaths;

		public MazeCanvas(long seed, int maxPaths) {
			this.seed = seed;
			this.maxPaths = maxPaths;
			regenMaze();
		}

		public void regenMaze() {
			grid = Grid.genMaze(seed, maxPaths);

			leftx = Integer.MAX_VALUE;
			rightx = Integer.MIN_VALUE;
			topy = Integer.MAX_VALUE;
			bottomy = Integer.MIN_VALUE;
			for (Coords c : grid.getKeys()) {
				leftx = Math.min(leftx, c.getX());
				rightx = Math.max(rightx, c.getX());
				topy = Math.min(topy, c.getY());
				bottomy = Math.max(bottomy, c.getY());
			}
			setPreferredSize(new Dimension((rightx - leftx) * 10 + 40, (bottomy - topy) * 10 + 40));
			setVisible(true);
			repaint();
		}

		@Override
		public void paint(Graphics g2) {
			super.paint(g2);
			Graphics g = g2;
			BufferedImage bi = null;
			if (SAVE_IMAGE) {
				bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				g = bi.createGraphics();
			}
			g.setColor(Color.BLACK);
			for (Coords coords : grid.getKeys()) {
				drawCell(g, coords);
			}
			g.setColor(Color.RED);
			drawCell(g, grid.getEntrance());

			if (playerPosition.isActive) {
				int relx = (playerPosition.getX() - grid.getMinX() * 16);
				int rely = (playerPosition.getY() - grid.getMinY() * 16);
				int playerX = (playerPosition.getX() - grid.getMinX() * 16) * 10 / 16 + 10;
				int playerY = (playerPosition.getY() - grid.getMinY() * 16) * 10 / 16 + 10;
				g.setColor(Color.BLUE);
				g.fillOval(playerX - 5, playerY - 5, 10, 10);
			}
			if (SAVE_IMAGE) {
				try {
					ImageIO.write(bi, "PNG", new File("maze_" + seed + "_" + maxPaths + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			g2.drawImage(bi, 0, 0, null);
		}

		private void drawCell(Graphics g, Coords coords) {
			Cell c = grid.getCell(coords);
			Point p = coordToPoint(coords);

			if (grid.isInSolution(coords)) {
				Color color = g.getColor();
				g.setColor(Color.YELLOW);
				g.fillRect(p.x, p.y, 10, 10);
				g.setColor(color);
			} else {
				Color color = g.getColor();
				g.setColor(Color.WHITE);
				g.fillRect(p.x, p.y, 10, 10);
				g.setColor(color);
			}
			g.fillRect(p.x - 2, p.y - 2, 4, 4);
			g.fillRect(p.x + 8, p.y - 2, 4, 4);
			g.fillRect(p.x - 2, p.y + 8, 4, 4);
			g.fillRect(p.x + 8, p.y + 8, 4, 4);
			if (c.getOpenSides()[0] == 0) {
				g.fillRect(p.x + 2, p.y - 2, 6, 4);
			}
			if (c.getOpenSides()[1] == 0) {
				g.fillRect(p.x + 8, p.y + 2, 4, 6);
			}
			if (c.getOpenSides()[2] == 0) {
				g.fillRect(p.x + 2, p.y + 8, 6, 4);
			}
			if (c.getOpenSides()[3] == 0) {
				g.fillRect(p.x - 2, p.y + 2, 4, 6);
			}
		}

		private Point coordToPoint(Coords coords) {
			int x = coords.getX() - leftx;
			int y = coords.getY() - topy;
			return new Point(x * 10 + 10, y * 10 + 10);
		}
	}

	private class PlayerPosition {
		private int x, y;
		private boolean isActive;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public boolean isActive() {
			return isActive;
		}

		public void setActive(boolean active) {
			isActive = active;
		}
	}

	private class PlayerPositionListener {

		private final PlayerPosition playerPosition;
		private final MazeCanvas mazeCanvas;

		public PlayerPositionListener(PlayerPosition player, MazeCanvas mazeCanvas) {
			playerPosition = player;
			this.mazeCanvas = mazeCanvas;
			new Thread(this::runServer).start();
		}

		private void runServer() {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				while(true) {
					Socket s = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						String[] tokens = line.split(" ");
						if (tokens[0].equals("pos")) {
							playerPosition.setX(Integer.parseInt(tokens[1]));
							playerPosition.setY(Integer.parseInt(tokens[2]));
							playerPosition.setActive(true);
							mazeCanvas.repaint();
						}
						if (tokens[0].equals("seed")) {
							long seed = Long.parseLong(tokens[1]);
							mazeCanvas.setSeed(seed);
							seedTextField.setText(seed + "");
							mazeCanvas.regenMaze();
						}
					}
					playerPosition.setActive(false);
					mazeCanvas.repaint();
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
