package com.blestep.sportsbracelet.module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Alarm;
import com.blestep.sportsbracelet.entity.Sleep;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BTModule {
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothGattCharacteristic mNotifyCharacteristic;
    public static final int REQUEST_ENABLE_BT = 1001;

    public static final String BARCELET_BT_NAME = "J-Band";
    public static final UUID SERVIE_UUID = UUID
            .fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * Write, APP send command to wristbands using this characteristic
     */
    public static final UUID CHARACTERISTIC_UUID_WRITE = UUID
            .fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
    /**
     * Notify, wristbands send data to APP using this characteristic
     */
    public static final UUID CHARACTERISTIC_UUID_NOTIFY = UUID
            .fromString("0000ffc2-0000-1000-8000-00805f9b34fb");

    /**
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
        Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((Activity) context).startActivityForResult(enableBtIntent,
                REQUEST_ENABLE_BT);
    }

    public BTModule() {
    }

    /**
     * 搜索手环
     */
    public static void scanDevice(LeScanCallback mLeScanCallback) {
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        // mBluetoothAdapter.startLeScan(new UUID[] { SERVIE_UUID },
        // mLeScanCallback);
    }

    /**
     * 设置手环当前时间
     *
     * @param mBluetoothGatt
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
        byteArray[0] = (byte) BTConstants.HEADER_SYNTIMEDATA;
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
        byte[] byteArray = new byte[6];
        int weight = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 30);
        int height = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 100);
        int age = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_AGE, 5);
        int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, 0);
        int stride = 75;
        byteArray[0] = (byte) BTConstants.HEADER_SYNUSERINFO;
        byteArray[1] = (byte) weight;
        byteArray[2] = (byte) height;
        byteArray[3] = (byte) age;
        byteArray[4] = (byte) gender;
        byteArray[5] = (byte) stride;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 设置闹钟
     *
     * @param mBluetoothGatt
     */
    public static void setAlarm(Context context, BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[18];
        byteArray[0] = (byte) BTConstants.HEADER_SYNALARM_NEW;
        ArrayList<Alarm> alarms = DBTools.getInstance(context).selectAllAlarm();
        if (!SPUtiles.getBooleanValue(BTConstants.SP_KEY_ALARM_SYNC_FINISH, false)) {
            // 第一组
            byteArray[1] = 0x00;
            for (int i = 0; i < 4; i++) {
                if (alarms.size() > i) {
                    Alarm alarm = alarms.get(i);
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(Integer.parseInt(alarm.type)), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(Utils.binaryString2hexString(alarm.state), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(3), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(Utils.binaryString2hexString("00000000"), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(0), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(0), 16);
                }
            }
            writeCharacteristicData(mBluetoothGatt, byteArray);
        } else {
            // 第二组
            byteArray[1] = 0x01;
            for (int i = 0; i < 4; i++) {
                int index = i + 4;
                if (alarms.size() > index) {
                    Alarm alarm = alarms.get(index);
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(Integer.parseInt(alarm.type)), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(Utils.binaryString2hexString(alarm.state), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(3), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(Utils.binaryString2hexString("00000000"), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(0), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(0), 16);
                }
            }
            writeCharacteristicData(mBluetoothGatt, byteArray);
        }
    }

    /**
     * 设置单位制式
     *
     * @param mBluetoothGatt
     */
    public static void setUnit(Context context, BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_UNIT_SYSTEM;
        boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        byteArray[1] = isBritish ? (byte) 0x01 : (byte) 0x00;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 设置时间格式
     *
     * @param mBluetoothGatt
     */
    public static void setTime(Context context, BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_TIME_SYSTEM;
        int time_system = SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0);
        byteArray[1] = time_system == 0 ? (byte) 0x00 : (byte) 0x01;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 设置时间格式
     *
     * @param mBluetoothGatt
     */
    public static void setLight(Context context, BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_LIGHT_SYSTEM;
        int light_system = SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1);
        byteArray[1] = light_system == 0 ? (byte) 0x00 : (byte) 0x01;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 获取当前电量
     *
     * @param mBluetoothGatt
     */
    public static void getBatteryData(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x00;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 获取当前版本号
     *
     * @param mBluetoothGatt
     */
    public static void getVersionData(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x06;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 获取步数
     *
     * @param mBluetoothGatt
     */
    public static void getStepData(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x01;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 清空手环数据
     *
     * @param mBluetoothGatt
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
     */
    public static void getSleepIndex(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x02;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 获取睡眠数据
     *
     * @param mBluetoothGatt
     */
    public static void getSleepRecord(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x03;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 获取睡眠总数
     *
     * @param mBluetoothGatt
     */
    public static void getSleepCount(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) BTConstants.HEADER_GETDATA;
        byteArray[1] = 0x12;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 震动
     *
     * @param mBluetoothGatt
     */
    public static void shakeBand(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[5];
        byteArray[0] = 0x17;
        byteArray[1] = 0x02;
        byteArray[2] = 0x03;
        byteArray[3] = 0x03;
        byteArray[4] = 0x03;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 寻找手环震动
     *
     * @param mBluetoothGatt
     */
    public static void shakeFindBand(BluetoothGatt mBluetoothGatt) {
        byte[] byteArray = new byte[5];
        byteArray[0] = 0x17;
        byteArray[1] = 0x02;
        byteArray[2] = 0x02;
        byteArray[3] = 0x0A;
        byteArray[4] = 0x0A;
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    /**
     * 来电震动
     *
     * @param mBluetoothGatt
     */
    public static void phoneComingShakeBand(BluetoothGatt mBluetoothGatt, String showText, boolean isPhoneNumber) {
        try {
            LogModule.e("来电显示：" + showText);
            byte[] byteArray = new byte[20];
            byteArray[0] = Integer.valueOf(Integer.toHexString(153), 16).byteValue();
            if (isPhoneNumber) {
                byteArray[1] = 0x01;
            } else {
                byteArray[1] = 0x00;
            }
            byteArray[2] = 0x00;
            byteArray[3] = showText.length() > 16 ? Integer.valueOf(Integer.toHexString(16), 16).byteValue() : Integer.valueOf(Integer.toHexString(showText.length()), 16).byteValue();
            for (int i = 0; i < showText.length() && i < 16; i++) {
                int c = (int) showText.charAt(i);
                byteArray[i + 4] = Integer.valueOf(Integer.toHexString(c), 16).byteValue();
            }
            writeCharacteristicData(mBluetoothGatt, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 来短信震动
     *
     * @param mBluetoothGatt
     */
    public static void smsComingShakeBand(BluetoothGatt mBluetoothGatt, String showText, boolean isPhoneNumber) {
        byte[] byteArray = new byte[20];
        byteArray[0] = Integer.valueOf(Integer.toHexString(154), 16).byteValue();
        if (isPhoneNumber) {
            byteArray[1] = 0x01;
        } else {
            byteArray[1] = 0x00;
        }
        byteArray[2] = 0x00;
        byteArray[3] = showText.length() > 16 ? Integer.valueOf(Integer.toHexString(16), 16).byteValue() : Integer.valueOf(Integer.toHexString(showText.length()), 16).byteValue();
        for (int i = 0; i < showText.length() && i < 16; i++) {
            int c = (int) showText.charAt(i);
            byteArray[i + 4] = Integer.valueOf(Integer.toHexString(c), 16).byteValue();
        }
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
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                        .getCharacteristics();
                // 遍历所有特征，找到发出的特征
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.startsWith("0000ffc2")) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                            if (mNotifyCharacteristic != null) {
                                setCharacteristicNotification(mBluetoothGatt,
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothGatt
                                    .readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = gattCharacteristic;
                            setCharacteristicNotification(mBluetoothGatt,
                                    gattCharacteristic, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public static void setCharacteristicNotification(
            BluetoothGatt mBluetoothGatt,
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
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
            if (descriptor == null) {
                return;
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public static void writeCharacteristicData(BluetoothGatt mBluetoothGatt,
                                               byte[] byteArray) {
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);

        // LogModule.i("writeCharacteristicData...service:" + service);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        // LogModule.i("writeCharacteristicData...characteristic:" + characteristic);
        if (characteristic == null) {
            return;
        }
        characteristic.setValue(byteArray);
        characteristic
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 解析保存更新记步数据
     *
     * @param formatDatas
     * @param context
     */
    public static void saveStepData(String[] formatDatas, Context context) {
        // 保存步数
        // 日期
        String year = formatDatas[2];
        String month = formatDatas[3];
        String day = formatDatas[4];
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000 + Integer.parseInt(Utils.decodeToString(year)),
                Integer.parseInt(Utils.decodeToString(month)) - 1,
                Integer.parseInt(Utils.decodeToString(day)));
        SimpleDateFormat sdf = new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD);
        Date date = calendar.getTime();
        // 步数
        String step3 = formatDatas[5];
        String step2 = formatDatas[6];
        String step1 = formatDatas[7];
        String step0 = formatDatas[8];
        StringBuilder sb = new StringBuilder();
        sb.append(step3).append(step2).append(step1).append(step0);
        // 时长
        String duration1 = formatDatas[9];
        String duration0 = formatDatas[10];
        // 距离
        String distance1 = formatDatas[11];
        String distance0 = formatDatas[12];
        // 卡路里
        String calories1 = formatDatas[13];
        String calories0 = formatDatas[14];
        String dateStr = sdf.format(date);
        LogModule.e("日期：" + dateStr);
        Intent intent = new Intent(BTConstants.ACTION_LOG);
        intent.putExtra("log", "日期：" + dateStr);
        context.sendBroadcast(intent);

        String count = Utils.decodeToString(sb.toString());
        LogModule.e("步数：" + count);
        intent.putExtra("log", "步数：" + count);
        context.sendBroadcast(intent);

        String duration = Utils.decodeToString(duration1 + duration0);
        LogModule.e("时长：" + duration);
        intent.putExtra("log", "时长：" + duration);
        context.sendBroadcast(intent);

        String distance = new DecimalFormat().format(Integer.parseInt(Utils
                .decodeToString(distance1 + distance0)) * 0.1);
        LogModule.e("距离：" + distance);
        intent.putExtra("log", "距离：" + distance);
        context.sendBroadcast(intent);

        String calories = Utils.decodeToString(calories1 + calories0);
        LogModule.e("卡路里：" + Utils.decodeToString(calories1 + calories0));
        intent.putExtra("log",
                "卡路里：" + Utils.decodeToString(calories1 + calories0));
        context.sendBroadcast(intent);

        Step step = new Step();
        step.date = dateStr;
        step.count = count;
        step.duration = duration;
        step.distance = distance;
        step.calories = calories;
        if (!DBTools.getInstance(context).isStepExist(step.date)) {
            DBTools.getInstance(context).insertStep(step);
        } else {
            // 更新全部记录
            DBTools.getInstance(context).updateStep(step);
        }
    }

    /**
     * 解析保存睡眠数据
     *
     * @param formatDatas
     * @param context
     * @param sleepMaps
     */
    public static void saveSleepIndex(String[] formatDatas, Context context, Map<Integer, String> sleepMaps) {
        SimpleDateFormat sdf = new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD_HH_MM);
        Calendar calendar = Calendar.getInstance();
        // 起始时间
        String startYear = formatDatas[2];
        String startMonth = formatDatas[3];
        String startDay = formatDatas[4];
        String startHour = formatDatas[5];
        String startMin = formatDatas[6];
        calendar.set(2000 + Integer.parseInt(Utils.decodeToString(startYear)),
                Integer.parseInt(Utils.decodeToString(startMonth)) - 1,
                Integer.parseInt(Utils.decodeToString(startDay)),
                Integer.parseInt(Utils.decodeToString(startHour)),
                Integer.parseInt(Utils.decodeToString(startMin)));
        Date startDate = calendar.getTime();
        String startDateStr = sdf.format(startDate);
        // 结束时间
        String endYear = formatDatas[7];
        String endMonth = formatDatas[8];
        String endDay = formatDatas[9];
        String endHour = formatDatas[10];
        String endMin = formatDatas[11];
        calendar.set(2000 + Integer.parseInt(Utils.decodeToString(endYear)),
                Integer.parseInt(Utils.decodeToString(endMonth)) - 1,
                Integer.parseInt(Utils.decodeToString(endDay)),
                Integer.parseInt(Utils.decodeToString(endHour)),
                Integer.parseInt(Utils.decodeToString(endMin)));
        Date endDate = calendar.getTime();
        String endDateStr = sdf.format(endDate);
        // 深睡
        String deep1 = formatDatas[12];
        String deep0 = formatDatas[13];
        String deep = Utils.decodeToString(deep1 + deep0);
        // 浅睡
        String light1 = formatDatas[14];
        String light0 = formatDatas[15];
        String light = Utils.decodeToString(light1 + light0);
        // 清醒
        String awake1 = formatDatas[16];
        String awake0 = formatDatas[17];
        String awake = Utils.decodeToString(awake1 + awake0);

        // 记录睡眠日期
        String date = new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD).format(endDate);
        // 暂存睡眠数据，以index为key，以日期为value，方便更新record;
        sleepMaps.put(Integer.valueOf(Utils.decodeToString(formatDatas[1])), date);

        // 构造睡眠数据
        Sleep sleep = new Sleep();
        sleep.date = date;
        sleep.start = startDateStr;
        sleep.end = endDateStr;
        sleep.deep = deep;
        sleep.light = light;
        sleep.awake = awake;
        LogModule.i(sleep.toString());
        if (!DBTools.getInstance(context).isSleepExist(sleep.date)) {
            DBTools.getInstance(context).insertSleep(sleep);
        } else {
            // 更新全部记录
            DBTools.getInstance(context).updateSleep(sleep);
        }
    }

    /**
     * 更新睡眠记录
     *
     * @param formatDatas
     * @param context
     * @param sleepMaps
     */
    public static void updateSleepRecord(String[] formatDatas, Context context, Map<Integer, String> sleepMaps) {
        String date = sleepMaps.get(Integer.valueOf(Utils.decodeToString(formatDatas[1])));
        Sleep sleep = DBTools.getInstance(context).selectSleep(date);
        if (sleep != null) {
            int len = Integer.valueOf(Utils.decodeToString(formatDatas[3]));
            StringBuilder builder = new StringBuilder(sleep.record == null ? "" : sleep.record);
            for (int i = 0; i < len && 4 + i < formatDatas.length; i++) {
                builder.append(formatDatas[4 + i]);
            }
            sleep.record = builder.toString();
            LogModule.i(sleep.toString());
            DBTools.getInstance(context).updateSleep(sleep);
        }
    }
}
