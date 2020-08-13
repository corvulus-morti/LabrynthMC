package com.github.labrynthmc.mazegen;

import com.github.labrynthmc.Labrynth;
import com.sun.jna.platform.unix.X11;
import org.apache.logging.log4j.Level;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.labrynthmc.Labrynth.LOGGER;
import static com.github.labrynthmc.Labrynth.MAZE_SIZES;

public class Grid implements Serializable {
	public static final Coords MOVE[] = {
		new Coords(0, -1), //north
		new Coords(1, 0),  //east
		new Coords(0, 1),  //south
		new Coords(-1, 0) //west
	};

	private HashMap<Coords, Cell> grid = new HashMap<>();
	private List<Coords> solution = new ArrayList<>();
	private Set<Coords> solutionSet = new HashSet<>();
	private Coords center;
	private Coords fixedCells[];
	private Coords entrance;
	private int Dx[];
	private int Dy[];
	private int size;

//	private Grid(long seed) {
//		r = new Random(seed);
//	}

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

	public void addPath(ArrayList<Coords> path){
		if (this.grid.get(path.get(0)) == null) this.addCell(path.get(0));
		for (int n = 1; n < path.size(); n++) {
			Coords last = path.get(n - 1);
			Coords curr = path.get(n);

			if (this.getCell(curr) == null) this.addCell(curr);
			if (curr.getX() - last.getX() == 1) {
				this.getCell(last).setSide(1, true);
				this.getCell(curr).setSide(3, true);
			} else if (curr.getX() - last.getX() == -1) {
				this.getCell(last).setSide(3, true);
				this.getCell(curr).setSide(1, true);
			} else if (curr.getY() - last.getY() == 1) {
				this.getCell(last).setSide(2, true);
				this.getCell(curr).setSide(0, true);
			} else if (curr.getY() - last.getY() == -1) {
				this.getCell(last).setSide(0, true);
				this.getCell(curr).setSide(2, true);
			}
			if (this.getCell(curr).getType() == '0')
				Labrynth.LOGGER.log(Level.ERROR,
						"Something went wrong in path with element " + n + ":"
								+ "\n\tCurrent: " + this.getCell(curr) + " " + curr
								+ "\n\tLast: " + this.getCell(last) + " " + last
				);
		}
	}

	private Coords findStart(int rad, double std, double scalar, int maxAttempts, Random r) {
		Coords start = new Coords();
		Coords center = this.getCenter();
		int attempts = 0;
		double randRad;
		double randAngle;
		do {
			randRad = r.nextGaussian() * std + rad;
			randAngle = r.nextDouble()*2*Math.PI;
			int X = (int) Math.round(randRad * Math.cos(randAngle)) + center.getX();
			int Y = (int) Math.round(randRad * Math.sin(randAngle)) + center.getY();
			start.setX(X);
			start.setY(Y);
			if (++attempts == maxAttempts) {
				attempts = 0;
				rad = (int)(scalar * Math.sqrt(this.grid.size()/Math.PI));
			}
		} while (this.getCell(start) != null);


		return start;
	}

	private ArrayList<Coords> buildPath(Coords start, Random r){
		if (this.getCell(start) != null) {
//			LOGGER.info("Start location already in maze.");
			return null;
		}
		ArrayList<Coords> path = new ArrayList<>();

		path.add(start);
		Coords pos = new Coords(start.getX(), start.getY());
		int d = r.nextInt(4);
		int rot;
		while (this.getCell(pos) == null) {
			rot = r.nextInt(3) - 1;
			d = (d + rot + 4) % 4;

			pos = pos.add(MOVE[d]);

			byte check = 0;
			for (Coords p : path) if (pos.equals(p)) {
//				LOGGER.info("Definitely hit myself.");
				check |= 1;
			}
			if (check == 0) {
				for (Coords p : fixedCells)
					if (pos.equals(p)) {
//						LOGGER.info("Definitely hit fixed cell.");
						check |= 1;
					}
			}
			if (check == 1) return null;
			path.add(pos);
		}
		return path;
	}

	//CREATE METHOD TO GENERATE STRUCTURES IN GRID

