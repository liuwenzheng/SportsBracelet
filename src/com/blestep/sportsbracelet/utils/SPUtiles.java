package com.blestep.sportsbracelet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtiles {
	public static final String SP_NAME = "sp_name_sportsbracelet";
	public static SharedPreferences sp;
	public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
	public static final String SP_KEY_BATTERY = "sp_key_battery";
	public static final String SP_KEY_STEP_AIM = "sp_key_aim";
	public static final String SP_KEY_USER_NAME = "sp_key_name";
	public static final String SP_KEY_USER_GENDER = "sp_key_gender";
	public static final String SP_KEY_USER_AGE = "sp_key_age";
	public static final String SP_KEY_USER_BIRTHDAT = "sp_key_birthday";
	public static final String SP_KEY_USER_HEIGHT = "sp_key_height";
	public static final String SP_KEY_USER_WEIGHT = "sp_key_weight";
	public static final String SP_KEY_IS_FIRST_OPEN = "sp_key_is_first_open";

	public static SharedPreferences getInstance(Context context) {
		sp = context.getSharedPreferences(SP_NAME, context.MODE_PRIVATE);
		return sp;
	}

	public static void setStringValue(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getStringValue(String key, String defValue) {
		return sp.getString(key, defValue);
	}

	public static void setBooleanValue(String key, boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static boolean getBooleanValue(String key, boolean defValue) {
		return sp.getBoolean(key, defValue);
	}

	public static void setIntValue(String key, int value) {
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static int getIntValue(String key, int defValue) {
		return sp.getInt(key, defValue);
	}

	public static void setFloatValue(String key, float value) {
		Editor editor = sp.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public static float getFloatValue(String key, float defValue) {
		return sp.getFloat(key, defValue);
	}
}
