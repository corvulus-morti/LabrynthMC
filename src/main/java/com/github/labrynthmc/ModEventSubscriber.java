package com.github.labrynthmc;


import com.github.labrynthmc.mazegen.Coords;
import com.github.labrynthmc.mazegen.Grid;
import com.github.labrynthmc.settings.MazeSizeMenuOption;
import com.github.labrynthmc.world.FeatureInit;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

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
		if (screen.getTitle().getFormattedText().equals("Create New World"))
			if (!MazeSizeMenuOption.buttonAdded) {
				//MazeSizeMenuOption.buttonAdded = true;

				MazeSizeMenuOption.mazeSizeButton.visible = false;

				int n;
				for (n = 0; n < screen.children().toArray().length; n++)
					if (screen.children().toArray()[n] instanceof Button)
						if (((Button) screen.children().toArray()[n]).getMessage().equals("More World Options...")) break;

				Button childButton = ((Button) screen.children().toArray()[n]);

				Button newMoreOptions = new Button(
						childButton.x,
						childButton.y+30,
						childButton.getWidth(),
						childButton.getHeight(),
						childButton.getMessage(),
						(b)->{
							childButton.onPress();
							MazeSizeMenuOption.mazeSizeButton.visible = !MazeSizeMenuOption.mazeSizeButton.visible;
						});
				MazeSizeMenuOption.mazeSizeButton.x = childButton.x;
				MazeSizeMenuOption.mazeSizeButton.y = childButton.y;
				MazeSizeMenuOption.mazeSizeButton.setWidth(childButton.getWidth());
				MazeSizeMenuOption.mazeSizeButton.setHeight(childButton.getHeight());
				event.addWidget(newMoreOptions);
				event.addWidget(MazeSizeMenuOption.mazeSizeButton);
				event.removeWidget(childButton);
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
		if(!DEBUG) {
			return;
		}
		if (e.getEntityLiving() == null || !(e.getEntityLiving() instanceof PlayerEntity)) {
			return;
		}
		Entity player = e.getEntity();
		MAZE_DRAW_UPDATE_HANDLER.updatePlayerPosition(player.getPosition());
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


}
