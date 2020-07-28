package com.github.labrynthmc.structures;

import com.github.labrynthmc.util.Utils;

import java.util.*;

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

//		Utils.throttle(() -> {
//			Set<LightBlockPos> tmp = new HashSet<>();
//			long t = System.currentTimeMillis();
//			synchronized (lock) {
//				tmp.addAll(set);
//			}
//			Settings.writeUnbreakableBlocks(tmp);
//		}, token, 5000);
	}

	private static void setCurrentUnbreakableSet() {
		String saveDir = Utils.getCurrentSaveDirectory();
		if (!saveDir.equals(currentSaveDir)) {
			currentSaveDir = saveDir;
//			set = Settings.readUnbreakableBlocks();
			if (set == null) {
				set = new UnbreakableBlockSet();
			}
		}
	}

	private static class UnbreakableBlockSet implements Set<LightBlockPos> {
		Set<Long> data = new HashSet<>();

		@Override
		public int size() {
			return data.size();
		}

		@Override
		public boolean isEmpty() {
			return data.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof LightBlockPos)) {
				return false;
			}
			return data.contains(((LightBlockPos) o).data);
		}

		@Override
		public Iterator<LightBlockPos> iterator() {
			return new Iterator<LightBlockPos>() {

				Iterator<Long> i = data.iterator();

				@Override
				public boolean hasNext() {
					return i.hasNext();
				}

				@Override
				public LightBlockPos next() {
					return new LightBlockPos(i.next());
				}
			};
		}

		@Override
		public Object[] toArray() {
			return data.stream().map(v -> new LightBlockPos(v)).toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			LightBlockPos[] r = (LightBlockPos[]) a;
			int i = 0;
			for (long d : data) {
				r[i] = new LightBlockPos(d);
			}
			return a;
		}

		@Override
		public boolean add(LightBlockPos lightBlockPos) {
			return data.add(lightBlockPos.data);
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof LightBlockPos)) {
				return false;
			}
			return data.remove(((LightBlockPos) o).data);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object o : c) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends LightBlockPos> c) {
			boolean changed = false;
			for (LightBlockPos lightBlockPos : c) {
				changed |= data.add(lightBlockPos.data);
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean changed = false;
			for (long d : data) {
				if (c.contains(new LightBlockPos(d))) {
					changed |= data.remove(d);
				}
			}
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for (Object o : c) {
				if (o instanceof LightBlockPos) {
					changed |= data.remove(((LightBlockPos) o).data);
				}
			}
			return changed;
		}

		@Override
		public void clear() {
			data.clear();
		}
	}
}
