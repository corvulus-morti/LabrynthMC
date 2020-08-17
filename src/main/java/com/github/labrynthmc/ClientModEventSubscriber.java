package com.github.labrynthmc;

import com.github.labrynthmc.settings.MazeSizeMenuOption;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.structures.UnbreakableBlocks;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

import static com.github.labrynthmc.Labrynth.*;
import static com.github.labrynthmc.util.Utils.isNether;

public class ClientModEventSubscriber {

	@SubscribeEvent
	public void onPlayerMove(LivingEvent.LivingUpdateEvent e) {
		if (!DEBUG) {
			return;
		}
		if (e.getEntityLiving() == null || !(e.getEntityLiving() instanceof PlayerEntity)) {
			return;
		}
		if (!isNether(e)) {
			return;
		}
		Entity player = e.getEntity();
		MAZE_DRAW_UPDATE_HANDLER.updatePlayerPosition(player.getPosition(), player.getYaw(1.0F));
	}

	@SubscribeEvent
	public void onPlayerBreakSpeed(PlayerEvent.BreakSpeed e) {
		if (!isNether(e)) {
			return;
		}
		BlockPos pos = e.getPos();
		LightBlockPos lightPos = new LightBlockPos(pos);
		if (UnbreakableBlocks.getUnbreakableBlocks().contains(lightPos)) {
			e.setNewSpeed(0);
		} else {
			Labrynth.clientToServerHandler.apply("canBreak " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), (s) -> {
				if (s.equals("false")) {
					UnbreakableBlocks.addUnbreakableBlock(lightPos);
				}
			});
		}
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
}
