package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;
import com.umeng.analytics.MobclickAgent;

public class StepActivity extends BaseActivity implements OnItemClickListener,
		ICircleProgressValue {
	private CircleProgressView circleView;
	private TextView tv_conn_status, tv_current_value;
	private ListView lv_devices;
	private SimpleAdapter mAdapter;
	private ArrayList<HashMap<String, String>> mDevices;
	private BTService mBtService;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (BTConstants.ACTION_BLE_DEVICES_DATA.equals(intent
						.getAction())) {
					tv_conn_status.setText("结束扫描...");
					ArrayList<BleDevice> devices = (ArrayList<BleDevice>) intent
							.getExtras().getSerializable(
									BTConstants.EXTRA_KEY_DEVICES);
					for (int i = 0; i < devices.size(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						BleDevice device = devices.get(i);
						map.put("name", device.name);
						map.put("address", device.address);
						mDevices.add(map);
					}
					mAdapter.notifyDataSetChanged();
				} else if (BTConstants.ACTION_DISCOVER_SUCCESS.equals(intent
						.getAction())) {
					tv_conn_status.setText("连接成功...");
					lv_devices.setVisibility(View.GONE);
					circleView.setVisibility(View.VISIBLE);
				} else if (BTConstants.ACTION_CONN_STATUS_DISCONNECTED
						.equals(intent.getAction())) {
					tv_conn_status.setText("断开连接...");
				} else if (BTConstants.ACTION_REFRESH_DATA.equals(intent
						.getAction())) {
					int battery = SPUtiles.getIntValue(
							BTConstants.SP_KEY_BATTERY, 0);
					tv_conn_status.setText("电量为" + battery + "%");
					Step step = DBTools.getInstance(StepActivity.this)
							.selectCurrentStep();
					if (step != null) {
						circleView.setMaxValue(5000);
						circleView.setValueAnimated(Float.valueOf(step.count));
					}

				} else if (BTConstants.ACTION_CONN_STATUS_TIMEOUT.equals(intent
						.getAction())) {
					tv_conn_status.setText("无法连接到设备");
					Step step = DBTools.getInstance(StepActivity.this)
							.selectCurrentStep();
					if (step != null) {
						circleView.setVisibility(View.VISIBLE);
						circleView.setMaxValue(5000);
						circleView.setValueAnimated(Float.valueOf(step.count));
					}
				}
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.step_page);
		circleView = (CircleProgressView) findViewById(R.id.circleView);
		circleView.setMaxValue(100);
		circleView.setValueAnimated(0);
		circleView.setmProgressValue(this);
		tv_conn_status = (TextView) findViewById(R.id.tv_conn_status);
		lv_devices = (ListView) findViewById(R.id.lv_devices);
		tv_current_value = (TextView) findViewById(R.id.tv_current_value);
		mDevices = new ArrayList<HashMap<String, String>>();
		mAdapter = new SimpleAdapter(this, mDevices,
				R.layout.devices_list_item, new String[] { "name", "address" },
				new int[] { R.id.tv_device_name, R.id.tv_device_address });
		lv_devices.setAdapter(mAdapter);
		lv_devices.setOnItemClickListener(this);
		bindService(new Intent(this, BTService.class), mServiceConnection,
				BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(BTConstants.ACTION_BLE_DEVICES_DATA);
		filter.addAction(BTConstants.ACTION_DISCOVER_SUCCESS);
		filter.addAction(BTConstants.ACTION_CONN_STATUS_DISCONNECTED);
		filter.addAction(BTConstants.ACTION_REFRESH_DATA);
		filter.addAction(BTConstants.ACTION_CONN_STATUS_TIMEOUT);
		registerReceiver(mReceiver, filter);
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// 注销广播接收器
		unregisterReceiver(mReceiver);
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BTModule.REQUEST_ENABLE_BT:
				tv_conn_status.setText("开始扫描...");
				mBtService.scanDevice();
				break;

			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		// 关闭蓝牙服务
		unbindService(mServiceConnection);
		stopService(new Intent(this, BTService.class));
		mBtService = null;
		super.onDestroy();
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogModule.d("连接服务onServiceConnected...");
			mBtService = ((LocalBinder) service).getService();
			// 开启蓝牙
			if (!BTModule.isBluetoothOpen()) {
				BTModule.openBluetooth(StepActivity.this);
			} else {
				tv_conn_status.setText("开始扫描...");
				mBtService.scanDevice();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogModule.d("断开服务onServiceDisconnected...");
			mBtService = null;
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		lv_devices.setVisibility(View.GONE);
		circleView.setVisibility(View.VISIBLE);
		mBtService.connectBle(mDevices.get(position).get("address"));
	}

	@Override
	public void getProgressValue(int value) {
		tv_current_value.setVisibility(View.VISIBLE);
		tv_current_value.setText(value + "");

	}
}
