package com.github.labrynthmc.settings;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Grid;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.world.World;

import java.io.*;

import static com.github.labrynthmc.Labrynth.*;
import static com.github.labrynthmc.util.Utils.getCurrentSaveDirectory;

public class MazeSizeMenuOption {

    public static Button mazeSizeButton = new Button(0, 0, 200, 20, "Maze Size: Small", (b) ->
        {
            mazeSize = (mazeSize+1)% MAZE_SIZES.length;
            switch (mazeSize){
                case 0:
                    b.setMessage("Maze Size: Small");
                    break;
                case 1:
                    b.setMessage("Maze Size: Medium");
                    break;
                case 2:
                    b.setMessage("Maze Size: Large");
                    break;
                case 3:
                    b.setMessage("Maze Size: Insane");
                    break;
            }
            LOGGER.info("Maze size set to " + MAZE_SIZES[mazeSize] + ".");
        });


}
