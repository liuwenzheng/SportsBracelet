package com.blestep.sportsbracelet.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.umeng.analytics.MobclickAgent;

public class ClearDataActivity extends BaseActivity implements OnClickListener {

	private BTService mBtService;
	private TextView tv_clear_bracelet, tv_clear_phone;
	private ImageView iv_back;
	private boolean mIsChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clear_data_page);
		initListener();
		initData();
	}

	private void initListener() {
		findViewById(R.id.tv_clear_bracelet).setOnClickListener(this);
		findViewById(R.id.tv_clear_phone).setOnClickListener(this);
		findViewById(R.id.iv_back).setOnClickListener(this);
	}

	private void initData() {
		bindService(new Intent(this, BTService.class), mServiceConnection,
				BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogModule.i("连接服务onServiceConnected...");
			mBtService = ((LocalBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogModule.i("断开服务onServiceDisconnected...");
			mBtService = null;
		}
	};

	@Override
	public void onClick(final View v) {
		if (v.getId() == R.id.iv_back) {
			backToHome();
			return;
		}
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(R.string.clear_tips);
		builder.setPositiveButton(R.string.clear_tips_confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (v.getId()) {
						case R.id.tv_clear_phone:
							DBTools.getInstance(ClearDataActivity.this)
									.deleteAllData();
							ToastUtils.showToast(ClearDataActivity.this,
									R.string.clear_success);
							mIsChanged = true;
							break;
						case R.id.tv_clear_bracelet:
							if (mBtService.isConnDevice()) {
								mBtService.clearData();
								ToastUtils.showToast(ClearDataActivity.this,
										R.string.clear_success);
							} else {
								ToastUtils.showToast(ClearDataActivity.this,
										R.string.clear_fail_conn_device);
							}
							mIsChanged = true;
							break;

						default:
							break;
						}

						dialog.dismiss();
					}
				});
		builder.setNegativeButton(R.string.clear_tips_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});
		builder.show();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToHome();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void backToHome() {
		if (!mIsChanged) {
			setResult(RESULT_CANCELED);
			this.finish();
		} else {
			// 有值更改
			setResult(RESULT_OK);
			this.finish();
		}

	}
}
