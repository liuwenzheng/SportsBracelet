package com.blestep.sportsbracelet.service;

import java.util.Calendar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.TelephonyManager;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

public class BTService extends Service implements LeScanCallback {
	private boolean mIsStartScan = false;
	private static final long SCAN_PERIOD = 5000;
	private static final int GATT_ERROR_TIMEOUT = 133;

	public static Handler mHandler;
	// private ArrayList<BleDevice> mDevices;
	public BluetoothGatt mBluetoothGatt;
	private BluetoothGattCallback mGattCallback;
	private boolean isReconnect = false;

	@Override
	public void onCreate() {
		mHandler = new Handler(getApplication().getMainLooper());
		LogModule.d("创建BTService...onCreate");
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(BTConstants.ACTION_PHONE_STATE);
		filter.addAction(BTConstants.ACTION_SMS_RECEIVED);
		registerReceiver(mReceiver, filter);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogModule.d("启动BTService...onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	private IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		LogModule.d("绑定BTService...onBind");
		return mBinder;
	}

	/**
	 * 搜索手环
	 */
	public void scanDevice() {
		// String address =
		// SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, "");
		// mDevices = new ArrayList<BleDevice>();
		if (!mIsStartScan) {
			mIsStartScan = true;
			BTModule.scanDevice(this);
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mIsStartScan) {
						LogModule.i(SCAN_PERIOD / 1000 + "s后停止扫描");
						BTModule.mBluetoothAdapter.stopLeScan(BTService.this);
						mIsStartScan = false;
						Intent intent = new Intent(
								BTConstants.ACTION_BLE_DEVICES_DATA_END);
						// intent.putExtra("devices", mDevices);
						sendBroadcast(intent);
					}
				}
			}, SCAN_PERIOD);
		} else {
			LogModule.i("正在扫描中...");
		}

	}

	/**
	 * 连接手环
	 */
	public void connectBle(String address) {
		final BluetoothDevice device = BTModule.mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			return;
		} else {

			mGattCallback = new BluetoothGattCallback() {
				// private int count;

				public void onConnectionStateChange(BluetoothGatt gatt,
						int status, int newState) {
					super.onConnectionStateChange(gatt, status, newState);
					LogModule.d("onConnectionStateChange...status:" + status
							+ "...newState:" + newState);
					switch (newState) {
					case BluetoothProfile.STATE_CONNECTED:
						if (status == GATT_ERROR_TIMEOUT) {
							disConnectBle();
							Intent intent = new Intent(
									BTConstants.ACTION_CONN_STATUS_TIMEOUT);
							sendBroadcast(intent);
						} else {
							if (mBluetoothGatt == null) {
								BluetoothDevice device = BTModule.mBluetoothAdapter
										.getRemoteDevice(SPUtiles
												.getStringValue(
														BTConstants.SP_KEY_DEVICE_ADDRESS,
														null));
								mBluetoothGatt = device.connectGatt(
										BTService.this, false, mGattCallback);
								return;
							}
							mBluetoothGatt.discoverServices();
						}
						break;
					case BluetoothProfile.STATE_DISCONNECTED:
						disConnectBle();
						Intent intent = new Intent(
								BTConstants.ACTION_CONN_STATUS_DISCONNECTED);
						sendBroadcast(intent);
						if (!isReconnect) {
							LogModule.d("开始重连...");
							new Thread(runnableReconnect).start();
						}
						break;
					}
				};

				public void onServicesDiscovered(BluetoothGatt gatt, int status) {
					super.onServicesDiscovered(gatt, status);
					LogModule.d("onServicesDiscovered...status:" + status);
					if (status == BluetoothGatt.GATT_SUCCESS) {
						BTModule.setCharacteristicNotify(mBluetoothGatt);
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent(
										BTConstants.ACTION_DISCOVER_SUCCESS);
								sendBroadcast(intent);
							}
						}, 1000);
					} else {
						Intent intent = new Intent(
								BTConstants.ACTION_DISCOVER_FAILURE);
						sendBroadcast(intent);
					}
				};

				public void onCharacteristicRead(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic, int status) {
					super.onCharacteristicRead(gatt, characteristic, status);
					LogModule.d("onCharacteristicRead...");
				};

				public void onCharacteristicWrite(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic, int status) {
					super.onCharacteristicWrite(gatt, characteristic, status);
					LogModule.d("onCharacteristicWrite...");
					if (status == BluetoothGatt.GATT_SUCCESS) {
						LogModule.d("onCharacteristicWrite...success");
					} else {
						LogModule.d("onCharacteristicWrite...failure");
					}
				};

				public void onCharacteristicChanged(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic) {
					super.onCharacteristicChanged(gatt, characteristic);
					LogModule.d("onCharacteristicChanged...");
					// BTModule.setCharacteristicNotify(mBluetoothGatt);
					byte[] data = characteristic.getValue();
					String[] formatDatas = Utils.formatData(data,
							characteristic);
					// StringBuilder stringBuilder = new
					// StringBuilder(formatDatas.length);
					// for (String string : formatDatas)
					// stringBuilder.append(string + " ");
					// LogModule.i("转化后：" + stringBuilder.toString());
					// 获取总记录数
					int header = Integer.valueOf(Utils
							.decodeToString(formatDatas[0]));
					if (header == BTConstants.HEADER_BACK_ACK) {
						int ack = Integer.valueOf(Utils
								.decodeToString(formatDatas[1]));
						Intent intent = new Intent(BTConstants.ACTION_ACK);
						intent.putExtra(BTConstants.EXTRA_KEY_ACK_VALUE, ack);
						BTService.this.sendBroadcast(intent);
						return;
					}
					if (header == BTConstants.HEADER_BACK_RECORD) {
						// count = 0;
						// int stepRecord = Integer.valueOf(formatDatas[1]);
						// int sleepRecord = Integer.valueOf(formatDatas[2]);
						// 保存电量
						int battery = Integer.valueOf(Utils
								.decodeToString(formatDatas[3]));
						// count = stepRecord;
						// LogModule.i("手环中的记录总数为：" + count);
						Intent intent = new Intent(
								BTConstants.ACTION_REFRESH_DATA_BATTERY);
						intent.putExtra(BTConstants.EXTRA_KEY_BATTERY_VALUE,
								battery);
						sendBroadcast(intent);
						return;
					}
					if (header == BTConstants.HEADER_BACK_SLEEP_INDEX) {
						Intent intent = new Intent(
								BTConstants.ACTION_REFRESH_DATA_SLEEP_INDEX);
						sendBroadcast(intent);
						return;

					}
					if (header == BTConstants.HEADER_BACK_SLEEP_RECORD) {
						Intent intent = new Intent(
								BTConstants.ACTION_REFRESH_DATA_SLEEP_RECORD);
						sendBroadcast(intent);
						return;

					}

					// count--;
					BTModule.saveBleData(formatDatas, getApplicationContext());
					// LogModule.i(count + "...");
					// if (count == 0) {
					// LogModule.i("延迟1s发送广播更新数据");
					// mHandler.postDelayed(new Runnable() {
					// @Override
					// public void run() {
					// Intent intent = new
					// Intent(BTConstants.ACTION_REFRESH_DATA);
					// sendBroadcast(intent);
					// }
					// }, 1000);
					// }
				};
			};
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mBluetoothGatt = device.connectGatt(BTService.this, false,
							mGattCallback);
				}
			}, 1000);

		}
	}

	/**
	 * 断开手环
	 */
	public void disConnectBle() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			BTModule.mNotifyCharacteristic = null;
			mBluetoothGatt = null;
		}
	}

	@Override
	public void onLeScan(final BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		if (device != null) {
			if (Utils.isEmpty(device.getName())) {
				return;
			}
			BleDevice bleDevice = new BleDevice();
			bleDevice.name = device.getName();
			bleDevice.address = device.getAddress();
			bleDevice.rssi = rssi;
			Intent intent = new Intent(BTConstants.ACTION_BLE_DEVICES_DATA);
			intent.putExtra("device", bleDevice);
			sendBroadcast(intent);
			// mDevices.add(bleDevice);
		}
	}

	/**
	 * 同步时间
	 */
	public void synTimeData() {
		BTModule.setCurrentTime(mBluetoothGatt);
	}

	/**
	 * 初始化触摸按键
	 */
	public void synTouchButton() {
		BTModule.setTouchButton(mBluetoothGatt);
	}

	/**
	 * 同步用户数据
	 */
	public void synUserInfoData() {
		BTModule.setUserInfo(mBluetoothGatt);
	}

	/**
	 * 同步闹钟
	 */
	public void synAlarmData() {
		BTModule.setAlarm(this, mBluetoothGatt);
	}

	/**
	 * 同步睡眠时间
	 */
	public void synSleepTime() {
		BTModule.setSleep(this, mBluetoothGatt);
	}

	/**
	 * 获取手环记步
	 */
	public void getStepData() {
		BTModule.getStepData(mBluetoothGatt);
	}

	/**
	 * 获取手环睡眠指数
	 */
	public void getSleepIndex() {
		BTModule.getSleepIndex(mBluetoothGatt);
	}

	/**
	 * 获取手环睡眠记录
	 */
	public void getSleepRecord() {
		BTModule.getSleepRecord(mBluetoothGatt);
	}

	/**
	 * 获取手环电量数据
	 */
	public void getBatteryData() {
		BTModule.getBatteryData(mBluetoothGatt);
	}

	/**
	 * 清空手环数据
	 */
	public void clearData() {
		BTModule.clearData(mBluetoothGatt);
	}

	/**
	 * 寻找手环
	 */
	public void shakeFindBand() {
		BTModule.shakeFindBand(mBluetoothGatt);
	}

	/**
	 * 是否连接手环
	 * 
	 * @return
	 */
	public boolean isConnDevice() {
		BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext()
				.getSystemService(Context.BLUETOOTH_SERVICE);
		String address = SPUtiles.getStringValue(
				BTConstants.SP_KEY_DEVICE_ADDRESS, null);
		if (address == null) {
			return false;
		}
		int connState = bluetoothManager.getConnectionState(
				BTModule.mBluetoothAdapter.getRemoteDevice(address),
				BluetoothProfile.GATT);
		if (connState == BluetoothProfile.STATE_CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BTConstants.ACTION_PHONE_STATE)) {
				// 如果是来电
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);

				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:
					// 来电
					String incoming_number = intent
							.getStringExtra("incoming_number");
					LogModule.d("来电号码:" + incoming_number);
					// log:来电号码:18801283616
					if (isConnDevice()
							&& SPUtiles
									.getBooleanValue(
											BTConstants.SP_KEY_COMING_PHONE_ALERT,
											true)) {
						if (SPUtiles
								.getBooleanValue(
										BTConstants.SP_KEY_COMING_PHONE_NODISTURB_ALERT,
										false)) {
							// SimpleDateFormat sdf = new
							// SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD_HH_MM);
							String startTime = SPUtiles
									.getStringValue(
											BTConstants.SP_KEY_COMING_PHONE_NODISTURB_START_TIME,
											"00:00");
							String endTime = SPUtiles
									.getStringValue(
											BTConstants.SP_KEY_COMING_PHONE_NODISTURB_END_TIME,
											"00:00");
							Calendar startCalendar = Calendar.getInstance();
							startCalendar.set(Calendar.HOUR_OF_DAY,
									Integer.valueOf(startTime.split(":")[0]));
							startCalendar.set(Calendar.MINUTE,
									Integer.valueOf(startTime.split(":")[1]));
							startCalendar.set(Calendar.SECOND, 0);

							Calendar endCalendar = Calendar.getInstance();
							endCalendar.set(Calendar.HOUR_OF_DAY,
									Integer.valueOf(endTime.split(":")[0]));
							endCalendar.set(Calendar.MINUTE,
									Integer.valueOf(endTime.split(":")[1]));
							endCalendar.set(Calendar.SECOND, 0);

							if (startCalendar.equals(endCalendar)) {
								LogModule.d("勿扰时段开始结束相同...");
								isAllowConstants(incoming_number);
								return;
							}

							Calendar current = Calendar.getInstance();
							if (startCalendar.after(endCalendar)) {
								endCalendar.add(Calendar.DAY_OF_MONTH, 1);
							}
							if (current.after(startCalendar)
									&& current.before(endCalendar)) {
								LogModule.d("勿扰时段内不震动...");
								return;
							}
							isAllowConstants(incoming_number);
						} else {
							isAllowConstants(incoming_number);
						}
					}
					break;
				}
			}
			if (intent.getAction().equals(BTConstants.ACTION_SMS_RECEIVED)) {
				// Object[] pduses = (Object[]) intent.getExtras().get("pdus");
				// for (Object pdus : pduses) {
				// byte[] pdusmessage = (byte[]) pdus;
				// SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
				// String mobile = sms.getOriginatingAddress();// 发送短信的手机号码
				// LogModule.d("来短信号码:" + mobile);
				// // log:来短信号码:+8618801283616
				//
				// }
				if (isConnDevice()) {
					BTModule.smsComingShakeBand(mBluetoothGatt);
				}
			}
		}

	};

	/**
	 * 是否允许联系人来电提醒
	 * 
	 * @param incoming_number
	 */
	private void isAllowConstants(String incoming_number) {
		if (SPUtiles.getBooleanValue(
				BTConstants.SP_KEY_COMING_PHONE_CONTACTS_ALERT, false)) {
			if (Utils.isNotEmpty(incoming_number)
					&& getPhoneContacts(incoming_number)) {
				BTModule.phoneComingShakeBand(mBluetoothGatt);
			}
		} else {
			BTModule.phoneComingShakeBand(mBluetoothGatt);
		}
	}

	/** 电话号码 **/
	private static final int PHONES_NUMBER_INDEX = 1;
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };

	/**
	 * 得到手机通讯录联系人信息
	 * 
	 * @param incoming_number
	 **/
	private boolean getPhoneContacts(String incoming_number) {
		ContentResolver resolver = getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (Utils.isEmpty(phoneNumber))
					continue;
				if (incoming_number.equals(phoneNumber)) {
					return true;
				} else {
					continue;
				}
			}
			phoneCursor.close();
		}
		return false;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogModule.d("解绑BTService...onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		LogModule.d("销毁BTService...onDestroy");
		disConnectBle();
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	public class LocalBinder extends Binder {
		public BTService getService() {
			return BTService.this;
		}
	}

	public void connectGatt() {
		String address = SPUtiles.getStringValue(
				BTConstants.SP_KEY_DEVICE_ADDRESS, null);
		if (address == null) {
			return;
		}
		BluetoothDevice device = BTModule.mBluetoothAdapter
				.getRemoteDevice(SPUtiles.getStringValue(
						BTConstants.SP_KEY_DEVICE_ADDRESS, null));
		if (device != null) {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt.disconnect();
				mBluetoothGatt = null;
			}
			mBluetoothGatt = device.connectGatt(BTService.this, false,
					mGattCallback);
		} else {
			LogModule
					.d("the bluetoothDevice is null, please reset the bluetoothDevice");
		}
	}

	Runnable runnableReconnect = new Runnable() {

		@Override
		public void run() {
			isReconnect = true;
			if (!isConnDevice()) {
				LogModule.d("重连中...");
				mHandler.postDelayed(this, 10 * 1000);
				if (BTModule.isBluetoothOpen()) {
					connectGatt();
				} else {
					LogModule.d("蓝牙未开启...");
				}
			} else {
				isReconnect = false;
				LogModule.d("设备已连接...");
			}
		}
	};
}
