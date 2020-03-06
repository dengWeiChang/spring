package org.framework.util;

import org.framework.lang.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ConcurrentMap}
 *
 * <p>提供并发访问容器，支持空key及空value
 *
 * <p><b>NODE:</b>引用意味着某个节点可能在GC时被回收，
 * 可能会有一个未知的线程删除entries
 *
 * @param <K>
 * @param <V>
 *
 * @author dengweichang
 */
public class ConcurrentReferenceHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

	/**
	 * 默认的初始容量
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * 默认的加载因子
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * 默认引用类型
	 * <p>软引用
	 */
	private static final ReferenceType DEFAULT_REFERENCE_TYPE = ReferenceType.SOFT;

	/**
	 * 最大并发级别
	 */
	private static final int MAX_CONCURRENCY_LEVEL = 1 << 16;

	/**
	 * 最大分段数
	 */
	private static final int MAX_SEGMENT_SIZE = 1 << 30;

	/**
	 * hash高阶位形成的分段数组
	 */
	private final Segment[] segments;

	private final float loadFactor;

	/**
	 * 计算segment数组大小以及hash索引的位移值
	 */
	private final int shift;

	private final ReferenceType referenceType;

	private volatile Set<Map.Entry<K, V>> entrySet;

	/**
	 * 构造
	 * @param initialCapacity 初始容量
	 * @param loadFactor 加载因子
	 * @param concurrencyLevel 预计并发数
	 * @param referenceType 引用类型
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentReferenceHashMap(
			int initialCapacity, float loadFactor, int concurrencyLevel, ReferenceType referenceType) {
		// TODO assert
		this.loadFactor = loadFactor;
		//在期望并发数与最大并发数之间取一个二次方的值的进位量（以1为基准）
		this.shift = calculateShift(concurrencyLevel, MAX_CONCURRENCY_LEVEL);
		//还原并发容量
		int size = 1 << this.shift;
		this.referenceType = referenceType;
		//initialCapacity + size 可能超出int范围，为防止溢出 -1L
		//(initialCapacity - 1)/size + 1;-1防止容量恰好为正数，多出一个容量为无效容量
		int roundedUpSegmentCapacity = (int)((initialCapacity + size - 1L) / size);
		//segment最大容量
		int initialSize = 1 << calculateShift(roundedUpSegmentCapacity, MAX_SEGMENT_SIZE);
		Segment[] segments =(Segment[]) Array.newInstance(Segment.class, size);
		int resizeThreshold = (int)(initialSize * loadFactor);
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new Segment(initialSize, resizeThreshold);
		}
		this.segments = segments;
	}

	/**
	 * 偏移量
	 * @param minimumValue 最小值
	 * @param maximumValue 最大值
	 * @return 1 << shift 处于最大最小值之间
	 */
	private static int calculateShift(int minimumValue, int maximumValue) {
		int shift = 0;
		int value = 1;
		while (value < minimumValue && value < maximumValue) {
			value <<= 1;
			shift++;
		}
		return shift;
	}

	//	private final Segment[] segments;

	private static final class Entry<K, V> implements Map.Entry<K, V> {

		@Nullable
		private final K key;

		@Nullable
		private volatile V value;

		public Entry(@Nullable K key, @Nullable V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		@Nullable
		public K getKey() {
			return this.key;
		}

		@Override
		@Nullable
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			V previous = this.value;
			this.value = value;
			return previous;
		}

		@Override
		public String toString() {
			return this.key + "-" + this.value;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Map.Entry)) {
				return false;
			}
			Map.Entry otherEntry = (Map.Entry)other;
			return ObjectUtils.nullSafeEquals(getKey(), otherEntry.getKey()) &&
					ObjectUtils.nullSafeEquals(getValue(), otherEntry.getValue());
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.key) ^ ObjectUtils.nullSafeHashCode(this.value);
		}
	}

	/**
	 * 引用类型枚举
	 */
	public enum ReferenceType {
		/**
		 * {@link java.lang.ref.SoftReference}
		 */
		SOFT,

		/**
		 * {@link java.lang.ref.WeakReference}
		 */
		WEAK
	}

	/**
	 * map中包含的引用
	 * @param <K> key type
	 * @param <V> value type
	 */
	private interface Reference<K, V> {

		/**
		 * 返回entry的引用，若entry不可用则返回null
		 */
		@Nullable
		Entry<K, V> get();

		/**
		 * 返回引用的hash值
		 * @return hash
		 */
		int getHash();

		/**
		 * 返回chain的下一个引用
		 * @return next reference
		 */
		Reference<K, V> getNext();

		/**
		 * 释放，并确保其被返回
		 * {@code ReferenceManager#pollForPurge()}.
		 */
		void release();
	}

	private class ReferenceManager {
		private final ReferenceQueue<Entry<K, V>> queue = new ReferenceQueue<>();

		public Reference<K, V> createReference(Entry<K, V> entry, int hash, @Nullable Reference<K, V> next) {
			if (referenceType == ReferenceType.WEAK) {
				return new WeakEntryReference<>(entry, hash, next, this.queue);
			}
			return new SoftEntryReference<>(entry, hash, next, this.queue);
		}

		@SuppressWarnings("unchecked")
		public Reference<K, V> pollForPurge() {
			return (Reference<K, V>)this.queue.poll();
		}
	}

	public final float getLoadFactor() {
		return this.loadFactor;
	}

	private static final class SoftEntryReference<K, V> extends SoftReference<Entry<K, V>> implements Reference<K, V> {

		private final int hash;

		@Nullable
		private final Reference<K, V> nextReference;

		public SoftEntryReference(Entry<K, V> entry, int hash, Reference<K, V> nextReference,
								  ReferenceQueue<Entry<K, V>> queue) {
			super(entry, queue);
			this.hash = hash;
			this.nextReference = nextReference;
		}

		@Override
		public int getHash() {
			return this.hash;
		}

		@Override
		public Reference<K, V> getNext() {
			return this.nextReference;
		}

		@Override
		public void release() {
			enqueue();
			clear();
		}
	}

	private static final class WeakEntryReference<K, V> extends WeakReference<Entry<K, V>> implements Reference<K, V> {

		private final int hash;

		@Nullable
		private final Reference<K, V> nextReference;

		public WeakEntryReference(Entry<K, V> entry, int hash, Reference<K, V> nextReference,
								  ReferenceQueue<Entry<K, V>> queue) {
			super(entry, queue);
			this.hash = hash;
			this.nextReference = nextReference;
		}

		@Override
		public int getHash() {
			return this.hash;
		}

		@Override
		public Reference<K, V> getNext() {
			return this.nextReference;
		}

		@Override
		public void release() {
			enqueue();
			clear();
		}
	}

	/**
	 * 单个分段
	 */
	@SuppressWarnings("unchecked")
	private final class Segment extends ReentrantLock {
		private final ReferenceManager referenceManager = new ReferenceManager();
		private final int initialSize;
		private volatile Reference<K, V>[] references;
		private final AtomicInteger count = new AtomicInteger(0);
		private int resizeThreshold;

		public Segment(int initialSize, int resizeThreshold) {
			this.initialSize = initialSize;
			this.resizeThreshold = resizeThreshold;
			this.references = createReferenceArray(initialSize);
		}

		public Reference<K, V> getReference(@Nullable Object key, int hash, Restructure restructure) {
			if (restructure == Restructure.WHEN_NECESSARY) {
				restructureIfNecessary(false);
			}
			if (this.count.get() == 0) {
				return null;
			}
			Reference<K, V>[] references = this.references;
			int index = getIndex(hash, references);
			Reference<K, V> head = references[index];
			return findInChain(head, key, hash);
		}

		public void clear() {
			if (this.count.get() == 0) {
				return;
			}
			lock();
			try {
				this.references = createReferenceArray(this.initialSize);
				this.resizeThreshold = (int) (this.references.length * getLoadFactor());
				this.count.set(0);
			}
			finally {
				unlock();
			}
		}

		private int getIndex(int hash, Reference<K, V>[] references) {
			return (hash & (references.length - 1));
		}

		protected final void restructureIfNecessary(boolean allowResize) {
			int currCount = this.count.get();
			boolean needResize = allowResize && (currCount > 0 && currCount >= this.resizeThreshold);
			Reference<K, V> ref = this.referenceManager.pollForPurge();
			if (ref != null || (needResize)) {
				restructure(allowResize, ref);
			}
		}

		/**
		 * 重新构造segment,通俗来讲就是扩容
		 * @param allowResize 是否允许重新设置大小
		 * @param ref 引用对象
		 */
		private void restructure(boolean allowResize, @Nullable Reference<K, V> ref) {
			boolean needsResize;
			lock();
			try {
				int countAfterRestructure = this.count.get();
				Set<Reference<K, V>> toPurge = Collections.emptySet();
				if (ref != null) {
					toPurge = new HashSet<>();
					while (ref != null) {
						toPurge.add(ref);
						ref = this.referenceManager.pollForPurge();
					}
				}
				countAfterRestructure -= toPurge.size();
				needsResize = (countAfterRestructure > 0 && countAfterRestructure >= this.resizeThreshold);
				boolean resizing = false;
				int  restructureSize = this.references.length;
				if (allowResize && needsResize && restructureSize < MAX_SEGMENT_SIZE) {
					restructureSize <<= 1;
					resizing = true;
				}
				Reference<K, V>[] restructured = resizing ? createReferenceArray(restructureSize) : this.references;
				for (int i = 0; i < this.references.length; i++) {
					ref = this.references[i];
					if (!resizing) {
						restructured[i] = null;
					}
					while (ref != null) {
						if (!toPurge.contains(ref)) {
							Entry<K, V> entry = ref.get();
							int index = getIndex(ref.getHash(), restructured);
							restructured[index] = this.referenceManager.createReference(entry, ref.getHash(), restructured[index]);
						}
						ref = ref.getNext();
					}
					if (resizing) {
						this.references = restructured;
						this.resizeThreshold = (int) (this.references.length * getLoadFactor());
					}
					this.count.set(Math.max(countAfterRestructure, 0));
				}
			}
			finally {
				unlock();
			}
		}


		@Nullable
		private Reference<K, V> findInChain(Reference<K, V> ref, @Nullable Object key, int hash) {
			Reference<K, V> currRef = ref;
			while (currRef != null) {
				if (currRef.getHash() == hash) {
					Entry<K, V> entry = currRef.get();
					if (entry != null) {
						K entryKey = entry.getKey();
						if (ObjectUtils.nullSafeEquals(entryKey, key)) {
							return currRef;
						}
					}
				}
				currRef = currRef.getNext();
			}
			return null;
		}

		public final int getSize() {
			return this.references.length;
		}

		public final int getCount() {
			return this.count.get();
		}
	}

	/**
	 * 必要条件
	 */
	protected enum Restructure {

		WHEN_NECESSARY, NEVER
	}


	@SuppressWarnings("unchecked")
	private Reference<K, V>[] createReferenceArray(int size) {
		return new Reference[size];
	}





	@Override
	public V putIfAbsent(K key, V value) {
		return null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		return false;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return false;
	}

	@Override
	public V replace(K key, V value) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		return null;
	}

	@Override
	public V put(K key, V value) {
		return null;
	}

	@Override
	public V remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {

	}

	@Override
	public void clear() {

	}

	@Override
	public Set<K> keySet() {
		return null;
	}

	@Override
	public Collection<V> values() {
		return null;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return null;
	}


}
