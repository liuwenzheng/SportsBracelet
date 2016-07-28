package com.blestep.sportsbracelet.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.receiver.BaseBroadcastReceiver;
import com.blestep.sportsbracelet.view.BottomNavView;

public class BaseActivity extends FragmentActivity implements BottomNavView.OnBottomNavClickListener {
    private FinishBroadCastReceiver mBroadCastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadCastReceiver = new FinishBroadCastReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver);
    }

    @Override
    public void onPreClick() {

    }

    @Override
    public void onNextClick() {

    }

    protected void finishActivity(Class<? extends Activity> clazz) {
        final Intent intent = new Intent(BTConstants.ACTION_FINISH_ACTIVITY);
        intent.putExtra("className", clazz.getName());
        sendBroadcast(intent);
    }

    protected final void finishActivities(Class<? extends Activity>... clazzs) {
        for (Class<? extends Activity> clazz : clazzs) {
            finishActivity(clazz);
        }
    }

    class FinishBroadCastReceiver extends BaseBroadcastReceiver {

        public FinishBroadCastReceiver(Context context) {
            super(context);
        }

        @Override
        public IntentFilter getIntentFilter() {
            return new IntentFilter(BTConstants.ACTION_FINISH_ACTIVITY);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String className = intent.getStringExtra("className");
            if (className != null && className.equals(BaseActivity.this.getClass().getName())
                    && !BaseActivity.this.isFinishing()) {
                BaseActivity.this.finish();
            }
        }
    }
}
