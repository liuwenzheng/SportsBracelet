package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.SPUtiles;

public class MenuRightFragment extends Fragment implements OnClickListener {
	private View mView;
	private MainActivity mainActivity;
	private TextView tv_user_name;
	private ImageView iv_user_pic;
	private RelativeLayout rl_center_userinfo, rl_center_target,
			rl_center_clear;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView == null) {
			mView = inflater.inflate(R.layout.right_menu, container, false);
		}
		tv_user_name = (TextView) mView.findViewById(R.id.tv_user_name);
		iv_user_pic = (ImageView) mView.findViewById(R.id.iv_user_pic);
		mView.findViewById(R.id.rl_center_userinfo).setOnClickListener(this);
		mView.findViewById(R.id.rl_center_target).setOnClickListener(this);
		mView.findViewById(R.id.rl_center_clear).setOnClickListener(this);

		return mView;
	}

	@Override
	public void onResume() {
		iniData();
		super.onResume();
	}

	private void iniData() {
		int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, 0);
		String name = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_NAME, "");
		switch (gender) {
		case 0:
			iv_user_pic.setImageResource(R.drawable.pic_male);
			break;
		case 1:
			iv_user_pic.setImageResource(R.drawable.pic_female);
			break;

		default:
			break;
		}
		tv_user_name.setText(name);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_center_userinfo:
			startActivity(new Intent(mainActivity, UserInfoActivity.class));
			break;
		case R.id.rl_center_target:
			startActivity(new Intent(mainActivity, TargetActivity.class));
			break;
		case R.id.rl_center_clear:
			startActivity(new Intent(mainActivity, ClearDataActivity.class));
			break;

		default:
			break;
		}

	}
}
