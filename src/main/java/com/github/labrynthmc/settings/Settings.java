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
				out.writeInt(pos.x);
				out.writeInt(pos.y);
				out.writeInt(pos.z);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
