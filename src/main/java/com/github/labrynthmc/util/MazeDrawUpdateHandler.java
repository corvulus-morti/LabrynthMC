package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.*;

public class MazeDrawUpdateHandler {

	public static int PORT = 58888;

	private static MazeDrawUpdateHandler instance;

	ThreadPoolExecutor executor;

	BlockPos lastPlayerPosition;
	float lastYaw = 0.0f;
	Socket s;
	PrintStream out;

	private MazeDrawUpdateHandler() {
		executor = new ThreadPoolExecutor(1, 1, 0,TimeUnit.MILLISECONDS,
				new LinkedBlockingDeque<>());
	}

	public static MazeDrawUpdateHandler getInstance() {
		if (instance == null) {
			instance = new MazeDrawUpdateHandler();
		}
		return instance;
	}

	public void updatePlayerPosition(BlockPos pos, float yaw) {
		if (lastPlayerPosition != null &&
				pos.getX() == lastPlayerPosition.getX() && pos.getZ() == lastPlayerPosition.getZ() && yaw == lastYaw) {
			return;
		}

		if (Labrynth.DEBUG) {
			Labrynth.LOGGER.log(Level.INFO, "updating the players location" + pos.getX() + ", " + pos.getZ());
		}

		lastPlayerPosition = pos;
		lastYaw = yaw;

		executor.getQueue().poll();
		executor.execute(() -> getPrintStream().println("pos " + pos.getX() + " " + pos.getZ() + " " + yaw));
	}

	public void updateWorldSeed(long seed) {
		if (s != null) {
			try {
				Labrynth.LOGGER.log(Level.INFO, "closing the connection");
				s.close(); // close for now so that we can open a new one
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		executor.execute(() -> getPrintStream().println("seed " + seed));

	}

	public void updateMaxPaths(int paths) {
		if (s != null) {
			try {
				Labrynth.LOGGER.log(Level.INFO, "closing the connection");
				s.close(); // close for now so that we can open a new one
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		executor.execute(() -> getPrintStream().println("maxPaths " + paths));

	}

	private PrintStream getPrintStream() {

		if (s == null || s.isClosed() || s.isOutputShutdown()) {
			Labrynth.LOGGER.log(Level.INFO, "need new connection");
			try {
				s = new Socket("localhost", PORT);
				out = new PrintStream(s.getOutputStream());
			} catch (IOException e) {
				Labrynth.LOGGER.log(Level.ERROR, "failed connection");
				e.printStackTrace();
			}
		}
		return out;
	}

}
