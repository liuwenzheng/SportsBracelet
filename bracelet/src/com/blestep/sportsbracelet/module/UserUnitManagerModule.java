package com.blestep.sportsbracelet.module;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.UserInfoLayoutActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.view.CustomDialog;
import com.jp.wheelview.WheelView;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by lwz on 2016/6/18 0018.
 */


public class UserUnitManagerModule {
    private static final int MIN_HEIGHT_CM = 100;
    private static final int MAX_HEIGHT_CM = 200;
    private static final int MIN_HEIGHT_FT = 3;
    private static final int MAX_HEIGHT_FT = 6;
    private static final int MIN_HEIGHT_IN = 0;
    private static final int MAX_HEIGHT_IN = 11;
    private static final int MIN_WEIGHT_KG = 30;
    private static final int MIN_WEIGHT_LB = 66;
    private static final float CM_TO_IN = 0.3937008f;
    private static final float KG_TO_LB = 2.2046226f;
    private static final float KM_TO_MI = 0.6213712f;
    private WheelView wv_height_cm;
    private WheelView wv_height_ft;
    private WheelView wv_height_in;
    private WheelView wv_height_unit;
    private WheelView wv_weight_kg;
    private WheelView wv_weight_lb;
    private WheelView wv_weight_unit;
    private int heightValue;
    private TextView tv_user_height;
    private int weightValue;
    private TextView tv_user_weight;
    private int selectedHeightUnit;
    private int selectedWeightUnit;
    private OnUnitFinishedListener listener;

