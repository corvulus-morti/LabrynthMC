package com.github.labrynthmc;


import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Grid
{
	public static final Random r = new Random();
	private HashMap<Coords, Cell> grid;
	private Coords center;
	private int Dx[] = new int[2];
	private int Dy[] = new int[2];

	public Coords getCenter() {return center;}
	public Cell getCell(Coords pos){return grid.get(pos);}

	public void addCell(Coords pos)
	{
		if (grid.isEmpty())
		{
			Dx = new int[] {pos.getX(),pos.getX()};
			Dy = new int[] {pos.getY(),pos.getY()};
			center = pos;
		}

		grid.put(pos, new Cell());

		if (pos.getX() < Dx[0]) Dx[0] = pos.getX();
		else if (pos.getX() > Dx[1]) Dx[1] = pos.getX();
		if (pos.getY() < Dy[0]) Dy[0] = pos.getY();
		else if (pos.getY() > Dy[1]) Dy[1] = pos.getY();
	}

	//CREATE METHOD TO GENERATE STRUCTURES IN GRID

	public static class Coords
	{
		private int x, y;

		public Coords(){}
		public Coords(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getX(){return x;}
		public int getY(){return y;}
		public void setX(int x){this.x = x;}
		public void setY(int y){this.y = y;}

		public Coords add(Coords a)
		{
			Coords ret = new Coords(a.getX()+x,a.getY()+y);
			return ret;
		}

		public boolean equals(Object o)
		{
			Coords a = (Coords) o;
			return this.x == a.getX() && this.y == a.getY();
		}


		public int hashCode(){return 31*x+y;}
	}

	public static void generate()
	{
		Grid grid = new Grid();
		int center[] = {r.nextInt(),r.nextInt()};
		Coords pos = new Coords(center[0],center[1]);

		Coords move[] = new Coords[4];
		move[0] = new Coords(0,-1);
		move[1] = new Coords(1,0);
		move[2] = new Coords(0,1);
		move[3] = new Coords(-1,0);


		grid.addCell(pos);
		grid.addCell(pos.add(move[0]));
		grid.addCell(pos.add(move[1]));
		grid.addCell(pos.add(move[2]));
		grid.addCell(pos.add(move[3]));

		grid.getCell(pos).setOpenSides("1111");
		grid.getCell(pos.add(move[0])).setOpenSides("1111");
		grid.getCell(pos.add(move[1])).setOpenSides("1111");
		grid.getCell(pos.add(move[2])).setOpenSides("1111");
		grid.getCell(pos.add(move[3])).setOpenSides("1111");

		grid.addCell(pos.add(move[0]).add(move[3]));
		grid.addCell(pos.add(move[0]).add(move[1]));
		grid.addCell(pos.add(move[2]).add(move[3]));
		grid.addCell(pos.add(move[2]).add(move[1]));
		grid.getCell(pos.add(move[0]).add(move[3])).setOpenSides("0110");
		grid.getCell(pos.add(move[0]).add(move[1])).setOpenSides("0011");
		grid.getCell(pos.add(move[2]).add(move[3])).setOpenSides("1100");
		grid.getCell(pos.add(move[2]).add(move[1])).setOpenSides("1001");

		grid.addCell(pos.add(move[0]).add(move[0]));
		grid.addCell(pos.add(move[1]).add(move[1]));
		grid.addCell(pos.add(move[2]).add(move[2]));
		grid.addCell(pos.add(move[3]).add(move[3]));
		grid.getCell(pos.add(move[0]).add(move[0])).setOpenSides("0010");
		grid.getCell(pos.add(move[1]).add(move[1])).setOpenSides("0001");
		grid.getCell(pos.add(move[2]).add(move[2])).setOpenSides("0100");
		grid.getCell(pos.add(move[3]).add(move[3])).setOpenSides("1000");

		final Coords fixed[] =
				{
						pos.add(move[0]).add(move[1]),
						pos.add(move[0]).add(move[3]),
						pos.add(move[2]).add(move[1]),
						pos.add(move[2]).add(move[3])
				};

		int loopz = 500;
		int lz = 0;

		while (lz < loopz)
		{
			lz++;
			ArrayList<Coords> path = new ArrayList<>();

			Coords start = new Coords();
			float std[] = {10,10};
			do{
				start.setX((int) Math.round(r.nextGaussian()*std[0]+center[0]));
				start.setY((int) Math.round(r.nextGaussian()*std[1]+center[1]));
			}while(grid.getCell(start) != null);

			path.add(start);
			int d = r.nextInt()%4;
			pos = new Coords(start.getX(),start.getY());
			pos = pos.add(move[d]);

			while (grid.getCell(pos) == null)
			{
				int rot = r.nextInt()%(3) - 1;
				d = (d+rot+4)%4;
				pos = pos.add(move[d]);

				byte check = 0;
				for (Coords p: path) if (pos.equals(p)) check = 1;
				for (Coords p: fixed) if (pos.equals(p)) check = 1;
				if (check == 1)
				{
					do{
						start = new Coords(
								(int) Math.round(r.nextGaussian()*std[0]+center[0]),
								(int) Math.round(r.nextGaussian()*std[1]+center[1])
						);
					}while(grid.getCell(start) != null);
					path = new ArrayList<>();
					path.add(start);
					pos = new Coords(start.getX(),start.getY());
					d = r.nextInt()%4;
					continue;
				}
				path.add(pos);
			}

			grid.getCell(path.get(0));
			for(int n = 1; n < path.size(); n++)
			{
				Coords last = path.get(n-1);
				Coords curr = path.get(n);

				if (grid.getCell(curr) == null) grid.addCell(curr);
				if (curr.getX() - last.getX() ==  1) {
					grid.getCell(last).setSide(1, true);
					grid.getCell(curr).setSide(3, true);
				}
				else if (curr.getX() - last.getX() ==  -1) {
					grid.getCell(last).setSide(3, true);
					grid.getCell(curr).setSide(1, true);
				}
				else if (curr.getY() - last.getY() ==  1) {
					grid.getCell(last).setSide(2, true);
					grid.getCell(curr).setSide(0, true);
				}
				else if (curr.getY() - last.getY() ==  -1) {
					grid.getCell(last).setSide(0, true);
					grid.getCell(curr).setSide(2, true);
				}
			}
		}

	}
}


