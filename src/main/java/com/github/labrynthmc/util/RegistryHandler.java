package com.github.labrynthmc.util;


import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.items.ItemBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryHandler {
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<Item>(ForgeRegistries.ITEMS, Labrynth.MODID);

	public static void init() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}


	//ITEMS
	public static final RegistryObject<Item> BRANDONS_TEST_ITEM = ITEMS.register("brandons_test_item", ItemBase::new);


	//Register Features
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T entry, String registryKey) {
		entry.setRegistryName(new ResourceLocation(Labrynth.MODID, registryKey));
		registry.register(entry);
		return entry;
	}

}
