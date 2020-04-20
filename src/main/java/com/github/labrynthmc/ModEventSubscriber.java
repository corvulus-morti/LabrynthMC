package com.github.labrynthmc;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber
{
	@SubscribeEvent
	public void netherLabrynthWorldGen(WorldEvent.Load event)
	{
		if (!event.getWorld().isRemote()) {
			IWorld world = event.getWorld();
			Dimension dim = world.getDimension();
			DimensionType dimType = dim.getType();

			System.out.println("Dimension ID = " + dimType.getId());

			switch (dimType.getId()) {
				case -1:
					Grid maze = Grid.genMaze(world.getSeed(), 500);
					System.out.println("Maze generated, at " + maze.getCenter() + ", because world loaded, with seed " + world.getSeed() + ".");
					break;
				case 0:
					break;
				case 1:
					break;
			}
		}
	}

	public static <T extends IForgeRegistryEntry<T>> T setup(final T entry, final String name) {
		return setup(entry, new ResourceLocation(Labrynth.MODID, name));
	}

	public static <T extends IForgeRegistryEntry<T>> T setup(final T entry, final ResourceLocation registryName) {
		entry.setRegistryName(registryName);
		return entry;
	}

}
