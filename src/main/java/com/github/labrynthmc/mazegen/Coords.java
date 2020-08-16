package com.github.labrynthmc.mazegen;

import java.io.Serializable;

public class Coords implements Serializable {
	private int x, y;

	public Coords() {
	}

	public Coords(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Coords add(Coords a) {
		return new Coords(a.getX() + x, a.getY() + y);
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		Coords a = (Coords) o;
		return this.x == a.getX() && this.y == a.getY();
	}

	public String toString() {
		return "(" + this.x + "," + this.y + ")";
	}

	public int hashCode() {
		return 31 * x + y;
	}
}
