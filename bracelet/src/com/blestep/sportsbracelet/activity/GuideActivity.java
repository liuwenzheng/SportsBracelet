package com.blestep.sportsbracelet.activity;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.fragment.TextFragment;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.blestep.sportsbracelet.view.GradientLinearView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GuideActivity extends FragmentActivity {
    @Bind(R.id.gll_bg)
    GradientLinearView gllBg;
    @Bind(R.id.csvp_guide)
    ControlScrollViewPager csvpGuide;
    @Bind(R.id.iv_guide_icon)
    ImageView ivGuideIcon;
    @Bind(R.id.frame_guide_icon)
    FrameLayout frameGuideIcon;
    private int[] colors = new int[8];
    private List<Fragment> views = new ArrayList<>();
    private ArgbEvaluator mArgbEvaluator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        initColors();
        initFragment();
        GuideViewPagerAdapter mAdapter = new GuideViewPagerAdapter(getSupportFragmentManager());
        csvpGuide.setAdapter(mAdapter);
        csvpGuide.setCurrentItem(0);
        csvpGuide.setOnPageChangeListener(new PageChangeLisener());
        mArgbEvaluator = new ArgbEvaluator();
    }

    class PageChangeLisener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            setBgColor(position, positionOffset);
            setGuideImage(position, positionOffset);
        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    private void setGuideImage(int position, float positionOffset) {
        if (position >= 3)
            return;
        int marginTop;
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
            marginTop = (int) ((1 - positionOffset) * frameGuideIcon.getHeight() * 2);
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
        views.add(new TextFragment());
        views.add(new TextFragment());
        views.add(new TextFragment());
        views.add(new TextFragment());
        views.add(new TextFragment());
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
