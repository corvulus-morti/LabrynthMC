package com.github.labrynthmc.settings;

import com.github.labrynthmc.Labrynth;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

        try {
            File saveData = Objects.requireNonNull(world.getServer().getDataDirectory().listFiles())[0];
            Scanner readData = new Scanner(saveData);
            size = readData.nextInt();
        } catch (IOException e) {
            LOGGER.info("Maze config not found. Creating config.");
            size = mazeSize;
            MazeSizeMenuOption.addSettingToWorld(world);
        }

        return size;
    }

    public static void addSettingToWorld(World world)
    {
        File f = world.getServer().getDataDirectory();
        File saveData = new File(f,"mazeSize.dat");
        try {
            PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(saveData)));
            printStream.println(Labrynth.mazeSize);
            printStream.close();
        } catch (IOException e) {
            Labrynth.LOGGER.error("FILE NOT FOUND");
        }
    }


}
