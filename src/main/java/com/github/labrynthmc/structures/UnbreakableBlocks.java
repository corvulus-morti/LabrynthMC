package com.github.labrynthmc.structures;

import com.github.labrynthmc.util.Utils;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

public class UnbreakableBlocks {

	private static final Object lock = new Object();

	private static String currentSaveDir;
	private static UnbreakableBlockSet set;

	public static UnbreakableBlockSet getUnbreakableBlocks () {
		setCurrentUnbreakableSet();
		return set;
	}

	public static void addUnbreakableBlock(LightBlockPos block) {
		setCurrentUnbreakableSet();
		synchronized (lock) {
			set.add(block);
		}
	}

	private static void setCurrentUnbreakableSet() {
		String saveDir = Utils.getCurrentSaveDirectory();
		if (!saveDir.equals(currentSaveDir)) {
			currentSaveDir = saveDir;
			if (set == null) {
				set = new UnbreakableBlockSet();
			}
		}
	}

	public static class UnbreakableBlockSet implements Set<LightBlockPos> {
		Map<ChunkPos, Set<Long>> data = new HashMap<>();

		@Override
		public int size() {
			int total = 0;
			synchronized (data) {
				for (Set<Long> set : data.values()) {
					total += set.size();
				}
			}
			return total;
		}

		@Override
		public boolean isEmpty() {
			synchronized (data) {
				return data.isEmpty();
			}
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof LightBlockPos)) {
				return false;
			}
			LightBlockPos pos = (LightBlockPos) o;
			ChunkPos chunkPos = getChunkPos(pos);
			synchronized (data) {
				if (!data.containsKey(chunkPos)) {
					return false;
				}
				return data.get(chunkPos).contains(pos.data);
			}
		}

		@Override
		public Iterator<LightBlockPos> iterator() {
			return new Iterator<LightBlockPos>() {

				Iterator<Set<Long>> i = data.values().iterator();
				Iterator<Long> i2;

				@Override
				public boolean hasNext() {
					if (i2 == null) {
						if (i.hasNext()) {
							i2 = i.next().iterator();
						} else {
							return false;
						}
					}
					if (!i2.hasNext()) {
						if (i.hasNext()) {
							i2 = i.next().iterator();
						}
					}
					return i2.hasNext();
				}

				@Override
				public LightBlockPos next() {
					if (i2 == null) {
						if (i.hasNext()) {
							i2 = i.next().iterator();
						} else {
							throw new NoSuchElementException();
						}
					}
					if (!i2.hasNext()) {
						if (i.hasNext()) {
							i2 = i.next().iterator();
						}
					}
					return new LightBlockPos(i2.next());
				}
			};
		}

		@Override
		public Object[] toArray() {
			Object[] ret;
			synchronized (data) {
				ret = new Object[size()];
				int i = 0;
				for (Set<Long> value : data.values()) {
					for (Long l : value) {
						ret[i++] = new LightBlockPos(l);
					}
				}
			}
			return ret;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			LightBlockPos[] r = (LightBlockPos[]) a;
			synchronized (data) {
				int i = 0;
				for (Set<Long> value : data.values()) {
					for (Long l : value) {
						r[i++] = new LightBlockPos(l);
					}
				}
			}
			return a;
		}

		@Override
		public boolean add(LightBlockPos lightBlockPos) {
			ChunkPos chunkPos = getChunkPos(lightBlockPos);
			synchronized (data) {
				Set<Long> set = data.getOrDefault(chunkPos, new HashSet<>());
				data.put(chunkPos, set);
				return set.add(lightBlockPos.data);
			}
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof LightBlockPos)) {
				return false;
			}
			ChunkPos chunkPos = getChunkPos(((LightBlockPos) o));
			synchronized (data) {
				Set<Long> set = data.get(chunkPos);
				if (set == null) {
					return false;
				}
				boolean ret = set.remove(((LightBlockPos) o).data);
				if (set.isEmpty()) {
					data.remove(chunkPos);
				}
				return ret;
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			synchronized (data) {
				for (Object o : c) {
					if (!contains(o)) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends LightBlockPos> c) {
			boolean changed = false;
			synchronized (data) {
				for (LightBlockPos lightBlockPos : c) {
					changed |= add(lightBlockPos);
				}
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean changed = false;
			synchronized (data) {
				for (LightBlockPos pos : this) {
					if (!c.contains(pos)) {
						changed |= remove(pos);
					}
				}
			}
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			synchronized (data) {
				for (Object o : c) {
					if (o instanceof LightBlockPos) {
						changed |= remove(o);
					}
				}
			}
			return changed;
		}

		@Override
		public void clear() {
			synchronized (data) {
				data.clear();
			}
		}

		private ChunkPos getChunkPos(LightBlockPos pos) {
			return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		}

		public Set<LightBlockPos> getUnbreakableBlockSetInChunk(ChunkPos pos) {
			Set<Long> set = data.get(pos);
			if (set == null) {
				return Collections.emptySet();
			}
			return set.stream().map(v -> new LightBlockPos(v)).collect(Collectors.toSet());
		}

		public Set<LightBlockPos> getUnbreakableBlockSetInChunk(int chunkPosX, int chunkPosZ) {
			return getUnbreakableBlockSetInChunk(new ChunkPos(chunkPosX, chunkPosZ));
		}
	}
}
