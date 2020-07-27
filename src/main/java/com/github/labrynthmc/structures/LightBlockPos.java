package com.github.labrynthmc.structures;

import com.github.labrynthmc.ModEventSubscriber;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class LightBlockPos {
	public int x, y, z;
	public LightBlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public LightBlockPos(BlockPos pos) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LightBlockPos)) return false;
		LightBlockPos that = (LightBlockPos) o;
		return x == that.x &&
				y == that.y &&
				z == that.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public String toString() {
		return "LightBlockPos{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				'}';
	}
}
