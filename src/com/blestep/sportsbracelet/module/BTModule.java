package com.blestep.sportsbracelet.module;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import com.blestep.sportsbracelet.AppConstants;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

public class BTModule {
	public static BluetoothAdapter mBluetoothAdapter;
	public static BluetoothGattCharacteristic mNotifyCharacteristic;
	public static final int REQUEST_ENABLE_BT = 1001;

	public static final String BARCELET_BT_NAME = "J-Band";
	public static final UUID SERVIE_UUID = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
	public static final UUID CHARACTERISTIC_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	/**
	 * Write, APP send command to wristbands using this characteristic
	 */
	public static final UUID CHARACTERISTIC_UUID_WRITE = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
	/**
	 * Notify, wristbands send data to APP using this characteristic
	 */
	public static final UUID CHARACTERISTIC_UUID_NOTIFY = UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");

	/**
	 * 
	 * @return
	 */
	public static boolean isBluetoothOpen() {
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			return false;
		}
		return true;
	}

	/**
	 * 打开蓝牙
	 * 
	 * @param context
	 */
	public static void openBluetooth(Context context) {
		// Ensures Bluetooth is available on the device and it is enabled. If
		// not,
		// displays a dialog requesting user permission to enable Bluetooth.
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}

	public BTModule() {
	}

	/**
	 * 搜索手环
	 */
	public static void scanDevice(LeScanCallback mLeScanCallback) {
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	/**
	 * 设置手环当前时间
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void setCurrentTime(BluetoothGatt mBluetoothGatt) {
		// 取得手机当前时间，并设置到手环上
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int date = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		byte[] byteArray = new byte[7];
		byteArray[0] = 0x11;
		byteArray[1] = (byte) (year - 2000);
		byteArray[2] = (byte) month;
		byteArray[3] = (byte) date;
		byteArray[4] = (byte) hour;
		byteArray[5] = (byte) minute;
		byteArray[6] = (byte) second;
		writeCharacteristicData(mBluetoothGatt, byteArray);

	}

	/**
	 * 设置用户信息
	 * 
	 * @param mBluetoothGatt
	 */
	public static void setUserInfo(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[5];
		int weight = SPUtiles.getIntValue(SPUtiles.SP_KEY_USER_WEIGHT, 30);
		int height = SPUtiles.getIntValue(SPUtiles.SP_KEY_USER_HEIGHT, 100);
		int age = SPUtiles.getIntValue(SPUtiles.SP_KEY_USER_AGE, 5);
		int gender = SPUtiles.getIntValue(SPUtiles.SP_KEY_USER_WEIGHT, 0);
		byteArray[0] = 0x12;
		byteArray[1] = (byte) weight;
		byteArray[2] = (byte) height;
		byteArray[3] = (byte) age;
		byteArray[4] = (byte) gender;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 获取当前电量
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void getBatteryData(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[2];
		byteArray[0] = 0x16;
		byteArray[1] = 0x00;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 获取步数
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void getStepData(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[2];
		byteArray[0] = 0x16;
		byteArray[1] = 0x01;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 清空手环数据
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void clearData(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[1];
		byteArray[0] = 0x15;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 获取睡眠记录
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void getSleepData(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[2];
		byteArray[0] = 0x16;
		byteArray[1] = 0x02;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 震动
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void shakeBand(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[5];
		byteArray[0] = 0x17;
		byteArray[1] = 0x02;
		byteArray[2] = 0x02;
		byteArray[3] = 0x01;
		byteArray[4] = 0x01;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 将所有手环特征设置为notify方式
	 * 
	 * @param mBluetoothGatt
	 */
	public static void setCharacteristicNotify(BluetoothGatt mBluetoothGatt) {
		List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
		if (gattServices == null)
			return;
		String uuid = null;
		// 遍历所有服务，找到手环的服务
		for (BluetoothGattService gattService : gattServices) {
			uuid = gattService.getUuid().toString();
			if (uuid.startsWith("0000ffc0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				// 遍历所有特征，找到发出的特征
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					uuid = gattCharacteristic.getUuid().toString();
					if (uuid.startsWith("0000ffc2")) {
						int charaProp = gattCharacteristic.getProperties();
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

							if (mNotifyCharacteristic != null) {
								setCharacteristicNotification(mBluetoothGatt, mNotifyCharacteristic, false);
								mNotifyCharacteristic = null;
							}
							mBluetoothGatt.readCharacteristic(gattCharacteristic);
						}
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mNotifyCharacteristic = gattCharacteristic;
							setCharacteristicNotification(mBluetoothGatt, gattCharacteristic, true);
						}
					}
				}
			}
		}
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public static void setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		/**
		 * 打开数据FFF4
		 */
		// This is specific to Heart Rate Measurement.
		if (CHARACTERISTIC_UUID_NOTIFY.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public static void writeCharacteristicData(BluetoothGatt mBluetoothGatt, byte[] byteArray) {
		BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);
		LogModule.i("writeCharacteristicData...service:" + service);
		if (service == null) {
			return;
		}
		BluetoothGattCharacteristic characteristic = null;
		characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
		LogModule.i("writeCharacteristicData...characteristic:" + characteristic);
		if (characteristic == null) {
			return;
		}
		characteristic.setValue(byteArray);
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * 根据不同命令头保存数据
	 * 
	 * @param formatDatas
	 * @param context
	 * @param index
	 */
	public static void saveBleData(String[] formatDatas, Context context, int index) {
		int header = Integer.valueOf(formatDatas[0]);
		switch (header) {
		case AppConstants.HEADER_BACK_STEP:
			// 保存步数
			// 日期
			String year = formatDatas[2];
			String month = formatDatas[3];
			String day = formatDatas[4];
			Calendar calendar = Calendar.getInstance();
			calendar.set(2000 + Integer.valueOf(year), Integer.valueOf(month) - 1, Integer.valueOf(day));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = calendar.getTime();
			// 步数
			String step3 = Utils.decodeToHex(formatDatas[5]);
			String step2 = Utils.decodeToHex(formatDatas[6]);
			String step1 = Utils.decodeToHex(formatDatas[7]);
			String step0 = Utils.decodeToHex(formatDatas[8]);
			StringBuilder sb = new StringBuilder();
			sb.append(step3).append(step2).append(step1).append(step0);
			// 时长
			String duration1 = Utils.decodeToHex(formatDatas[9]);
			String duration0 = Utils.decodeToHex(formatDatas[10]);
			// 距离
			String distance1 = Utils.decodeToHex(formatDatas[11]);
			String distance0 = Utils.decodeToHex(formatDatas[12]);
			// 卡路里
			String calories1 = Utils.decodeToHex(formatDatas[13]);
			String calories0 = Utils.decodeToHex(formatDatas[14]);
			String dateStr = sdf.format(date);
			LogModule.e("日期：" + dateStr);

			String count = Utils.decodeToString(sb.toString());
			LogModule.e("步数：" + count);

			String duration = Utils.decodeToString(duration1 + duration0);
			LogModule.e("时长：" + duration);

			String distance = new DecimalFormat()
					.format(Integer.valueOf(Utils.decodeToString(distance1 + distance0)) * 0.01);
			LogModule.e("距离：" + distance);

			String calories = Utils.decodeToString(calories1 + calories0);
			LogModule.e("卡路里：" + Utils.decodeToString(calories1 + calories0));

			Step step = new Step();
			step.date = dateStr;
			step.count = count;
			step.duration = duration;
			step.distance = distance;
			step.calories = calories;
			if (!DBTools.getInstance(context).isStepExist(step.date)) {
				DBTools.getInstance(context).insertStep(step);
				// 更新最新记录
			} else if (index == 0) {
				DBTools.getInstance(context).updateStep(step);
			}
			break;

		default:
			break;
		}
	}
}
