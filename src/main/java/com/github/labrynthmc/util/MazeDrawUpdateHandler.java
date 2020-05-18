package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MazeDrawUpdateHandler {

	public static int PORT = 58888;

	private static MazeDrawUpdateHandler instance;

	Executor executor;

	BlockPos lastPlayerPosition;
	Socket s;
	PrintStream out;

	private MazeDrawUpdateHandler() {
		executor = Executors.newSingleThreadExecutor();
	}

	public static MazeDrawUpdateHandler getInstance() {
		if (instance == null) {
			instance = new MazeDrawUpdateHandler();
		}
		return instance;
	}

	public void updatePlayerPosition(BlockPos pos) {
		if (lastPlayerPosition != null &&
				pos.getX() == lastPlayerPosition.getX() && pos.getY() == lastPlayerPosition.getY()) {
			return;
		}

		Labrynth.LOGGER.log(Level.INFO, "updating the players location" + pos.getX() + ", " + pos.getZ());

		lastPlayerPosition = pos;

		executor.execute(() -> getPrintStream().println("pos " + pos.getX() + " " + pos.getZ()));
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
