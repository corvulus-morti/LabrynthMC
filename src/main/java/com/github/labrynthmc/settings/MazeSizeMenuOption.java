package com.github.labrynthmc.settings;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Grid;
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

    public static Grid getWorldMaze(World world)
    {
        Grid maze;
        String dir = Minecraft.getInstance().gameDir.getAbsolutePath();
        String save = Minecraft.getInstance().getIntegratedServer().getFolderName();
        File mazeSaveData = new File(dir+"/saves/"+save+"/","maze_grid.dat");

        int size;
        File mazeSizeData = new File(dir+"/saves/"+save+"/","maze_size.dat");

        if (!mazeSaveData.exists()) {
            size = mazeSize;
            maze = Grid.genMaze(world.getSeed(),MAZE_SIZES[size]);
            saveMaze(mazeSaveData,maze);
            saveMazeSize(mazeSizeData, size);
        }
        else {
            maze = loadMaze(mazeSaveData);
            size = loadMazeSize(mazeSizeData);
        }

        return maze;
    }

    public static void saveMaze(File file, Grid maze) {
        try {
            ObjectOutputStream obOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            obOut.writeObject(maze);
            obOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public static Grid loadMaze(File file) {
        try {
            ObjectInputStream obIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return (Grid) obIn.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return null;
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
