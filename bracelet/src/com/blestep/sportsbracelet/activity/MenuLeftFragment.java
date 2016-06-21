package com.blestep.sportsbracelet.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;

public class MenuLeftFragment extends Fragment implements OnClickListener {
	private View mView;
	private MainActivity mainActivity;
	private TextView tv_bracelet_name, tv_alert_coming_call_state;
	private ImageView iv_battery_state, iv_conn_state;
	private CheckBox cb_alert_low_battery, cb_alert_find_band;
	private BTService mBtService;
	private boolean isContinue = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView == null) {
			mView = inflater.inflate(R.layout.left_menu, container, false);
		}
		initView();
		initListener();
		initData();
		return mView;
	}

	@Override
	public void onResume() {
		initData();
		super.onResume();
	}

	private void initView() {
		tv_bracelet_name = (TextView) mView.findViewById(R.id.tv_bracelet_name);
		tv_alert_coming_call_state = (TextView) mView
				.findViewById(R.id.tv_alert_coming_call_state);
		iv_battery_state = (ImageView) mView
				.findViewById(R.id.iv_battery_state);
		iv_conn_state = (ImageView) mView.findViewById(R.id.iv_conn_state);
		cb_alert_low_battery = (CheckBox) mView
				.findViewById(R.id.cb_alert_low_battery);
		cb_alert_find_band = (CheckBox) mView
				.findViewById(R.id.cb_alert_find_band);
		// if (mainActivity.getmBtService() != null &&
		// mainActivity.getmBtService().isConnDevice()) {
		// cb_alert_find_band.setEnabled(true);
		// } else {
		// cb_alert_find_band.setEnabled(false);
		// }
		cb_alert_find_band
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							if (mainActivity.getmBtService() != null) {
								mBtService = mainActivity.getmBtService();
								if (mBtService.isConnDevice()) {
									isContinue = true;
									startFindBandShake();
								} else {
									ToastUtils
											.showToast(
													mainActivity,
													R.string.alert_find_band_conn_false);
									cb_alert_find_band.setChecked(false);
								}
							} else {
								ToastUtils.showToast(mainActivity,
										R.string.alert_find_band_conn_false);
								cb_alert_find_band.setChecked(false);
							}
						} else {
							isContinue = false;
						}

					}
				});
	}

	private void startFindBandShake() {
		new Thread() {
			public void run() {
				while (isContinue) {
					mBtService.shakeFindBand();
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	private void initListener() {
		mView.findViewById(R.id.rl_alert_coming_call).setOnClickListener(this);
		mView.findViewById(R.id.rl_alert_alarm).setOnClickListener(this);
		mView.findViewById(R.id.rl_bind_bracelet).setOnClickListener(this);
		mView.findViewById(R.id.rl_bracelet_reset).setOnClickListener(this);
		mView.findViewById(R.id.rl_about).setOnClickListener(this);
	}

	private void initData() {
		tv_bracelet_name.setText(SPUtiles.getStringValue(
				BTConstants.SP_KEY_DEVICE_NAME, ""));
		if (SPUtiles.getBooleanValue(BTConstants.SP_KEY_COMING_PHONE_ALERT,
				true)) {
			tv_alert_coming_call_state
					.setText(getString(R.string.alert_coming_call_open));
		} else {
			tv_alert_coming_call_state
					.setText(getString(R.string.alert_coming_call_close));
		}
	}

	public void updateView(BTService mBtService) {
		if (mBtService.isConnDevice()) {
			iv_conn_state.setImageResource(R.drawable.conn_state_success);
		} else {
			iv_conn_state.setImageResource(R.drawable.conn_state_failure);
		}
		int battery = SPUtiles.getIntValue(BTConstants.SP_KEY_BATTERY, 0);
		if (battery == 0) {
			iv_battery_state.setImageResource(R.drawable.battery_one);
		}
		if (battery == 25) {
			iv_battery_state.setImageResource(R.drawable.battery_one);
		}
		if (battery == 50) {
			iv_battery_state.setImageResource(R.drawable.battery_two);
		}
		if (battery == 75) {
			iv_battery_state.setImageResource(R.drawable.battery_three);
		}
		if (battery == 100) {
			iv_battery_state.setImageResource(R.drawable.battery_four);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_alert_coming_call:
			startActivity(new Intent(mainActivity, PhoneComingActivity.class));
			break;
		case R.id.rl_alert_alarm:
			startActivity(new Intent(mainActivity, AlarmActivity.class));
			break;
		case R.id.rl_bind_bracelet:
			startActivity(new Intent(mainActivity, BindDeviceActivity.class));
			break;
		case R.id.rl_bracelet_reset:
			AlertDialog.Builder builder = new Builder(mainActivity);
			builder.setMessage(R.string.bracelet_reset_alert);
			builder.setPositiveButton(R.string.bracelet_reset_alert_confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SPUtiles.clearAllData();
							DBTools.getInstance(mainActivity).deleteAllData();
							mainActivity.getmBtService().mBluetoothGatt.close();
							mainActivity.getmBtService().mBluetoothGatt = null;
							dialog.dismiss();
							mainActivity.finish();
						}
					});
			builder.setNegativeButton(R.string.bracelet_reset_alert_cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.show();
			break;
		case R.id.rl_about:
			startActivity(new Intent(mainActivity, AboutActivity.class));
			break;

		default:
			break;
		}

	}

}
