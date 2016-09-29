package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.UnitManagerModule;
import com.blestep.sportsbracelet.utils.InMemoryCursor;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.TimelineChartView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.blestep.sportsbracelet.R.layout.history_step;

public class HistoryStepActivity extends BaseActivity {

    @Bind(R.id.tlc_graph_step)
    TimelineChartView tlc_graph_step;
    @Bind(R.id.tv_step_count_daily)
    TextView tv_step_count_daily;
    @Bind(R.id.tv_step_count_daily_value)
    TextView tv_step_count_daily_value;
    @Bind(R.id.tv_step_duration_daily)
    TextView tv_step_duration_daily;
    @Bind(R.id.tv_step_duration_daily_value)
    TextView tv_step_duration_daily_value;
    @Bind(R.id.tv_step_distance_daily)
    TextView tv_step_distance_daily;
    @Bind(R.id.tv_step_distance_daily_value)
    TextView tv_step_distance_daily_value;
    @Bind(R.id.tv_step_calorie_daily)
    TextView tv_step_calorie_daily;
    @Bind(R.id.tv_step_calorie_daily_value)
    TextView tv_step_calorie_daily_value;
    @Bind(R.id.rg_history_bottom_tab_parent)
    RadioGroup rg_history_bottom_tab_parent;
    private ArrayList<Step> mSteps;
    private HashMap<String, Step> mStepsMap;
    private Calendar mStart = Calendar.getInstance();
    private final NumberFormat NUMBER_FORMATTER = new DecimalFormat("#0.0");
    private final String[] COLUMN_NAMES = {"position", "step_label", "step_count", "step_duration",
            "step_distance", "step_calorie"};
    private InMemoryCursor mCursor;
    private Calendar mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(history_step);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mSteps = DBTools.getInstance(this).selectAllStep();
        mStepsMap = new HashMap<>();
        for (Step step : mSteps) {
            mStepsMap.put(step.date, step);
        }
        mToday = Calendar.getInstance();
        mToday.set(Calendar.HOUR_OF_DAY, 0);
        mToday.set(Calendar.MINUTE, 0);
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);
        if (mSteps.size() > 0) {
            mStart = Utils.strDate2Calendar(mSteps.get(0).date, BTConstants.PATTERN_YYYY_MM_DD);
        }
        if (mStart == null) {
            return;
        }
        tlc_graph_step.addOnSelectedItemChangedListener(new TimelineChartView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(TimelineChartView.Item selectedItem) {
                // 记步
                tv_step_count_daily_value.setText((int) selectedItem.stepCount + "");
                // 时长
                String strDuration = String.valueOf((int) selectedItem.stepDuration);
                SpannableString durationSpan = new SpannableString(strDuration + "分钟");
                durationSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(HistoryStepActivity.this, R.color.grey_666666))
                        , 0, strDuration.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                durationSpan.setSpan(new AbsoluteSizeSpan(16, true)
                        , 0, strDuration.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_step_duration_daily_value.setText(durationSpan);
                // 距离
                String distanceUnit;
                if (SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)) {
                    selectedItem.stepDistance = UnitManagerModule.kmToMi((float) selectedItem.stepDistance);
                    distanceUnit = getString(R.string.step_distance_unit_british);
                } else {
                    distanceUnit = getString(R.string.step_distance_unit);
                }
                String strDistance = String.valueOf(NUMBER_FORMATTER.format(selectedItem.stepDistance));
                SpannableString distanceSpan = new SpannableString(strDistance + distanceUnit);
                distanceSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(HistoryStepActivity.this, R.color.grey_666666))
                        , 0, strDistance.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                distanceSpan.setSpan(new AbsoluteSizeSpan(16, true)
                        , 0, strDistance.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_step_distance_daily_value.setText(distanceSpan);
                // 消耗
                String strCalorie = String.valueOf((int) selectedItem.stepCalorie);
                SpannableString calorieSpan = new SpannableString(strCalorie + getString(R.string.setting_target_calorie_unit));
                calorieSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(HistoryStepActivity.this, R.color.grey_666666))
                        , 0, strCalorie.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                calorieSpan.setSpan(new AbsoluteSizeSpan(16, true)
                        , 0, strCalorie.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_step_calorie_daily_value.setText(calorieSpan);
            }
        });
        rg_history_bottom_tab_parent.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_history_unit_day:
                        tv_step_count_daily.setText("步数");
                        tv_step_duration_daily.setText("活动时长");
                        tv_step_distance_daily.setText("里程");
                        tv_step_calorie_daily.setText("消耗");
                        buildDayView();
                        return;
                    case R.id.rb_history_unit_week:
                        buildWeekView();
                        break;
                    case R.id.rb_history_unit_month:
                        buildMonthView();
                        break;
                    case R.id.rb_history_unit_year:
                        buildYearView();
                        break;
                }
                tv_step_count_daily.setText("日均步数");
                tv_step_duration_daily.setText("日均活动");
                tv_step_distance_daily.setText("总里程");
                tv_step_calorie_daily.setText("总消耗");
            }
        });
        int barStepAim = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 100);
        tlc_graph_step.setTargetValue(barStepAim);
        buildDayView();
    }

    private void buildDayView() {
        tlc_graph_step.setIsShowTargetDashedLine(true);
        tlc_graph_step.setBarItemSpace(getResources().getDimension(R.dimen.tlcDefBarItemSpace));
        tlc_graph_step.setBarItemWidth(getResources().getDimension(R.dimen.tlcDefBarItemWidth));
        // 创建天日期
        mCursor = creatDataByDay();
        tlc_graph_step.observeData(mCursor);
    }

    private void buildWeekView() {
        tlc_graph_step.setIsShowTargetDashedLine(false);
        tlc_graph_step.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_step.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建周日期
        mCursor = creatDataByWeek();
        tlc_graph_step.observeData(mCursor);
    }


    private void buildMonthView() {
        tlc_graph_step.setIsShowTargetDashedLine(false);
        tlc_graph_step.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_step.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建月日期
        mCursor = creatDataByMonth();
        tlc_graph_step.observeData(mCursor);
    }


    private void buildYearView() {
        tlc_graph_step.setIsShowTargetDashedLine(false);
        tlc_graph_step.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_step.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建年日期
        mCursor = creatDataByYear();
        tlc_graph_step.observeData(mCursor);
    }

    private InMemoryCursor creatDataByYear() {
        InMemoryCursor cursor = new InMemoryCursor(COLUMN_NAMES);
        List<Object[]> data = new ArrayList<>();
        Calendar startDay = (Calendar) mStart.clone();
        int position = 0;
        int lastYear = mToday.get(Calendar.YEAR) - 1;
        while (startDay.compareTo(mToday) <= 0) {
            int year = startDay.get(Calendar.YEAR);
            Object[] item = new Object[6];
            item[0] = position;
            double count = 0, duration = 0, distance = 0, calories = 0;
            int daysInYear = 0;
            while (startDay.get(Calendar.YEAR) == year && startDay.compareTo(mToday) <= 0) {
                Step step = mStepsMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (step == null) {
                    step = new Step("0", "0", "0", "0");
                }
                count += Double.parseDouble(step.count);
                duration += Double.parseDouble(step.duration);
                distance += Double.parseDouble(step.distance);
                calories += Double.parseDouble(step.calories);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
                daysInYear++;
            }

            if (year == mToday.get(Calendar.YEAR)) {
                item[1] = "今年";
            } else if (year == lastYear) {
                item[1] = "去年";
            } else {
                item[1] = year;
            }
            item[2] = count / daysInYear;
            item[3] = duration / daysInYear;
            item[4] = distance;
            item[5] = calories;
            data.add(item);
            position++;
        }
        cursor.addAll(data);
        return cursor;
    }

    private InMemoryCursor creatDataByMonth() {
        InMemoryCursor cursor = new InMemoryCursor(COLUMN_NAMES);
        List<Object[]> data = new ArrayList<>();
        Calendar startDay = (Calendar) mStart.clone();
        int position = 0;
        int lastMonth = mToday.get(Calendar.MONTH);
        if (lastMonth == 1) {
            lastMonth = 12;
        } else {
            lastMonth = lastMonth - 1;
        }
        while (startDay.compareTo(mToday) <= 0) {
            int month = startDay.get(Calendar.MONTH);
            Object[] item = new Object[6];
            item[0] = position;
            double count = 0, duration = 0, distance = 0, calories = 0;
            int daysInMonth = 0;
            while (startDay.get(Calendar.MONTH) == month && startDay.compareTo(mToday) <= 0) {
                Step step = mStepsMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (step == null) {
                    step = new Step("0", "0", "0", "0");
                }
                count += Double.parseDouble(step.count);
                duration += Double.parseDouble(step.duration);
                distance += Double.parseDouble(step.distance);
                calories += Double.parseDouble(step.calories);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
                daysInMonth++;
            }
            if (startDay.get(Calendar.YEAR) == mToday.get(Calendar.YEAR) && month == mToday.get(Calendar.MONTH)) {
                item[1] = getString(R.string.history_this_month);
            } else if (startDay.get(Calendar.YEAR) == mToday.get(Calendar.YEAR) && month == lastMonth) {
                item[1] = "上月";
            } else if (month == 1) {
                item[1] = String.format("%s/%s月", startDay.get(Calendar.YEAR), month);
            } else {
                item[1] = String.format("%s月", month);
            }
            item[2] = count / daysInMonth;
            item[3] = duration / daysInMonth;
            item[4] = distance;
            item[5] = calories;
            data.add(item);
            position++;
        }
        cursor.addAll(data);
        return cursor;
    }

    private InMemoryCursor creatDataByWeek() {
        InMemoryCursor cursor = new InMemoryCursor(COLUMN_NAMES);
        List<Object[]> data = new ArrayList<>();
        Calendar startDay = (Calendar) mStart.clone();
        int lastWeek = Utils.getWeekInChina(mToday) - 1;
        int position = 0;
        while (startDay.compareTo(mToday) <= 0) {
            int week = Utils.getWeekInChina(startDay);
            Object[] item = new Object[6];
            item[0] = position;
            String startWeekDay = "", endWeekDay = "";
            double count = 0, duration = 0, distance = 0, calories = 0;
            while (Utils.getWeekInChina(startDay) == week && startDay.compareTo(mToday) <= 0) {
                Step step = mStepsMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (step == null) {
                    step = new Step("0", "0", "0", "0");
                }
                int day = Utils.getWeekDayInChina(startDay);
                if (day == 1) {
                    startWeekDay = Utils.calendar2strDate(startDay, BTConstants.PATTERN_MM_DD);
                }
                if (day == 7) {
                    endWeekDay = Utils.calendar2strDate(startDay, BTConstants.PATTERN_MM_DD);
                }
                count += Double.parseDouble(step.count);
                duration += Double.parseDouble(step.duration);
                distance += Double.parseDouble(step.distance);
                calories += Double.parseDouble(step.calories);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
            }
            if (week == Utils.getWeekInChina(mToday)) {
                item[1] = getString(R.string.history_this_week);
            } else if (week == lastWeek) {
                item[1] = "上周";
            } else {
                item[1] = String.format("%s~%s", startWeekDay, endWeekDay);
            }
            item[2] = count / 7;
            item[3] = duration / 7;
            item[4] = distance;
            item[5] = calories;
            data.add(item);
            position++;
        }
        cursor.addAll(data);
        return cursor;
    }

    private InMemoryCursor creatDataByDay() {
        InMemoryCursor cursor = new InMemoryCursor(COLUMN_NAMES);
        List<Object[]> data = new ArrayList<>();

        int position = 0;
        Calendar startDay = (Calendar) mStart.clone();
        while (startDay.compareTo(mToday) <= 0) {
            data.add(createItemByDay(position, startDay));
            startDay.add(Calendar.DAY_OF_MONTH, 1);
            position++;
        }
        cursor.addAll(data);
        return cursor;
    }

    private Object[] createItemByDay(int position, Calendar selectDate) {
        Object[] item = new Object[6];
        item[0] = position;
        Calendar yesterday = (Calendar) mToday.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        if (selectDate.compareTo(mToday) == 0) {
            item[1] = getString(R.string.history_today);
        } else if (selectDate.compareTo(yesterday) == 0) {
            item[1] = getString(R.string.history_yesterday);
        } else {
            item[1] = Utils.calendar2strDate(selectDate, BTConstants.PATTERN_MM_DD);
        }
        Step step = mStepsMap.get(Utils.calendar2strDate(selectDate, BTConstants.PATTERN_YYYY_MM_DD));
        if (step != null) {
            item[2] = Double.parseDouble(step.count);
            item[3] = Double.parseDouble(step.duration);
            item[4] = Double.parseDouble(step.distance);
            item[5] = Double.parseDouble(step.calories);
        } else {
            item[2] = 0;
            item[3] = 0;
            item[4] = 0;
            item[5] = 0;
        }
        return item;
    }

    @OnClick(R.id.iv_back)
    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finishActivityAnim();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishActivityAnim();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishActivityAnim() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
