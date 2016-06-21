package com.blestep.sportsbracelet.module;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.view.CustomDialog;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by lwz on 2016/6/18 0018.
 */


public class UnitManagerModule {
    private static final int MIN_HEIGHT_CM = 100;
    private static final int MIN_HEIGHT_FT = 3;
    private static final int MIN_HEIGHT_IN = 0;
    private static final int MIN_WEIGHT_KG = 30;
    private static final int MIN_WEIGHT_LB = 66;
    private static final float CM_TO_IN = 0.3937008f;
    private static final float KG_TO_LB = 2.2046226f;
    private WheelView wv_height_cm;
    private WheelView wv_height_ft;
    private WheelView wv_height_in;
    private WheelView wv_height_unit;
    private WheelView wv_weight_kg;
    private WheelView wv_weight_lb;
    private WheelView wv_weight_unit;
    private WheelView.WheelViewStyle style;
    private int heightValue;
    private TextView tv_setting_userinfo_height;
    private TextView tv_height_unit;
    private int weightValue;
    private TextView tv_setting_userinfo_weight;
    private TextView tv_weight_unit;

    public void createHeightDialog(final Activity activity) {
        tv_setting_userinfo_height = ButterKnife.findById(activity, R.id.tv_setting_userinfo_height);
        heightValue = Integer.valueOf(tv_setting_userinfo_height.getTag().toString());
        tv_height_unit = ButterKnife.findById(activity, R.id.tv_height_unit);

        tv_setting_userinfo_weight = ButterKnife.findById(activity, R.id.tv_setting_userinfo_weight);
        weightValue = Integer.valueOf(tv_setting_userinfo_weight.getTag().toString());
        tv_weight_unit = ButterKnife.findById(activity, R.id.tv_weight_unit);

        final CustomDialog.Builder builder = new CustomDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.wheelview_height, null);
        wv_height_cm = ButterKnife.findById(view, R.id.wv_height_cm);
        wv_height_ft = ButterKnife.findById(view, R.id.wv_height_ft);
        wv_height_in = ButterKnife.findById(view, R.id.wv_height_in);
        wv_height_unit = ButterKnife.findById(view, R.id.wv_height_unit);
        style = new WheelView.WheelViewStyle();
        style.selectedTextSize = 20;
        style.textSize = 16;
        SPUtiles.getInstance(activity);
        resetHeightWheel(activity);
        wv_height_unit.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_height_unit.setSkin(WheelView.Skin.Holo);
        wv_height_unit.setWheelData(createHeightUnit(activity));
        wv_height_unit.setStyle(style);
        wv_height_unit.setSelection(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? 1 : 0);
        wv_height_unit.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                if (position == 0 && !SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)) {
                    return;
                }
                if (position == 1 && SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)) {
                    return;
                }
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, position == 0 ? false : true);
                resetHeightWheel(activity);
            }
        });


        builder.setContentView(view);
        builder.setTitle(R.string.setting_userinfo_height);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
                if (!isBritish) {
                    heightValue = Integer.valueOf(wv_height_cm.getSelectionItem().toString());
                    tv_setting_userinfo_height.setText(heightValue + "");
                    tv_setting_userinfo_height.setTag(heightValue + "");
                    tv_height_unit.setText(wv_height_unit.getSelectionItem().toString());

                    tv_setting_userinfo_weight.setText(tv_setting_userinfo_weight.getTag().toString());
                    tv_weight_unit.setText(activity.getString(R.string.setting_userinfo_weight_unit));
                } else {
                    heightValue = InToCm(Integer.valueOf(wv_height_ft.getSelectionItem().toString()), Integer.valueOf(wv_height_in.getSelectionItem().toString()));
                    tv_setting_userinfo_height.setText(String.format("%s'%s''", wv_height_ft.getSelectionItem().toString(), wv_height_in.getSelectionItem().toString()));
                    tv_setting_userinfo_height.setTag(heightValue + "");
                    tv_height_unit.setText(wv_height_unit.getSelectionItem().toString());

                    tv_setting_userinfo_weight.setText(kgToLb(Integer.valueOf(tv_setting_userinfo_weight.getTag().toString())) + "");
                    tv_weight_unit.setText(activity.getString(R.string.setting_userinfo_weight_unit_british));
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void createWeightDialog(final Activity activity) {
        tv_setting_userinfo_height = ButterKnife.findById(activity, R.id.tv_setting_userinfo_height);
        heightValue = Integer.valueOf(tv_setting_userinfo_height.getTag().toString());
        tv_height_unit = ButterKnife.findById(activity, R.id.tv_height_unit);

        tv_setting_userinfo_weight = ButterKnife.findById(activity, R.id.tv_setting_userinfo_weight);
        weightValue = Integer.valueOf(tv_setting_userinfo_weight.getTag().toString());
        tv_weight_unit = ButterKnife.findById(activity, R.id.tv_weight_unit);

        final CustomDialog.Builder builder = new CustomDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.wheelview_weight, null);
        wv_weight_kg = ButterKnife.findById(view, R.id.wv_weight_kg);
        wv_weight_lb = ButterKnife.findById(view, R.id.wv_weight_lb);
        wv_weight_unit = ButterKnife.findById(view, R.id.wv_weight_unit);
        style = new WheelView.WheelViewStyle();
        style.selectedTextSize = 20;
        style.textSize = 16;
        SPUtiles.getInstance(activity);
        resetWeightWheel(activity);
        wv_weight_unit.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_weight_unit.setSkin(WheelView.Skin.Holo);
        wv_weight_unit.setWheelData(createWeightUnit(activity));
        wv_weight_unit.setStyle(style);
        wv_weight_unit.setSelection(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? 1 : 0);
        wv_weight_unit.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                if (position == 0 && !SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)) {
                    return;
                }
                if (position == 1 && SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)) {
                    return;
                }
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, position == 0 ? false : true);
                resetWeightWheel(activity);
            }
        });


        builder.setContentView(view);
        builder.setTitle(R.string.setting_userinfo_weight);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
                if (!isBritish) {
                    weightValue = Integer.valueOf(wv_weight_kg.getSelectionItem().toString());
                    tv_setting_userinfo_weight.setText(weightValue  + "");
                    tv_setting_userinfo_weight.setTag(weightValue  + "");
                    tv_weight_unit.setText(wv_weight_unit.getSelectionItem().toString());

                    tv_setting_userinfo_height.setText(tv_setting_userinfo_height.getTag().toString());
                    tv_height_unit.setText(activity.getString(R.string.setting_userinfo_height_unit));
                } else {
                    weightValue = LbToKg(Integer.valueOf(wv_weight_lb.getSelectionItem().toString()));
                    tv_setting_userinfo_weight.setText(wv_weight_lb.getSelectionItem().toString());
                    tv_setting_userinfo_weight.setTag(weightValue  + "");
                    tv_weight_unit.setText(wv_weight_unit.getSelectionItem().toString());

                    tv_setting_userinfo_height.setText(String.format("%s'%s''", cmToFt(Integer.valueOf(tv_setting_userinfo_height.getTag().toString())),
                            cmToIn(Integer.valueOf(tv_setting_userinfo_height.getTag().toString()))));
                    tv_height_unit.setText(activity.getString(R.string.setting_userinfo_height_unit_british));
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void resetWeightWheel(Activity activity) {
        boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        if (!isBritish) {
            wv_weight_lb.setVisibility(View.GONE);
            initWeightKg(activity, weightValue);
        } else {
            wv_weight_kg.setVisibility(View.GONE);
            initWeightLb(activity, weightValue);
        }
    }

    private void initWeightLb(Activity activity, int weightValue) {
        wv_weight_lb.setSkin(WheelView.Skin.Holo);
        wv_weight_lb.setWheelSize(3);
        wv_weight_lb.setWheelData(createWeightLb());
        wv_weight_lb.setSelection(kgToLb(weightValue) - MIN_WEIGHT_LB);
        wv_weight_lb.setStyle(style);
        wv_weight_lb.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_weight_lb.addOnGlobalLayoutListener();
    }

    private void initWeightKg(Activity activity, int weightValue) {
        wv_weight_kg.setSkin(WheelView.Skin.Holo);
        wv_weight_kg.setWheelData(createWeightKg());
        wv_weight_kg.setWheelSize(3);
        wv_weight_kg.setSelection(weightValue - MIN_WEIGHT_KG);
        wv_weight_kg.setStyle(style);
        wv_weight_kg.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_weight_kg.addOnGlobalLayoutListener();
    }

    private void resetHeightWheel(Activity activity) {
        boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        if (!isBritish) {
            wv_height_ft.setVisibility(View.GONE);
            wv_height_in.setVisibility(View.GONE);
            initHeightCm(activity, heightValue);
        } else {
            wv_height_cm.setVisibility(View.GONE);
            initHeightFt(activity, heightValue);
            initHeightIn(activity, heightValue);
        }
    }

    private void initHeightIn(Activity activity, int heightValue) {
        wv_height_in.setSkin(WheelView.Skin.Holo);
        wv_height_in.setWheelData(createHeightIn());
        wv_height_in.setWheelSize(3);
        wv_height_in.setSelection(cmToIn(heightValue) - MIN_HEIGHT_IN);
        wv_height_in.setStyle(style);
        wv_height_in.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_height_in.addOnGlobalLayoutListener();
    }

    private void initHeightFt(Activity activity, int heightValue) {
        wv_height_ft.setSkin(WheelView.Skin.Holo);
        wv_height_ft.setWheelData(createHeightFt());
        wv_height_ft.setWheelSize(3);
        wv_height_ft.setSelection(cmToFt(heightValue) - MIN_HEIGHT_FT);
        wv_height_ft.setStyle(style);
        wv_height_ft.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_height_ft.addOnGlobalLayoutListener();
    }


    private void initHeightCm(Activity activity, int heightValue) {
        wv_height_cm.setSkin(WheelView.Skin.Holo);
        wv_height_cm.setWheelData(createHeightCm());
        wv_height_cm.setWheelSize(3);
        wv_height_cm.setSelection(heightValue - MIN_HEIGHT_CM);
        wv_height_cm.setStyle(style);
        wv_height_cm.setWheelAdapter(new ArrayWheelAdapter(activity));
        wv_height_cm.addOnGlobalLayoutListener();
    }

    private ArrayList<String> createHeightCm() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_CM; i <= 200; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createHeightFt() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_FT; i <= 6; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createHeightIn() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_IN; i <= 11; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createWeightKg() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_WEIGHT_KG; i <= 150; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createWeightLb() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_WEIGHT_LB; i <= 331; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createHeightUnit(Activity activity) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(activity.getString(R.string.setting_userinfo_height_unit));
        list.add(activity.getString(R.string.setting_userinfo_height_unit_british));
        return list;
    }

    private ArrayList<String> createWeightUnit(Activity activity) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(activity.getString(R.string.setting_userinfo_weight_unit));
        list.add(activity.getString(R.string.setting_userinfo_weight_unit_british));
        return list;
    }

    public static int cmToIn(int heightValue) {
        return (int)(heightValue * CM_TO_IN % 12);
    }

    public static int InToCm(int ft, int in) {
        return (int)((ft * 12 + in) / CM_TO_IN);
    }

    public static int cmToFt(int heightValue) {
        return (int)(heightValue * CM_TO_IN / 12);
    }

    public static int kgToLb(int weightValue) {
        if (weightValue == 150) {
            return new BigDecimal(weightValue * KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() -1;
        }
        return new BigDecimal(weightValue * KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static int LbToKg(int kg) {
        return new BigDecimal(kg / KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }
}
