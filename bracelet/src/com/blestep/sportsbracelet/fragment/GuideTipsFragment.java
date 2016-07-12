package com.blestep.sportsbracelet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;

import butterknife.ButterKnife;

public class GuideTipsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guide_tips_text, null);
        TextView tv_guide_tips = ButterKnife.findById(view, R.id.tv_guide_tips);
        String tips = (String) getArguments().get("tips");
        tv_guide_tips.setText(tips);
        return view;
    }
}