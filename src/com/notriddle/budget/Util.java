package com.notriddle.budget;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class Util {
    public static Bundle packSparseIntArray(SparseIntArray a) {
        Bundle b = new Bundle();
        for (int i = 0; i != a.size(); ++i) {
            b.putInt(Integer.toString(a.keyAt(i)), a.valueAt(i));
        }
        return b;
    }
    public static SparseIntArray unpackSparseIntArray(Bundle b) {
        SparseIntArray a = new SparseIntArray();
        Set<String> k = b.keySet();
        Iterator<String> i = k.iterator();
        while (i.hasNext()) {
            String key = i.next();
            a.put(Integer.parseInt(key), b.getInt(key));
        }
        return a;
    }
    public static Bundle packSparseLongArray(SparseArray a) {
        Bundle b = new Bundle();
        for (int i = 0; i != a.size(); ++i) {
            b.putLong(Integer.toString(a.keyAt(i)), (Long) a.valueAt(i));
        }
        return b;
    }
    public static SparseArray unpackSparseLongArray(Bundle b) {
        SparseArray a = new SparseArray();
        Set<String> k = b.keySet();
        Iterator<String> i = k.iterator();
        while (i.hasNext()) {
            String key = i.next();
            a.put(Integer.parseInt(key), b.getLong(key));
        }
        return a;
    }
	static public int numberOf(SparseBooleanArray items, boolean value) {
				if (items == null) return 0;
		int retVal = 0;
		for (int i = 0; i != items.size(); ++i) {
			if (items.get(items.keyAt(i)) == value)
				retVal += 1;
		}
		return retVal;
	}
};
