package com.colorchen.qbase.utils;

import android.content.Context;

import com.orhanobut.hawk.Hawk;

/**
 * name：加密保存keyvalue 类
 * @author: ChenQ
 * @date: 2018-1-3
 */
public class HawkUtils {
    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        Hawk.init(context).build();
    }

    public static void setLong(String key, long value) {
        putValue(key, value);
    }

    public static void setString(String key, String value) {
        putValue(key, value);
    }

    public static void setBoolean(String key, boolean value) {
        putValue(key, value);
    }

    public static void setInt(String key, int value) {
        putValue(key, value);
    }

    public static void setDouble(String key, double value) {
        putValue(key, value);
    }

    public static void setObject(String key, Object value) {
        putValue(key, value);
    }

    public static boolean delete(String key) {
        return deleteValue(key);
    }

    public static void clear() {
        Hawk.deleteAll();
    }

    public static long getLong(String key) {
        return getValue(key, 0L);
    }

    public static long getLong(String key,long defaultTime) {
        return getValue(key, defaultTime);
    }

    public static String getString(String key) {
        return getValue(key, "");
    }

    public static double getDouble(String key) {
        return getValue(key, 0D);
    }

    public static boolean getBoolean(String key) {
        return getValue(key, false);
    }

    public static boolean getBoolean(String key, boolean def) {
        return getValue(key, def);
    }

    public static Object getObject(String key) {
        return getValue(key, null);
    }

    public static int getInt(String key) {
        return getValue(key, 0);
    }

    public static <T> void putValue(String key, T value) {
        String md5Key = EncryptUtil.encryptMD5ToString(key);
        Hawk.put(md5Key, value);
    }

    public static <T> T getValue(String key, T defaultValue) {
        String md5Key = EncryptUtil.encryptMD5ToString(key);
        return Hawk.get(md5Key, defaultValue);
    }

    public static boolean deleteValue(String key) {
        String md5Key = EncryptUtil.encryptMD5ToString(key);
        return Hawk.delete(md5Key);
    }

}
