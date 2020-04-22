package com.github.labrynthmc.world;

import com.github.labrynthmc.structures.StructureLabrynth;
import com.github.labrynthmc.structures.StructureLabrynthPieces;
import com.github.labrynthmc.util.RegistryHandler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Locale;


public class FeatureInit
{
	//Static instance of our structure so we can reference it and add it to biomes easily.
	public static Structure<NoFeatureConfig> LABRYNTH = new StructureLabrynth(NoFeatureConfig::deserialize);
	public static IStructurePieceType LABRYNTH_PIECE = StructureLabrynthPieces.Piece::new;

	/*
	 * Registers the features and structures. Normal Features will be registered here too.
	 */
	public static void registerFeatures(Register<Feature<?>> event)
	{

		IForgeRegistry<Feature<?>> registry = event.getRegistry();

		/* Registers the structure itself and sets what its path is. In this case,
		 * the structure will have the resourcelocation of structure_tutorial:run_down_house .
		 *
		 * It is always a good idea to register your regular features too so that other mods
		 * can use them too directly from the Forge Registry. It great for mod compatibility.
		 */
		RegistryHandler.register(registry, LABRYNTH, "labrynth");
		register(LABRYNTH_PIECE, "LABRYNTH_PIECE");
	}


	/*
	 * Registers the structures pieces themselves. If you don't do this part, Forge will complain to you in the Console.
	 */
	static IStructurePieceType register(IStructurePieceType structurePiece, String key)
	{
		return Registry.register(Registry.STRUCTURE_PIECE, key.toLowerCase(Locale.ROOT), structurePiece);
	}
}