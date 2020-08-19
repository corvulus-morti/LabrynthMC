package com.github.labrynthmc;


import com.github.labrynthmc.items.MazeCenterCompassItem;
import com.github.labrynthmc.items.MazeEntranceCompassItem;
import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.structures.UnbreakableBlocks;
import com.github.labrynthmc.util.Utils;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.List;

import static com.github.labrynthmc.Labrynth.*;
import static com.github.labrynthmc.util.Utils.isNether;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public void serverStart(FMLServerStartingEvent e) {
		if (DEBUG) {
			LOGGER.log(Level.INFO, "starting server, generating maze");
		}
		String dir = e.getServer().getDataDirectory().getAbsolutePath() + File.separator + e.getServer().getFolderName();
		if(FMLEnvironment.dist.isClient()) {
			dir = e.getServer().getDataDirectory().getAbsolutePath()
					+ File.separator + "saves" + File.separator + e.getServer().getFolderName() + File.separator;
		}
		Utils.setCurrentSaveDirectory(dir);
		generateMaze(e.getServer().getWorld(DimensionType.THE_NETHER));
	}

	/**
	 * Register our Labrynth structure
	 */
	@SubscribeEvent
	public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
		FeatureInit.registerFeatures(event);

		if (DEBUG) {
			LOGGER.log(Level.INFO, "Registered features/structures.");
		}
	}

	@SubscribeEvent
	public void onSpawnNewNetherPortal(EntityTravelToDimensionEvent e) {
		LOGGER.log(Level.INFO,""+e.getDimension().getId());
		if (e.getDimension().getId() == -1) {
			Coords chunk = new Coords((int) (e.getEntity().getPosX()/16), (int) (e.getEntity().getPosZ()/16));
			if (Labrynth.labrynth.getCell(chunk) != null && e.getEntity().getPosY() <= 43)
				e.getEntity().setPosition(e.getEntity().getPosX(), 64, e.getEntity().getPosZ());
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate e) {
		if (!isNether(e.getWorld())) {
			return;
		}
		List<BlockPos> blocks = e.getExplosion().getAffectedBlockPositions();
		for (int i = 0; i < blocks.size(); i++) {
			if (UnbreakableBlocks.getUnbreakableBlocks().contains(new LightBlockPos(blocks.get(i)))) {
				blocks.remove(i--);
			}
		}
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final Item mazeCenterCompassItem = new MazeCenterCompassItem(new Item.Properties().group(ItemGroup.TOOLS));
		final Item mazeEntranceCompassItem = new MazeEntranceCompassItem(new Item.Properties().group(ItemGroup.TOOLS));
		mazeCenterCompassItem.setRegistryName(new ResourceLocation(Labrynth.MODID, "maze_center_compass"));
		mazeEntranceCompassItem.setRegistryName(new ResourceLocation(Labrynth.MODID, "maze_entrance_compass"));
		event.getRegistry().registerAll(mazeCenterCompassItem, mazeEntranceCompassItem);
	}

	@SubscribeEvent
	public void onConnection(PlayerEvent.PlayerLoggedInEvent e) {
		if (!(e.getEntity() instanceof ServerPlayerEntity)) {
			return;
		}
		Labrynth.sendMazeAttrsToClient(((ServerPlayerEntity) e.getEntity()).connection.netManager);
	}

	@SubscribeEvent
	public void checkBreakTheUnbreakable(BlockEvent.BreakEvent e) {
		e.setCanceled(UnbreakableBlocks.getUnbreakableBlocks().contains(e.getPos()));
	}
}
