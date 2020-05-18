package com.github.labrynthmc.structures;

import com.github.labrynthmc.mazegen.Cell;
import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.Labrynth;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.Level;

import java.util.Random;
import java.util.function.Function;

public class StructureLabrynth extends Structure<NoFeatureConfig> {

	public StructureLabrynth(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn) {
		super(configFactoryIn);
	}

	//*
	@Override
	protected ChunkPos getStartPositionForPosition(ChunkGenerator<?> chunkGenerator, Random random, int x, int z, int spacingOffsetsX, int spacingOffsetsZ) {
		Coords pos = new Coords(x, z);
		if (Labrynth.labrynth.getCell(pos) != null)
			return new ChunkPos(x, z);
		else return null;
	}

	//*/

	@Override
	public String getStructureName() {
		return Labrynth.MODID + ":labrynth";
	}

	@Override
	public IStartFactory getStartFactory() {
		return StructureLabrynth.Start::new;
	}

	@Override
	public int getSize() {
		return 0;
	}

	protected int getSeedModifier() {
		return 12345678;
	}

	@Override
	public boolean func_225558_a_(BiomeManager p_225558_1_, ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ, Biome biome) {
		ChunkPos chunkpos = this.getStartPositionForPosition(chunkGen, rand, chunkPosX, chunkPosZ, 0, 0);

		//Checks to see if current chunk is valid to spawn in.
		if (chunkpos != null && chunkPosX == chunkpos.x && chunkPosZ == chunkpos.z) {
			//Checks if the biome can spawn this structure.
			if (chunkGen.hasStructure(biome, this)) {
				return true;
			}
		}

		return false;
	}

	public static class Start extends StructureStart {
		public Start(Structure<?> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn) {
			//Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
			int x = (chunkX * 16);
			int z = (chunkZ * 16);

			//Finds the y value of the terrain at location.
			//int surfaceY = generator.func_222531_c(x, z, Heightmap.Type.WORLD_SURFACE_WG);
			int surfaceY = 20;
			BlockPos blockpos2 = new BlockPos(x, surfaceY, z);

			Coords center = Labrynth.labrynth.getCenter();
			//LOGGER.log(Level.DEBUG, "Labrynth generated at " + center +".");

			Coords pos = new Coords(chunkX, chunkZ);
			if (pos.equals(Labrynth.labrynth.getEntrance())) {
				if (Labrynth.DEBUG) {
					Labrynth.LOGGER.log(Level.INFO, "Placing entrance at " + "(" + x + ", " + surfaceY + ", " + z + ")");
				}
			}

			Cell cell = Labrynth.labrynth.getCell(pos);
			int curX = (pos.getX() * 16);
			int curZ = (pos.getY() * 16);
			byte[] os = cell.getOpenSides();
			int o = 8 * os[0] + 4 * os[1] + 2 * os[2] + 1 * os[3];
			ResourceLocation cellType = StructureLabrynthPieces.FOUR_WAY;
			BlockPos bp = new BlockPos(curX, surfaceY, curZ);
			int r;
			outer:
			for (r = 0; r < 4; r++) {
				//System.out.println(o + "");
				switch (o) {
					case 8: // D
						//System.out.println("Placing (D) " + cell + " at " + bp + " with rotation " + r);
						cellType = StructureLabrynthPieces.DEAD_END;
						break outer;
					case 12: // L
						//System.out.println("Placing (L) " + cell + " at " + bp + " with rotation " + r);
						cellType = StructureLabrynthPieces.ELL;
						break outer;
					case 10: // H
						//System.out.println("Placing (H) " + cell + " at " + bp + " with rotation " + r);
						cellType = StructureLabrynthPieces.HALL_WAY;
						break outer;
					case 13: // T
						//System.out.println("Placing (T) " + cell + " at " + bp + " with rotation " + r);
						cellType = StructureLabrynthPieces.TEE;
						break outer;
					case 15: // 4
						//System.out.println("Placing (4) " + cell + " at " + bp + " with rotation " + r);
						cellType = StructureLabrynthPieces.FOUR_WAY;
						break outer;
				}
				if (r == 3) {
					Labrynth.LOGGER.log(Level.ERROR, "Not sure what kind of piece this is " + cell);
				}
//					o = (o >> 1) + (o & 1) * 0x8;
				o = ((o << 1) & 15) + (o >> 3);
			}
			int xOffset = 0;
			int zOffset = 0;
			switch (r) {
				case 1:
					xOffset = 15;
					break;
				case 2:
					xOffset = 15;
					zOffset = 15;
					break;
				case 3:
					zOffset = 15;
					break;
			}
			bp = new BlockPos(curX + xOffset, surfaceY, curZ + zOffset);
			StructureLabrynthPieces.start(templateManagerIn, cellType, bp, Rotation.values()[r % 4], this.components);

			this.recalculateStructureSize();
		}
	}

}
