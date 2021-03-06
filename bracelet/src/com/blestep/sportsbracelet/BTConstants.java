package com.blestep.sportsbracelet;

public class BTConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    // action
    /**
     * 广播action
     */
    // 搜索到的设备信息数据
    public static final String ACTION_BLE_DEVICES_DATA = "action_ble_devices_data";
    public static final String ACTION_BLE_DEVICES_DATA_END = "action_ble_devices_data_end";
    // 发现状态
    public static final String ACTION_DISCOVER_SUCCESS = "action_discover_success";
    public static final String ACTION_DISCOVER_FAILURE = "action_discover_failure";
    // 断开连接
    public static final String ACTION_CONN_STATUS_DISCONNECTED = "action_conn_status_success";
    // 刷新数据
    public static final String ACTION_REFRESH_DATA = "action_refresh_data";
    // 刷新电量数据
    public static final String ACTION_REFRESH_DATA_BATTERY = "action_refresh_data_battery";
    // 手环应答
    public static final String ACTION_ACK = "action_ack";
    // 刷新版本号
    public static final String ACTION_REFRESH_DATA_VERSION = "action_refresh_data_version";
    // 连接超时
    public static final String ACTION_CONN_STATUS_TIMEOUT = "action_conn_status_timeout";
    // 刷新进度
    public static final String ACTION_REFRESH_PROGRESS = "action_refresh_progress";
    // 同步成功
    public static final String ACTION_SYNC_SUCCESS = "action_sync_success";
    // 创建页面
    public static final String ACTION_CREATE_VIEW = "action_create_view";

    public static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_FINISH_ACTIVITY = "action.finishActivity";
    // sp
    public static final String SP_NAME = "sp_name_sportsbracelet";
    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
    public static final String SP_KEY_DEVICE_NAME = "sp_key_device_NAME";
    public static final String SP_KEY_BATTERY = "sp_key_battery";
    public static final String SP_KEY_VERSION = "sp_key_version";
    public static final String SP_KEY_STEP_AIM = "sp_key_aim";
    public static final String SP_KEY_STEP_AIM_POINT_X = "sp_key_aim_point_x";
    public static final String SP_KEY_STEP_AIM_POINT_Y = "sp_key_aim_point_y";
    public static final String SP_KEY_STEP_AIM_CALORIE = "sp_key_aim_calorie";
    public static final String SP_KEY_STEP_AIM_STATE = "sp_key_aim_state";
    public static final String SP_KEY_STEP_AIM_CALORIE_WALK = "sp_key_aim_calorie_walk";
    public static final String SP_KEY_STEP_AIM_CALORIE_RUN = "sp_key_aim_calorie_run";
    public static final String SP_KEY_STEP_AIM_CALORIE_BIKE = "sp_key_aim_calorie_bike";
    public static final String SP_KEY_USER_NAME = "sp_key_name";
    public static final String SP_KEY_USER_GENDER = "sp_key_gender";
    public static final String SP_KEY_USER_AGE = "sp_key_age";
    public static final String SP_KEY_USER_BIRTHDAT = "sp_key_birthday";
    public static final String SP_KEY_USER_HEIGHT = "sp_key_height";
    public static final String SP_KEY_USER_WEIGHT = "sp_key_weight";
    public static final String SP_KEY_IS_FIRST_OPEN = "sp_key_is_first_open";
    public static final String SP_KEY_COMING_PHONE_ALERT = "sp_key_coming_phone_alert";
    public static final String SP_KEY_COMING_PHONE_CONTACTS_ALERT = "sp_key_coming_phone_contacts_alert";
    public static final String SP_KEY_COMING_PHONE_NODISTURB_ALERT = "sp_key_coming_phone_nodisturb_alert";
    public static final String SP_KEY_COMING_PHONE_NODISTURB_START_TIME = "sp_key_coming_phone_nodisturb_start_time";
    public static final String SP_KEY_COMING_PHONE_NODISTURB_END_TIME = "sp_key_coming_phone_nodisturb_end_time";
    public static final String SP_KEY_TOUCHBUTTON = "sp_key_touchbutton";
    public static final String SP_KEY_IS_BRITISH_UNIT = "sp_key_is_british_unit";
    public static final String SP_KEY_TIME_SYSTEM = "sp_key_time_system";
    public static final String SP_KEY_LIGHT_SYSTEM = "sp_key_light_system";
    public static final String SP_KEY_ALARM_SYNC_FINISH = "sp_key_alarm_sync_finish";
    public static final String SP_KEY_HEART_RATE_SHOW = "sp_key_heart_rate_show";
    public static final String SP_KEY_HEART_RATE_INTERVAL = "sp_key_heart_rate_interval";
    public static final String SP_KEY_CURRENT_SYNC_DATE = "sp_key_current_sync_date";
    public static final String SP_KEY_IS_OLD = "sp_key_is_old";
    // Extra_key
    /**
     * intent传值key
     */
    // 设备列表
    public static final String EXTRA_KEY_DEVICES = "devices";
    public static final String EXTRA_KEY_ALARM = "extra_key_alarm";
    public static final String EXTRA_KEY_HISTORY = "extra_key_history";
    public static final String EXTRA_KEY_ACK_VALUE = "extra_key_ack_value";
    public static final String EXTRA_KEY_BATTERY_VALUE = "extra_key_battery_value";
    public static final String EXTRA_KEY_VERSION_VALUE = "extra_key_version_value";
    public static final String EXTRA_KEY_BACK_HEADER = "extra_key_back_header";
    // request_code
    public static final int REQUEST_CODE_SYSTEM = 101;
    public static final int REQUEST_CODE_ALARM = 102;
    public static final int REQUEST_CODE_MATCH = 103;
    public static final int REQUEST_CODE_HISTORY = 104;
    public static final int REQUEST_CODE_USERINFO = 105;
    public static final int REQUEST_CODE_TARGET = 106;
    public static final int REQUEST_CODE_CLEAR = 107;
    public static final int REQUEST_CODE_HEART_RATE_EXPLAIN = 108;
    public static final int REQUEST_CODE_HEART_RATE_INTERVAL = 109;
    public static final int REQUEST_CODE_HEART_RATE_DAILY = 110;
    public static final int REQUEST_CODE_ABOUT = 111;
    public static final int REQUEST_CODE_PHONE_ALERT = 112;

    /**
     * 返回数据header
     */
    // 固件版本号
    public static final int HEADER_FIRMWARE_VERSION = 144;
    // 存储状态及电量
    public static final int HEADER_BACK_RECORD = 145;
    // 记步记录
    public static final int HEADER_BACK_STEP = 146;
    // 睡眠指数
    public static final int HEADER_BACK_SLEEP_INDEX = 147;
    // 睡眠记录
    public static final int HEADER_BACK_SLEEP_RECORD = 148;
    // ACK
    public static final int HEADER_BACK_ACK = 150;
    // 设置成功
    public static final int HEADER_BACK_SUCCESS = 165;
    // 设置错误
    public static final int HEADER_BACK_ERROR = 164;
    // 心率记录
    public static final int HEADER_HEART_RATE = 168;
    // 获取内部版本号
    public static final int HEADER_BACK_INSIDE_VERSION = 9;
    // 同步时间
    public static final int HEADER_SYNTIMEDATA = 17;
    // 同步用户数据
    public static final int HEADER_SYNUSERINFO = 18;
    // 获取睡眠index
    public static final int TYPE_GET_SLEEP_INDEX = 2;
    // 获取睡眠record
    public static final int TYPE_GET_SLEEP_RECORD = 3;
    // 获取总数据
    public static final int TYPE_GET_COUNT = 18;
    // 获取当天
    public static final int TYPE_GET_CURRENT = 19;
    // 获取数据
    public static final int HEADER_GETDATA = 22;
    // 心率间隔
    public static final int TYPE_SET_HEART_RATE_INTERVAL = 23;
    // 读取心率
    public static final int TYPE_GET_HEART_RATE = 24;
    // 单位制式
    public static final int HEADER_UNIT_SYSTEM = 35;
    // 时间格式
    public static final int HEADER_TIME_SYSTEM = 36;
    // 自动亮屏幕
    public static final int HEADER_LIGHT_SYSTEM = 37;
    // 新同步闹钟
    public static final int HEADER_SYNALARM_NEW = 38;
}
