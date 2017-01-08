package com.blestep.sportsbracelet.activity;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseHandler;
import com.blestep.sportsbracelet.fragment.MainTabHeartRate;
import com.blestep.sportsbracelet.fragment.MainTabSleep;
import com.blestep.sportsbracelet.fragment.MainTabSteps;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.extras.PullToRefreshViewPager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener {

    @Bind(R.id.rb_indicator_step)
    RadioButton rb_indicator_step;
    @Bind(R.id.rb_indicator_heart_rate)
    RadioButton rb_indicator_heart_rate;
    @Bind(R.id.rb_indicator_sleep)
    RadioButton rb_indicator_sleep;
    @Bind(R.id.rl_main_header)
    RelativeLayout rl_main_header;
    @Bind(R.id.rl_main_content)
    RelativeLayout rl_main_content;
    @Bind(R.id.tv_header_sleep_title)
    TextView tv_header_sleep_title;
    @Bind(R.id.tv_header_steps_title)
    TextView tv_header_steps_title;
    @Bind(R.id.tv_header_heart_rate_title)
    TextView tv_header_heart_rate_title;
    private FragmentStatePagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private ProgressDialog mDialog;
    private BTService mBtService;
    public boolean mNeedRefreshData = true;
    private static final Object LOCK = new Object();
    // 同步状态集合（未同步则跳过执行下一条）
    private ConcurrentHashMap<Integer, Boolean> mSyncMap = new ConcurrentHashMap<>();
    private boolean mHeartRateShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initListener();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTConstants.ACTION_CONN_STATUS_TIMEOUT);
        filter.addAction(BTConstants.ACTION_CONN_STATUS_DISCONNECTED);
        filter.addAction(BTConstants.ACTION_DISCOVER_SUCCESS);
        filter.addAction(BTConstants.ACTION_DISCOVER_FAILURE);
        filter.addAction(BTConstants.ACTION_REFRESH_DATA);
        filter.addAction(BTConstants.ACTION_ACK);
        filter.addAction(BTConstants.ACTION_REFRESH_DATA_BATTERY);
        filter.addAction(BTConstants.ACTION_REFRESH_DATA_VERSION);
        filter.setPriority(100);
        // filter.addAction(BTConstants.ACTION_REFRESH_DATA_SLEEP_INDEX);
        // filter.addAction(BTConstants.ACTION_REFRESH_DATA_SLEEP_RECORD);
        registerReceiver(mReceiver, filter);
        bindService(new Intent(this, BTService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBtService != null && mBtService.isConnDevice() && !mIsConnDevice) {
            if (mNeedRefreshData) {
                LogModule.i("打开页面同步数据");
                autoPullUpdate(getString(R.string.step_syncdata_waiting));
            } else {
                LogModule.i("不需要同步数据");
                mNeedRefreshData = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        // stopService(new Intent(this, BTService.class));
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // 注销广播接收器
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    public BTService getmBtService() {
        return mBtService;
    }

    public void setmBtService(BTService mBtService) {
        this.mBtService = mBtService;
    }

    private FrameLayout frame_main_conn_tips, frame_main_tips;
    private MainTabSteps mTabSteps;
    private MainTabSleep mTabSleep;
    private MainTabHeartRate mTabHeartRate;
    private Fragment leftMenuFragment, rightMenuFragment;
    private PullToRefreshViewPager pull_refresh_viewpager;
    private ViewPager mViewPager;
    private boolean mIsConnDevice = false;
    private boolean mIsSyncData = false;

    private void initView() {
        pull_refresh_viewpager = (PullToRefreshViewPager) findViewById(R.id.pull_refresh_viewpager);
        // 初始化SlideMenu
        initRightMenu();
        // 初始化ViewPager
        initViewPager();
        frame_main_conn_tips = (FrameLayout) findViewById(R.id.frame_main_conn_tips);
        frame_main_conn_tips.setVisibility(View.GONE);
        frame_main_tips = (FrameLayout) findViewById(R.id.frame_main_tips);
        frame_main_tips.setVisibility(View.GONE);
    }

    public void setPullToRefreshViewEnable(boolean enable) {
        pull_refresh_viewpager.setScrollToHeader(enable);
    }

    private void initListener() {
        frame_main_conn_tips.setOnClickListener(this);
        pull_refresh_viewpager.setOnRefreshListener(new OnRefreshListener<ViewPager>() {

            @Override
            public void onRefresh(PullToRefreshBase<ViewPager> refreshView) {
                if (mBtService.isConnDevice()) {
                    if (!mIsSyncData) {
                        LogModule.i("下拉刷新同步数据");
                        syncAllData();
                    }
                } else {
                    if (!mIsConnDevice) {
                        LogModule.i("未连接，先连接手环");
                        mIsConnDevice = true;
                        autoPullUpdate(getString(R.string.setting_device));
                        mBtService.connectBle(SPUtiles.getStringValue(
                                BTConstants.SP_KEY_DEVICE_ADDRESS, null));
                    }
                }
            }
        });
        rb_indicator_step.setChecked(true);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            FloatEvaluator floatEvaluator = new FloatEvaluator();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!mHeartRateShow) {
                    if (position % 2 == 0) {
                        rl_main_header.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_04b6bb));
                        int bgColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.blue_04b6bb),
                                ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                        rl_main_header.setBackgroundColor(bgColor);
                        // 字体颜色
                        tv_header_steps_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        int stepsColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.white_ffffff),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        tv_header_steps_title.setTextColor(stepsColor);
                        tv_header_sleep_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9));
                        int sleepColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9),
                                ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        tv_header_sleep_title.setTextColor(sleepColor);
                        // 字体大小
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        float stepsSize = floatEvaluator.evaluate(positionOffset, 18, 14);
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, stepsSize);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        float sleepSize = floatEvaluator.evaluate(positionOffset, 14, 18);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, sleepSize);
                    } else {
                        rl_main_header.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                        int bgColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.blue_00334d),
                                ContextCompat.getColor(MainActivity.this, R.color.blue_04b6bb));
                        rl_main_header.setBackgroundColor(bgColor);
                        // 字体颜色
                        tv_header_steps_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        int stepsColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5),
                                ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        tv_header_steps_title.setTextColor(stepsColor);
                        tv_header_sleep_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        int sleepColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.white_ffffff),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9));
                        tv_header_sleep_title.setTextColor(sleepColor);
                        // 字体大小
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        float stepsSize = floatEvaluator.evaluate(positionOffset, 14, 18);
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, stepsSize);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        float sleepSize = floatEvaluator.evaluate(positionOffset, 18, 14);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, sleepSize);
                    }
                } else {
                    if (position % 3 == 0) {
                        rl_main_header.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_04b6bb));
                        int bgColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.blue_04b6bb),
                                ContextCompat.getColor(MainActivity.this, R.color.green_00af6b));
                        rl_main_header.setBackgroundColor(bgColor);
                        // 字体颜色
                        tv_header_steps_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        int stepsColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.white_ffffff),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        tv_header_steps_title.setTextColor(stepsColor);

                        tv_header_heart_rate_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9));
                        int heartRateColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9),
                                ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        tv_header_heart_rate_title.setTextColor(heartRateColor);

                        tv_header_sleep_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9));
                        int sleepColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_95d7d9),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        tv_header_sleep_title.setTextColor(sleepColor);
                        // 字体大小
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        float stepsSize = floatEvaluator.evaluate(positionOffset, 18, 14);
                        tv_header_steps_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, stepsSize);
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        float heartRateSize = floatEvaluator.evaluate(positionOffset, 14, 18);
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, heartRateSize);
                    } else if (position % 3 == 1) {
                        rl_main_header.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green_00af6b));
                        int bgColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.green_00af6b),
                                ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                        rl_main_header.setBackgroundColor(bgColor);
                        // 字体颜色
                        tv_header_steps_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        int stepsColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        tv_header_steps_title.setTextColor(stepsColor);

                        tv_header_heart_rate_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        int heartRateColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.white_ffffff),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        tv_header_heart_rate_title.setTextColor(heartRateColor);

                        tv_header_sleep_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        int sleepColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9),
                                ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        tv_header_sleep_title.setTextColor(sleepColor);
                        // 字体大小
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        float heartRateSize = floatEvaluator.evaluate(positionOffset, 18, 14);
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, heartRateSize);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        float sleepSize = floatEvaluator.evaluate(positionOffset, 14, 18);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, sleepSize);
                    } else if (position % 3 == 2) {
                        rl_main_header.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                        int bgColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.blue_00334d),
                                ContextCompat.getColor(MainActivity.this, R.color.green_00af6b));
                        rl_main_header.setBackgroundColor(bgColor);
                        // 字体颜色
                        tv_header_steps_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        int stepsColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        tv_header_steps_title.setTextColor(stepsColor);

                        tv_header_heart_rate_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5));
                        int heartRateColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.grey_a3adb5),
                                ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        tv_header_heart_rate_title.setTextColor(heartRateColor);

                        tv_header_sleep_title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white_ffffff));
                        int sleepColor = (int) argbEvaluator.evaluate(positionOffset, ContextCompat.getColor(MainActivity.this, R.color.white_ffffff),
                                ContextCompat.getColor(MainActivity.this, R.color.grey_b4e7d9));
                        tv_header_sleep_title.setTextColor(sleepColor);
                        // 字体大小
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        float heartRateSize = floatEvaluator.evaluate(positionOffset, 14, 18);
                        tv_header_heart_rate_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, heartRateSize);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        float sleepSize = floatEvaluator.evaluate(positionOffset, 18, 14);
                        tv_header_sleep_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, sleepSize);
                    }
                }
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    rb_indicator_step.setChecked(true);
                    rl_main_content.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_00bdc2));
                }
                if (i == 1) {
                    if (mHeartRateShow) {
                        rb_indicator_heart_rate.setChecked(true);
                        rl_main_content.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green_00af6b));
                    } else {
                        rb_indicator_sleep.setChecked(true);
                        rl_main_content.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                    }
                }
                if (i == 2) {
                    rb_indicator_sleep.setChecked(true);
                    rl_main_content.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue_00334d));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (BTConstants.ACTION_CONN_STATUS_TIMEOUT.equals(intent.getAction())
                        || BTConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(intent.getAction())
                        || BTConstants.ACTION_DISCOVER_FAILURE.equals(intent.getAction())) {
                    if (leftMenuFragment != null && leftMenuFragment.isVisible()) {
                        ((MenuLeftFragment) leftMenuFragment).updateView(mBtService);
                    }
                    mIsConnDevice = false;
                    mIsSyncData = false;
                    LogModule.i("配对失败...");
                    pull_refresh_viewpager.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.setting_device));
                    pull_refresh_viewpager.onRefreshComplete();
                    ToastUtils.showToast(MainActivity.this, R.string.setting_device_conn_failure);
                    frame_main_conn_tips.setVisibility(View.VISIBLE);
                    frame_main_tips.setVisibility(View.GONE);
                    // if (mDialog != null) {
                    // mDialog.dismiss();
                    // }
                }
                if (BTConstants.ACTION_DISCOVER_SUCCESS.equals(intent.getAction())) {
                    mIsConnDevice = false;
                    LogModule.i("配对成功...");
                    if (leftMenuFragment != null && leftMenuFragment.isVisible()) {
                        ((MenuLeftFragment) leftMenuFragment).updateView(mBtService);
                    }
                    ToastUtils.showToast(MainActivity.this, R.string.setting_device_conn_success);
                    pull_refresh_viewpager.onRefreshComplete();
                    autoPullUpdate(getString(R.string.step_syncdata_waiting));
                    frame_main_conn_tips.setVisibility(View.GONE);
                    frame_main_tips.setVisibility(View.GONE);
                    // if (mDialog != null) {
                    // mDialog.dismiss();
                    // }
                    LogModule.i("配对完开始同步数据");
                    // syncAllData();
                    // frame_main_tips.setText(R.string.step_syncdata_waiting);
                    // frame_main_tips.setVisibility(View.VISIBLE);
                    // mDialog = ProgressDialog.show(MainActivity.this, null,
                    // getString(R.string.step_syncdata_waiting),
                    // false, false);
                }
                if (BTConstants.ACTION_REFRESH_DATA.equals(intent.getAction())) {
                    int header = intent.getIntExtra(BTConstants.EXTRA_KEY_BACK_HEADER, 0);
                    switch (header) {
                        case 0:
                            resetSyncMap(BTConstants.HEADER_BACK_COUNT);
                            int heartRateCount = intent.getIntExtra("heartRateCount", 0);
                            if (mHeartRateShow && heartRateCount != 0) {
                                LogModule.i("支持心率且心率有数据");
                                mBtService.setHeartRateInterval();
                                executeNextTask(BTConstants.TYPE_SET_HEART_RATE_INTERVAL, 3000);
                            } else {
                                syncSuccess();
                            }
                            break;
                        case BTConstants.TYPE_SET_HEART_RATE_INTERVAL:
                            resetSyncMap(header);
                            mBtService.getHeartRate();
                            executeNextTask(BTConstants.TYPE_GET_HEART_RATE, 3000);
                            break;
                        case BTConstants.HEADER_HEART_RATE:
                            resetSyncMap(BTConstants.TYPE_GET_HEART_RATE);
                            syncSuccess();
                            break;
                        case BTConstants.HEADER_FIRMWARE_VERSION:
                            resetSyncMap(header);
                            mBtService.getBatteryData();
                            executeNextTask(BTConstants.HEADER_BACK_RECORD, 3000);
                            break;
                        case BTConstants.HEADER_BACK_RECORD:
                            resetSyncMap(header);
                            // 判断是否已同步过当天数据
                            String syncDate = SPUtiles.getStringValue(BTConstants.SP_KEY_CURRENT_SYNC_DATE, "");
                            String currentDate = Utils.calendar2strDate(Calendar.getInstance(), BTConstants.PATTERN_YYYY_MM_DD);
                            if (syncDate.equals(currentDate)) {
                                LogModule.i("同步过当天数据");
                                mBtService.getCurrentData();
                                executeNextTask(BTConstants.TYPE_GET_CURRENT, 5000);
                            } else {
                                LogModule.i("未同步过当天数据");
                                mBtService.getStepData();
                                executeNextTask(BTConstants.HEADER_BACK_STEP, 5000);
                            }
                            break;
                        case BTConstants.HEADER_BACK_STEP:
                            resetSyncMap(header);
                            mBtService.getDataCount();
                            executeNextTask(BTConstants.HEADER_BACK_COUNT, 3000);
                            break;
                        case BTConstants.HEADER_BACK_COUNT:
                            resetSyncMap(header);
                            mBtService.getSleepIndex();
                            break;
                        case BTConstants.HEADER_BACK_SLEEP_INDEX:
                            resetSyncMap(header);
                            mBtService.getSleepRecord();
                            executeNextTask(BTConstants.HEADER_BACK_SLEEP_RECORD, 5000);
                            break;
                        case BTConstants.HEADER_BACK_SLEEP_RECORD:
                            resetSyncMap(header);
                            syncSuccess();
                            break;
                        case BTConstants.TYPE_GET_CURRENT:
                            resetSyncMap(header);
                            break;
                    }
                }
                if (BTConstants.ACTION_ACK.equals(intent.getAction())) {
                    int ack = intent.getIntExtra(BTConstants.EXTRA_KEY_ACK_VALUE, 0);
                    if (ack == BTConstants.HEADER_BACK_INSIDE_VERSION) {
                        resetSyncMap(ack);
                        createView();
                        mBtService.syncTimeData();
                        executeNextTask(BTConstants.HEADER_SYNTIMEDATA, 3000);
                    } else if (ack == BTConstants.HEADER_SYNTIMEDATA) {
                        resetSyncMap(ack);
                        mBtService.syncUserInfoData();
                        executeNextTask(BTConstants.HEADER_SYNUSERINFO, 3000);
                    } else if (ack == BTConstants.HEADER_SYNUSERINFO) {
                        resetSyncMap(ack);
                        // 先同步前四组闹钟数据，没有则发送0
                        mBtService.syncAlarmData();
                        executeNextTask(BTConstants.HEADER_SYNALARM_NEW, 3000);
                    } else if (ack == BTConstants.HEADER_SYNALARM_NEW) {
                        resetSyncMap(ack);
                        if (!SPUtiles.getBooleanValue(BTConstants.SP_KEY_ALARM_SYNC_FINISH, false)) {
                            // 再同步后四组数据，没有则发送0
                            SPUtiles.setBooleanValue(BTConstants.SP_KEY_ALARM_SYNC_FINISH, true);
                            mBtService.syncAlarmData();
                        } else {
                            // 闹钟同步完后，发送单位数据
                            SPUtiles.setBooleanValue(BTConstants.SP_KEY_ALARM_SYNC_FINISH, false);
                            mBtService.syncUnit();
                            executeNextTask(BTConstants.HEADER_UNIT_SYSTEM, 3000);
                        }
                    } else if (ack == BTConstants.HEADER_UNIT_SYSTEM) {
                        resetSyncMap(ack);
                        mBtService.syncTime();
                        executeNextTask(BTConstants.HEADER_TIME_SYSTEM, 3000);
                    } else if (ack == BTConstants.HEADER_TIME_SYSTEM) {
                        resetSyncMap(ack);
                        mBtService.syncLight();
                        executeNextTask(BTConstants.HEADER_LIGHT_SYSTEM, 3000);
                    } else if (ack == BTConstants.HEADER_LIGHT_SYSTEM) {
                        resetSyncMap(ack);
                        mBtService.getVersionData();
                        executeNextTask(BTConstants.HEADER_FIRMWARE_VERSION, 3000);
                    }
                }
            }

        }
    };

    private void syncSuccess() {
        mIsSyncData = false;
        LogModule.i("同步成功...");
        // 同步了全部数据
        SPUtiles.setStringValue(BTConstants.SP_KEY_CURRENT_SYNC_DATE, Utils.calendar2strDate(Calendar.getInstance(), BTConstants.PATTERN_YYYY_MM_DD));
        pull_refresh_viewpager.onRefreshComplete();
        if (mTabSteps != null && mTabSteps.isVisible()) {
            mTabSteps.updateView();
        }
        if (mTabSleep != null && mTabSleep.isVisible()) {
            mTabSleep.updateView(Calendar.getInstance());
        }
        if (mTabHeartRate != null && mTabHeartRate.isVisible()) {
            mTabHeartRate.updateView();
        }
        if (leftMenuFragment != null && leftMenuFragment.isVisible()) {
            ((MenuLeftFragment) leftMenuFragment).updateView(mBtService);
        }
        frame_main_tips.setVisibility(View.GONE);
        ToastUtils.showToast(MainActivity.this, R.string.syn_success);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogModule.i("连接服务onServiceConnected...");
            mBtService = ((LocalBinder) service).getService();
            // 开启蓝牙
            if (!BTModule.isBluetoothOpen()) {
                BTModule.openBluetooth(MainActivity.this);
            } else {
                LogModule.i("连接手环or同步数据？");
                isConnService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogModule.i("断开服务onServiceDisconnected...");
            mBtService.mBluetoothGatt = null;
            mBtService = null;
        }
    };

    private void isConnService() {
        if (mBtService.isConnDevice()) {
            LogModule.i("已经连接手环开始同步数据");
            autoPullUpdate(getString(R.string.step_syncdata_waiting));
            // syncAllData();
            frame_main_conn_tips.setVisibility(View.GONE);
            // frame_main_tips.setText(R.string.step_syncdata_waiting);
            // frame_main_tips.setVisibility(View.VISIBLE);
            // mDialog = ProgressDialog.show(MainActivity.this, null,
            // getString(R.string.step_syncdata_waiting),
            // false, false);
        } else {
            LogModule.i("未连接，先连接手环");
            mIsConnDevice = true;
            autoPullUpdate(getString(R.string.setting_device));
            mBtService.connectBle(SPUtiles.getStringValue(
                    BTConstants.SP_KEY_DEVICE_ADDRESS, null));
            // frame_main_tips.setText(R.string.setting_device);
            // frame_main_tips.setVisibility(View.VISIBLE);
            // mDialog = ProgressDialog.show(MainActivity.this, null,
            // getString(R.string.setting_device), false,
            // false);
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends BaseHandler<MainActivity> {

        public MyHandler(MainActivity mainActivity) {
            super(mainActivity);
        }

        @Override
        protected void handleMessage(MainActivity mainActivity, Message msg) {
        }
    }

    /**
     * 同步数据
     */
    private void syncAllData() {
        mIsSyncData = true;
        // 5.0偶尔会出现获取不到数据的情况，这时候延迟发送命令，解决问题
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mBtService.getInsideVersion();
                executeNextTask(BTConstants.HEADER_BACK_INSIDE_VERSION, 3000);
            }
        }, 500);
    }

    // 设置超时任务，如果未收到回复，执行下条命令
    private void executeNextTask(final int headerGetdata, int delayMillis) {
        mSyncMap.put(headerGetdata, true);
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mSyncMap.get(headerGetdata)) {
                    switch (headerGetdata) {
                        case BTConstants.HEADER_BACK_INSIDE_VERSION:
                            LogModule.i("同步内部版本超时，发送同步时间命令");
                            resetSyncMap(headerGetdata);
                            mBtService.syncTimeData();
                            executeNextTask(BTConstants.HEADER_SYNTIMEDATA, 3000);
                            break;
                        case BTConstants.HEADER_SYNTIMEDATA:
                            LogModule.i("同步时间超时，发送同步用户数据命令");
                            resetSyncMap(headerGetdata);
                            mBtService.syncUserInfoData();
                            executeNextTask(BTConstants.HEADER_SYNUSERINFO, 3000);
                            break;
                        case BTConstants.HEADER_SYNUSERINFO:
                            LogModule.i("同步用户数据超时，发送同步闹钟命令");
                            resetSyncMap(headerGetdata);
                            mBtService.syncAlarmData();
                            executeNextTask(BTConstants.HEADER_SYNALARM_NEW, 3000);
                            break;
                        case BTConstants.HEADER_SYNALARM_NEW:
                            LogModule.i("同步闹钟超时，发送同步单位命令");
                            resetSyncMap(headerGetdata);
                            SPUtiles.setBooleanValue(BTConstants.SP_KEY_ALARM_SYNC_FINISH, false);
                            mBtService.syncUnit();
                            executeNextTask(BTConstants.HEADER_UNIT_SYSTEM, 3000);
                            break;
                        case BTConstants.HEADER_UNIT_SYSTEM:
                            LogModule.i("同步单位超时，发送同步时间格式命令");
                            resetSyncMap(headerGetdata);
                            mBtService.syncTime();
                            executeNextTask(BTConstants.HEADER_TIME_SYSTEM, 3000);
                            break;
                        case BTConstants.HEADER_TIME_SYSTEM:
                            LogModule.i("同步时间格式超时，发送同步翻腕亮屏命令");
                            resetSyncMap(headerGetdata);
                            mBtService.syncLight();
                            executeNextTask(BTConstants.HEADER_LIGHT_SYSTEM, 3000);
                            break;
                        case BTConstants.HEADER_LIGHT_SYSTEM:
                            LogModule.i("同步翻腕亮屏超时，发送获取版本信息命令");
                            resetSyncMap(headerGetdata);
                            mBtService.getVersionData();
                            executeNextTask(BTConstants.HEADER_FIRMWARE_VERSION, 3000);
                            break;
                        case BTConstants.HEADER_FIRMWARE_VERSION:
                            LogModule.i("同步获取版本信息超时，发送获取电量命令");
                            resetSyncMap(headerGetdata);
                            mBtService.getBatteryData();
                            executeNextTask(BTConstants.HEADER_BACK_RECORD, 3000);
                            break;
                        case BTConstants.HEADER_BACK_RECORD:
                            LogModule.i("同步获取电量超时，发送获取记步命令");
                            resetSyncMap(headerGetdata);
                            // 判断是否已同步过当天数据
                            String syncDate = SPUtiles.getStringValue(BTConstants.SP_KEY_CURRENT_SYNC_DATE, "");
                            String currentDate = Utils.calendar2strDate(Calendar.getInstance(), BTConstants.PATTERN_YYYY_MM_DD);
                            if (syncDate.equals(currentDate)) {
                                LogModule.i("同步过当天数据");
                                mBtService.getCurrentData();
                                executeNextTask(BTConstants.TYPE_GET_CURRENT, 5000);
                            } else {
                                LogModule.i("未同步过当天数据");
                                mBtService.getStepData();
                                executeNextTask(BTConstants.HEADER_BACK_STEP, 5000);
                            }
                            break;
                        case BTConstants.HEADER_BACK_STEP:
                            LogModule.i("同步获取记步超时，发送获取睡眠总数命令");
                            resetSyncMap(headerGetdata);
                            mBtService.getDataCount();
                            executeNextTask(BTConstants.HEADER_BACK_COUNT, 3000);
                            break;
                        case BTConstants.HEADER_BACK_COUNT:
                            LogModule.i("同步获取睡眠总数超时，提示同步成功！");
                            resetSyncMap(headerGetdata);
                            syncSuccess();
                            break;
                        case BTConstants.HEADER_BACK_SLEEP_RECORD:
                            LogModule.i("同步获取睡眠record超时，提示同步成功！");
                            syncSuccess();
                            break;
                        case BTConstants.TYPE_SET_HEART_RATE_INTERVAL:
                            LogModule.i("同步心率间隔超时，发送获取心率命令");
                            resetSyncMap(headerGetdata);
                            mBtService.getHeartRate();
                            executeNextTask(BTConstants.TYPE_GET_HEART_RATE, 3000);
                            break;
                        case BTConstants.TYPE_GET_HEART_RATE:
                            LogModule.i("同步获取心率超时，提示同步成功！");
                            resetSyncMap(headerGetdata);
                            syncSuccess();
                            break;
                        case BTConstants.TYPE_GET_CURRENT:
                            LogModule.i("同步获取当天数据超时，提示同步成功！");
                            resetSyncMap(headerGetdata);
                            syncSuccess();
                            break;
                    }
                }
            }
        }, delayMillis);
    }

    // 重置超时任务
    private void resetSyncMap(int headerGetdata) {
        mSyncMap.put(headerGetdata, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BTModule.REQUEST_ENABLE_BT:
                    mIsConnDevice = true;
                    autoPullUpdate(getString(R.string.setting_device));
                    mBtService.connectBle(SPUtiles.getStringValue(
                            BTConstants.SP_KEY_DEVICE_ADDRESS, null));
                    // frame_main_tips.setText(R.string.setting_device);
                    // frame_main_tips.setVisibility(View.VISIBLE);
                    // mDialog = ProgressDialog
                    // .show(MainActivity.this, null,
                    // getString(R.string.setting_device), false, false);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initViewPager() {
        mViewPager = pull_refresh_viewpager.getRefreshableView();
        /**
         * 初始化Adapter
         */
        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getItemPosition(Object object) {
                return PagerAdapter.POSITION_NONE;
            }
        };
        mViewPager.setAdapter(mAdapter);
        createView();
        mViewPager.setOffscreenPageLimit(2);
    }

    /**
     * @Date 2017/1/7
     * @Author wenzheng.liu
     * @Description 创建页面
     */
    private void createView() {
        mHeartRateShow = SPUtiles.getBooleanValue(BTConstants.SP_KEY_HEART_RATE_SHOW, false);
        if (mFragments.isEmpty()) {
            mTabSteps = new MainTabSteps();
            mTabHeartRate = new MainTabHeartRate();
            mTabSleep = new MainTabSleep();
            mFragments.add(mTabSteps);
            if (mHeartRateShow) {
                mFragments.add(mTabHeartRate);
                rb_indicator_heart_rate.setVisibility(View.VISIBLE);
                tv_header_heart_rate_title.setVisibility(View.VISIBLE);
            }
            mFragments.add(mTabSleep);
            mAdapter.notifyDataSetChanged();
        } else {
            if (mFragments.size() == 2) {
                if (mHeartRateShow) {
                    mFragments.add(1, mTabHeartRate);
                    mAdapter.notifyDataSetChanged();
                    rb_indicator_heart_rate.setVisibility(View.VISIBLE);
                    tv_header_heart_rate_title.setVisibility(View.VISIBLE);
                }
            }
            if (mFragments.size() == 3) {
                if (!mHeartRateShow) {
                    mFragments.remove(mTabHeartRate);
                    mAdapter.notifyDataSetChanged();
                    rb_indicator_heart_rate.setVisibility(View.GONE);
                    tv_header_heart_rate_title.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initRightMenu() {

        leftMenuFragment = new MenuLeftFragment();
        setBehindContentView(R.layout.left_menu_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
        SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // menu.setBehindWidth(i);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        // menu.setBehindScrollScale(1.0f);
        menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        // 设置右边（二级）侧滑菜单
        menu.setSecondaryMenu(R.layout.right_menu_frame);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        rightMenuFragment = new MenuRightFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
    }

    public void showLeftMenu(View view) {
        getSlidingMenu().showMenu();
    }

    public void showRightMenu(View view) {
        getSlidingMenu().showSecondaryMenu();
    }

    @OnClick({R.id.frame_main_conn_tips, R.id.tv_header_sleep_title, R.id.tv_header_steps_title, R.id.tv_header_heart_rate_title})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frame_main_conn_tips:
                frame_main_conn_tips.setVisibility(View.GONE);
                if (!BTModule.isBluetoothOpen()) {
                    BTModule.openBluetooth(MainActivity.this);
                    return;
                }
                mBtService.connectBle(SPUtiles.getStringValue(
                        BTConstants.SP_KEY_DEVICE_ADDRESS, null));
                mIsConnDevice = true;
                autoPullUpdate(getString(R.string.setting_device));
                // frame_main_tips.setText(R.string.setting_device);
                // frame_main_tips.setVisibility(View.VISIBLE);
                // mDialog = ProgressDialog.show(MainActivity.this, null,
                // getString(R.string.setting_device), false, false);
                break;
            case R.id.tv_header_sleep_title:
                mViewPager.setCurrentItem(mHeartRateShow ? 2 : 1);
                break;
            case R.id.tv_header_heart_rate_title:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.tv_header_steps_title:
                mViewPager.setCurrentItem(0);
                break;

            default:
                break;
        }

    }

    /**
     * 自动下拉刷新
     */
    private void autoPullUpdate(String tips) {
        pull_refresh_viewpager.getLoadingLayoutProxy().setRefreshingLabel(tips);
        new MyHandler(this).postDelayed(new Runnable() {

            @Override
            public void run() {
                pull_refresh_viewpager.setRefreshing();
            }
        }, 1500);
    }

    @Override
    public void onBackPressed() {
        Builder builder = new Builder(this);
        builder.setMessage(R.string.main_exit_tips);
        builder.setPositiveButton(R.string.main_exit_tips_confirm,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                });
        builder.setNegativeButton(R.string.main_exit_tips_cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
