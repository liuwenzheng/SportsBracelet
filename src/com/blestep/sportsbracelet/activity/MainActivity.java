package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.blestep.sportsbracelet.AppConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener {

	private ControlScrollViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private ProgressDialog mDialog;
	private BTService mBtService;
	private TextView tv_main_conn_tips, tv_main_tips;
	private MainTab01 tab01;
	private MainTab02 tab02;
	private MainTab03 tab03;
	private Fragment leftMenuFragment, rightMenuFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		// 初始化SlideMenu
		initRightMenu();
		// 初始化ViewPager
		initViewPager();
		tv_main_conn_tips = (TextView) findViewById(R.id.tv_main_conn_tips);
		tv_main_conn_tips.setVisibility(View.GONE);
		tv_main_tips = (TextView) findViewById(R.id.tv_main_tips);
		tv_main_tips.setVisibility(View.GONE);
	}

	private void initListener() {
		tv_main_conn_tips.setOnClickListener(this);
	}

	private void initData() {
	}

	@Override
	protected void onStart() {
		bindService(new Intent(this, BTService.class), mServiceConnection, BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onResume() {
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(AppConstants.ACTION_CONN_STATUS_TIMEOUT);
		filter.addAction(AppConstants.ACTION_CONN_STATUS_DISCONNECTED);
		filter.addAction(AppConstants.ACTION_DISCOVER_SUCCESS);
		filter.addAction(AppConstants.ACTION_DISCOVER_FAILURE);
		filter.addAction(AppConstants.ACTION_REFRESH_DATA);
		registerReceiver(mReceiver, filter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 注销广播接收器
		unregisterReceiver(mReceiver);
		super.onPause();
	}

	@Override
	protected void onStop() {
		unbindService(mServiceConnection);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (AppConstants.ACTION_CONN_STATUS_TIMEOUT.equals(intent.getAction())
						|| AppConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(intent.getAction())
						|| AppConstants.ACTION_DISCOVER_FAILURE.equals(intent.getAction())) {
					LogModule.d("配对失败...");
					ToastUtils.showToast(MainActivity.this, R.string.setting_device_conn_failure);
					tv_main_conn_tips.setVisibility(View.VISIBLE);
					tv_main_tips.setVisibility(View.GONE);
					// if (mDialog != null) {
					// mDialog.dismiss();
					// }
				}
				if (AppConstants.ACTION_DISCOVER_SUCCESS.equals(intent.getAction())) {
					LogModule.d("配对成功...");
					ToastUtils.showToast(MainActivity.this, R.string.setting_device_conn_success);
					tv_main_conn_tips.setVisibility(View.GONE);
					tv_main_tips.setVisibility(View.GONE);
					// if (mDialog != null) {
					// mDialog.dismiss();
					// }
					mBtService.synTimeData();
					mBtService.synUserInfoData();
					mBtService.getSportData();
					tv_main_tips.setText(R.string.step_syncdata_waiting);
					tv_main_tips.setVisibility(View.VISIBLE);
					// mDialog = ProgressDialog.show(MainActivity.this, null,
					// getString(R.string.step_syncdata_waiting),
					// false, false);
				}
				if (AppConstants.ACTION_REFRESH_DATA.equals(intent.getAction())) {
					if (tab01 != null && tab01.isVisible()) {
						tab01.updateView();
					}
					int battery = SPUtiles.getIntValue(SPUtiles.SP_KEY_BATTERY, 0);
					LogModule.i("电量为" + battery + "%");
					tv_main_tips.setVisibility(View.GONE);
					if (leftMenuFragment != null && leftMenuFragment.isVisible()) {
						((MenuLeftFragment)leftMenuFragment).updateView(mBtService);
					}
					// if (mDialog != null) {
					// mDialog.dismiss();
					// }
				}
			}

		}
	};
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogModule.d("连接服务onServiceConnected...");
			mBtService = ((LocalBinder) service).getService();
			// 开启蓝牙
			if (!BTModule.isBluetoothOpen()) {
				BTModule.openBluetooth(MainActivity.this);
			} else {
				LogModule.d("连接手环or同步数据？");
				if (mBtService.isConnDevice()) {
					mBtService.synTimeData();
					mBtService.synUserInfoData();
					mBtService.getSportData();
					tv_main_tips.setText(R.string.step_syncdata_waiting);
					tv_main_tips.setVisibility(View.VISIBLE);
					// mDialog = ProgressDialog.show(MainActivity.this, null,
					// getString(R.string.step_syncdata_waiting),
					// false, false);
				} else {
					mBtService.connectBle(SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, null));
					tv_main_tips.setText(R.string.setting_device);
					tv_main_tips.setVisibility(View.VISIBLE);
					// mDialog = ProgressDialog.show(MainActivity.this, null,
					// getString(R.string.setting_device), false,
					// false);
				}

			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogModule.d("断开服务onServiceDisconnected...");
			mBtService = null;
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BTModule.REQUEST_ENABLE_BT:
				mBtService.connectBle(SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, null));
				tv_main_tips.setText(R.string.setting_device);
				tv_main_tips.setVisibility(View.VISIBLE);
				// mDialog = ProgressDialog
				// .show(MainActivity.this, null,
				// getString(R.string.setting_device), false, false);

				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initViewPager() {
		mViewPager = (ControlScrollViewPager) findViewById(R.id.id_viewpager);
		mViewPager.setScrollable(false);
		tab01 = new MainTab01();
		tab02 = new MainTab02();
		tab03 = new MainTab03();
		mFragments.add(tab01);
		// mFragments.add(tab02);
		// mFragments.add(tab03);
		/**
		 * 初始化Adapter
		 */
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragments.get(arg0);
			}
		};
		mViewPager.setAdapter(mAdapter);
	}

	private void initRightMenu() {

		leftMenuFragment = new MenuLeftFragment();
		setBehindContentView(R.layout.left_menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
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
		rightMenuFragment = new MenuRightFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
	}

	public void showLeftMenu(View view) {
		getSlidingMenu().showMenu();
	}

	public void showRightMenu(View view) {
		getSlidingMenu().showSecondaryMenu();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_main_conn_tips:
			mBtService.connectBle(SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, null));
			tv_main_conn_tips.setVisibility(View.GONE);
			tv_main_tips.setText(R.string.setting_device);
			tv_main_tips.setVisibility(View.VISIBLE);
			// mDialog = ProgressDialog.show(MainActivity.this, null,
			// getString(R.string.setting_device), false, false);
			break;

		default:
			break;
		}

	}

}
