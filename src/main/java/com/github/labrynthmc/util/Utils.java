package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.mazegen.Coords;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class Utils {

	private static ExecutorService executorService = Executors.newFixedThreadPool(4);
	private static Thread t;
	private static PriorityBlockingQueue<FutureTask> tasks = new PriorityBlockingQueue<>();
	private static Map<Object, FutureTask> tasksMap = new HashMap<>();

	private static Object lock = new Object();

	private static class FutureTask implements Comparable<FutureTask> {

		Object token;
		long launchTime;
		long rateLimit;
		Runnable runnable;

		private FutureTask(Object token, long launchTime, long rateLimit, Runnable runnable) {
			this.token = token;
			this.launchTime = launchTime;
			this.rateLimit = rateLimit;
			this.runnable = runnable;
		}

		@Override
		public int compareTo(FutureTask o) {
			return Long.compare(launchTime, o.launchTime);
		}
	}

	/**
	 * When something needs to be run repeatedly but should not be run multiple times in a given time window.
	 * @param runnable Task to run
	 * @param token Unique object to identify which task is being run
	 * @param rateLimit The timeframe to only run the task once in
	 */
	public static void throttle(Runnable runnable, Object token, long rateLimit) {
		if (t == null) {
			t = new Thread(() -> {
				while (true) {
					try {
						FutureTask futureTask = tasks.take();
						synchronized (lock) {
							tasks.put(futureTask);
						}
						Thread.sleep(Math.max(0, futureTask.launchTime - System.currentTimeMillis()));
						synchronized (lock) {
							if (futureTask.runnable == null) {
								tasksMap.remove(futureTask.token);
								tasks.poll();
							} else {
								executorService.submit(futureTask.runnable);
								futureTask.launchTime = System.currentTimeMillis() + futureTask.rateLimit;
								futureTask.runnable = null;
								tasks.poll();
								tasks.offer(futureTask);
							}
						}
					} catch (InterruptedException e) {
					}
				}
			});
			t.start();
		}
		synchronized (lock) {
			if (tasksMap.containsKey(token)) {
				FutureTask futureTask = tasksMap.get(token);
				futureTask.runnable = runnable;
				futureTask.rateLimit = rateLimit;
			} else {
				FutureTask futureTask = new FutureTask(token, System.currentTimeMillis(), rateLimit, runnable);
				tasksMap.put(token, futureTask);
				tasks.put(futureTask);
				t.interrupt();
			}
		}
	}

	private static String saveDir = ".";
	public static String getCurrentSaveDirectory() {
		return saveDir;
	}
	public static void setCurrentSaveDirectory(String dir) {
		File f = new File(dir);
		f.mkdir();
		saveDir = dir;
	}

	public static BlockPos coordToBlockPos(Coords coords) {
		return new BlockPos(coords.getX() * 16 + 8, Labrynth.MAZE_Y_POS + 10, coords.getY() * 16 + 8);
	}

	public static boolean isNether(EntityEvent e) {
		return isNether(e.getEntity().getEntityWorld());
	}

	public static boolean isNether(IWorld world) {
		return world.getDimension().getType().equals(DimensionType.THE_NETHER);
	}
}
