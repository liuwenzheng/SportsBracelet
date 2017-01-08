package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
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
import com.blestep.sportsbracelet.entity.Sleep;
import com.blestep.sportsbracelet.utils.InMemoryCursor;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.TimelineChartViewSleep;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.blestep.sportsbracelet.R.layout.history_sleep;

/**
 * 算法问题：现在的算法显然是直接叠加求和（（22 - 0） + （1 - 0）） / 2 = 11.5。得到错误的日均11.5点入睡。
 * 错误在于时间的变化是连续的而不是离散的，不能直接用入睡时间之和求平均。
 * 正确的算法是选择0点做锚点，用入睡时间距0点的距离之和求平均。
 * 注意入睡时间到0点的距离不会大于12，如果入睡时间大于12点，则取入睡时间 - 24。
 * 沿用之前的例子，（（22 - 24） + （1 - 0）） / 2 = -0.5，即日均0点前0.5小时入睡，即日均23.5点入睡（日均晚上入睡）
 */
public class HistorySleepActivity extends BaseActivity {

    @Bind(R.id.tlc_graph_sleep)
    TimelineChartViewSleep tlc_graph_sleep;
    @Bind(R.id.tv_asleep_duration_name)
    TextView tv_asleep_duration_name;
    @Bind(R.id.tv_asleep_duration)
    TextView tv_asleep_duration;
    @Bind(R.id.tv_deep_sleep_name)
    TextView tv_deep_sleep_name;
    @Bind(R.id.tv_deep_sleep)
    TextView tv_deep_sleep;
    @Bind(R.id.tv_light_sleep_name)
    TextView tv_light_sleep_name;
    @Bind(R.id.tv_light_sleep)
    TextView tv_light_sleep;
    @Bind(R.id.tv_bed_time_name)
    TextView tv_bed_time_name;
    @Bind(R.id.tv_bed_time)
    TextView tv_bed_time;
    @Bind(R.id.tv_wake_up_time_name)
    TextView tv_wake_up_time_name;
    @Bind(R.id.tv_wake_up_time)
    TextView tv_wake_up_time;
    @Bind(R.id.tv_awake_duration_name)
    TextView tv_awake_duration_name;
    @Bind(R.id.tv_awake_duration)
    TextView tv_awake_duration;

