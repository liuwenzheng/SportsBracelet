package com.blestep.sportsbracelet.activity;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.fragment.ImageFragment;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.blestep.sportsbracelet.view.GradientLinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GuideActivity extends FragmentActivity {
    @Bind(R.id.gll_bg)
    GradientLinearLayout gllBg;
    @Bind(R.id.csvp_guide)
    ControlScrollViewPager csvpGuide;
    private int[] colors = new int[8];
    private List<Fragment> views = new ArrayList<>();

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
    }

    class PageChangeLisener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            int guideBg1 = (Integer) new ArgbEvaluator().evaluate(positionOffset, colors[0], colors[1]);
            int guideBg2 = (Integer) new ArgbEvaluator().evaluate(positionOffset, colors[2], colors[3]);
            int guideBg3 = (Integer) new ArgbEvaluator().evaluate(positionOffset, colors[4], colors[5]);
            int guideBg4 = (Integer) new ArgbEvaluator().evaluate(positionOffset, colors[6], colors[7]);
            switch (position) {
                case 0:
                    gllBg.setGradient(guideBg1, guideBg2);
                    break;
                case 1:
                    gllBg.setGradient(guideBg2, guideBg3);
                    break;
                case 2:
                    gllBg.setGradient(guideBg3, guideBg4);
                    break;
            }
        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

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
        views.add(new ImageFragment());
        views.add(new ImageFragment());
        views.add(new ImageFragment());
        views.add(new ImageFragment());
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
