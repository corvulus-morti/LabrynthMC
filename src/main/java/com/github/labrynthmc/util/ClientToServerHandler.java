package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.structures.UnbreakableBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ClientToServerHandler {

	static final int IDX = 45985335;
	private static final String PROTOCOL_VERSION = "1";

	private final SimpleChannel simpleChannel;
	Map<Integer, CompletableFuture<String>> futures = new HashMap<>();
	Map<Integer, Consumer<String>> consumers = new HashMap<>();

	public volatile int idx = 0;

	public ClientToServerHandler() {
		simpleChannel = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("labrynthmc", "clienttoserverhandler"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
		);
		simpleChannel.registerMessage(IDX, IntString.class,
				(intString, packetBuffer) -> {
					packetBuffer.writeInt(intString.id);
					packetBuffer.writeString(intString.s);
					packetBuffer.writeBoolean(intString.isResponse);
				},
				packetBuffer -> {
					int id = packetBuffer.readInt();
					String s = packetBuffer.readString(Integer.MAX_VALUE / 4);
					boolean isResponse = packetBuffer.readBoolean();
					return new IntString(id, s, isResponse);
				},
				(intString, contextSupplier) -> {
					if (intString.isResponse) {
						if (futures.containsKey(intString.id)) {
							futures.get(intString.id).complete(intString.s);
						} else if (consumers.containsKey(intString.id)) {
							consumers.get(intString.id).accept(intString.s);
						}
					} else {
						simpleChannel.sendTo(new IntString(intString.id, handle(intString.s), true),
								contextSupplier.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
					}
				}
		);
	}

	public Future<String> apply(String s) {
		IntString is = new IntString(idx++, s, false);
		simpleChannel.sendToServer(is);
		CompletableFuture<String> ret = new CompletableFuture<>();
		futures.put(is.id, ret);
		return ret;
	}

	public void apply(String s, Consumer<String> consumer) {
		IntString is = new IntString(idx++, s, false);
		simpleChannel.sendToServer(is);
		consumers.put(is.id, consumer);
	}

	private static final String handle(String query) {
		String[] split = query.split(" ");
		if (split.length == 0) {
			return "";
		}
		try {
			switch (split[0]) {
				case "canBreak": {
					int x = Integer.parseInt(split[1]);
					int y = Integer.parseInt(split[2]);
					int z = Integer.parseInt(split[3]);
					return "" + !UnbreakableBlocks.getUnbreakableBlocks().contains(new LightBlockPos(x, y, z));
				}
			}
		} catch (Throwable t) {
			Labrynth.LOGGER.error("Unable to process request " + query, t);
		}
		return "";
	}

	static class IntString {
		int id;
		String s;
		boolean isResponse;

		IntString(int id, String s, boolean isResponse) {
			this.id = id;
			this.s = s;
			this.isResponse = isResponse;
		}
	}
}
