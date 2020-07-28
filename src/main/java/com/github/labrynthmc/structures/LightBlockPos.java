package com.github.labrynthmc.structures;

import net.minecraft.util.math.BlockPos;

public class LightBlockPos {

	public long data;

	public LightBlockPos(long data) {
		this.data = data;
	}

	public LightBlockPos(int x, int y, int z) {
		x += 1 << 27;
		y += 0;
		z += 1 << 27;
		if (x >= 0 && y >= 0 && z >= 0 && x < 1 << 28 && y < 1 << 8 && z < 1 << 28) {
			data = (((long) x) << 36) | (((long) y) << 28) | z;
		}
	}
	public LightBlockPos(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LightBlockPos)) return false;
		LightBlockPos that = (LightBlockPos) o;
		return data == that.data;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(data);
	}

	public int getX() {
		return (int) ((data >>> 36) & ((1 << 28) - 1)) - (1 << 27);
	}

	public int getY() {
		return (int) ((data >>> 28) & ((1 << 8) - 1));
	}

	public int getZ() {
		return (int) (data & ((1 << 28) - 1)) - (1 << 27);
	}

	@Override
	public String toString() {
		return "LightBlockPos{" +
				"x=" + getX() +
				", y=" + getY() +
				", z=" + getZ() +
				'}';
	}
}
