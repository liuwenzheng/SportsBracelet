/*
 * Copyright (C) 2015 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.blestep.sportsbracelet.R;

public class TimelineChartView extends View {

    private static final String TAG = "TimelineChartView";

    /**
     * A class that represents a item information.
     */
    public static class Item {
        private Item() {
        }

        public String label;
        public double stepCount;
        public double stepDuration;
        public double stepDistance;
        public double stepCalorie;
    }

    /**
     * An interface definition to notify item selection event.
     */
    public interface OnSelectedItemChangedListener {
        /**
         * Called when a item was selected.
         *
         * @param selectedItem information about the selected item
         */
        void onSelectedItemChanged(Item selectedItem);

        /**
         * Called when there is no selection.
         */
        // void onNothingSelected();
    }


    private Cursor mCursor;
    private SparseArray<Object[]> mData = new SparseArray<>();
    private double mMaxValue;
    private double mTargetValue;
    private final Item mItem = new Item();

    private final RectF mViewArea = new RectF();
    private final RectF mGraphArea = new RectF();
    private final RectF mFooterArea = new RectF();
    private float mFooterBarHeight;

    private float mBarItemWidth;
    private float mBarItemSpace;
    private float mBarWidth;
    private float mTopSpaceHeight;
    private Shader mViewAreaBgShader;

    private Paint mViewAreaBgPaint;
    private Paint mGraphAreaBgPaint;
    private Paint mFooterAreaBgPaint;
    private Paint mGraphBottomLinePaint;


    private boolean mIsShowTargetDashedLine;
    private Paint mGraphTargetDashedLinePaint;

    private Paint mBarItemBgPaint;
    private Paint mHighlightBarItemBgPaint;
    private TextPaint mLabelFgPaint;
    private TextPaint mHighlightLabelFgPaint;


    private int mCurrentPosition = -1;
    private long mLastPosition = -1;
    private float mCurrentOffset = 0.f;
    private float mLastOffset = -1.f;
    private float mMaxOffset = 0.f;
    private float mInitialTouchOffset = 0.f;
    private float mInitialTouchX = 0.f;
    private float mInitialTouchY = 0.f;

    private int mMaxBarItemsInScreen = 0;
    private final int[] mItemsOnScreen = new int[2];
    /**
     * 计算标签文字的高度，用于让footer里的文字居中
     */
    private float mTickLabelMinHeight;

    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;
    /**
     * 开始滑动的距离
     */
    private float mTouchSlop;
    /**
     * 最大速度
     */
    private float mMaxFlingVelocity;

    private static final int STATE_IDLE = 0;
    private static final int STATE_INITIALIZE = 1;
    private static final int STATE_MOVING = 2;
    private static final int STATE_FLINGING = 3;
    private static final int STATE_SCROLLING = 4;
    private int mState = STATE_IDLE;

    private static final int MSG_ON_SELECTION_ITEM_CHANGED = 1;
    private static final int MSG_COMPUTE_DATA = 4;
    private static final int MSG_UPDATE_COMPUTED_DATA = 5;

    private Handler mUiHandler;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundHandlerThread;


    private final Handler.Callback mMessenger = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                // Ui thread
                case MSG_ON_SELECTION_ITEM_CHANGED:
                    notifyOnSelectionItemChanged();
                    return true;
                case MSG_UPDATE_COMPUTED_DATA:

                    // Redraw the data and notify the changes
                    notifyOnSelectionItemChanged();

                    // Update the graph view
                    ViewCompat.postInvalidateOnAnimation(TimelineChartView.this);
                    return true;

                // Non-Ui thread
                case MSG_COMPUTE_DATA:
                    performComputeData();
                    return true;
            }
            return false;
        }
    };

    private OnSelectedItemChangedListener mOnSelectedItemChangedCallback;

    private final Object mLock = new Object();


    public TimelineChartView(Context ctx) {
        this(ctx, null, 0);
    }

    public TimelineChartView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }

    public TimelineChartView(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx, attrs, defStyleAttr);
    }

    private void init(Context ctx, AttributeSet attrs, int defStyleAttr) {
        mUiHandler = new Handler(Looper.getMainLooper(), mMessenger);

        final Resources res = getResources();

        final ViewConfiguration vc = ViewConfiguration.get(ctx);
        mTouchSlop = vc.getScaledTouchSlop() / 2;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mScroller = new OverScroller(ctx);

        int footerLabelColor = ContextCompat.getColor(getContext(), R.color.tlcDefFooterLabelColor);
        int barItemBg = ContextCompat.getColor(getContext(), R.color.tlcDefBarItemBg);
        int highlightBarItemBg = ContextCompat.getColor(getContext(), R.color.tlcDefHighlightBarItemBg);

        mBarItemBgPaint = new Paint();
        mBarItemBgPaint.setColor(barItemBg);
        mHighlightBarItemBgPaint = new Paint();
        mHighlightBarItemBgPaint.setColor(highlightBarItemBg);

        mFooterBarHeight = res.getDimension(R.dimen.tlcDefFooterBarHeight);

        mViewAreaBgPaint = new Paint();

        mGraphAreaBgPaint = new Paint();
        mGraphAreaBgPaint.setColor(Color.TRANSPARENT);

        mFooterAreaBgPaint = new Paint();
        mFooterAreaBgPaint.setColor(Color.TRANSPARENT);

        mGraphBottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphBottomLinePaint.setColor(Color.WHITE);
        mGraphBottomLinePaint.setStrokeWidth(1);
        // 目标虚线
        mGraphTargetDashedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphTargetDashedLinePaint.setStyle(Paint.Style.STROKE);
        mGraphTargetDashedLinePaint.setColor(Color.WHITE);
        mGraphTargetDashedLinePaint.setStrokeWidth(1);
        PathEffect pathEffect = new DashPathEffect(new float[]{9, 3}, 1);
        mGraphTargetDashedLinePaint.setPathEffect(pathEffect);


        // labelPaint抗锯齿，不需要文本缓存
        mLabelFgPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mLabelFgPaint.setColor(footerLabelColor);
//        DisplayMetrics dp = getResources().getDisplayMetrics();
//        float labelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, res.getDimension(R.dimen.tlcDefFooterLabelSize), dp);
        mLabelFgPaint.setTextSize(res.getDimension(R.dimen.tlcDefFooterLabelSize));

        mHighlightLabelFgPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mHighlightLabelFgPaint.setColor(Color.WHITE);
        mHighlightLabelFgPaint.setTextSize(res.getDimension(R.dimen.tlcDefFooterLabelSize));


        mBarItemWidth = res.getDimension(R.dimen.tlcDefBarItemWidth);
        mBarItemSpace = res.getDimension(R.dimen.tlcDefBarItemSpace);
        mTopSpaceHeight = res.getDimension(R.dimen.tlcDefTopSpace);

        // SurfaceView requires a background
        if (getBackground() == null) {
            setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        }

        // Initialize stuff
        setupBackgroundHandler();
        setupTickLabels();

        // Initialize the drawing refs (this will be update when we have
        // the real size of the canvas)
        computeBoundAreas();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupBackgroundHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Destroy background thread
        mBackgroundHandlerThread.quit();
        mBackgroundHandler = null;
        mBackgroundHandlerThread = null;

        // Destroy cursor
        releaseCursor();

        // Destroy internal tracking variables
        clear();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Returns the height in pixels of the footer area.
     */
//    public float getFooterBarHeight() {
//        return mFooterBarHeight;
//    }

    /**
     * Sets the height in pixels of the footer area.
     */
//    public void setFooterHeight(float height) {
//        if (mFooterBarHeight != height) {
//            mFooterBarHeight = height;
//            computeBoundAreas();
//            setupTickLabels();
//            requestLayout();
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
//    }

    /**
     * Returns the space in pixels between bar items.
     */
    public float getBarItemSpace() {
        return mBarItemSpace;
    }

    /**
     * Sets the space in pixels between bar items.
     */
    public void setBarItemSpace(float barItemSpace) {
        if (mBarItemSpace != barItemSpace) {
            mBarItemSpace = barItemSpace;
            computeMaxBarItemsInScreen();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Returns the width in pixels of a bar item.
     */
    public float getBarItemWidth() {
        return mBarItemWidth;
    }

    /**
     * Sets the width in pixels of a bar item.
     */
    public void setBarItemWidth(float barItemWidth) {
        if (mBarItemWidth != barItemWidth) {
            mBarItemWidth = barItemWidth;
            computeBoundAreas();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    /**
     * 设置是否显示目标值
     *
     * @param mIsShowTargetDashedLine
     */
    public void setIsShowTargetDashedLine(boolean mIsShowTargetDashedLine) {
        this.mIsShowTargetDashedLine = mIsShowTargetDashedLine;
    }


    /**
     * 设置目标值
     *
     * @param mTargetValue
     */
    public void setTargetValue(double mTargetValue) {
        this.mTargetValue = mTargetValue;
    }


    public void addOnSelectedItemChangedListener(OnSelectedItemChangedListener cb) {
        mOnSelectedItemChangedCallback = cb;
    }

    /**
     * Registers the cursor and start observing changes on it.
     * <p/>
     * The cursor <i>MUST</i> follow the next constrains:
     * <ul>
     * <li>The first field must contains a timestamp, which represent
     * a time in the graph timeline. This value will be the key to access to
     * the graph information.</li>
     * <li>One or more float/double numeric in the rest of the fields of
     * the cursor. Every one of this fields will represent a serie in the
     * graph.</li>
     * </ul>
     *
     * @param c the cursor to observe.
     */
    public void observeData(Cursor c) {
        releaseCursor();

        // Ensure we have a valid handler (if for some reason view wasn't attached yet)
        setupBackgroundHandler();

        // Save the cursor reference and listen for changes
        mCursor = c;
        reloadCursorData();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final float x = mScroller.getCurrX();
        return (direction < 0 && x < mMaxOffset) || (direction > 0 && x > 0);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final int action = event.getActionMasked();
        final int index = event.getActionIndex();
        final int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Initialize velocity tracker
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                mScroller.forceFinished(true);
                mState = STATE_INITIALIZE;

                mInitialTouchOffset = mCurrentOffset;
                mInitialTouchX = event.getX();
                mInitialTouchY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                float diffX = event.getX() - mInitialTouchX;
                float diffY = event.getY() - mInitialTouchY;
                if (Math.abs(diffX) > mTouchSlop || mState >= STATE_MOVING) {
                    mCurrentOffset = mInitialTouchOffset + diffX;
                    if (mCurrentOffset < 0) {
                        onOverScroll();
                        mCurrentOffset = 0;
                    } else if (mCurrentOffset > mMaxOffset) {
                        onOverScroll();
                        mCurrentOffset = mMaxOffset;
                    }
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    mState = STATE_MOVING;
                    ViewCompat.postInvalidateOnAnimation(this);
                } else if (Math.abs(diffY) > mTouchSlop && mState < STATE_MOVING) {
                    return false;
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mState >= STATE_MOVING) {
                    final int velocity = (int) VelocityTrackerCompat.getXVelocity(
                            mVelocityTracker, pointerId);
                    mScroller.forceFinished(true);
                    mState = STATE_FLINGING;
                    mScroller.fling((int) mCurrentOffset, 0, velocity, 0, 0, (int) mMaxOffset, 0, 0);
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    // Reset scrolling state
                    mState = STATE_IDLE;
                }
                return true;
        }
        return false;
    }

    private void onOverScroll() {
        final boolean needOverScroll;
        synchronized (mLock) {
            needOverScroll = mData.size() >= Math.floor(mMaxBarItemsInScreen / 2);
        }
        final int overScrollMode = ViewCompat.getOverScrollMode(this);
        if (overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                (overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && needOverScroll)) {
            boolean needsInvalidate = false;
            if (mCurrentOffset > mMaxOffset) {
                needsInvalidate = true;
            }
            if (mCurrentOffset < 0) {
                needsInvalidate = true;
            }

            if (needsInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void computeScroll() {
        super.computeScroll();

        // Determine whether we still scrolling and needs a viewport refresh
        final boolean scrolling = mScroller.computeScrollOffset();
        if (scrolling) {
            float x = mScroller.getCurrX();
            if (x > mMaxOffset || x < 0) {
                return;
            }
            mCurrentOffset = x;
            ViewCompat.postInvalidateOnAnimation(this);
        } else if (mState > STATE_MOVING) {
            boolean needsInvalidate = false;
            final boolean needOverScroll;
            synchronized (mLock) {
                needOverScroll = mData.size() >= Math.floor(mMaxBarItemsInScreen / 2);
            }
            final int overScrollMode = ViewCompat.getOverScrollMode(this);
            if (overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS || (needOverScroll &&
                    overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS)) {
                float x = mScroller.getCurrX();
                if (x >= mMaxOffset) {
                    needsInvalidate = true;
                }
                if (x < 0) {
                    needsInvalidate = true;
                }
            }
            if (!needsInvalidate) {
                // Reset state
                mState = STATE_IDLE;
                mLastPosition = -1;
            } else {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }


        int position = computeNearestPositionFromOffset(mCurrentOffset);

        // If we are not centered in a item, perform an scroll
        if (mState == STATE_IDLE) {
            smoothScrollTo(position);
        }

        if (mCurrentPosition != position) {
            // Don't perform selection operations while we are just scrolling
            if (mState != STATE_SCROLLING) {
                mCurrentPosition = position;

                // Notify any valid item, but only notify invalid items if
                // we are not panning/scrolling
                if (mCurrentPosition >= 0 || !scrolling) {
                    Message.obtain(mUiHandler, MSG_ON_SELECTION_ITEM_CHANGED).sendToTarget();
                }
            }
        }
    }

    /**
     * Performs a smooth transition of the current viewport of this view to
     * the timestamp passed as argument. If timestamp doesn't exists no
     * operation will be performed.
     */
    public void smoothScrollTo(int position) {

        final float offset = computeOffsetForPosition(position);
        if (offset >= 0 && offset != mCurrentOffset) {
            int dx = (int) (mCurrentOffset - offset) * -1;
            mScroller.forceFinished(true);
            mState = STATE_SCROLLING;
            mLastPosition = mCurrentPosition;
            mScroller.startScroll((int) mCurrentOffset, 0, dx, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private float computeOffsetForPosition(int position) {
        final SparseArray<Object[]> data;
        synchronized (mLock) {
            data = mData;
        }
        final int index = data.indexOfKey(position);
        if (index >= 0) {
            final int size = data.size();
            return (mBarWidth * (size - index - 1));
        }
        return -1;
    }

    private int computeNearestPositionFromOffset(float offset) {
        final SparseArray<Object[]> data;
        synchronized (mLock) {
            data = mData;
        }
        int size = data.size() - 1;
        if (size < 0) {
            return -1;
        }

        // So we are in an bar area, so we have a valid index
        final int index = size - ((int) Math.ceil((offset - (mBarItemWidth / 2) - mBarItemSpace) / mBarWidth));
        return data.keyAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas c) {
        // 设置范围
        c.clipRect(mViewArea);
        mViewAreaBgShader = new LinearGradient(0, 0, 0, mViewArea.height(),
                ContextCompat.getColor(getContext(), R.color.tlcDefGraphBgColorStart),
                ContextCompat.getColor(getContext(), R.color.tlcDefGraphBgColorEnd),
                Shader.TileMode.CLAMP);
        mViewAreaBgPaint.setShader(mViewAreaBgShader);
        // 绘制区域
        c.drawRect(mViewArea, mViewAreaBgPaint);
        c.drawRect(mGraphArea, mGraphAreaBgPaint);
        c.drawRect(mFooterArea, mFooterAreaBgPaint);
        // 绘制分割线
        c.drawLine(0, mGraphArea.height(), mGraphArea.width(), mGraphArea.height(), mGraphBottomLinePaint);


        final SparseArray<Object[]> data;
        final double maxValue;
        synchronized (mLock) {
            data = mData;
            if (mIsShowTargetDashedLine && mTargetValue > mMaxValue) {
                maxValue = mTargetValue;
            } else {
                maxValue = mMaxValue;
            }
        }
        boolean hasData = data.size() > 0;
        if (hasData) {


            // 计算屏幕内显示的起始结束位置
            computeItemsOnScreen(data.size());
            // 绘制目标虚线
            drawTargetLine(c);
            // 绘制柱状图
            drawBarItems(c, data, maxValue);
            // 绘制底部标签
            drawTickLabels(c, data);

        }
    }

    private void drawTargetLine(Canvas c) {
        if (mIsShowTargetDashedLine && mTargetValue > 0) {
            if (mTargetValue > mMaxValue) {
                Path path = new Path();
                path.moveTo(0, mTopSpaceHeight);
                path.lineTo(mGraphArea.width(), mTopSpaceHeight);
                c.drawPath(path, mGraphTargetDashedLinePaint);
            } else {
                float height = mGraphArea.height();
                float y = (float) (height - (height * ((mTargetValue * 100) / mMaxValue)) / 100) + mTopSpaceHeight;
                Path path = new Path();
                path.moveTo(0, y);
                path.lineTo(mGraphArea.width(), y);
                c.drawPath(path, mGraphTargetDashedLinePaint);
            }
        }
    }

    private void drawBarItems(Canvas c, SparseArray<Object[]> data,
                              double maxValue) {

        final float halfItemBarWidth = mBarItemWidth / 2;
        final float height = mGraphArea.height();
        final Paint seriesBgPaint;
        final Paint highlightSeriesBgPaint;
        synchronized (mLock) {
            seriesBgPaint = mBarItemBgPaint;
            highlightSeriesBgPaint = mHighlightBarItemBgPaint;
        }

        // Apply zoom animation
        final float graphCenterX = mGraphArea.left + (mGraphArea.width() / 2);

        final int size = data.size() - 1;
        for (int i = mItemsOnScreen[1]; i >= mItemsOnScreen[0] && i <= data.size(); i--) {
            final float barCenterX = graphCenterX + mCurrentOffset - (mBarWidth * (size - i));
            // 记步数据
            double value = (double) data.valueAt(i)[1];
            float barTop = (float) (height - ((height * ((value * 100) / maxValue)) / 100)) + mTopSpaceHeight;
            float barBottom = height;
            float barLeft = barCenterX - halfItemBarWidth;
            float barRight = barCenterX + halfItemBarWidth;
            final Paint paint;
            // 判断是否高亮
            paint = barLeft < graphCenterX &&
                    barRight > graphCenterX &&
                    (mLastPosition == mCurrentPosition || (mState != STATE_SCROLLING))
                    ? highlightSeriesBgPaint : seriesBgPaint;
            // 画柱状图
            c.drawRect(barLeft, mGraphArea.top + barTop, barRight, mGraphArea.top + barBottom, paint);
        }
    }

    private void drawTickLabels(Canvas c, SparseArray<Object[]> data) {
        final int size = data.size() - 1;
        final float graphCenterX = mGraphArea.left + (mGraphArea.width() / 2);
        final float halfItemBarWidth = mBarItemWidth / 2;
        for (int i = mItemsOnScreen[1]; i >= mItemsOnScreen[0] && i <= data.size(); i--) {
            final float barCenterX = graphCenterX + mCurrentOffset - (mBarWidth * (size - i));
            float barLeft = barCenterX - halfItemBarWidth;
            float barRight = barCenterX + halfItemBarWidth;
            // Update the dynamic layout
            String label = (String) data.valueAt(i)[0];
            // 文字居中
            // Calculate the x position and draw the layout
            final float x = graphCenterX + mCurrentOffset - (mBarWidth * (size - i))
                    - (mLabelFgPaint.measureText(label) / 2);
            final int restoreCount = c.save();
            c.translate(x, mFooterArea.top + (mFooterArea.height() / 2 - mTickLabelMinHeight / 2));
            final Paint paint;
            // 判断是否高亮
            paint = barLeft < graphCenterX &&
                    barRight > graphCenterX &&
                    (mLastPosition == mCurrentPosition || (mState != STATE_SCROLLING))
                    ? mHighlightLabelFgPaint : mLabelFgPaint;
            c.drawText(label, 0, 0, paint);
            c.restoreToCount(restoreCount);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewArea.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        computeBoundAreas();
    }

    private void computeItemsOnScreen(int Datasize) {
        if (mLastOffset == mCurrentOffset) {
            return;
        }
        int size = Datasize - 1;
        float offset = mCurrentOffset + (mBarItemWidth / 2);
        int last = size - (int) Math.floor(offset / mBarWidth)
                + (int) Math.ceil(mMaxBarItemsInScreen / 2);
        int rest = 0;
        if (last > size) {
            rest = last - size;
            last = size;
        }
        int first = last - (mMaxBarItemsInScreen - 1) + rest;
        if (first < 0) {
            first = 0;
        }

        // Save the item positions
        mItemsOnScreen[0] = first;
        mItemsOnScreen[1] = last;
        mLastOffset = mCurrentOffset;
    }

    private void computeBoundAreas() {

        mGraphArea.set(mViewArea);
        mGraphArea.bottom = Math.max(mViewArea.bottom - mFooterBarHeight, 0);
        mFooterArea.set(mViewArea);
        mFooterArea.top = mGraphArea.bottom;
        mFooterArea.bottom = mGraphArea.bottom + mFooterBarHeight;


        // Compute max bar items here too
        computeMaxBarItemsInScreen();
    }

    private void computeMaxBarItemsInScreen() {
        mBarWidth = mBarItemWidth + mBarItemSpace;
        mMaxBarItemsInScreen = (int) Math.ceil(mGraphArea.width() / mBarWidth) + 2;
    }

    private synchronized void setupBackgroundHandler() {
        if (mBackgroundHandler == null) {
            // Create a background thread
            mBackgroundHandlerThread = new HandlerThread(TAG + "BackgroundThread");
            mBackgroundHandlerThread.start();
            mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper(), mMessenger);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupTickLabels() {
        synchronized (mLock) {
            mTickLabelMinHeight = mLabelFgPaint.descent() + mLabelFgPaint.ascent();
        }
    }

    private void reloadCursorData() {
        Message.obtain(mBackgroundHandler, MSG_COMPUTE_DATA).sendToTarget();
    }

    private void performComputeData() {
        clear();
        // Process the data
        processData();


        // Swap temporary refs
        mScroller.forceFinished(true);
        mState = STATE_IDLE;

        // Update the view and notify
        Message.obtain(mUiHandler, MSG_UPDATE_COMPUTED_DATA).sendToTarget();


        // Update the graph view
        ViewCompat.postInvalidateOnAnimation(TimelineChartView.this);
    }

    private void processData() {
        if (mCursor != null && !mCursor.isClosed() && mCursor.moveToFirst()) {
            // Load the cursor to memory
            double max = 0d;
            final SparseArray<Object[]> data = new SparseArray<>(mCursor.getCount());

            do {
                int position = mCursor.getInt(0);
                String label = mCursor.getString(1);
                double stepCount = mCursor.getDouble(2);
                double stepDuration = mCursor.getDouble(3);
                double stepDistance = mCursor.getDouble(4);
                double stepCalorie = mCursor.getDouble(5);
                if (stepCount > max) {
                    max = stepCount;
                }
                Object[] values = new Object[]{label, stepCount, stepDuration, stepDistance, stepCalorie};
                data.put(position, values);
            } while (mCursor.moveToNext());

            // Calculate the max available offset
            int size = data.size() - 1;
            float maxOffset = mBarWidth * size;

            //swap data
            synchronized (mLock) {
                mData = data;
                mMaxValue = max;
                mMaxOffset = maxOffset;
                mLastOffset = -1.f;
                mCurrentPosition = size;
                mCurrentOffset = computeOffsetForPosition(mCurrentPosition);
                setupTickLabels();
            }
        }
    }

    private void clear() {
        synchronized (mLock) {
            mData.clear();
            mMaxValue = 0d;
            mCurrentPosition = -1;
            mCurrentOffset = 0;
        }
    }

    private void notifyOnSelectionItemChanged() {
        if (mOnSelectedItemChangedCallback == null) {
            return;
        }
        final Item item = obtainItem(mCurrentPosition);
        if (item == null) {
            // mOnSelectedItemChangedCallback.onNothingSelected();
        } else {
            mOnSelectedItemChangedCallback.onSelectedItemChanged(item);
        }
    }

    private Item obtainItem(int position) {
        final Object[] data;
        if (position == -1)
            return null;
        synchronized (mLock) {
            data = mData.get(position);
        }
        if (data == null) {
            return null;
        }

        // Compute item. Restore original sort before notify
        mItem.label = (String) data[0];
        mItem.stepCount = (double) data[1];
        mItem.stepDuration = (double) data[2];
        mItem.stepDistance = (double) data[3];
        mItem.stepCalorie = (double) data[4];
        return mItem;
    }

    private void releaseCursor() {
        if (mCursor != null) {
            if (!mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = null;
            if (mItem != null) {
                mItem.label = "";
                mItem.stepCount = 0;
                mItem.stepDuration = 0;
                mItem.stepDistance = 0;
                mItem.stepCalorie = 0;
            }
        }
    }
}
