package com.github.labrynthmc;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Labrynth.MODID)
public final class Labrynth
{

    public static final String MODID = "labrynthmc";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Labrynth()
    {
        LOGGER.debug("Hello!");
        MinecraftForge.EVENT_BUS.register(new ModEventSubscriber());
    }

}