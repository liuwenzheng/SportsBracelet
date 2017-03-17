package com.blestep.sportsbracelet;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.IOUtils;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class BaseApplication extends Application {
    private static final String appFolder = "fitpolo";
    private static String PATH_LOGCAT;

    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setCatchUncaughtExceptions(true);
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appFolder;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + appFolder;
        }
        Printer filePrinter = new FilePrinter.Builder(PATH_LOGCAT)
                .fileNameGenerator(new DateFileNameGenerator())
                .logFlattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss} {l}/{t}: {m}"))
                .build();
        LogConfiguration config = new LogConfiguration.Builder().tag("fitpolo").build();
        XLog.init(BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.NONE, config, new AndroidPrinter(), filePrinter);
        // 初始化数据库
        DBTools.getInstance(getApplicationContext());
        // 初始化SharedPreference
        SPUtiles.getInstance(getApplicationContext());
        // 启动蓝牙服务
        startService(new Intent(this, BTService.class));
        // 初始化蓝牙适配器
        BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BTModule.mBluetoothAdapter = bluetoothManager.getAdapter();
        Thread.setDefaultUncaughtExceptionHandler(new BTUncaughtExceptionHandler());
    }

    public class BTUncaughtExceptionHandler implements
            Thread.UncaughtExceptionHandler {
        private static final String LOGTAG = "BTUncaughtExceptionHandler";

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // 读取stacktrace信息
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            StringBuffer errorReport = new StringBuffer();
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            try {
                packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packInfo != null) {
                String version = packInfo.versionName;
                errorReport.append(version);
                errorReport.append("\r\n");
            }
            errorReport.append(result.toString());
            IOUtils.setCrashLog(errorReport.toString());
            LogModule.e("uncaughtException errorReport=" + errorReport);
            // 友盟上传报错信息
            MobclickAgent.reportError(getApplicationContext(), ex);
            MobclickAgent.onKillProcess(getApplicationContext());
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
