package com.blestep.sportsbracelet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.blestep.sportsbracelet.module.LogModule;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_name";
    // 数据库版本号
    private static final int DB_VERSION = 1;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STEP);
        db.execSQL(CREATE_TABLE_ALARM);
        db.execSQL(CREATE_TABLE_SLEEP);
        LogModule.i("创建数据库");
    }

    /**
     * 升级时数据库时调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * 删除数据库
     *
     * @param context
     * @return
     */
    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(DB_NAME);
    }

    // 步数表
    private static final String CREATE_TABLE_STEP = "CREATE TABLE "
            + DBConstants.TABLE_NAME_STEP
            // id
            + " (" + DBConstants.STEP_FIELD_ID
            + " INTEGER primary key autoincrement, "
            // 日期
            + DBConstants.STEP_FIELD_DATE + " TEXT,"
            // 总数
            + DBConstants.STEP_FIELD_COUNT + " TEXT,"
            // 时长
            + DBConstants.STEP_FIELD_DURATION + " TEXT,"
            // 距离
            + DBConstants.STEP_FIELD_DISTANCE + " TEXT,"
            // 卡路里
            + DBConstants.STEP_FIELD_CALORIES + " TEXT);";
    // 闹钟表
    private static final String CREATE_TABLE_ALARM = "CREATE TABLE "
            + DBConstants.TABLE_NAME_ALARM
            // id
            + " (" + DBConstants.ALARM_FIELD_ID
            + " INTEGER primary key autoincrement, "
            // 名称
            + DBConstants.ALARM_FIELD_NAME + " TEXT,"
            // 时间
            + DBConstants.ALARM_FIELD_TIME + " TEXT,"
            // 类型
            + DBConstants.ALARM_FIELD_TYPE + " TEXT,"
            // 状态
            + DBConstants.ALARM_FIELD_STATE + " TEXT);";
    // 睡眠表
    private static final String CREATE_TABLE_SLEEP = "CREATE TABLE "
            + DBConstants.TABLE_NAME_SLEEP
            // id
            + " (" + DBConstants.SLEEP_FIELD_ID
            + " INTEGER primary key autoincrement, "
            // 日期
            + DBConstants.SLEEP_FIELD_DATE + " TEXT,"
            // 开始时间
            + DBConstants.SLEEP_FIELD_START + " TEXT,"
            // 结束时间
            + DBConstants.SLEEP_FIELD_END + " TEXT,"
            // 深睡时间
            + DBConstants.SLEEP_FIELD_DEEP + " TEXT,"
            // 浅睡时间
            + DBConstants.SLEEP_FIELD_LIGHT + " TEXT,"
            // 清醒时间
            + DBConstants.SLEEP_FIELD_AWAKE + " TEXT,"
            // 睡眠记录
            + DBConstants.SLEEP_FIELD_RECORD + " TEXT);";
}
