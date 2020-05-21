package com.github.labrynthmc;


import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;

import static com.github.labrynthmc.Labrynth.DEBUG;
import static com.github.labrynthmc.Labrynth.generateMaze;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public void serverStart(FMLServerStartingEvent e) {
		if (DEBUG) {
			Labrynth.LOGGER.log(Level.INFO, "starting server, generating maze");
		}
		generateMaze(e.getServer().getWorld(DimensionType.THE_NETHER));
	}

	/**
	 * Register our Labrynth structure
	 */
	@SubscribeEvent
	public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
		FeatureInit.registerFeatures(event);

		if (DEBUG) {
			Labrynth.LOGGER.log(Level.INFO, "Registered features/structures.");
		}
	}

	@SubscribeEvent
	public void onPlayerMove(LivingEvent.LivingUpdateEvent e) {
		if(!DEBUG) {
			return;
		}
		if (e.getEntityLiving() == null || !(e.getEntityLiving() instanceof PlayerEntity)) {
			return;
		}
		Entity player = e.getEntity();
		Labrynth.MAZE_DRAW_UPDATE_HANDLER.updatePlayerPosition(player.getPosition());
	}

	@SubscribeEvent
	public void asdfadfsfdas(EntityTravelToDimensionEvent e) {
		e.getEntity().setPositionAndRotation(0, 50, 0, 0, 0);
	}


}
