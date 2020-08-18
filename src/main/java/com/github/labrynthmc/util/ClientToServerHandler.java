package com.github.labrynthmc.util;

import com.github.labrynthmc.Labrynth;
import com.github.labrynthmc.structures.LightBlockPos;
import com.github.labrynthmc.structures.UnbreakableBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
			PROTOCOL_VERSION::equals,//
			PROTOCOL_VERSION::equals
		);
		simpleChannel.registerMessage(IDX, IntString.class,
				(intString, packetBuffer) -> {
					String toSend = intString.s;
					byte[] toSendByte = null;
					boolean isCompressed = false;
					if (toSend.length() > 256) {
						toSendByte = compress(intString.s);
						isCompressed = toSendByte != null;
					}
					packetBuffer.writeInt(intString.id);
					packetBuffer.writeBoolean(intString.isResponse);
					packetBuffer.writeBoolean(isCompressed);
					if (isCompressed) {
						packetBuffer.writeByteArray(toSendByte);
					} else {
						packetBuffer.writeString(toSend);
					}
				},
				packetBuffer -> {
					int id = packetBuffer.readInt();
					boolean isResponse = packetBuffer.readBoolean();
					boolean isCompressed = packetBuffer.readBoolean();
					String s = "";
					if (isCompressed) {
						s = decompress(packetBuffer.readByteArray());
					} else {
						s = packetBuffer.readString(Integer.MAX_VALUE / 4);
					}
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
					contextSupplier.get().setPacketHandled(true);
				}
		);
	}

	public Future<String> applyForResult(String s) {
		if (!hasConnection()) {
			CompletableFuture<String> ret = new CompletableFuture<>();
			ret.complete(handle(s));
			return ret;
		} else {
			IntString is = new IntString(idx++, s, false);
			CompletableFuture<String> ret = new CompletableFuture<>();
			futures.put(is.id, ret);
			simpleChannel.sendToServer(is);
			return ret;
		}
	}

	public void apply(String s, Consumer<String> consumer) {
		if (!hasConnection()) {
			consumer.accept(handle(s));
		} else {
			IntString is = new IntString(idx++, s, false);
			simpleChannel.sendToServer(is);
			consumers.put(is.id, consumer);
		}
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
				case "getUnbreakableBlocksChunk": {
					int chunkX = Integer.parseInt(split[1]);
					int chunkZ = Integer.parseInt(split[2]);
					StringBuilder builder = new StringBuilder();
					for (LightBlockPos pos : UnbreakableBlocks.getUnbreakableBlocks().getUnbreakableBlockSetInChunk(chunkX, chunkZ)) {
						builder.append(String.format("%d %d %d ", pos.getX(), pos.getY(), pos.getZ()));
					}
					for (int x = chunkX << 4; x < (chunkX << 4) + 16; x++) {
						for (int z = chunkZ << 4; z < (chunkZ << 4) + 16; z++) {
							for (int y = 20; y < 45; y++) {
								if (UnbreakableBlocks.getUnbreakableBlocks().contains(new LightBlockPos(x, y, z))) {
									builder.append(String.format("%d %d %d ", x, y, z));
								}
							}
						}
					}
					return builder.toString();
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

	private static byte[] compress(String s) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gout = null;
		try {
			gout = new GZIPOutputStream(out);
			gout.write(s.getBytes());
			gout.close();
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String decompress(byte[] bytes) {
		try {
			GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes));
			StringWriter writer = new StringWriter();
			byte[] buf = new byte[1024];
			char[] charbuf = new char[1024];
			int n;
			while((n = in.read(buf)) != -1) {
				for (int i = 0; i < n; i++) {
					charbuf[i] = (char) buf[i];
				}
				writer.write(charbuf, 0, n);
			}
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static boolean hasConnection() {
		return Minecraft.getInstance().getConnection() != null;
	}
}