    public void createHeightDialog(final Activity activity) {
        listener = (UserInfoLayoutActivity)activity;
        tv_user_height = ButterKnife.findById(activity, R.id.tv_user_height);
        heightValue = Integer.valueOf(tv_user_height.getTag().toString());

        tv_user_weight = ButterKnife.findById(activity, R.id.tv_user_weight);
        weightValue = Integer.valueOf(tv_user_weight.getTag().toString());

        final CustomDialog.Builder builder = new CustomDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.wheelview_height, null);
        wv_height_cm = ButterKnife.findById(view, R.id.wv_height_cm);
        wv_height_ft = ButterKnife.findById(view, R.id.wv_height_ft);
        wv_height_in = ButterKnife.findById(view, R.id.wv_height_in);
        wv_height_unit = ButterKnife.findById(view, R.id.wv_height_unit);
        SPUtiles.getInstance(activity);
        resetHeightWheel(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false));
        wv_height_unit.setData(createHeightUnit(activity));
        wv_height_unit.setDefault(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? 1 : 0);
        wv_height_unit.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                if (id == selectedHeightUnit) {
                    return;
                }
                if (id == 0) {
                    heightValue = InToCm(Integer.valueOf(wv_height_ft.getSelectedText()), Integer.valueOf(wv_height_in.getSelectedText()));
                }
                if (id == 1) {
                    heightValue = Integer.valueOf(wv_height_cm.getSelectedText());
                }
                resetHeightWheel(id == 1);
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        builder.setContentView(view);
        builder.setTitle(R.string.setting_userinfo_height);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener.heightFinished();
                boolean isBritish = selectedHeightUnit == 1;
                if (!isBritish) {
                    heightValue = Integer.valueOf(wv_height_cm.getSelectedText());
                    tv_user_height.setText(String.format("%s%s",
                            heightValue,
                            wv_height_unit.getSelectedText()));
                    tv_user_height.setTag(heightValue + "");
                    if (SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 0) != 0) {
                        tv_user_weight.setText(String.format("%s%s",
                                tv_user_weight.getTag().toString(),
                                activity.getString(R.string.setting_userinfo_weight_unit)));
                    }
                } else {
                    heightValue = InToCm(Integer.valueOf(wv_height_ft.getSelectedText()), Integer.valueOf(wv_height_in.getSelectedText()));
                    tv_user_height.setText(String.format("%s'%s''%s",
                            wv_height_ft.getSelectedText(),
                            wv_height_in.getSelectedText(),
                            wv_height_unit.getSelectedText()));
                    tv_user_height.setTag(heightValue + "");
                    if (SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 0) != 0) {
                        tv_user_weight.setText(String.format("%s%s",
                                kgToLb(Integer.valueOf(tv_user_weight.getTag().toString())),
                                activity.getString(R.string.setting_userinfo_weight_unit_british)));
                    }

                }
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, isBritish);
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

    public void resetHeightWheel(boolean isBritish) {
        selectedHeightUnit = isBritish ? 1 : 0;
        if (!isBritish) {
            initHeightCm();
            wv_height_cm.setVisibility(View.VISIBLE);
            wv_height_ft.setVisibility(View.GONE);
            wv_height_in.setVisibility(View.GONE);
        } else {
            initHeightFt();
            initHeightIn();
            wv_height_ft.setVisibility(View.VISIBLE);
            wv_height_in.setVisibility(View.VISIBLE);
            wv_height_cm.setVisibility(View.GONE);
        }
    }

    private void initHeightIn() {
        wv_height_in.setData(createHeightIn());
        wv_height_in.setDefault(cmToIn(heightValue) - MIN_HEIGHT_IN);
    }

    private void initHeightFt() {
        wv_height_ft.setData(createHeightFt());
        wv_height_ft.setDefault(cmToFt(heightValue) - MIN_HEIGHT_FT);
    }


    private void initHeightCm() {
        wv_height_cm.setData(createHeightCm());
        wv_height_cm.setDefault(heightValue - MIN_HEIGHT_CM);
    }


    public void createWeightDialog(final Activity activity) {
        listener = (UserInfoLayoutActivity)activity;
        tv_user_height = ButterKnife.findById(activity, R.id.tv_user_height);
        heightValue = Integer.valueOf(tv_user_height.getTag().toString());

        tv_user_weight = ButterKnife.findById(activity, R.id.tv_user_weight);
        weightValue = Integer.valueOf(tv_user_weight.getTag().toString());

        final CustomDialog.Builder builder = new CustomDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.wheelview_weight, null);
        wv_weight_kg = ButterKnife.findById(view, R.id.wv_weight_kg);
        wv_weight_lb = ButterKnife.findById(view, R.id.wv_weight_lb);
        wv_weight_unit = ButterKnife.findById(view, R.id.wv_weight_unit);
        SPUtiles.getInstance(activity);
        resetWeightWheel(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false));
        wv_weight_unit.setData(createWeightUnit(activity));
        wv_weight_unit.setDefault(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? 1 : 0);
        wv_weight_unit.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                if (id == selectedWeightUnit) {
                    return;
                }
                if (id == 0) {
                    weightValue = LbToKg(Integer.valueOf(wv_weight_lb.getSelectedText()));
                }
                if (id == 1) {
                    weightValue = Integer.valueOf(wv_weight_kg.getSelectedText());
                }
                resetWeightWheel(id == 1);
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        builder.setContentView(view);
        builder.setTitle(R.string.setting_userinfo_weight);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener.weightFinished();
                boolean isBritish = selectedWeightUnit == 1;
                if (!isBritish) {
                    weightValue = Integer.valueOf(wv_weight_kg.getSelectedText());
                    tv_user_weight.setText(String.format("%s%s",
                            weightValue,
                            wv_weight_unit.getSelectedText()));
                    tv_user_weight.setTag(weightValue + "");

                    if (SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 0) != 0) {
                        tv_user_height.setText(String.format("%s%s",
                                tv_user_height.getTag().toString(),
                                activity.getString(R.string.setting_userinfo_height_unit)));
                    }
                } else {
                    weightValue = LbToKg(Integer.valueOf(wv_weight_lb.getSelectedText()));
                    tv_user_weight.setText(String.format("%s%s",
                            wv_weight_lb.getSelectedText(),
                            wv_weight_unit.getSelectedText()));
                    tv_user_weight.setTag(weightValue + "");

                    if (SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 0) != 0) {
                        tv_user_height.setText(String.format("%s'%s''%s",
                                cmToFt(Integer.valueOf(tv_user_height.getTag().toString())),
                                cmToIn(Integer.valueOf(tv_user_height.getTag().toString())),
                                activity.getString(R.string.setting_userinfo_height_unit_british)));
                    }
                }
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, isBritish);
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

    public void resetWeightWheel(boolean isBritish) {
        selectedWeightUnit = isBritish ? 1 : 0;
        if (!isBritish) {
            initWeightKg();
            wv_weight_kg.setVisibility(View.VISIBLE);
            wv_weight_lb.setVisibility(View.GONE);
        } else {
            initWeightLb();
            wv_weight_lb.setVisibility(View.VISIBLE);
            wv_weight_kg.setVisibility(View.GONE);
        }
    }

    private void initWeightLb() {
        wv_weight_lb.setData(createWeightLb());
        wv_weight_lb.setDefault(kgToLb(weightValue) - MIN_WEIGHT_LB);
    }

    private void initWeightKg() {
        wv_weight_kg.setData(createWeightKg());
        wv_weight_kg.setDefault(weightValue - MIN_WEIGHT_KG);
    }


    private ArrayList<String> createHeightCm() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_CM; i <= MAX_HEIGHT_CM; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createHeightFt() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_FT; i <= MAX_HEIGHT_FT; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createHeightIn() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = MIN_HEIGHT_IN; i <= MAX_HEIGHT_IN; i++) {
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
        int in = new BigDecimal(heightValue * CM_TO_IN % 12).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (in > MAX_HEIGHT_IN) {
            in = MAX_HEIGHT_IN;
        }
        if (in < MIN_HEIGHT_IN) {
            in = MIN_HEIGHT_IN;
        }
        return in;
    }

    public static float kmToMi(float km) {
        float mi = new BigDecimal(km * KM_TO_MI).setScale(1, BigDecimal.ROUND_HALF_UP).intValue();
        return mi;
    }

    public static int InToCm(int ft, int in) {
        int cm = new BigDecimal((ft * 12 + in) / CM_TO_IN).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (cm > MAX_HEIGHT_CM) {
            cm = MAX_HEIGHT_CM;
        }
        if (cm < MIN_HEIGHT_CM) {
            cm = MIN_HEIGHT_CM;
        }
        return cm;
    }

    public static int cmToFt(int heightValue) {
        return (int) (heightValue * CM_TO_IN / 12);
    }

    public static int kgToLb(int weightValue) {
        if (weightValue == 150) {
            return new BigDecimal(weightValue * KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() - 1;
        }
        return new BigDecimal(weightValue * KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static int LbToKg(int kg) {
        return new BigDecimal(kg / KG_TO_LB).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public interface OnUnitFinishedListener {
        public void heightFinished();

        public void weightFinished();
    }
}
