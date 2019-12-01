package com.program.healthhallym.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class SharedPreferencesUtils {
    private volatile static SharedPreferencesUtils _instance = null;

    private final SharedPreferences cache;

    public static SharedPreferencesUtils getInstance(Context context, String cacheName) {
        if (_instance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (_instance == null) {
                    _instance = new SharedPreferencesUtils(context, cacheName);
                }
            }
        }

        return _instance;
    }

    private SharedPreferencesUtils(Context context, String cacheName) {
        Context applicationContext = context.getApplicationContext();
        context = applicationContext != null ? applicationContext : context;
        this.cache = context.getSharedPreferences(cacheName, Context.MODE_PRIVATE);
    }

    public void clearAll() {
        this.cache.edit().clear().commit();
    }

    public void clear(final List<String> keysToClear) {
        SharedPreferences.Editor editor = this.cache.edit();
        for(String key : keysToClear){
            editor.remove(key);
        }
        editor.commit();
    }

    /* String */
    public void put(String key, String value) {
        SharedPreferences.Editor editor = this.cache.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /* String */
    public String get(String key) {
        return this.cache.getString(key, null);
    }

    /* boolean */
    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = this.cache.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /* boolean */
    public boolean get(String key, boolean def) {
        return this.cache.getBoolean(key, def);
    }

    /* int */
    public void put(String key, int value) {
        SharedPreferences.Editor editor = this.cache.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /* int */
    public int get(String key, int def) {
        return this.cache.getInt(key, def);
    }

    /* long */
    public void put(String key, long value) {
        SharedPreferences.Editor editor = this.cache.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /* long */
    public long get(String key, long def) {
        return this.cache.getLong(key, def);
    }

    /* double */
    public void put(String key, double value) {
        SharedPreferences.Editor editor = this.cache.edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.commit();
    }

    /* double */
    public double get(String key, double def) {
        return Double.longBitsToDouble(this.cache.getLong(key, Double.doubleToLongBits(def)));
    }
}
