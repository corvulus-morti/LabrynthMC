package com.github.labrynthmc.mazegen;

import com.github.labrynthmc.Labrynth;
import org.apache.logging.log4j.Level;

import java.io.Serializable;
import java.util.*;

import static com.github.labrynthmc.Labrynth.MAZE_SIZES;

public class Grid implements Serializable {
	public static final Coords MOVE[] = {
		new Coords(0, -1), //north
		new Coords(1, 0),  //east
		new Coords(0, 1),  //south
		new Coords(-1, 0) //west
	};

	private  Random r;
	private HashMap<Coords, Cell> grid = new HashMap<>();
	private List<Coords> solution = new ArrayList<>();
	private Set<Coords> solutionSet = new HashSet<>();
	private Coords center = new Coords(0, 0);
	private Coords entrance = new Coords(0, 0);
	private int Dx[] = {Integer.MAX_VALUE, Integer.MIN_VALUE};
	private int Dy[] = {Integer.MAX_VALUE, Integer.MIN_VALUE};
	private int size;

	private Grid(long seed) {
		r = new Random(seed);
	}

	public Coords getCenter() {
		return center;
	}

	public Coords getEntrance() {
		return entrance;
	}

	public Cell getCell(Coords pos) {
		return grid.get(pos);
	}

	public Set<Coords> getKeys() {
		return grid.keySet();
	}

	public List<Coords> getSolution() {
		return solution;
	}

	public int getMinX() {
		return Dx[0];
	}
	public int getMaxX() {
		return Dx[1];
	}
	public int getMinY() {
		return Dy[0];
	}
	public int getMaxY() {
		return Dy[1];
	}

	public int getSize() {
		return size;
	}
	public void setSize(int size){ this.size = size;}

	public boolean isInSolution(Coords c) {
		return solutionSet.contains(c);
	}


	public void addCell(Coords pos) {
		if (this.grid.isEmpty()) {
			Dx = new int[]{pos.getX(), pos.getX()};
			Dy = new int[]{pos.getY(), pos.getY()};
			center = pos;
		}

		grid.put(pos, new Cell());

		if (pos.getX() < Dx[0]) Dx[0] = pos.getX();
		else if (pos.getX() > Dx[1]) Dx[1] = pos.getX();
		if (pos.getY() < Dy[0]) Dy[0] = pos.getY();
		else if (pos.getY() > Dy[1]) Dy[1] = pos.getY();
	}

	//CREATE METHOD TO GENERATE STRUCTURES IN GRID

