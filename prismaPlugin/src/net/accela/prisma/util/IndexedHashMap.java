package net.accela.prisma.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IndexedHashMap<K, V> {
    final HashMap<K, V> hashMap = new HashMap<>();
    final ArrayList<K> arrayList = new ArrayList<>();

    final Object lock = new Object();

    public ArrayList<K> getArrayList() {
        return arrayList;
    }

    public HashMap<K, V> getHashMap() {
        return hashMap;
    }

    public void put(@NotNull K key, @NotNull V value, int index) {
        synchronized (lock) {
            hashMap.put(key, value);
            arrayList.remove(key);
            arrayList.add(index, key);
        }
    }

    public void put(@NotNull K key, @NotNull V value) {
        synchronized (lock) {
            hashMap.put(key, value);
            arrayList.remove(key);
            arrayList.add(key);
        }
    }

    public V remove(@NotNull K key) {
        synchronized (lock) {
            arrayList.remove(key);
            return hashMap.remove(key);
        }
    }

    public V getValue(@NotNull K key) {
        return hashMap.get(key);
    }

    public V getValue(int index) {
        return hashMap.get(arrayList.get(index));
    }

    public K getKey(int index) {
        if (arrayList.size() != 0) return arrayList.get(index);
        return null;
    }

    public Collection<K> getKeys() {
        return hashMap.keySet();
    }

    public Collection<V> getValues() {
        return hashMap.values();
    }

    public int size() {
        return arrayList.size();
    }

    public boolean isEmpty() {
        return arrayList.isEmpty();
    }

    public boolean containsKey(K key) {
        return hashMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return hashMap.containsValue(value);
    }

    public void clear() {
        synchronized (lock) {
            hashMap.clear();
            arrayList.clear();
        }
    }

    @NotNull
    public Set<K> keySet() {
        return hashMap.keySet();
    }

    @NotNull
    public Collection<V> values() {
        return hashMap.values();
    }

    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return hashMap.entrySet();
    }

    public int indexOf(K key) {
        return arrayList.indexOf(key);
    }
}
