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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        ButterKnife.bind(this);
        tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
    }

    @OnClick({R.id.iv_back, R.id.tv_reset, R.id.ll_unit})
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
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, !SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false));
                tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
                break;
        }
    }
}
