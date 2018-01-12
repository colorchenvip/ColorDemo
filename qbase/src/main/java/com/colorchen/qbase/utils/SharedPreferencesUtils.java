package com.colorchen.qbase.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by wangsye on 2017-6-17.
 */

public class SharedPreferencesUtils {
    private static final String TAG = SharedPreferencesUtils.class.getSimpleName();
    private SharedPreferences pref;

    public SharedPreferencesUtils(Context context, String fileName) {
        this.pref = context.getSharedPreferences(
                fileName, Context.MODE_PRIVATE);
    }

    public void remove(String key) {
        pref.edit().remove(key).commit();
    }

    public void clear() {
        pref.edit().clear().commit();
    }

    public void setString(String key, String value) {
        pref.edit().putString(key, value).commit();
    }

    public String getString(String key) {
        String value = null;
        try {
            value = pref.getString(key, null);
        } catch (ClassCastException e) {
            Log.e(TAG, "Bad string value found for " + key, e);
        }
        return value;
    }

    public void setLong(String key, long value) {
        pref.edit().putLong(key, value).commit();
    }

    public void setFloat(String key, float value) {
        pref.edit().putFloat(key, value).commit();
    }

    public float getFloating(String key) {
        float value = 0;
        try {
            value = pref.getFloat(key, 0);
        } catch (ClassCastException e) {
            Log.e(TAG, "Bad float value found for " + key, e);
        }
        return value;
    }

    public long getLong(String key, long defaultValue) {
        long value = defaultValue;
        try {
            value = pref.getLong(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(TAG, "Bad Long value found for " + key, e);
        }
        return value;
    }

    public void setBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key) {
        boolean value = false;
        try {
            value = pref.getBoolean(key, false);
        } catch (ClassCastException e) {
            Log.e(TAG, "Bad boolean value found for " + key, e);
        }
        return value;
    }


}