	public void genMaze(long worldSeed, int maxPaths) {
		LOGGER.info("Starting maze generation.");
		this.grid = new HashMap<>();
		this.solution = new ArrayList<>();
		this.solutionSet = new HashSet<>();

		Random r = new Random(worldSeed);

		this.setSize(maxPaths);

		this.center = new Coords(
				(int) Math.round(r.nextGaussian() * 100),
				(int) Math.round(r.nextGaussian() * 100)
		);
		Coords pos = this.center;


		this.addCell(pos);
		this.addCell(pos.add(MOVE[0]));
		this.addCell(pos.add(MOVE[1]));
		this.addCell(pos.add(MOVE[2]));
		this.addCell(pos.add(MOVE[3]));

		this.getCell(pos).setOpenSides("1111");
		this.getCell(pos.add(MOVE[0])).setOpenSides("1111");
		this.getCell(pos.add(MOVE[1])).setOpenSides("1111");
		this.getCell(pos.add(MOVE[2])).setOpenSides("1111");
		this.getCell(pos.add(MOVE[3])).setOpenSides("1111");

		this.addCell(pos.add(MOVE[0]).add(MOVE[3]));
		this.addCell(pos.add(MOVE[0]).add(MOVE[1]));
		this.addCell(pos.add(MOVE[2]).add(MOVE[3]));
		this.addCell(pos.add(MOVE[2]).add(MOVE[1]));
		this.getCell(pos.add(MOVE[0]).add(MOVE[3])).setOpenSides("0110");
		this.getCell(pos.add(MOVE[0]).add(MOVE[1])).setOpenSides("0011");
		this.getCell(pos.add(MOVE[2]).add(MOVE[3])).setOpenSides("1100");
		this.getCell(pos.add(MOVE[2]).add(MOVE[1])).setOpenSides("1001");

		this.addCell(pos.add(MOVE[0]).add(MOVE[0]));
		this.addCell(pos.add(MOVE[1]).add(MOVE[1]));
		this.addCell(pos.add(MOVE[2]).add(MOVE[2]));
		this.addCell(pos.add(MOVE[3]).add(MOVE[3]));
		this.getCell(pos.add(MOVE[0]).add(MOVE[0])).setOpenSides("0010");
		this.getCell(pos.add(MOVE[1]).add(MOVE[1])).setOpenSides("0001");
		this.getCell(pos.add(MOVE[2]).add(MOVE[2])).setOpenSides("1000");
		this.getCell(pos.add(MOVE[3]).add(MOVE[3])).setOpenSides("0100");

		this.fixedCells = new Coords[]{
				pos.add(MOVE[0]).add(MOVE[1]),
				pos.add(MOVE[0]).add(MOVE[3]),
				pos.add(MOVE[2]).add(MOVE[1]),
				pos.add(MOVE[2]).add(MOVE[3])
		};

		for (int d=0; d < 4; d++) {
			ArrayList<Coords> path = new ArrayList<>();
			pos = center.add(MOVE[d]).add(MOVE[d]);
			path.add(pos);
			for (int n = 0; n < 4; n++)
			{
				pos = pos.add(MOVE[d]);
				path.add(pos);
			}
			this.addPath(path);
		}

		double std = 2;
		double scalar = 2;
		double rad = scalar*Math.sqrt(this.grid.size()/Math.PI);
		int maxAttempts = 100;
		int lz = 4;
		while (lz < 0.25*maxPaths) {
			boolean pathFound = false;
			ArrayList<Coords> path = new ArrayList<>();
			while (!pathFound){
				int attempts = 0;
				while (attempts < maxAttempts){
					double radius = r.nextGaussian()*std + rad;
					double theta = r.nextDouble()*2*Math.PI;
					Coords start = new Coords(
							(int) (radius*Math.cos(theta))+center.getX(),
							(int) (radius*Math.sin(theta))+center.getY()
					);

					path = this.buildPath(start,r);

					if (path != null){
						LOGGER.info("Round 1:  Path found. Attempt = " + attempts + " PATH # = " + lz);
						pathFound = true;
						break;
					}
					else {
//						LOGGER.info("PATH NOT FOUND! Attempt = " + attempts + " PATH # = " + lz);
						attempts++;
					}
				}
				if (pathFound) break;

				rad = scalar*Math.sqrt(this.grid.size()/Math.PI);
				LOGGER.info(""+lz+" "+rad);
			}
			this.addPath(path);
			lz++;
//			LOGGER.info("Path " + lz + " added.");
		}
		rad = 0;
		int maxDist = Math.max(Math.abs(this.center.getY() - this.Dy[0]) ,Math.abs(this.center.getY() - this.Dy[1]));
		std = 2*maxDist;
		while (lz < maxPaths) {
			boolean pathFound = false;
			ArrayList<Coords> path = new ArrayList<>();
			while (!pathFound){
				int attempts = 0;
				while (attempts < maxAttempts){
					double radius = r.nextGaussian()*std + rad;
					double theta = r.nextDouble()*2*Math.PI;
					Coords start = new Coords(
							(int) (radius*Math.cos(theta))+center.getX(),
							(int) (radius*Math.sin(theta))+center.getY()
					);

					path = this.buildPath(start,r);

					if (path != null){
						LOGGER.info("Round 2: Path found. Attempt = " + attempts + " PATH # = " + lz);
						pathFound = true;
						break;
					}
					else {
//						LOGGER.info("PATH NOT FOUND! Attempt = " + attempts + " PATH # = " + (int)(lz+0.25*maxPaths));
						attempts++;
					}
				}
				if (pathFound) break;

//				rad += 2*std;
//				LOGGER.info(""+lz+0.25*max+" "+rad);
			}
			this.addPath(path);
			lz++;
//			LOGGER.info("Path " + (int)(lz+0.25*maxPaths) + " added.");
		}

//		-8029957180441823171
		this.createEntrance(r);
	}

	private void createEntrance(Random r) {
		Set<Coords> visited = new HashSet<>();
		Queue<Coords> queue = new LinkedList<>();
		HashMap<Coords, Coords> nextCoordToCenter = new HashMap<>();
		queue.add(this.getCenter());

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
