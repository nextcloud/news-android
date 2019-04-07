package de.luhmer.owncloudnewsreader;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;


/**
 * Mock implementation of shared preference, which just saves data in memory using map.
 */
public class MockSharedPreference implements SharedPreferences {

    private final HashMap<String, Object> preferenceMap;
    private final MockSharedPreferenceEditor preferenceEditor;

    public MockSharedPreference() {
        preferenceMap = new HashMap<>();
        preferenceEditor = new MockSharedPreferenceEditor(preferenceMap);
    }

    @Override
    public Map<String, ?> getAll() {
        return preferenceMap;
    }

    @Nullable
    @Override
    public String getString(final String s, @Nullable final String s1) {
        Object v = preferenceMap.get(s);
        return v != null ? (String) v : s1;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(final String s, @Nullable final Set<String> set) {
        Object v = preferenceMap.get(s);
        return v != null ? (Set<String>) v : set;
    }

    @Override
    public int getInt(final String s, final int i) {
        Object v = preferenceMap.get(s);
        return v != null ? (int) v : i;
    }

    @Override
    public long getLong(final String s, final long l) {
        Object v = preferenceMap.get(s);
        return v != null ? (long) v : l;
    }

    @Override
    public float getFloat(final String s, final float f) {
        Object v = preferenceMap.get(s);
        return v != null ? (float) v : f;
    }

    @Override
    public boolean getBoolean(final String s, final boolean b) {
        Object v = preferenceMap.get(s);
        return v != null ? (boolean) v : b;
    }

    @Override
    public boolean contains(final String s) {
        return preferenceMap.containsKey(s);
    }

    @Override
    public Editor edit() {
        return preferenceEditor;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException();
    }

    public static class MockSharedPreferenceEditor implements Editor {

        private final HashMap<String, Object> preferenceMap;

        public MockSharedPreferenceEditor(final HashMap<String, Object> preferenceMap) {
            this.preferenceMap = preferenceMap;
        }

        @Override
        public Editor putString(final String s, @Nullable final String s1) {
            preferenceMap.put(s, s1);
            return this;
        }

        @Override
        public Editor putStringSet(final String s, @Nullable final Set<String> set) {
            preferenceMap.put(s, set);
            return this;
        }

        @Override
        public Editor putInt(final String s, final int i) {
            preferenceMap.put(s, i);
            return this;
        }

        @Override
        public Editor putLong(final String s, final long l) {
            preferenceMap.put(s, l);
            return this;
        }

        @Override
        public Editor putFloat(final String s, final float v) {
            preferenceMap.put(s, v);
            return this;
        }

        @Override
        public Editor putBoolean(final String s, final boolean b) {
            preferenceMap.put(s, b);
            return this;
        }

        @Override
        public Editor remove(final String s) {
            preferenceMap.remove(s);
            return this;
        }

        @Override
        public Editor clear() {
            preferenceMap.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public void apply() {
            // Nothing to do, everything is saved in memory.
        }
    }

}
