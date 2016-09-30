package com.zoe.floatservice;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {

    private static final String SHARE_PREFS_NAME = "leo";
    private static SharedPreferences sp = null;

    private static SharedPreferences getSP(Context ctx){
        if(sp == null){
            sp = ctx.getSharedPreferences(SHARE_PREFS_NAME,
                    Context.MODE_PRIVATE);
        }
        return sp;
    }

    public static void put(Context ctx, String key, boolean value) {
        getSP(ctx).edit().putBoolean(key, value).apply();
    }

    public static void put(Context ctx, String key, String value) {
        getSP(ctx).edit().putString(key, value).apply();
    }

    public static void put(Context ctx, String key, int value) {
        getSP(ctx).edit().putInt(key, value).apply();
    }

    public static boolean get(Context ctx, String key, boolean defaultValue) {
        return getSP(ctx).getBoolean(key, defaultValue);
    }

    public static String get(Context ctx, String key, String defaultValue) {
        return getSP(ctx).getString(key, defaultValue);
    }

    public static int get(Context ctx, String key, int defaultValue) {
        return getSP(ctx).getInt(key, defaultValue);
    }

}