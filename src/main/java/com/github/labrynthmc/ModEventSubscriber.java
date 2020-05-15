package com.github.labrynthmc;


import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import static com.github.labrynthmc.Labrynth.DEBUG;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	/**
	 * If WorldEvent.CreateSpawnPosition wasn't called, we need to generate the maze on worldevent
	 */
	@SubscribeEvent
	public void netherLabrynthWorldGen(WorldEvent.Load event) {
		if (Labrynth.labrynth == null) {
			if (DEBUG) {
				Labrynth.LOGGER.log(Level.INFO, "netherLabrynthWorldGen(), generating maze");
			}
			Labrynth.generateMaze(event.getWorld());
		}
	}

	/**
	 *  Create the maze on spawn creation
	*/
	@SubscribeEvent
	public void eventCreateSpawn(WorldEvent.CreateSpawnPosition event) {
		if (DEBUG) {
			Labrynth.LOGGER.log(Level.INFO, "onCreateSpawn(), generating maze");
		}
		Labrynth.generateMaze(event.getWorld());
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

}
