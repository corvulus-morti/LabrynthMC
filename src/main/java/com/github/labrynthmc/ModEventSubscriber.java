package com.github.labrynthmc;


import com.github.labrynthmc.items.MazeCenterCompassItem;
import com.github.labrynthmc.items.MazeEntranceCompassItem;
import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.settings.MazeSizeMenuOption;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.structures.UnbreakableBlocks;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;

import java.util.List;

import static com.github.labrynthmc.Labrynth.*;

@Mod.EventBusSubscriber(modid = Labrynth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public void serverStart(FMLServerStartingEvent e) {
		if (DEBUG) {
			LOGGER.log(Level.INFO, "starting server, generating maze");
		}
		generateMaze(e.getServer().getWorld(DimensionType.THE_NETHER));
	}

	@SubscribeEvent
	public void addOptionToWorldMenu(GuiScreenEvent.InitGuiEvent.Post event)
	{
		Screen screen = event.getGui();
		if (screen.getTitle().getFormattedText().equals("Create New World")) {

			MazeSizeMenuOption.mazeSizeButton.visible = false;
			switch (mazeSize){
				case 0:
					MazeSizeMenuOption.mazeSizeButton.setMessage("Maze Size: Small");
					break;
				case 1:
					MazeSizeMenuOption.mazeSizeButton.setMessage("Maze Size: Medium");
					break;
				case 2:
					MazeSizeMenuOption.mazeSizeButton.setMessage("Maze Size: Large");
					break;
				case 3:
					MazeSizeMenuOption.mazeSizeButton.setMessage("Maze Size: Insane");
					break;
			}
			List<? extends IGuiEventListener> children = screen.children();
			for (int n = 0; n < children.size(); n++) {
				if (children.get(n) instanceof Button) {
					Button childButton = ((Button) children.get(n));
						if (childButton.getMessage().equals("More World Options...") ||
								childButton.getMessage().equals("Done")) {
							MazeSizeMenuOption.mazeSizeButton.visible = childButton.getMessage().equals("Done");
							Button newMoreOptions = new Button(
									childButton.x,
									childButton.y+30,
									childButton.getWidth(),
									childButton.getHeight(),
									childButton.getMessage(),
									(b)->{
										childButton.onPress();
										MazeSizeMenuOption.mazeSizeButton.visible = !MazeSizeMenuOption.mazeSizeButton.visible;
										b.setMessage(childButton.getMessage());
									});
							MazeSizeMenuOption.mazeSizeButton.x = childButton.x;
							MazeSizeMenuOption.mazeSizeButton.y = childButton.y;
							MazeSizeMenuOption.mazeSizeButton.setWidth(childButton.getWidth());
							MazeSizeMenuOption.mazeSizeButton.setHeight(childButton.getHeight());
							event.addWidget(newMoreOptions);
							event.addWidget(MazeSizeMenuOption.mazeSizeButton);
							event.removeWidget(childButton);
							break;
						}
				}
			}
		}

	}

	/**
	 * Register our Labrynth structure
	 */
	@SubscribeEvent
	public static void onRegisterFeatures(final RegistryEvent.Register<Feature<?>> event) {
		FeatureInit.registerFeatures(event);

		if (DEBUG) {
			LOGGER.log(Level.INFO, "Registered features/structures.");
		}
	}

	@SubscribeEvent
	public void onPlayerMove(LivingEvent.LivingUpdateEvent e) {
		if (!DEBUG) {
			return;
		}
		if (e.getEntityLiving() == null || !(e.getEntityLiving() instanceof PlayerEntity)) {
			return;
		}
		if (!isNether()) {
			return;
		}
		Entity player = e.getEntity();
		MAZE_DRAW_UPDATE_HANDLER.updatePlayerPosition(player.getPosition(), player.getYaw(1.0F));
	}

	@SubscribeEvent
	public void onSpawnNewNetherPortal(EntityTravelToDimensionEvent e) {
		LOGGER.log(Level.INFO,""+e.getDimension().getId());
		if (e.getDimension().getId() == -1) {
			Coords chunk = new Coords((int) (e.getEntity().getPosX()/16), (int) (e.getEntity().getPosZ()/16));
			if (Labrynth.labrynth.getCell(chunk) != null && e.getEntity().getPosY() <= 43)
				e.getEntity().setPosition(e.getEntity().getPosX(), 64, e.getEntity().getPosZ());
		}
	}

	@SubscribeEvent
	public void onPlayerBreakSpeed(PlayerEvent.BreakSpeed e) {
		if (!isNether()) {
			return;
		}
		LightBlockPos myBlockPos = new LightBlockPos(e.getPos());
		if (UnbreakableBlocks.getUnbreakableBlocks().contains(myBlockPos)) {
			e.setNewSpeed(0);
		}
	}

	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate e) {
		if (!isNether()) {
			return;
		}
		List<BlockPos> blocks = e.getExplosion().getAffectedBlockPositions();
		for (int i = 0; i < blocks.size(); i++) {
			if (UnbreakableBlocks.getUnbreakableBlocks().contains(new LightBlockPos(blocks.get(i)))) {
				blocks.remove(i--);
			}
		}
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final Item mazeCenterCompassItem = new MazeCenterCompassItem(new Item.Properties());
		final Item mazeEntranceCompassItem = new MazeEntranceCompassItem(new Item.Properties());
		mazeCenterCompassItem.setRegistryName(new ResourceLocation(Labrynth.MODID, "maze_center_compass"));
		mazeEntranceCompassItem.setRegistryName(new ResourceLocation(Labrynth.MODID, "maze_entrance_compass"));
		event.getRegistry().registerAll(mazeCenterCompassItem, mazeEntranceCompassItem);
	}

	private boolean isNether() {
		try {
			return DimensionType.THE_NETHER.equals(Minecraft.getInstance().world.getDimension().getType());
		} catch (NullPointerException e) {
			return false;
		}
	}
}
