package com.blestep.sportsbracelet.service;

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
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BTService extends Service implements LeScanCallback {
    private boolean mIsStartScan = false;
    private static final long SCAN_PERIOD = 5000;
    private static final int GATT_ERROR_TIMEOUT = 133;

    public static Handler mHandler;
    // private ArrayList<BleDevice> mDevices;
    public BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mGattCallback;
    private boolean mIsReconnect = false;
    private volatile boolean mIsAutoDisConnect = false;
    private static final Object LOCK = new Object();

    @Override
    public void onCreate() {
        mHandler = new Handler(getApplication().getMainLooper());
        LogModule.i("创建BTService...onCreate");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTConstants.ACTION_PHONE_STATE);
        filter.addAction(BTConstants.ACTION_SMS_RECEIVED);
        registerReceiver(mReceiver, filter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.i("启动BTService...onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogModule.i("绑定BTService...onBind");
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
            LogModule.i(SCAN_PERIOD / 1000 + "s后停止扫描");
            mIsStartScan = true;
            BTModule.scanDevice(this);
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsStartScan) {
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
        LogModule.i("开始连接手环：" + address);
        if (TextUtils.isEmpty(address)) {
            return;
        }
        final BluetoothDevice device = BTModule.mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            return;
        } else {
            if (mBluetoothGatt != null) {
                synchronized (LOCK) {
                    mIsAutoDisConnect = true;
                    BTModule.mNotifyCharacteristic = null;
                }
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            mGattCallback = new BluetoothGattCallback() {
                private int stepsCount;

                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    LogModule.e("onConnectionStateChange...status:" + status
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
                            if (mIsAutoDisConnect) {
                                return;
                            }
                            disConnectBle();
                            Intent intent = new Intent(
                                    BTConstants.ACTION_CONN_STATUS_DISCONNECTED);
                            sendOrderedBroadcast(intent, null);
                            // 2016/7/9 当来电提醒打开时才启动重连机制
                            if (SPUtiles.getBooleanValue(BTConstants.SP_KEY_COMING_PHONE_ALERT, false) && !mIsReconnect) {
                                LogModule.i("开始重连...");
                                new Thread(runnableReconnect).start();
                            }
                            break;
                    }
                }

                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    LogModule.e("onServicesDiscovered...status:" + status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        synchronized (LOCK) {
                            mIsAutoDisConnect = false;
                        }
                        BTModule.setCharacteristicNotify(mBluetoothGatt);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(
                                        BTConstants.ACTION_DISCOVER_SUCCESS);
                                sendOrderedBroadcast(intent, null);
                            }
                        }, 1000);
                    } else {
                        Intent intent = new Intent(
                                BTConstants.ACTION_DISCOVER_FAILURE);
                        sendBroadcast(intent);
                    }
                }

                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    // LogModule.i("onCharacteristicRead...");
                }

                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    // LogModule.i("onCharacteristicWrite...");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        LogModule.i("onCharacteristicWrite...success");
                    } else {
                        LogModule.i("onCharacteristicWrite...failure");
                    }
                }

                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    // LogModule.i("onCharacteristicChanged...");
                    // BTModule.setCharacteristicNotify(mBluetoothGatt);
                    byte[] data = characteristic.getValue();
                    if (data == null || data.length == 0) {
                        return;
                    }
                    String[] formatDatas = Utils.formatData(data, characteristic);
                    // StringBuilder stringBuilder = new
                    // StringBuilder(formatDatas.length);
                    // for (String string : formatDatas)
                    // stringBuilder.append(string + " ");
                    // LogModule.i("转化后：" + stringBuilder.toString());
                    // 获取总记录数
                    int header = Integer.parseInt(Utils.decodeToString(formatDatas[0]));
                    if (header == BTConstants.HEADER_BACK_ACK) {
                        int ack = Integer.parseInt(Utils.decodeToString(formatDatas[1]));
                        Intent intent = new Intent(BTConstants.ACTION_ACK);
                        intent.putExtra(BTConstants.EXTRA_KEY_ACK_VALUE, ack);
                        BTService.this.sendBroadcast(intent);
                        return;
                    }
                    if (header == BTConstants.HEADER_BACK_RECORD) {
                        stepsCount = 0;
                        // 记步总数、睡眠指数总数、睡眠记录总数、电量
                        int stepRecord = Integer.parseInt(Utils.decodeToString(formatDatas[1]));
                        String sleepIndexAndrRecord = Utils.hexString2binaryString(Utils.decodeToString(formatDatas[2]));
                        if (sleepIndexAndrRecord.length() >= 8) {
                            int sleepIndex = Integer.parseInt(sleepIndexAndrRecord.substring(0, 4), 2);
                            int sleepRecord = Integer.parseInt(sleepIndexAndrRecord.substring(4, 8), 2);
                            LogModule.i("手环中的睡眠总数为：" + sleepIndex);
                            LogModule.i("手环中的睡眠记录总数为：" + sleepRecord);
                        }
                        int battery = Integer.parseInt(Utils.decodeToString(formatDatas[3]));
                        stepsCount = stepRecord;
                        LogModule.i("手环中的记步总数为：" + stepsCount);
                        Intent intent = new Intent(BTConstants.ACTION_REFRESH_DATA_BATTERY);
                        intent.putExtra(BTConstants.EXTRA_KEY_BATTERY_VALUE, battery);
                        sendBroadcast(intent);
                        return;
                    }
                    if (header == BTConstants.HEADER_FIRMWARE_VERSION) {
                        int major = Integer.parseInt(Utils
                                .decodeToString(formatDatas[1]));
                        int minor = Integer.parseInt(Utils
                                .decodeToString(formatDatas[2]));
                        int revision = Integer.parseInt(Utils
                                .decodeToString(formatDatas[3]));
                        Intent intent = new Intent(BTConstants.ACTION_REFRESH_DATA_VERSION);
                        intent.putExtra(BTConstants.EXTRA_KEY_VERSION_VALUE, String.format("%s.%s.%s", major, minor, revision));
                        BTService.this.sendBroadcast(intent);
                        return;
                    }
                    if (header == BTConstants.HEADER_BACK_STEP) {
                        BTModule.saveStepData(formatDatas, getApplicationContext());
                        stepsCount--;
                    }
                    if (header == BTConstants.HEADER_BACK_SLEEP_INDEX) {
                        BTModule.saveSleepIndex(formatDatas, getApplicationContext());
                    }
                    if (stepsCount <= 0) {
                        LogModule.i("延迟1s发送广播更新数据");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new
                                        Intent(BTConstants.ACTION_REFRESH_DATA);
                                sendBroadcast(intent);
                            }
                        }, 1000);
                    } else {
                        LogModule.i("还有" + stepsCount + "条记步数据未同步");
                    }
                }
            };
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt = device.connectGatt(BTService.this, false, mGattCallback);
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
    public void syncTimeData() {
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
    public void syncUserInfoData() {
        BTModule.setUserInfo(mBluetoothGatt);
    }

    /**
     * 同步闹钟
     */
    public void syncAlarmData() {
        BTModule.setAlarm(this, mBluetoothGatt);
    }

    /**
     * 同步单位制式
     */
    public void syncUnit() {
        BTModule.setUnit(this, mBluetoothGatt);
    }

    /**
     * 同步时间格式
     */
    public void syncTime() {
        BTModule.setTime(this, mBluetoothGatt);
    }

    /**
     * 同步点亮屏幕开关
     */
    public void syncLight() {
        BTModule.setLight(this, mBluetoothGatt);
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
     * 获取手环固件版本号
     */
    public void getVersionData() {
        BTModule.getVersionData(mBluetoothGatt);
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
                        LogModule.i("来电号码:" + incoming_number);
                        // log:来电号码:18801283616
                        if (!TextUtils.isEmpty(incoming_number) && isConnDevice() && SPUtiles.getBooleanValue(BTConstants.SP_KEY_COMING_PHONE_ALERT, false)) {
                            // ToastUtils.showToast(context, "phone number:" + incoming_number);
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
                                        Integer.parseInt(startTime.split(":")[0]));
                                startCalendar.set(Calendar.MINUTE,
                                        Integer.parseInt(startTime.split(":")[1]));
                                startCalendar.set(Calendar.SECOND, 0);

                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(Calendar.HOUR_OF_DAY,
                                        Integer.parseInt(endTime.split(":")[0]));
                                endCalendar.set(Calendar.MINUTE,
                                        Integer.parseInt(endTime.split(":")[1]));
                                endCalendar.set(Calendar.SECOND, 0);

                                if (startCalendar.equals(endCalendar)) {
                                    LogModule.i("勿扰时段开始结束相同...");
                                    isAllowConstants(incoming_number);
                                    return;
                                }

                                Calendar current = Calendar.getInstance();
                                if (startCalendar.after(endCalendar)) {
                                    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                                }
                                if (current.after(startCalendar)
                                        && current.before(endCalendar)) {
                                    LogModule.i("勿扰时段内不震动...");
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
                Object[] pduses = (Object[]) intent.getExtras().get("pdus");
                String mobile = "";
                for (Object pdus : pduses) {
                    byte[] pdusmessage = (byte[]) pdus;
                    SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
                    mobile = sms.getOriginatingAddress();// 发送短信的手机号码
                    // LogModule.d("来短信号码:" + mobile);
                    // // log:来短信号码:+8618801283616
                    //
                }
                if (isConnDevice()) {
                    String contactName = getPhoneContactsName(mobile);
                    Pattern p = Pattern.compile("[^a-zA-Z0-9 ]");
                    if (TextUtils.isEmpty(contactName)) {
                        Matcher m = p.matcher(mobile);
                        BTModule.smsComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), true);
                    } else {
                        Matcher m = p.matcher(contactName);
                        if (m.find()) {
                            Matcher matcher = p.matcher(mobile);
                            BTModule.phoneComingShakeBand(mBluetoothGatt, matcher.replaceAll("").trim(), true);
                        } else {
                            BTModule.phoneComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), false);
                        }
                    }
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
        String contactName = getPhoneContactsName(incoming_number);
        Pattern p = Pattern.compile("[^a-zA-Z0-9 ]");
        if (SPUtiles.getBooleanValue(
                BTConstants.SP_KEY_COMING_PHONE_CONTACTS_ALERT, false)) {
            if (Utils.isNotEmpty(incoming_number)
                    && !TextUtils.isEmpty(contactName)) {
                if (TextUtils.isEmpty(contactName)) {
                    Matcher m = p.matcher(incoming_number);
                    BTModule.phoneComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), true);
                } else {
                    Matcher m = p.matcher(contactName);
                    if (m.find()) {
                        Matcher matcher = p.matcher(incoming_number);
                        BTModule.phoneComingShakeBand(mBluetoothGatt, matcher.replaceAll("").trim(), true);
                    } else {
                        BTModule.phoneComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), false);
                    }
                }
            }
        } else {
            if (TextUtils.isEmpty(contactName)) {
                Matcher m = p.matcher(incoming_number);
                BTModule.phoneComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), true);
            } else {
                Matcher m = p.matcher(contactName);
                if (m.find()) {
                    Matcher matcher = p.matcher(incoming_number);
                    BTModule.phoneComingShakeBand(mBluetoothGatt, matcher.replaceAll("").trim(), true);
                } else {
                    BTModule.phoneComingShakeBand(mBluetoothGatt, m.replaceAll("").trim(), false);
                }
            }
        }
    }

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final String[] PHONES_PROJECTION = new String[]{
            Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};

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

    /**
     * 得到手机通讯录联系人姓名
     *
     * @param incoming_number
     **/
    private String getPhoneContactsName(String incoming_number) {
        try {
            ContentResolver resolver = getContentResolver();
            // 获取手机联系人
            Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
                    PHONES_PROJECTION, null, null, null);
            String name = null;
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    // 得到手机号码
                    String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;
                    phoneNumber = phoneNumber.replace(" ", "");
                    if (!TextUtils.isEmpty(incoming_number) && incoming_number.equals(phoneNumber)) {
                        //得到联系人名称
                        name = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                        if (Utils.isEmpty(name))
                            continue;
                        break;
                    } else {
                        continue;
                    }
                }
                phoneCursor.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogModule.i("解绑BTService...onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogModule.i("销毁BTService...onDestroy");
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
            mIsReconnect = true;
            if (!isConnDevice()) {
                LogModule.i("重连中...");
                mHandler.postDelayed(this, 10 * 1000);
                if (BTModule.isBluetoothOpen()) {
                    connectGatt();
                } else {
                    LogModule.i("蓝牙未开启...");
                }
            } else {
                mIsReconnect = false;
                LogModule.i("设备已连接...");
            }
        }
    };
}
