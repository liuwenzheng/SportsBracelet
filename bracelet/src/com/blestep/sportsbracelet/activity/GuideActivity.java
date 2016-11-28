package com.blestep.sportsbracelet.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.fragment.GuideTipsFragment;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.blestep.sportsbracelet.view.GradientLinearView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GuideActivity extends BaseActivity {
    @Bind(R.id.gll_bg)
    GradientLinearView gllBg;
    @Bind(R.id.csvp_guide)
    ControlScrollViewPager csvpGuide;
    @Bind(R.id.iv_guide_icon)
    ImageView ivGuideIcon;
    @Bind(R.id.frame_guide_icon)
    FrameLayout frameGuideIcon;
    @Bind(R.id.iv_guide_splash)
    ImageView ivGuideSplash;
    @Bind(R.id.ll_guide_dot)
    LinearLayout ll_guide_dot;
    @Bind(R.id.btn_start)
    Button btn_start;
    private int[] colors = new int[8];
    private List<Fragment> views = new ArrayList<>();
    private ArgbEvaluator mArgbEvaluator;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        MobclickAgent.setDebugMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.READ_PHONE_STATE
                                , Manifest.permission.READ_CONTACTS
                                , Manifest.permission.RECEIVE_SMS}
                        , PERMISSION_REQUEST_CODE);
                return;
            }
        }
        initContentView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ToastUtils.showToast(GuideActivity.this, "This app needs these permissions!");
                        GuideActivity.this.finish();
                        return;
                    }
                }
                initContentView();
            }
        }
    }

    private void initContentView() {
        if (!SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_FIRST_OPEN, true)) {
            setContentView(R.layout.splash_pass);
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(GuideActivity.this, MainActivity.class));
                    GuideActivity.this.finish();
                }
            }.start();
            return;
        }
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        initColors();
        initFragment();
        GuideViewPagerAdapter mAdapter = new GuideViewPagerAdapter(getSupportFragmentManager());
        csvpGuide.setAdapter(mAdapter);
        csvpGuide.setCurrentItem(0);
        csvpGuide.setOnPageChangeListener(new PageChangeLisener());
        mArgbEvaluator = new ArgbEvaluator();
        ((ImageView) ll_guide_dot.getChildAt(0)).setImageResource(R.drawable.guide_checked_true);
    }

    @OnClick({R.id.tv_register, R.id.tv_login, R.id.btn_start})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_register:
                break;
            case R.id.tv_login:
                break;
            case R.id.btn_start:
                startActivity(new Intent(this, ActivateBraceletActivity.class));
                finish();
                break;
        }
    }

    class PageChangeLisener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            setBgColor(position, positionOffset);
            setGuideImage(position, positionOffset);
            setSplashImage(position, positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < ll_guide_dot.getChildCount(); i++) {
                ((ImageView) ll_guide_dot.getChildAt(i)).setImageResource(R.drawable.guide_checked_false);
            }
            ((ImageView) ll_guide_dot.getChildAt(position)).setImageResource(R.drawable.guide_checked_true);
            if (position == 4) {
                btn_start.setVisibility(View.VISIBLE);
            } else {
                btn_start.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    private void setSplashImage(int position, float positionOffset) {
        int marginBottom = 0;
        if (position == 3) {
            if (positionOffset >= 0.5) {
                marginBottom = (int) (gllBg.getHeight() - positionOffset * 2 * gllBg.getHeight());
            }
        }
        if (position == 4) {
            marginBottom = -gllBg.getHeight();
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivGuideSplash.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, marginBottom);
        ivGuideSplash.setLayoutParams(lp);
    }

    private void setGuideImage(int position, float positionOffset) {
        int marginTop;
        ivGuideIcon.setVisibility(position == 4 ? View.GONE : View.VISIBLE);
        if (positionOffset < 0.5) {
            if (position == 0) {
                ivGuideIcon.setImageResource(R.drawable.guide_step);
            }
            if (position == 1) {
                ivGuideIcon.setImageResource(R.drawable.guide_calories);
            }
            if (position == 2) {
                ivGuideIcon.setImageResource(R.drawable.guide_alarm);
            }
            if (position == 3) {
                ivGuideIcon.setImageResource(R.drawable.guide_cloud);
            }
            marginTop = (int) (positionOffset * frameGuideIcon.getHeight() * 2);
        } else {
            if (position == 0) {
                ivGuideIcon.setImageResource(R.drawable.guide_calories);
            }
            if (position == 1) {
                ivGuideIcon.setImageResource(R.drawable.guide_alarm);
            }
            if (position == 2) {
                ivGuideIcon.setImageResource(R.drawable.guide_cloud);
            }
            if (position == 3) {
                marginTop = (int) (positionOffset * frameGuideIcon.getHeight() * 2);
            } else {
                marginTop = (int) ((1 - positionOffset) * frameGuideIcon.getHeight() * 2);
            }
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ivGuideIcon.getLayoutParams();
        lp.setMargins(lp.leftMargin, marginTop, lp.rightMargin, lp.bottomMargin);
        ivGuideIcon.setLayoutParams(lp);
    }

    private void setBgColor(int position, float positionOffset) {
        int guideBg1 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[0], colors[2]);
        int guideBg2 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[1], colors[3]);
        int guideBg3 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[2], colors[4]);
        int guideBg4 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[3], colors[5]);
        int guideBg5 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[4], colors[6]);
        int guideBg6 = (Integer) mArgbEvaluator.evaluate(positionOffset, colors[5], colors[7]);
        switch (position) {
            case 0:
                gllBg.setGradient(colors[0], colors[1]);
                gllBg.setGradient(guideBg1, guideBg2);
                break;
            case 1:
                gllBg.setGradient(colors[2], colors[3]);
                gllBg.setGradient(guideBg3, guideBg4);
                break;
            case 2:
                gllBg.setGradient(colors[4], colors[5]);
                gllBg.setGradient(guideBg5, guideBg6);
                break;
            case 3:
                gllBg.setGradient(colors[6], colors[7]);
                break;
            case 4:
                break;
        }
    }

    class GuideViewPagerAdapter extends FragmentPagerAdapter {

        public GuideViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return views.get(i);
        }

        @Override
        public int getCount() {
            return views.size();
        }
    }

    private void initFragment() {
        GuideTipsFragment fragment1 = new GuideTipsFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putString("tips", getString(R.string.guide_track_steps));
        fragment1.setArguments(bundle1);
        views.add(fragment1);
        GuideTipsFragment fragment2 = new GuideTipsFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putString("tips", getString(R.string.guide_calorie_control));
        fragment2.setArguments(bundle2);
        views.add(fragment2);
        GuideTipsFragment fragment3 = new GuideTipsFragment();
        Bundle bundle3 = new Bundle();
        bundle3.putString("tips", getString(R.string.guide_smart_alarms));
        fragment3.setArguments(bundle3);
        views.add(fragment3);
        GuideTipsFragment fragment4 = new GuideTipsFragment();
        Bundle bundle4 = new Bundle();
        bundle4.putString("tips", getString(R.string.guide_cloud_sync));
        fragment4.setArguments(bundle4);
        views.add(fragment4);
        GuideTipsFragment fragment5 = new GuideTipsFragment();
        Bundle bundle5 = new Bundle();
        bundle5.putString("tips", getString(R.string.guide_better_everyday));
        fragment5.setArguments(bundle5);
        views.add(fragment5);
    }

    private void initColors() {
        colors[0] = getResources().getColor(R.color.guide1_start_3d8400);
        colors[1] = getResources().getColor(R.color.guide1_end_7dcd00);
        colors[2] = getResources().getColor(R.color.guide2_start_ff7200);
        colors[3] = getResources().getColor(R.color.guide2_end_ff9300);
        colors[4] = getResources().getColor(R.color.guide3_start_4164df);
        colors[5] = getResources().getColor(R.color.guide3_end_6a89f2);
        colors[6] = getResources().getColor(R.color.guide4_start_6614ac);
        colors[7] = getResources().getColor(R.color.guide4_end_8735cc);
    }

}
