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
import java.util.List;
import java.util.*;

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

	private JTextPane mazeDetails;

	private MazeDraw() {

		seedTextField = new JTextField();
		seedTextField.setPreferredSize(new Dimension(500, 32));
		seedTextField.setToolTipText("Seed");
		seedTextField.setText(new Random().nextLong() + "");
		JTextField paths = new JTextField();
		paths.setPreferredSize(new Dimension(50, 32));
		paths.setToolTipText("Max paths");
		paths.setText(Labrynth.MAZE_SIZES[Labrynth.mazeSize]+ "");

		JPanel panel = new JPanel();
		JPanel mazePanel = new JPanel();

		JButton drawButton = new JButton();
		drawButton.setText("Draw");
		drawButton.addActionListener(e -> {
			mazeCanvas.setSeed(Long.parseLong(seedTextField.getText()));
			mazeCanvas.setMaxPaths(Integer.parseInt(paths.getText()));
			mazeCanvas.regenMaze();
		});

		mazePanel.setLayout(new BorderLayout());

		panel.add(seedTextField);
		panel.add(paths);
		panel.add(drawButton);

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.add(panel, BorderLayout.NORTH);

		mazeDetails = new JTextPane();
		mazeDetails.setText("Hello there");
		headerPanel.add(mazeDetails, BorderLayout.SOUTH);

		mazePanel.add(headerPanel, BorderLayout.NORTH);
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

	void updateMazeDetails(int numCells, int solutionSize, int solutionLength) {
		int secondsToSolveWhileRunning = (int) (solutionLength / 5.612);
		int minutesToSolveWhileRunning = secondsToSolveWhileRunning / 60 % 60;
		int hoursToSolveWhileRunning = secondsToSolveWhileRunning / 3600;
		secondsToSolveWhileRunning %= 60;
		mazeDetails.setText("Number cells: " + numCells + "; Number cells in solution: " + solutionSize
				+ "; Estimated solution length: " + solutionLength + " blocks; Esimated time to solve while sprinting: "
				+ String.format("%02d:%02d:%02d", hoursToSolveWhileRunning, minutesToSolveWhileRunning, secondsToSolveWhileRunning));
	}

	private static final double INNER_CORNER_DIST = 2 * Math.sqrt(3.5 * 3.5 + 0.5 * 0.5); // 0->1 with walking around the corner post
	private static final double CORRESPONDING_CORNER_DIST = Math.sqrt(11 * 11 + 4 * 4); // 0 -> 2
	private static final double OUTER_CORNER_DIST = Math.sqrt(11 * 11 * 2); // 0 -> 3
	private static final double CROSS_ACROSS_DIST = Math.sqrt(15 * 15 + 5 * 5); // 0 -> 4
	private static final double ACROSS_DIST = 15; // 0 -> 5
	private static final double ENTRANCE_TO_SIDE_DIST = Math.sqrt(7.5 * 7.5 + 4 * 4); // 0.5 -> 2
	private static final double ENTRANCE_TO_ACROSS = Math.sqrt(15 * 15 + 3.5 * 3.5); // 0.5 -> 4
	private static final double CENTER_TO_NODE = Math.sqrt(7.5 * 7.5 + 3.5 * 3.5); // Center -> Any
	/**
	 * Each cell has the nodes laid out as below. We can ignore adding nodes on unopened sides.
	 *
	 * Corners (e.g 0 <-> 7) have a distance of about 2 * sqrt(3.5^2 + 0.5^2) (simulates going around the corner).
	 *
	 *   0 1
	 * 7     2
	 * 6     3
	 *   5 4
	 */
	double estimateSolutionPathLength(Grid grid) {
		List<Coords> solution = grid.getSolution();

		Coords prevCoords = solution.get(0);
		Cell prevCell = grid.getCell(prevCoords);
		Node[] nodes = new Node[8];

		int entranceSide = 0;
		for (int i = 0; i < 4; i++) {
			if (prevCell.getOpenSides()[i] == 1) {
				nodes[2 * i] = new Node(prevCoords, 2 * i);
				nodes[2 * i + 1] = new Node(prevCoords, 2 * i + 1);
				if (grid.getCell(prevCoords.add(Grid.MOVE[i])) == null) {
					entranceSide = i;
				}
			}
		}
		connectCellNodes(nodes);

		Node entranceNode = new Node(prevCoords, 8);
		connectNodes(entranceNode, nodes[(2 * entranceSide + 2) % 8], ENTRANCE_TO_SIDE_DIST);
		connectNodes(entranceNode, nodes[(2 * entranceSide + 7) % 8], ENTRANCE_TO_SIDE_DIST);
		connectNodes(entranceNode, nodes[(2 * entranceSide + 4) % 8], ENTRANCE_TO_ACROSS);
		connectNodes(entranceNode, nodes[(2 * entranceSide + 5) % 8], ENTRANCE_TO_ACROSS);

		Node[] prevNodes = nodes;

		for (int s = 1; s < solution.size(); s++) {
			nodes = new Node[8];
			Coords coords = solution.get(s);
			Cell cell = grid.getCell(coords);
			for (int i = 0; i < 4; i++) {
				if (cell.getOpenSides()[i] == 1) {
					nodes[2 * i] = new Node(coords, 2 * i);
					nodes[2 * i + 1] = new Node(coords, 2 * i + 1);
				}
			}
			connectCellNodes(nodes);
			connectToPreviousNodes(coords, prevCoords, nodes, prevNodes);

			prevNodes = nodes;
			prevCoords = coords;
		}

		Node centerNode = new Node();
		for (int i = 0; i < 8; i++) {
			connectNodes(centerNode, prevNodes[i], CENTER_TO_NODE);
		}

		//Dijkstra to find the shortest distance for entrance to center
		PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.val));
		entranceNode.val = 0;
		queue.add(entranceNode);

		while (!queue.isEmpty()) {
			Node n = queue.poll();
			if (n.visited) {
				continue;
			}
			if (n.equals(centerNode)) {
				return n.val;
			}
			n.visited = true;
			int numNeighbors = n.neighborNodes.size();
			for (int i = 0; i < numNeighbors; i++) {
				Node neighborNode = n.neighborNodes.get(i);
				double neighborDistance = n.distances.get(i);
				double newDist = n.val + neighborDistance;
				if (newDist < neighborNode.val) {
					neighborNode.val = newDist;
					queue.add(neighborNode);
				}
			}
		}
		return 0;
	}

	private void connectToPreviousNodes(Coords coords, Coords prevCoords, Node[] nodes, Node[] prevNodes) {
		for (int i = 0; i < 4; i++) {
			if (coords.add(Grid.MOVE[i]).equals(prevCoords)) {
				connectNodes(nodes[2 * i], prevNodes[(2 * i + 5) % 8], 1);
				connectNodes(nodes[2 * i + 1], prevNodes[(2 * i + 4) % 8], 1);
				return;
			}
		}
	}

	private void connectCellNodes(Node[] nodes) {
		for (int i = 0; i < 8; i += 2) {
			connectNodes(nodes[i + 1], nodes[(i + 2) % 8], INNER_CORNER_DIST);
			connectNodes(nodes[i + 1], nodes[(i + 3) % 8], CORRESPONDING_CORNER_DIST);
			connectNodes(nodes[i], nodes[(i + 2) % 8], CORRESPONDING_CORNER_DIST);
			connectNodes(nodes[i], nodes[(i + 3) % 8], OUTER_CORNER_DIST);
			connectNodes(nodes[i >> 1], nodes[(i >> 1) + 4], CROSS_ACROSS_DIST);
			connectNodes(nodes[i], nodes[(i + 5) % 8], ACROSS_DIST);
		}
	}

	void connectNodes(Node a, Node b, double dist) {
		if (a == null || b == null) {
			return;
		}
		a.neighborNodes.add(b);
		a.distances.add(dist);
		b.neighborNodes.add(a);
		b.distances.add(dist);
	}

	private static class Node {
		List<Node> neighborNodes = new ArrayList<>();
		List<Double> distances = new ArrayList<>();
		double val = Double.MAX_VALUE;
		boolean visited = false;
		Coords coords;
		int i;

		public Node() {

		}

		public Node(Coords c, int i) {
			coords = c;
			this.i = i;
		}

		@Override
		public String toString() {
			return "Node{" +
					", coords=" + coords +
					", i=" + i +
					", val=" + val +
					", visited=" + visited +
					'}';
		}
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

		private double scale = 1;

		public MazeCanvas(long seed, int maxPaths) {
			this.seed = seed;
			this.maxPaths = maxPaths;
			super.addMouseWheelListener((e) -> {
				if (e.getWheelRotation() < 0) {
					scale *= 1.1;
				} else if (e.getWheelRotation() > 0) {
					scale /= 1.1;
				}
				repaint();
			});
			regenMaze();
		}

		public void regenMaze() {
			grid = Grid.genMaze(seed, maxPaths);

			updateMazeDetails(grid.getKeys().size(), grid.getSolution().size(), (int) estimateSolutionPathLength(grid));

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
			setVisible(true);
			repaint();
		}

		@Override
		public void paint(Graphics g2) {
			super.paint(g2);
			Graphics2D g = (Graphics2D) g2;
			setPreferredSize(new Dimension((int) (((rightx - leftx) * 10 + 40) * scale), (int) (((bottomy - topy) * 10 + 40) * scale)));
			BufferedImage bi = null;
			if (SAVE_IMAGE) {
				bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				g = bi.createGraphics();
			}
			g.scale(scale, scale);
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
					g2.drawImage(bi, 0, 0, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
