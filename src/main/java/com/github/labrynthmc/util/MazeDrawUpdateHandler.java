package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.*;

public class MazeDrawUpdateHandler {

	public static int PORT = 58888;

	private static MazeDrawUpdateHandler instance;

	private final Object updateLocationToken = new Object();

	private BlockPos lastPlayerPosition;
	private float lastYaw = 0.0f;
	private Socket s;
	private PrintStream out;

	private MazeDrawUpdateHandler() {
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

		lastPlayerPosition = pos;
		lastYaw = yaw;
		Utils.throttle(() -> getPrintStream().println("pos " + pos.getX() + " " + pos.getZ() + " " + yaw),
				updateLocationToken, 1);
	}

	public void updateWorldSeed(long seed) {
		if (s != null) {
			try {
				s.close(); // close for now so that we can open a new one
			} catch (IOException e) {
			}
		}
		Utils.throttle(() -> getPrintStream().println("seed " + seed),
				new Object(), 1);
	}

	public void updateMaxPaths(int paths) {
		if (s != null) {
			try {
				s.close(); // close for now so that we can open a new one
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Utils.throttle(() -> getPrintStream().println("maxPaths " + paths),
				new Object(), 1);
	}

	private PrintStream getPrintStream() {

		if (s == null || s.isClosed() || s.isOutputShutdown()) {
			try {
				s = new Socket("localhost", PORT);
				out = new PrintStream(s.getOutputStream());
			} catch (IOException e) {
			}
		}
		if (out == null) {
			return new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {}
			});
		}
		return out;
	}

}
