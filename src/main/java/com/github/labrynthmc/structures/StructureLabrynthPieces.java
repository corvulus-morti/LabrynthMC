package com.github.labrynthmc.structures;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class StructureLabrynthPieces
{
	public static final ResourceLocation FOUR_WAY = new ResourceLocation(Labrynth.MODID + ":four_way");
	public static final ResourceLocation HALL_WAY = new ResourceLocation(Labrynth.MODID + ":hall");
	public static final ResourceLocation ELL = new ResourceLocation(Labrynth.MODID + ":ell");
	public static final ResourceLocation TEE = new ResourceLocation(Labrynth.MODID + ":tee");
	public static final ResourceLocation DEAD_END = new ResourceLocation(Labrynth.MODID + ":dead");

	//private static final Map<ResourceLocation, BlockPos> OFFSET = ImmutableMap.of(LEFT_SIDE, new BlockPos(0, 1, 0), RIGHT_SIDE, new BlockPos(0, 1, 0));

	public static void start(TemplateManager templateManager, ResourceLocation resourceLocation, BlockPos pos, Rotation rotation, List<StructurePiece> pieceList, Random rand)
	{
		System.out.println("BLOCKPOS HERE!: "+pos);
		pieceList.add(new StructureLabrynthPieces.Piece(templateManager,resourceLocation,pos,rotation));
	}



	public static class Piece extends TemplateStructurePiece
	{
		private ResourceLocation templateResource;
		private Rotation rotation;
		public Piece(TemplateManager templateManager, ResourceLocation templateResource, BlockPos pos, Rotation rotation)
		{
			super(FeatureInit.LABRYNTH_PIECE, 0);
			this.rotation = rotation;
			this.templatePosition = pos;
			this.templateResource = templateResource;
			this.setupTemplate(templateManager);
		}

		public Piece(TemplateManager templateManager, CompoundNBT nbt) {
			super(FeatureInit.LABRYNTH_PIECE, nbt);
			this.templateResource = new ResourceLocation(nbt.getString("template"));
			this.rotation = Rotation.valueOf(nbt.getString("rotation"));
			setupTemplate(templateManager);
		}

		public void setupTemplate(TemplateManager templateManager) {


			Template template = templateManager.getTemplateDefaulted(this.templateResource);
			PlacementSettings placementSettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementSettings);
		}

		@Override
		protected void readAdditional(CompoundNBT nbt) {
			super.readAdditional(nbt);
			nbt.putString("template", this.templateResource.toString());
			nbt.putString("rotation", this.rotation.toString());
		}

		@Override
		protected void handleDataMarker(String function, BlockPos pos, IWorld worldIn, Random rand, MutableBoundingBox sbb)
		{
			//TODO
		}

		@Override
		public boolean func_225577_a_(IWorld worldIn, ChunkGenerator<?> p_225577_2_, Random randomIn, MutableBoundingBox structureBoundingBoxIn, ChunkPos chunkPos)
		{
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
			this.templatePosition.add(Template.transformedBlockPos(placementsettings, new BlockPos(0, 0, 0)));

			return super.func_225577_a_(worldIn, p_225577_2_, randomIn, structureBoundingBoxIn, chunkPos);
		}
	}

}
