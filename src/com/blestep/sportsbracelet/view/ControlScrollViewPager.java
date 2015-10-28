package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 可控制滑动的viewpager
 * 
 * @author wenzheng.liu
 * 
 */

public class ControlScrollViewPager extends ViewPager {

	private boolean scrollable = true;

	public ControlScrollViewPager(Context context) {
		super(context);
	}

	public ControlScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScrollable(boolean enable) {
		scrollable = enable;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (scrollable) {
			return super.onInterceptTouchEvent(event);
		} else {
			return false;
		}
	}
}