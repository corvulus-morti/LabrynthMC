package com.github.labrynthmc;

import com.github.labrynthmc.mazegen.Grid;
import com.github.labrynthmc.settings.MazeSizeMenuOption;
import com.github.labrynthmc.util.MazeDrawUpdateHandler;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SmallEndIslandsBiome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@Mod(Labrynth.MODID)
public final class Labrynth {

	public static final String MODID = "labrynthmc";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static MazeDrawUpdateHandler MAZE_DRAW_UPDATE_HANDLER = null;
	/** Set this to true when you want debugging logs */
	public static final boolean DEBUG = true;

	public static Grid labrynth;
	public static final int SMALL_MAZE = 300;
	public static final int MEDIUM_MAZE = 1200;
	public static final int LARGE_MAZE = 4800;
	public static final int INSANE_MAZE = 40000;
	public static int mazeSize = 0;
	public static final int MAZE_SIZES[] = {SMALL_MAZE,MEDIUM_MAZE,LARGE_MAZE,INSANE_MAZE};

	static {
		if (DEBUG) {
			MAZE_DRAW_UPDATE_HANDLER = MazeDrawUpdateHandler.getInstance();
		}
	}


	public Labrynth() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(new ModEventSubscriber());
	}

	private void setup(final FMLCommonSetupEvent event) {
		for (Biome biome : ForgeRegistries.BIOMES) {
			if (biome.getCategory().equals(Biome.Category.NETHER)) {
				biome.addStructure(FeatureInit.LABRYNTH.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
			}
			biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
					FeatureInit.LABRYNTH.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
							.withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
		}
	}

	/**
	 * Generates a maze with the world's seed and
	 * @param iWorld The world which supplies the seed
	 */
	public static void generateMaze(IWorld iWorld) {
		World world = iWorld.getWorld();
		DimensionType dimType = world.getDimension().getType();
		if (!world.isRemote()) {

			labrynth = MazeSizeMenuOption.getWorldMaze(world);
			LOGGER.info("Maze has size of " + labrynth.getSize() + " max paths.");
			if (DEBUG) {
				MAZE_DRAW_UPDATE_HANDLER.updateWorldSeed(iWorld.getSeed());
				MAZE_DRAW_UPDATE_HANDLER.updateMaxPaths(labrynth.getSize());
				LOGGER.log(Level.INFO, "Dimension ID = " + dimType.getId());
			}

			mazeSize = 0;

		} else if (DEBUG) {
			LOGGER.log(Level.INFO, "Not generating maze, world is remote");
		}
	}
}