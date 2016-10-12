package com.blestep.sportsbracelet.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.SPUtiles;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SystemActivity extends Activity {


    @Bind(R.id.tv_unit)
    TextView tvUnit;
    @Bind(R.id.tv_time)
    TextView tv_time;
    @Bind(R.id.tv_light)
    TextView tv_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        ButterKnife.bind(this);
        tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
        tv_time.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ?
                getString(R.string.bracelet_time_format_24) : getString(R.string.bracelet_time_format_12));
        tv_light.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ?
                getString(R.string.bracelet_light_on) : getString(R.string.bracelet_light_off));
    }

    @OnClick({R.id.iv_back, R.id.tv_reset, R.id.ll_unit, R.id.ll_time, R.id.ll_light})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.tv_reset:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.bracelet_reset_alert);
                builder.setPositiveButton(R.string.bracelet_reset_alert_confirm,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK);
                                dialog.dismiss();
                                SystemActivity.this.finish();
                            }
                        });
                builder.setNegativeButton(R.string.bracelet_reset_alert_cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
                break;
            case R.id.ll_unit:
                SPUtiles.getInstance(this);
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT,
                        !SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false));
                tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ?
                        getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
                break;
            case R.id.ll_time:
                SPUtiles.getInstance(this);
                SPUtiles.setIntValue(BTConstants.SP_KEY_TIME_SYSTEM,
                        SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ? 1 : 0);
                tv_time.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ?
                        getString(R.string.bracelet_time_format_24) : getString(R.string.bracelet_time_format_12));
                break;
            case R.id.ll_light:
                SPUtiles.getInstance(this);
                SPUtiles.setIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM,
                        SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ? 1 : 0);
                tv_light.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ?
                        getString(R.string.bracelet_light_on) : getString(R.string.bracelet_light_off));
                break;
        }
    }
}
