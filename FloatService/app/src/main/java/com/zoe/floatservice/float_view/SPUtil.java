package com.zoe.floatservice.float_view;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author yupeng
 * sharedPref工具及判断是否是当天内
 */
public class SPUtil {

	private static SharedPreferences sp = null;
	private static SharedPreferences getSP(Context ctx){
		if(sp == null){
			sp = ctx.getSharedPreferences("zoe_sp",
					Context.MODE_PRIVATE);
		}
		return sp;
	}

	public static void put(Context ctx, String key, boolean value) {
		getSP(ctx).edit().putBoolean(key, value).commit();
	}

	public static void put(Context ctx, String key, String value) {
		getSP(ctx).edit().putString(key, value).commit();
	}

	public static void put(Context ctx, String key, int value) {
		getSP(ctx).edit().putInt(key, value).commit();
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
