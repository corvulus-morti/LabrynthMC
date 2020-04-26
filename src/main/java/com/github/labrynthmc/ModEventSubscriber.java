package com.github.labrynthmc;


import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.Level;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {
	//*
	@SubscribeEvent
	public void netherLabrynthWorldGen(WorldEvent.Load event) {
		if (Labrynth.labrynth == null) {
			World world = event.getWorld().getWorld();
			DimensionType dimType = world.getDimension().getType();
			if (!world.isRemote()) {
				System.out.println("Dimension ID = " + dimType.getId());
				Labrynth.labrynth = Grid.genMaze(world.getSeed(), Labrynth.MAX_PATHS);
			}
		}
	}

	//*/
	@SubscribeEvent
	public void eventCreateSpawn(WorldEvent.CreateSpawnPosition event) {

		Labrynth.LOGGER.log(Level.INFO, "Attempted to create spawn point.");
		World world = event.getWorld().getWorld();
		DimensionType dimType = world.getDimension().getType();
		if (!world.isRemote()) {
			System.out.println("Dimension ID = " + dimType.getId());
			Labrynth.labrynth = Grid.genMaze(world.getSeed(), Labrynth.MAX_PATHS);
		}
	}


	@SubscribeEvent
	public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
		//registers the structures/features.
		//If you don't do this, you'll crash.
		FeatureInit.registerFeatures(event);

		Labrynth.LOGGER.log(Level.INFO, "Registered features/structures.");
	}


	//*
	public static <T extends IForgeRegistryEntry<T>> T setup(final T entry, final String name) {
		return setup(entry, new ResourceLocation(Labrynth.MODID, name));
	}

	public static <T extends IForgeRegistryEntry<T>> T setup(final T entry, final ResourceLocation registryName) {
		entry.setRegistryName(registryName);
		return entry;
	}
//*/
}
