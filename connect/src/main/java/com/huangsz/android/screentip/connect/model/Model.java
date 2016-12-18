package com.huangsz.android.screentip.connect.model;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.wearable.DataMap;

/**
 * Base model class, which contains information for rendering watch face, it is used for
 * the communication between the mobile and the wear.
 */
public abstract class Model {

    protected DataMap mDataMap;

    public Model(DataMap dataMap) {
        mDataMap = dataMap;
    }

    public Model() {
        this(new DataMap());
    }

    public DataMap getDataMap() {
        return mDataMap;
    }

    public boolean isEmpty() {
        return mDataMap.isEmpty();
    }

    public boolean containsKey(String key) {
        return mDataMap.containsKey(key);
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof Model && mDataMap != null) {
            return mDataMap.equals(((Model) another).mDataMap);
        }
        return super.equals(another);
    }

    public abstract void persistData(SharedPreferences.Editor prefEditor, String keyPrefix);

    public abstract void retrieveDataFromPersistence(
            SharedPreferences sharedPreferences, String keyPrefix);

    public Bundle toBundle() {
        return mDataMap.toBundle();
    }

    public void fromBundle(Bundle bundle) {
        mDataMap = mDataMap.fromBundle(bundle);
    }

    protected void persistIfNotNull(SharedPreferences.Editor prefEditor, String keyPrefix,
                                    String key, @Nullable Object value) {
        if (value == null) {
            return;
        }
        key = getFullPersistenceKey(keyPrefix, key);
        if (value instanceof String) {
            prefEditor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            prefEditor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            prefEditor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            prefEditor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            prefEditor.putLong(key, (Long) value);
        } else if (value instanceof Model) {
            prefEditor.putBoolean(key, true);
            ((Model) value).persistData(prefEditor, getFullPersistenceKeyPrefix(keyPrefix));
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid type when persisting %s, only primitive types can be" +
                            " persisted, %s is not!",
                            key,
                            value.getClass().getName()));
        }
    }

    protected void retrieveFromPersistenceIfContains(
            SharedPreferences sharedPreferences, String keyPrefix, String key, Class clazz) {
        String persistKey = getFullPersistenceKey(keyPrefix, key);
        if (!sharedPreferences.contains(persistKey)) {
            return;
        }
        if (clazz == String.class) {
            mDataMap.putString(key, sharedPreferences.getString(persistKey, null));
        } else if (clazz == Boolean.class) {
            mDataMap.putBoolean(key, sharedPreferences.getBoolean(persistKey, false));
        } else if (clazz == Integer.class) {
            mDataMap.putInt(key, sharedPreferences.getInt(persistKey, 0));
        } else if (clazz == Float.class) {
            mDataMap.putFloat(key, sharedPreferences.getFloat(persistKey, 0));
        } else if (clazz == Long.class) {
            mDataMap.putLong(key, sharedPreferences.getLong(persistKey, 0));
        } else if (Model.class.isAssignableFrom(clazz)) {
            try {
                Model model = (Model) clazz.newInstance();
                model.retrieveDataFromPersistence(
                        sharedPreferences, getFullPersistenceKeyPrefix(keyPrefix));
                mDataMap.putDataMap(key, model.getDataMap());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid type when retrieving %s from persistence, only" +
                            " primitive types can be persisted, %s is not!",
                            persistKey,
                            clazz.getName()));
        }
    }

    public String getFullPersistenceKeyPrefix(String keyPrefix) {
        return String.format("%s.%s", keyPrefix, getClass().getSimpleName());
    }

    private String getFullPersistenceKey(String keyPrefix, String key) {
        return String.format("%s#%s", getFullPersistenceKeyPrefix(keyPrefix), key);
    }
}
