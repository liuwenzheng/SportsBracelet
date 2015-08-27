package com.blestep.sportsbracelet.utils;

import android.content.Context;

public class Utils {

	/**
	 * 根据手机分辨率把dp转换成px(像素)
	 * 
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机分辨率把px转换成dp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0 || s.trim().equals("") || s.trim().equals("null");
	}

	public static boolean isNotEmpty(String s) {
		return s != null && s.length() != 0 && !s.trim().equals("") && !s.trim().equals("null");
	}
}
