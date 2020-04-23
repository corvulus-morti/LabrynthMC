package com.github.labrynthmc;

import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.world.biome.Biome;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Labrynth.MODID)
public final class Labrynth
{

    public static final String MODID = "labrynthmc";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static Grid labrynth;
    public static final int maxPaths = 500;


    //World Seed = -9024077830479927597

    public Labrynth()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        //RegistryHandler.initItems();

        MinecraftForge.EVENT_BUS.register(new ModEventSubscriber());
    }


    private void setup(final FMLCommonSetupEvent event)
    {
        for (Biome biome : ForgeRegistries.BIOMES)
        {
            // All structures needs to be added by .addStructure AND .addFeature in order to spawn.
            //
            // .addStructure tells Minecraft that this biome can start the generation of the structure.
            // .addFeature tells Minecraft that the pieces of the structure can be made in this biome.
            //
            // Thus it is best practice to do .addFeature for all biomes and do .addStructure as well for
            // the biome you want the structure to spawn in. That way, the structure will only spawn in the
            // biomes you want but will not get cut off when generating if part of it goes into a non-valid biome.
            //if (biome.getCategory().equals(Biome.Category.NETHER)) biome.addStructure(FeatureInit.LABRYNTH.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
            biome.addStructure(FeatureInit.LABRYNTH.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, FeatureInit.LABRYNTH.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                    .withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
        }
    }

    private void doClientStuff(final FMLClientSetupEvent event){}
/*
    public static final ItemGroup TAB = new ItemGroup("labrynthmcTab")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(RegistryHandler.BRANDONS_TEST_ITEM.get());
        }
    };
//*/
}