    @Bind(R.id.rg_history_bottom_tab_parent)
    RadioGroup rg_history_bottom_tab_parent;
    private ArrayList<Sleep> mSleep;
    private HashMap<String, Sleep> mSleepMap;
    private Calendar mStart = Calendar.getInstance();
    private final String[] COLUMN_NAMES = {"position", "sleep_label", "sleep_start", "sleep_end",
            "sleep_deep", "sleep_light", "sleep_awake", "sleep_asleep"};
    private InMemoryCursor mCursor;
    private Calendar mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(history_sleep);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mSleep = DBTools.getInstance(this).selectAllSleep();
        mSleepMap = new HashMap<>();
        for (Sleep sleep : mSleep) {
            mSleepMap.put(sleep.date, sleep);
        }
        mToday = Calendar.getInstance();
        mToday.set(Calendar.HOUR_OF_DAY, 0);
        mToday.set(Calendar.MINUTE, 0);
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);
        if (mSleep.size() > 0) {
            mStart = Utils.strDate2Calendar(mSleep.get(0).date, BTConstants.PATTERN_YYYY_MM_DD);
        }
        if (mStart == null) {
            return;
        }
        tlc_graph_sleep.addOnSelectedItemChangedListener(new TimelineChartViewSleep.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(TimelineChartViewSleep.Item selectedItem) {
                // 填充数据
                int asleep = (int) (selectedItem.sleepAsleep);
                int deep = (int) selectedItem.sleepDeep;
                int light = (int) selectedItem.sleepLight;
                int awake = (int) selectedItem.sleepAwake;
                tv_asleep_duration.setText(setSleepDuration(asleep / 60, asleep % 60));
                tv_deep_sleep.setText(setSleepDuration(deep / 60, deep % 60));
                tv_light_sleep.setText(setSleepDuration(light / 60, light % 60));
                tv_bed_time.setText(selectedItem.sleepStart);
                tv_wake_up_time.setText(selectedItem.sleepEnd);
                tv_awake_duration.setText(setSleepDuration(awake / 60, awake % 60));
            }
        });
        rg_history_bottom_tab_parent.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_history_unit_day:
                        tv_asleep_duration_name.setText(getString(R.string.sleep_asleep_duration));
                        tv_deep_sleep_name.setText(getString(R.string.sleep_deep));
                        tv_light_sleep_name.setText(getString(R.string.sleep_light));
                        tv_bed_time_name.setText(getString(R.string.sleep_bed_time));
                        tv_wake_up_time_name.setText(getString(R.string.sleep_wake_up));
                        tv_awake_duration_name.setText(getString(R.string.sleep_awake_duratioon));
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
                tv_asleep_duration_name.setText("日均睡眠");
                tv_deep_sleep_name.setText("日均深睡");
                tv_light_sleep_name.setText("日均浅睡");
                tv_bed_time_name.setText("日均入眠");
                tv_wake_up_time_name.setText("日均醒来");
                tv_awake_duration_name.setText("日均清醒");
            }
        });
        buildDayView();
    }

    private SpannableString setSleepDuration(int hour, int min) {
        int hourLength = (hour + " ").length();
        int minLength = (" " + min + " ").length();
        int hourUnit = getString(R.string.sleep_duration_unit_hour).length();
        SpannableString spannableString = new SpannableString(String.format("%s%s%s%s",
                hour + " ", getString(R.string.sleep_duration_unit_hour),
                " " + min + " ", getString(R.string.sleep_duration_unit_min)));
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_666666)),
                0, hourLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(15, true),
                0, hourLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_666666)),
                hourLength + hourUnit, hourLength + hourUnit + minLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(15, true),
                hourLength + hourUnit, hourLength + hourUnit + minLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void buildDayView() {
        tlc_graph_sleep.setBarItemSpace(getResources().getDimension(R.dimen.tlcDefBarItemSpace));
        tlc_graph_sleep.setBarItemWidth(getResources().getDimension(R.dimen.tlcDefBarItemWidth));
        // 创建天日期
        mCursor = creatDataByDay();
        tlc_graph_sleep.observeData(mCursor);
    }

    private void buildWeekView() {
        tlc_graph_sleep.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_sleep.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建周日期
        mCursor = creatDataByWeek();
        tlc_graph_sleep.observeData(mCursor);
    }


    private void buildMonthView() {
        tlc_graph_sleep.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_sleep.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建月日期
        mCursor = creatDataByMonth();
        tlc_graph_sleep.observeData(mCursor);
    }


    private void buildYearView() {
        tlc_graph_sleep.setBarItemSpace(getResources().getDimension(R.dimen.tlcWideBarItemSpace));
        tlc_graph_sleep.setBarItemWidth(getResources().getDimension(R.dimen.tlcWideBarItemWidth));
        // 创建年日期
        mCursor = creatDataByYear();
        tlc_graph_sleep.observeData(mCursor);
    }


    private InMemoryCursor creatDataByYear() {
        InMemoryCursor cursor = new InMemoryCursor(COLUMN_NAMES);
        List<Object[]> data = new ArrayList<>();
        Calendar startDay = (Calendar) mStart.clone();
        int position = 0;
        // int lastYear = mToday.get(Calendar.YEAR) - 1;
        while (startDay.compareTo(mToday) <= 0) {
            int year = startDay.get(Calendar.YEAR);
            Object[] item = new Object[8];
            item[0] = position;
            int start = 0, end = 0;
            double deep = 0, light = 0, awake = 0, asleep = 0;
            int daysInYear = 0;
            while (startDay.get(Calendar.YEAR) == year && startDay.compareTo(mToday) <= 0) {
                Sleep sleep = mSleepMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (sleep == null) {
                    sleep = new Sleep("2016-01-01 00:00", "2016-01-01 00:00", "0", "0", "0");
                }

                String startTime = sleep.start.substring(sleep.start.length() - 5, sleep.start.length());
                int startHour = Integer.parseInt(startTime.split(":")[0]);
                int startMin = Integer.parseInt(startTime.split(":")[1]);
                if (startHour > 12) {
                    start += startHour * 60 + startMin - 24 * 60;
                } else {
                    start += startHour * 60 + startMin;
                }
                String endTime = sleep.end.substring(sleep.end.length() - 5, sleep.end.length());
                int endHour = Integer.parseInt(endTime.split(":")[0]);
                int endMin = Integer.parseInt(endTime.split(":")[1]);
                end += endHour * 60 + endMin;

                deep += Double.parseDouble(sleep.deep);
                light += Double.parseDouble(sleep.light);
                awake += Double.parseDouble(sleep.awake);
                asleep += Double.parseDouble(sleep.deep) + Double.parseDouble(sleep.light);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
                daysInYear++;
            }

            if (year == mToday.get(Calendar.YEAR)) {
                item[1] = getString(R.string.history_this_year);
            }
//            else if (year == lastYear) {
//                item[1] = "去年";
//            }
            else {
                item[1] = year;
            }
            // 年均入睡，起床时间
            int averageStart = start / daysInYear;
            int averageEnd = end / daysInYear;
            Calendar startCalendar = (Calendar) mToday.clone();
            startCalendar.add(Calendar.MINUTE, averageStart);
            Calendar endCalendar = (Calendar) mToday.clone();
            endCalendar.add(Calendar.MINUTE, averageEnd);
            item[2] = Utils.calendar2strDate(startCalendar,BTConstants.PATTERN_HH_MM);
            item[3] = Utils.calendar2strDate(endCalendar,BTConstants.PATTERN_HH_MM);
            item[4] = deep / daysInYear;
            item[5] = light / daysInYear;
            item[6] = awake / daysInYear;
            item[7] = asleep / daysInYear;
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
//        int lastMonth = mToday.get(Calendar.MONTH);
//        if (lastMonth == 1) {
//            lastMonth = 12;
//        } else {
//            lastMonth = lastMonth - 1;
//        }
        while (startDay.compareTo(mToday) <= 0) {
            int month = startDay.get(Calendar.MONTH);
            Object[] item = new Object[8];
            item[0] = position;
            int start = 0, end = 0;
            double deep = 0, light = 0, awake = 0, asleep = 0;
            int daysInMonth = 0;
            while (startDay.get(Calendar.MONTH) == month && startDay.compareTo(mToday) <= 0) {
                Sleep sleep = mSleepMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (sleep == null) {
                    sleep = new Sleep("2016-01-01 00:00", "2016-01-01 00:00", "0", "0", "0");
                }

                String startTime = sleep.start.substring(sleep.start.length() - 5, sleep.start.length());
                int startHour = Integer.parseInt(startTime.split(":")[0]);
                int startMin = Integer.parseInt(startTime.split(":")[1]);
                if (startHour > 12) {
                    start += startHour * 60 + startMin - 24 * 60;
                } else {
                    start += startHour * 60 + startMin;
                }
                String endTime = sleep.end.substring(sleep.end.length() - 5, sleep.end.length());
                int endHour = Integer.parseInt(endTime.split(":")[0]);
                int endMin = Integer.parseInt(endTime.split(":")[1]);
                end += endHour * 60 + endMin;

                deep += Double.parseDouble(sleep.deep);
                light += Double.parseDouble(sleep.light);
                awake += Double.parseDouble(sleep.awake);
                asleep += Double.parseDouble(sleep.deep) + Double.parseDouble(sleep.light);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
                daysInMonth++;
            }
            if (startDay.get(Calendar.YEAR) == mToday.get(Calendar.YEAR) && month == mToday.get(Calendar.MONTH)) {
                item[1] = getString(R.string.history_this_month);
            }
//            else if (startDay.get(Calendar.YEAR) == mToday.get(Calendar.YEAR) && month == lastMonth) {
//                item[1] = "上月";
//            }
            else if (month == 12) {
                month = 1;
                item[1] = getString(R.string.history_month_unit, startDay.get(Calendar.YEAR), month);
            } else {
                item[1] = getString(R.string.history_month_units, month + 1);
            }
            // 月均入睡，起床时间
            int averageStart = start / daysInMonth;
            int averageEnd = end / daysInMonth;
            Calendar startCalendar = (Calendar) mToday.clone();
            startCalendar.add(Calendar.MINUTE, averageStart);
            Calendar endCalendar = (Calendar) mToday.clone();
            endCalendar.add(Calendar.MINUTE, averageEnd);
            item[2] = Utils.calendar2strDate(startCalendar,BTConstants.PATTERN_HH_MM);
            item[3] = Utils.calendar2strDate(endCalendar,BTConstants.PATTERN_HH_MM);
            item[4] = deep / daysInMonth;
            item[5] = light / daysInMonth;
            item[6] = awake / daysInMonth;
            item[7] = asleep / daysInMonth;
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
        // 不是周一从周一开始算
        if (Utils.getWeekDayInChina(startDay) != 1) {
            startDay.add(Calendar.DAY_OF_MONTH, 1 - Utils.getWeekDayInChina(startDay));
        }
        // int lastWeek = Utils.getWeekInChina(mToday) - 1;
        int position = 0;
        while (startDay.compareTo(mToday) <= 0) {
            int week = Utils.getWeekInChina(startDay);
            int year = startDay.get(Calendar.YEAR);
            Object[] item = new Object[8];
            item[0] = position;
            String startWeekDay = "", endWeekDay = "";
            int start = 0, end = 0;
            double deep = 0, light = 0, awake = 0, asleep = 0;
            while (Utils.getWeekInChina(startDay) == week && startDay.compareTo(mToday) <= 0) {
                Sleep sleep = mSleepMap.get(Utils.calendar2strDate(startDay, BTConstants.PATTERN_YYYY_MM_DD));
                if (sleep == null) {
                    sleep = new Sleep("2016-01-01 00:00", "2016-01-01 00:00", "0", "0", "0");
                }
                int day = Utils.getWeekDayInChina(startDay);
                if (day == 1) {
                    startWeekDay = Utils.calendar2strDate(startDay, BTConstants.PATTERN_MM_DD);
                }
                if (day == 7) {
                    endWeekDay = Utils.calendar2strDate(startDay, BTConstants.PATTERN_MM_DD);
                }

                String startTime = sleep.start.substring(sleep.start.length() - 5, sleep.start.length());
                int startHour = Integer.parseInt(startTime.split(":")[0]);
                int startMin = Integer.parseInt(startTime.split(":")[1]);
                if (startHour > 12) {
                    start += startHour * 60 + startMin - 24 * 60;
                } else {
                    start += startHour * 60 + startMin;
                }
                String endTime = sleep.end.substring(sleep.end.length() - 5, sleep.end.length());
                int endHour = Integer.parseInt(endTime.split(":")[0]);
                int endMin = Integer.parseInt(endTime.split(":")[1]);
                end += endHour * 60 + endMin;

                deep += Double.parseDouble(sleep.deep);
                light += Double.parseDouble(sleep.light);
                awake += Double.parseDouble(sleep.awake);
                asleep += Double.parseDouble(sleep.deep) + Double.parseDouble(sleep.light);
                startDay.add(Calendar.DAY_OF_MONTH, 1);
                // 跨年了，周变了
                if (year != startDay.get(Calendar.YEAR)) {
                    year = startDay.get(Calendar.YEAR);
                    week = Utils.getWeekInChina(startDay);
                }
            }
            if (week == Utils.getWeekInChina(mToday)) {
                item[1] = getString(R.string.history_this_week);
            }
//            else if (week == lastWeek) {
//                item[1] = "上周";
//            }
            else {
                item[1] = String.format("%s~%s", startWeekDay, endWeekDay);
            }
            // 周均入睡，起床时间
            int averageStart = start / 7;
            int averageEnd = end / 7;
            Calendar startCalendar = (Calendar) mToday.clone();
            startCalendar.add(Calendar.MINUTE, averageStart);
            Calendar endCalendar = (Calendar) mToday.clone();
            endCalendar.add(Calendar.MINUTE, averageEnd);
            item[2] = Utils.calendar2strDate(startCalendar,BTConstants.PATTERN_HH_MM);
            item[3] = Utils.calendar2strDate(endCalendar,BTConstants.PATTERN_HH_MM);
            item[4] = deep / 7;
            item[5] = light / 7;
            item[6] = awake / 7;
            item[7] = asleep / 7;
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
        Object[] item = new Object[8];
        item[0] = position;
//        Calendar yesterday = (Calendar) mToday.clone();
//        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        if (selectDate.compareTo(mToday) == 0) {
            item[1] = getString(R.string.history_today);
        }
//        else if (selectDate.compareTo(yesterday) == 0) {
//            item[1] = getString(R.string.history_yesterday);
//        }
        else {
            item[1] = Utils.calendar2strDate(selectDate, BTConstants.PATTERN_MM_DD);
        }
        Sleep sleep = mSleepMap.get(Utils.calendar2strDate(selectDate, BTConstants.PATTERN_YYYY_MM_DD));
        if (sleep != null) {
            item[2] = String.valueOf(sleep.start.substring(sleep.start.length() - 5, sleep.start.length()));
            item[3] = String.valueOf(sleep.end.substring(sleep.start.length() - 5, sleep.start.length()));
            item[4] = Double.parseDouble(sleep.deep);
            item[5] = Double.parseDouble(sleep.light);
            item[6] = Double.parseDouble(sleep.awake);
            item[7] = Double.parseDouble(sleep.deep) + Double.parseDouble(sleep.light);
        } else {
            item[2] = "00:00";
            item[3] = "00:00";
            item[4] = 0;
            item[5] = 0;
            item[6] = 0;
            item[7] = 0;
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
