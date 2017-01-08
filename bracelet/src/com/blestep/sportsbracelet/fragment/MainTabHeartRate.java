package com.blestep.sportsbracelet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HeartRateDailyActivity;
import com.blestep.sportsbracelet.activity.HeartRateExplainActivity;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.adapter.HeartRateAdapter;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.HeartRate;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;
import it.carlom.stikkyheader.core.StikkyHeaderListView;
import it.carlom.stikkyheader.core.animator.AnimatorBuilder;
import it.carlom.stikkyheader.core.animator.HeaderStikkyAnimator;

public class MainTabHeartRate extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainTabHeartRate";
    @Bind(R.id.lv_heart_rate)
    ListView lvHeartRate;
    @Bind(R.id.iv_heart_rate)
    ImageView ivHeartRate;
    @Bind(R.id.tv_heart_rate_present)
    TextView tvHeartRatePresent;
    @Bind(R.id.header)
    RelativeLayout header;

    private View mView;
    private MainActivity mainActivity;
    private ArrayList<HeartRate> mLists;
    private HeartRateAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogModule.i(TAG + "-->onActivityCreated");
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        LogModule.i(TAG + "-->onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogModule.i(TAG + "-->onPause");
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogModule.i(TAG + "-->onCreateView");
        mView = inflater.inflate(R.layout.main_tab_heart_rate, container, false);
        ButterKnife.bind(this, mView);
        initView();
        initDate();
        return mView;
    }

    private HeaderStikkyAnimator mHeaderStikkyAnimator = new HeaderStikkyAnimator() {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View image = getHeader().findViewById(R.id.iv_heart_rate);
            View tv_header = getHeader().findViewById(R.id.rl_header_rate);
            int top = tv_header.getTop();
            int offset = top - Utils.dip2px(mainActivity, 30);
            image.setPivotX(200);
            image.setPivotY(0);
            return AnimatorBuilder.create().applyRotationX(image, 70).applyTranslation(image, 0, 0).applyFade(image, 0).applyTranslation(tv_header, 0, -offset);
        }
    };

    private void initView() {
        int minHeight = getResources().getDimensionPixelSize(R.dimen.heart_rate_header_height_min);
        int height = getResources().getDimensionPixelSize(R.dimen.heart_rate_header_height);
        final int heightOffset = height - minHeight;
        StikkyHeaderListView SHListView = (StikkyHeaderListView) StikkyHeaderBuilder.stickTo(lvHeartRate)
                .setHeader(header)
                .allowTouchBehindHeader(true)
                .animator(mHeaderStikkyAnimator)
                .minHeightHeader(minHeight)
                .build();
        SHListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_IDLE:
                        final int distance = getScrollY();
                        lvHeartRate.post(new Runnable() {
                            @Override
                            public void run() {
                                if (distance > heightOffset * 0.75 && distance <= heightOffset) {
                                    lvHeartRate.smoothScrollBy(heightOffset - distance, 200);
                                } else if (distance <= heightOffset * 0.75 && distance > 0) {
                                    lvHeartRate.smoothScrollBy(-distance, 200);
                                }
                            }
                        });
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (getScrollY() > 0) {
                    mainActivity.setPullToRefreshViewEnable(false);
                } else {
                    mainActivity.setPullToRefreshViewEnable(true);
                }
            }
        });
    }

    public int getScrollY() {
        View c = lvHeartRate.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = lvHeartRate.getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    private void initDate() {
        mLists = new ArrayList<>();
        mAdapter = new HeartRateAdapter(getActivity(), mLists);
        lvHeartRate.setAdapter(mAdapter);
        lvHeartRate.setOnItemClickListener(this);
//        for (int i = 0; i < 4; i++) {
//            HeartRate rate = new HeartRate();
//            rate.time = "9月12日 16:33";
//            rate.value = i + "";
//            mLists.add(rate);
//        }
//        mAdapter.notifyDataSetChanged();
    }


    public void updateView() {
        mLists.clear();
        final ArrayList<HeartRate> heartRates = DBTools.getInstance(getActivity()).selectAllHeartRate();
        if (!heartRates.isEmpty()) {
            tvHeartRatePresent.setText(heartRates.get(0).value);
        }
        mLists.addAll(heartRates);
        mAdapter.notifyDataSetChanged();
        ViewTreeObserver vto = lvHeartRate.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lvHeartRate.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (lvHeartRate.getFooterViewsCount() == 0) {
                    int height = lvHeartRate.getHeight();
                    int minHeight = getResources().getDimensionPixelSize(R.dimen.heart_rate_header_height_min);
                    int itemHeight = getResources().getDimensionPixelSize(R.dimen.heart_rate_item_height);
                    int footHeight = height - minHeight - heartRates.size() * itemHeight;
                    if (footHeight > 0) {
                        View v = new View(getActivity());
                        v.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, footHeight));
                        lvHeartRate.addFooterView(v);
                    }
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogModule.i(TAG + "-->onDestroyView");
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.tv_heart_rate_explain)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_heart_rate_explain:
                // 跳转说明
                startActivityForResult(new Intent(mainActivity, HeartRateExplainActivity.class), BTConstants.REQUEST_CODE_HEART_RATE_EXPLAIN);
                mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BTConstants.REQUEST_CODE_HEART_RATE_EXPLAIN) {
            mainActivity.mNeedRefreshData = false;
        }
        if (requestCode == BTConstants.REQUEST_CODE_HEART_RATE_DAILY) {
            mainActivity.mNeedRefreshData = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HeartRate rate = (HeartRate) lvHeartRate.getItemAtPosition(position);
        if (rate == null) {
            return;
        }
        Intent intent = new Intent(mainActivity, HeartRateDailyActivity.class);
        intent.putExtra("heartRate", rate);
        startActivityForResult(intent, BTConstants.REQUEST_CODE_HEART_RATE_EXPLAIN);
        mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
