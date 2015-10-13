package com.blestep.sportsbracelet;

public class AppConstants {
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
	// 连接超时
	public static final String ACTION_CONN_STATUS_TIMEOUT = "action_conn_status_timeout";
	/**
	 * intent传值key
	 */
	// 设备列表
	public static final String INTENT_EXTRA_KEY_DEVICES = "devices";
	/**
	 * 返回数据header
	 */
	// 存储状态及电量
	public static final int HEADER_BACK_RECORD = 145;
	// 记步记录
	public static final int HEADER_BACK_STEP = 146;
	// 记步记录
	public static final int HEADER_BACK_ACK = 150;
}
