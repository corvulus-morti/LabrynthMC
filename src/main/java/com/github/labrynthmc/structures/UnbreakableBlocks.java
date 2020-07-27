package com.github.labrynthmc.structures;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.settings.Settings;
import com.github.labrynthmc.util.Utils;

import java.util.HashSet;
import java.util.Set;

public class UnbreakableBlocks {

	private static final Object token = new Object();
	private static final Object lock = new Object();

	private static String currentSaveDir;
	private static Set<LightBlockPos> set;

	public static Set<LightBlockPos> getUnbreakableBlocks () {
		setCurrentUnbreakableSet();
		return set;
	}

	public static void addUnbreakableBlock(LightBlockPos block) {
		setCurrentUnbreakableSet();
		synchronized (lock) {
			set.add(block);
		}

		Utils.throttle(() -> {
			Set<LightBlockPos> tmp = new HashSet<>();
			synchronized (lock) {
				tmp.addAll(set);
			}
			Settings.writeUnbreakableBlocks(tmp);
		}, token, 1000);
	}

	private static void setCurrentUnbreakableSet() {
		String saveDir = Utils.getCurrentSaveDirectory();
		if (!saveDir.equals(currentSaveDir)) {
			currentSaveDir = saveDir;
			set = Settings.readUnbreakableBlocks();
			if (set == null) {
				set = new HashSet<>();
			}
		}
	}

}
