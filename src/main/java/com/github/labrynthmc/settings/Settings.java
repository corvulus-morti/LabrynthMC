package com.github.labrynthmc.settings;

import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.util.Utils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Settings {

	private static String UNBREAKABLE_BLOCKS_FILE = "unbreakable.dat";

	public static Set<LightBlockPos> readUnbreakableBlocks() {
		File file = new File(Utils.getCurrentSaveDirectory(), UNBREAKABLE_BLOCKS_FILE);
		if (!file.exists()) {
			return null;
		}
		Set<LightBlockPos> ret = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = in.readLine()) != null ) {
				String[] split = line.split(",");
				ret.add(new LightBlockPos(
						Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])));
			}
			in.close();
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
			PrintStream out = new PrintStream(new FileOutputStream(file));
			for (LightBlockPos pos : posSet) {
				out.println(pos.x + "," + pos.y + "," + pos.z);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
