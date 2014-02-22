package de.croggle.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

public class SparseArray<T> {
	private final MapEntry<Integer, T> finder;
	private final Comparator<Map.Entry<Integer, T>> comp;
	private final LinkedList<Map.Entry<Integer, T>> l;

	public SparseArray() {
		comp = new Comparator<Map.Entry<Integer, T>>() {
			@Override
			public int compare(Map.Entry<Integer, T> lhs,
					Map.Entry<Integer, T> rhs) {
				return lhs.getKey() - rhs.getKey();
			}
		};
		l = new LinkedList<Map.Entry<Integer, T>>();
		finder = new MapEntry<Integer, T>();
	}

	public T put(int key, T val) {
		int pos = find(key);
		if (pos < 0) {
			// insert
			pos = -pos - 1;
			l.add(pos, new MapEntry<Integer, T>(key, val));
			return null;
		} else {
			// replace
			Map.Entry<Integer, T> elm = l.get(pos);
			T previous = elm.getValue();
			elm.setValue(val);
			return previous;
		}
	}

	public T get(int key) {
		int pos = find(key);
		if (pos < 0) {
			return null;
		}
		return l.get(pos).getValue();
	}

	public int keyAt(int i) {
		return l.get(i).getKey();
	}

	public T valueAt(int i) {
		return l.get(i).getValue();
	}

	public int size() {
		return l.size();
	}

	private int find(int key) {
		finder.setKey(key);
		int result = Collections.binarySearch(l, finder, comp);
		return result;
	}
}
