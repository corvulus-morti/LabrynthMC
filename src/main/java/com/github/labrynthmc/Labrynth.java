package com.github.labrynthmc;

import com.github.labrynthmc.mazegen.Grid;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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

@Mod(Labrynth.MODID)
public final class Labrynth {

	public static final String MODID = "labrynthmc";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	/** Set this to true when you want debugging logs */
	public static final boolean DEBUG = true;

	public static Grid labrynth;
	public static final int MAX_PATHS = 500;

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
			if (DEBUG) {
				LOGGER.log(Level.INFO, "Dimension ID = " + dimType.getId());
			}
			labrynth = Grid.genMaze(world.getSeed(), MAX_PATHS);

		} else if (DEBUG) {
			LOGGER.log(Level.INFO, "Not generating maze, world is remote");
		}
	}
}