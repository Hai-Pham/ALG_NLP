package edu.berkeley.nlp.assignments.assign1.student.Utility;

import edu.berkeley.nlp.util.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;

/** Open Addressing HashMap from Shorts -> Integers
 *
 */
public class LongShortOpenHashMap {

    private long[] keys;

    private short[] values;

    private int size = 0;
    private int sizeInTheory = 0;

    private final long EMPTY_KEY = -1;

    private final double MAX_LOAD_FACTOR;

    public boolean put(long k, short v) {
        if (size / (double) keys.length > MAX_LOAD_FACTOR) {
            rehash();
        }
        return putHelp(k, v, keys, values);

    }

    public LongShortOpenHashMap() {
        this(10);
    }

    public LongShortOpenHashMap(int initialCapacity_) {
        this(initialCapacity_, 0.7);
    }

    public LongShortOpenHashMap(int initialCapacity_, double loadFactor) {
        int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
        MAX_LOAD_FACTOR = loadFactor;
        values = new short[cap];
        Arrays.fill(values, (short) -1);
        keys = new long[cap];
        Arrays.fill(keys, -1); // added to avoid collision with k = 0
        sizeInTheory = initialCapacity_;
    }

    private void rehash() {
        long[] newKeys = new long[keys.length * 3 / 2];
        short[] newValues = new short[values.length * 3 / 2];
        Arrays.fill(newValues, (short)-1);
        Arrays.fill(newKeys, -1);
        size = 0;
        for (int i = 0; i < keys.length; ++i) {
            long curr = keys[i];
            if (curr != EMPTY_KEY) {
                short val = values[i];
                putHelp(curr, val, newKeys, newValues);
            }
        }
        keys = newKeys;
        values = newValues;
    }

    public void rehash(double expandedRatio) {
        long[] newKeys = new long[ (int)(keys.length * expandedRatio)];
        short[] newValues = new short[(int) (values.length * expandedRatio)];
        Arrays.fill(newValues, (short)0);
        Arrays.fill(newKeys, -1);
        size = 0;
        for (int i = 0; i < keys.length; ++i) {
            long curr = keys[i];
            if (curr != EMPTY_KEY) {
                short val = values[i];
                putHelp(curr, val, newKeys, newValues);
            }
        }
        keys = newKeys;
        values = newValues;
    }

    private boolean putHelp(long k, short v, long[] keyArray, short[] valueArray) {
        int pos = getInitialPos(k, keyArray);
        long curr = keyArray[pos];
        while (curr != EMPTY_KEY && curr != k) {
            pos++;
            if (pos == keyArray.length) pos = 0;
            curr = keyArray[pos];
        }

        valueArray[pos] = v;
        if (curr == EMPTY_KEY) {
            size++;
            keyArray[pos] = k;
            return true;
        }
        return false;
    }

    private int getInitialPos(long k, long[] keyArray) {
        int hash = getHashCode(k);
        int pos = hash % keyArray.length;
        if (pos < 0) pos += keyArray.length;
        // N.B. Doing it this old way causes Integer.MIN_VALUE to be
        // handled incorrect since -Integer.MIN_VALUE is still
        // Integer.MIN_VALUE
//		if (hash < 0) hash = -hash;
//		int pos = hash % keyArray.length;
        return pos;
    }
    // helper for hash code
    private int getHashCode(long n) {
        return (int) ((131111L*n)^n^(1973*n)%sizeInTheory);
    }

    public short get(long k) {
        int pos = find(k);

        return values[pos];
    }

    private int find(long k) {
        int pos = getInitialPos(k, keys);
        long curr = keys[pos];
        while (curr != EMPTY_KEY && curr != k) {
            pos++;
            if (pos == keys.length) pos = 0;
            curr = keys[pos];
        }
        return pos;
    }

    public void increment(long k, short c) {
        int pos = find(k);
        long currKey = keys[pos];
        if (currKey == EMPTY_KEY) {
            put(k, c);
        } else
            values[pos]++;
    }

    public static class Entry
    {
        /**
         * @param key
         * @param value
         */
        public Entry(long key, short value) {
            super();
            this.key = key;
            this.value = value;
        }

        public long key;

        public short value;

        public long getKey() {
            return key;
        }

        public short getValue() {
            return value;
        }
    }

    private class EntryIterator extends MapIterator<Entry>
    {
        public Entry next() {
            final int nextIndex = nextIndex();

            return new Entry(keys[nextIndex], values[nextIndex]);
        }
    }

    private abstract class MapIterator<E> implements Iterator<E>
    {
        public MapIterator() {
            end = keys.length;
            next = -1;
            nextIndex();
        }

        public boolean hasNext() {
            return next < end;
        }

        int nextIndex() {
            int curr = next;
            do {
                next++;
            } while (next < end && keys[next] == EMPTY_KEY);
            return curr;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int next, end;
    }

    public Iterable<Entry> entrySet() {
        return CollectionUtils.iterable(new EntryIterator());
    }

    public int size() {
        return size;
    }

    public int actualSize() {
        return keys.length;
    }

    /**
     * Automatic Optimization method to free up unused entries in this map
     *
     */
    public void autoOptimizeStorage(){
        double utilization = size / (double) actualSize();
        rehash(utilization + 0.15);
    }
}

