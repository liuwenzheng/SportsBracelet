package com.blestep.sportsbracelet.utils;

import java.util.Random;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.db.chart.Tools;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.bounce.BounceEaseOut;
import com.db.chart.view.animation.easing.elastic.ElasticEaseOut;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

public class DataRetriever {

	private final static String[] mColors = { "#b4efff", "#00bba7", "#e06c5d",
			"#35babf", "#ffb74d" };

	public static boolean randBoolean() {
		return Math.random() < 0.5;
	}

	public static int randNumber(int min, int max) {
		return new Random().nextInt((max - min) + 1) + min;
	}

	public static float randValue(float min, float max) {
		return (new Random().nextFloat() * (max - min)) + min;
	}

	public static float randDimen(float min, float max) {
		float ya = (new Random().nextFloat() * (max - min)) + min;
		return Tools.fromDpToPx(ya);
	}

	public static YController.LabelPosition getYPosition() {
		switch (new Random().nextInt(2)) {
		case 0:
			return YController.LabelPosition.NONE;
		case 1:
			return YController.LabelPosition.INSIDE;
		default:
			return YController.LabelPosition.OUTSIDE;
		}
	}

	public static XController.LabelPosition getXPosition() {
		switch (new Random().nextInt(1)) {
		case 0:
			return XController.LabelPosition.OUTSIDE;
		case 1:
			return XController.LabelPosition.INSIDE;
		default:
			return XController.LabelPosition.NONE;
		}
	}

	public static Paint randPaint() {

		Paint paint = new Paint();
		paint.setColor(Color.parseColor("#4dc1c6"));
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(Tools.fromDpToPx(1));
		// if (randBoolean())
		// paint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 0));

		return paint;

	}

	public static boolean hasFill(int index) {
		return (index == 2) ? true : false;
	}

	public static Animation randAnimation(Runnable endAction, int size) {

		int[] order = new int[size];
		for (int i = 0; i < size; i++)
			order[i] = i;
		shuffleArray(order);
		return new Animation().setEasing(new QuintEaseOut()).setOverlap(1.0f)
				.setAlpha(6).setEndAction(endAction);
	}

	// Implementing Fisher�Yates shuffle
	private static void shuffleArray(int[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public static String getColor(int index) {

		switch (0) {
		case 0:
			return mColors[0];
		case 1:
			return mColors[1];
		case 2:
			return mColors[2];
		case 3:
			return mColors[0];
		case 4:
			return mColors[1];
		default:
			return mColors[2];
		}
	}
}
