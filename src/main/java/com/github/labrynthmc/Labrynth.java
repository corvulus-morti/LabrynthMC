package com.github.labrynthmc;

import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.mazegen.Grid;
import com.github.labrynthmc.settings.Settings;
import com.github.labrynthmc.util.ClientToServerHandler;
import com.github.labrynthmc.util.MazeDrawUpdateHandler;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Labrynth.MODID)
public final class Labrynth {

	public static final String MODID = "labrynthmc";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	private static final String PROTOCOL_VERSION = "1";
	public static final int MAZE_Y_POS = 20;
	public static MazeDrawUpdateHandler MAZE_DRAW_UPDATE_HANDLER = null;
	/** Set this to true when you want debugging logs */
	public static final boolean DEBUG = true;

	public static Grid labrynth = new Grid();
	public static final int SMALL_MAZE = 300;
	public static final int MEDIUM_MAZE = 1200;
	public static final int LARGE_MAZE = 4800;
	public static final int INSANE_MAZE = 40000;
	public static int mazeSize = 0;
	public static final int MAZE_SIZES[] = {SMALL_MAZE,MEDIUM_MAZE,LARGE_MAZE,INSANE_MAZE};

	private static SimpleChannel getMazeStuffOnConnectChannel;
	public static ClientToServerHandler clientToServerHandler;

	private static Coords center;
	private static Coords entrance;

	static {
		if (DEBUG) {
			MAZE_DRAW_UPDATE_HANDLER = MazeDrawUpdateHandler.getInstance();
		}
	}


	public Labrynth() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(new ModEventSubscriber());
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.register(new ClientModEventSubscriber());
		}

		clientToServerHandler = new ClientToServerHandler();

		getMazeStuffOnConnectChannel = NetworkRegistry.newSimpleChannel(
				new ResourceLocation("labrynthmc", "mazestuff"),
				() -> PROTOCOL_VERSION,
				PROTOCOL_VERSION::equals,
				PROTOCOL_VERSION::equals
		);
		getMazeStuffOnConnectChannel.registerMessage(3, MazePair.class,
				(myMessage, packetBuffer) -> {
			packetBuffer.writeInt(myMessage.mazeCenter.getX());
			packetBuffer.writeInt(myMessage.mazeCenter.getY());
			packetBuffer.writeInt(myMessage.mazeEntrance.getX());
			packetBuffer.writeInt(myMessage.mazeEntrance.getY());
		},
				packetBuffer -> new MazePair(
						packetBuffer.readInt(),
						packetBuffer.readInt(),
						packetBuffer.readInt(),
						packetBuffer.readInt()),
				(myMessage, contextSupplier) -> {
			center = myMessage.mazeCenter;
			entrance = myMessage.mazeEntrance;
			contextSupplier.get().setPacketHandled(true);
		});
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
			labrynth = Settings.getWorldMaze(world);
			center = labrynth.getCenter();
			entrance = labrynth.getEntrance();
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

	public static void sendMazeAttrsToClient(NetworkManager n) {
		getMazeStuffOnConnectChannel.sendTo(new MazePair(center, entrance), n, NetworkDirection.PLAY_TO_CLIENT);
	}



	private static class MazePair {
		Coords mazeCenter;
		Coords mazeEntrance;

		public MazePair(int centerX, int centerY, int entranceX, int entranceY) {
			this(new Coords(centerX, centerY), new Coords(entranceX, entranceY));
		}

		public MazePair(Coords center, Coords entrance) {
			mazeCenter = center;
			mazeEntrance = entrance;
		}
	}

	public static Coords getCenter() {
		return center;
	}

	public static Coords getEntrance() {
		return entrance;
	}
}