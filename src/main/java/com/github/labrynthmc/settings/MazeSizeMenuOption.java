package com.github.labrynthmc.settings;

import com.github.labrynthmc.Labrynth;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

import static com.github.labrynthmc.Labrynth.*;

public class MazeSizeMenuOption {

    public static boolean buttonAdded = false;
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

    public static int getWorldMazeSize(World world)
    {
        int size;

        String dir = Minecraft.getInstance().gameDir.getAbsolutePath();
        String save = Minecraft.getInstance().getIntegratedServer().getFolderName();
        File saveData = new File(dir+"/saves/"+save+"/","mazeSize.txt");
        if (!saveData.exists()) {
            saveMazeSize(saveData, mazeSize);
            size = mazeSize;
        }
        else {
            size = loadMazeSize(saveData);
        }

        return size;
    }

    public static void saveMazeSize(File file, int mazeSize) {
        try {
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            LOGGER.error(mazeSize);
            bw.write(""+mazeSize);
            bw.close();
        } catch (IOException e) {
            Labrynth.LOGGER.error("FILE NOT FOUND");
        }
    }
    public static int loadMazeSize(File file) {
        int size = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            size = Integer.parseInt(br.readLine());
            br.close();
        } catch (IOException e) {
            Labrynth.LOGGER.error("FILE NOT FOUND");
        }

        return size;
    }


}
