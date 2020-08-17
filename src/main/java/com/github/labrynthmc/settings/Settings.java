package com.github.labrynthmc.settings;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Grid;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.util.Utils;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static com.github.labrynthmc.Labrynth.*;
import static com.github.labrynthmc.util.Utils.getCurrentSaveDirectory;

public class Settings {

	private static String UNBREAKABLE_BLOCKS_FILE = "unbreakable.dat";

	public static Set<LightBlockPos> readUnbreakableBlocks() {
		File file = new File(Utils.getCurrentSaveDirectory(), UNBREAKABLE_BLOCKS_FILE);
		if (!file.exists()) {
			return null;
		}
		Set<LightBlockPos> ret = new HashSet<>();
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			while (true) {
				ret.add(new LightBlockPos(in.readInt(), in.readInt(), in.readInt()));
			}
		} catch (EOFException e) {
			return ret;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeUnbreakableBlocks(Set<LightBlockPos> posSet) {
		File file = new File(Utils.getCurrentSaveDirectory(), UNBREAKABLE_BLOCKS_FILE);

		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			for (LightBlockPos pos : posSet) {
				out.writeInt(pos.getX());
				out.writeInt(pos.getY());
				out.writeInt(pos.getZ());
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Grid getWorldMaze(World world)
	{
		Grid maze = new Grid();
		File mazeSaveData = new File(getCurrentSaveDirectory(), "maze_grid.dat");
		File mazeSizeData = new File(getCurrentSaveDirectory(), "maze_size.dat");

		int size;

		if (!mazeSaveData.exists()) {
			size = mazeSize;
			maze.genMaze(world.getSeed(), MAZE_SIZES[size]);
			saveMaze(mazeSaveData, maze);
			saveMazeSize(mazeSizeData, size);
		} else {
			maze = loadMaze(mazeSaveData);
			size = loadMazeSize(mazeSizeData);
		}

		return maze;
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

}
