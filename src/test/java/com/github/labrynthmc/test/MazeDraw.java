package com.github.labrynthmc.test;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Cell;
import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.mazegen.Grid;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MazeDraw extends JFrame {
	public static void main(String[] args) {
		new MazeDraw();
	}

	private MazeDraw() {

		JTextField seed = new JTextField();
		seed.setPreferredSize(new Dimension(500, 32));
		seed.setToolTipText("Seed");
		seed.setText(new Random().nextLong() + "");
		JTextField paths = new JTextField();
		paths.setPreferredSize(new Dimension(50, 32));
		paths.setToolTipText("Max paths");
		paths.setText(Labrynth.MAX_PATHS + "");

		JPanel panel = new JPanel();
		JPanel mazePanel = new JPanel();

		JButton drawButton = new JButton();
		drawButton.setText("Draw");
		drawButton.addActionListener(e -> {
			mazePanel.remove(1);
			mazePanel.add(new MazeCanvas(Long.parseLong(seed.getText()), Integer.parseInt(paths.getText())));
			mazePanel.revalidate();
		});

		BorderLayout borderLayout = new BorderLayout();
		mazePanel.setLayout(borderLayout);

		panel.add(seed);
		panel.add(paths);
		panel.add(drawButton);

		mazePanel.add(panel, BorderLayout.NORTH);
		mazePanel.add(new MazeCanvas(Long.parseLong(seed.getText()), Integer.parseInt(paths.getText())), BorderLayout.CENTER);

		add(mazePanel);

		setVisible(true);
		pack();
		setSize(getWidth(), 750);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	}

	private class MazeCanvas extends JPanel {

		int cellSize = 10;

		Grid grid;

		int leftx = Integer.MAX_VALUE;
		int rightx = Integer.MIN_VALUE;
		int topy = Integer.MAX_VALUE;
		int bottomy = Integer.MIN_VALUE;

		int width = rightx - leftx;
		int height = bottomy - topy;

		public MazeCanvas(long seed, int maxPaths) {
			grid = Grid.genMaze(seed, maxPaths);
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
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.BLACK);
			System.out.println(grid.getKeys().size());
			for (Coords coords : grid.getKeys()) {
				drawCell(g, coords);
			}
			g.setColor(Color.RED);
			drawCell(g, grid.getEntrance());
		}

		private void drawCell(Graphics g, Coords coords) {
			Cell c = grid.getCell(coords);
			Point p = coordToPoint(coords);
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
			return new Point(x * cellSize + 10, y * cellSize + 10);
		}
	}
}
