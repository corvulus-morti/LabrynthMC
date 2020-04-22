package com.github.labrynthmc;


import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.Level;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber
{
/*
	@SubscribeEvent
	public void netherLabrynthWorldGen(WorldEvent.Load event)
	{
		World world = event.getWorld().getWorld();
		DimensionType dimType = world.getDimension().getType();
		if (!world.isRemote())
		{
			System.out.println("Dimension ID = " + dimType.getId());

			switch (dimType.getId()) {
				case -1:
					Labrynth.labrynth = Grid.genMaze(world.getSeed(), 0);
					System.out.println("Maze generated, at " + Labrynth.labrynth.getCenter() + ", because world loaded, with seed " + world.getSeed() + ".");
					break;
				case 0:
					break;
				case 1:
					break;
			}
		}
	}
*/
	@SubscribeEvent
	public void eventCreateSpawn(WorldEvent.CreateSpawnPosition event)
	{
		World world = event.getWorld().getWorld();
		DimensionType dimType = world.getDimension().getType();
		if (!world.isRemote())
		{
			System.out.println("Dimension ID = " + dimType.getId());
			Labrynth.labrynth = Grid.genMaze(world.getSeed(), 100);
		}
	}

	@SubscribeEvent
	public void eventWorldTypeEvent(WorldTypeEvent event) {
		WorldType worldType = event.getWorldType();
		worldType.getId();
		System.out.println("WorldTypeEvent occurred!");
	}


	@SubscribeEvent
	public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event)
	{
		//registers the structures/features.
		//If you don't do this, you'll crash.
		FeatureInit.registerFeatures(event);

		Labrynth.LOGGER.log(Level.INFO, "features/structures registered.");
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