	public static Grid genMaze(long worldSeed, int maxPaths) {
		Grid grid = new Grid(worldSeed);

		grid.setSize(maxPaths);

		Random r = grid.r;
		int center[] = {
				(int) Math.round(r.nextGaussian() * 10),
				(int) Math.round(r.nextGaussian() * 10)
		};
		Coords pos = new Coords(center[0], center[1]);


		grid.addCell(pos);
		grid.addCell(pos.add(MOVE[0]));
		grid.addCell(pos.add(MOVE[1]));
		grid.addCell(pos.add(MOVE[2]));
		grid.addCell(pos.add(MOVE[3]));

		grid.getCell(pos).setOpenSides("1111");
		grid.getCell(pos.add(MOVE[0])).setOpenSides("1111");
		grid.getCell(pos.add(MOVE[1])).setOpenSides("1111");
		grid.getCell(pos.add(MOVE[2])).setOpenSides("1111");
		grid.getCell(pos.add(MOVE[3])).setOpenSides("1111");

		grid.addCell(pos.add(MOVE[0]).add(MOVE[3]));
		grid.addCell(pos.add(MOVE[0]).add(MOVE[1]));
		grid.addCell(pos.add(MOVE[2]).add(MOVE[3]));
		grid.addCell(pos.add(MOVE[2]).add(MOVE[1]));
		grid.getCell(pos.add(MOVE[0]).add(MOVE[3])).setOpenSides("0110");
		grid.getCell(pos.add(MOVE[0]).add(MOVE[1])).setOpenSides("0011");
		grid.getCell(pos.add(MOVE[2]).add(MOVE[3])).setOpenSides("1100");
		grid.getCell(pos.add(MOVE[2]).add(MOVE[1])).setOpenSides("1001");

		grid.addCell(pos.add(MOVE[0]).add(MOVE[0]));
		grid.addCell(pos.add(MOVE[1]).add(MOVE[1]));
		grid.addCell(pos.add(MOVE[2]).add(MOVE[2]));
		grid.addCell(pos.add(MOVE[3]).add(MOVE[3]));
		grid.getCell(pos.add(MOVE[0]).add(MOVE[0])).setOpenSides("0010");
		grid.getCell(pos.add(MOVE[1]).add(MOVE[1])).setOpenSides("0001");
		grid.getCell(pos.add(MOVE[2]).add(MOVE[2])).setOpenSides("1000");
		grid.getCell(pos.add(MOVE[3]).add(MOVE[3])).setOpenSides("0100");

		final Coords fixed[] =
				{
						pos.add(MOVE[0]).add(MOVE[1]),
						pos.add(MOVE[0]).add(MOVE[3]),
						pos.add(MOVE[2]).add(MOVE[1]),
						pos.add(MOVE[2]).add(MOVE[3])
				};

		int lz = 0;

		while (lz < maxPaths) {
			lz++;
			ArrayList<Coords> path = new ArrayList<>();

			Coords start = new Coords();
			int attempts = 0;
			int std = 50;
			int rad = (int) Math.sqrt(grid.grid.size() / Math.PI);
			double randRad;
			double randAngle;
			do {
				randRad = r.nextGaussian() * std + (double) rad;
				randAngle = r.nextDouble()*2*Math.PI;
				int X = (int) Math.round(randRad * Math.cos(randAngle)) + center[0];
				int Y = (int) Math.round(randRad * Math.sin(randAngle)) + center[1];
				start.setX(X);
				start.setY(Y);
			} while (grid.getCell(start) != null);
			attempts = 0;
			path.add(start);
			pos = new Coords(start.getX(), start.getY());
			//pos = pos.add(move[d]);

			int d = r.nextInt(4);
			while (grid.getCell(pos) == null) {
				int rot = r.nextInt(3) - 1;
				d = (d + rot + 4) % 4;

				pos = pos.add(MOVE[d]);

				byte check = 0;
				for (Coords p : path) if (pos.equals(p)) check |= 1;
				for (Coords p : fixed) if (pos.equals(p)) check |= 1;
				if (check == 1) {
					do {
						randRad = r.nextGaussian() * std + (double) rad;
						randAngle = r.nextDouble() * 2 * Math.PI;
						int X = (int)Math.round( randRad*Math.cos(randAngle) ) + center[0];
						int Y = (int)Math.round( randRad*Math.sin(randAngle) ) + center[1];
						start.setX(X);
						start.setY(Y);
						if (++attempts == 100) {
							attempts = 0;
							rad += 2 * std;
						}
					} while (grid.getCell(start) != null);
					attempts = 0;
					path = new ArrayList<>();
					path.add(start);
					pos = new Coords(start.getX(), start.getY());
					d = r.nextInt(4);
					continue;
				}
				path.add(pos);
			}

			grid.addCell(path.get(0));
			for (int n = 1; n < path.size(); n++) {
				Coords last = path.get(n - 1);
				Coords curr = path.get(n);

				if (grid.getCell(curr) == null) grid.addCell(curr);
				if (curr.getX() - last.getX() == 1) {
					grid.getCell(last).setSide(1, true);
					grid.getCell(curr).setSide(3, true);
				} else if (curr.getX() - last.getX() == -1) {
					grid.getCell(last).setSide(3, true);
					grid.getCell(curr).setSide(1, true);
				} else if (curr.getY() - last.getY() == 1) {
					grid.getCell(last).setSide(2, true);
					grid.getCell(curr).setSide(0, true);
				} else if (curr.getY() - last.getY() == -1) {
					grid.getCell(last).setSide(0, true);
					grid.getCell(curr).setSide(2, true);
				}
				if (grid.getCell(curr).getType() == '0')
					Labrynth.LOGGER.log(Level.ERROR,
							"Something went wrong in path " + lz + " with element " + n + ":"
									+ "\n\tCurrent: " + grid.getCell(curr) + " " + curr
									+ "\n\tLast: " + grid.getCell(last) + " " + last
					);
			}
		}

		grid.createEntrance();

		return grid;
	}

	private void createEntrance() {
		Set<Coords> visited = new HashSet<>();
		Queue<Coords> queue = new LinkedList<>();
		HashMap<Coords, Coords> nextCoordToCenter = new HashMap<>();
		queue.add(center);

		Coords lastCandidate = null;
		while (!queue.isEmpty()) {
			Coords coords = queue.poll();
			if (visited.contains(coords)) {
				continue;
			}
			visited.add(coords);
			boolean isOnSide = getCell(coords.add(MOVE[0])) == null || getCell(coords.add(MOVE[1])) == null
					|| getCell(coords.add(MOVE[2])) == null || getCell(coords.add(MOVE[3])) == null;
			if (isOnSide) {
				lastCandidate = coords;
			}
			List<Integer> sides = Arrays.asList(0,1,2,3);
			Collections.shuffle(sides, r); // Add new sides in a random order
			for (int side : sides) {
				Cell c = getCell(coords);
				if (c.getOpenSides()[side] == 1) {
					Coords next = coords.add(MOVE[side]);
					if (!visited.contains(next)) {
						queue.add(next);
						nextCoordToCenter.put(next, coords);
					}
				}
			}

		}

		if (lastCandidate != null) {
			entrance = lastCandidate;
		} else {
			entrance = center;
		}

//		List<Integer> sides = Arrays.asList(0,1,2,3);
//		Collections.shuffle(sides, r); // Add new sides in a random order
//		for (int side : sides) {
//			Cell c = getCell(entrance);
//			if (getCell(entrance.add(MOVE[side])) == null) {
//				c.setOpenSide(side, true);
//				break;
//			}
//		}

		Coords c = entrance;
		while(!c.equals(center)) {
			solution.add(c);
			c = nextCoordToCenter.get(c);
		}
		solution.add(c);
		solutionSet.addAll(solution);
	}

}


