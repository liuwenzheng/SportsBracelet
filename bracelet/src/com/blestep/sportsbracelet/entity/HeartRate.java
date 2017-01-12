package com.blestep.sportsbracelet.entity;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.utils.Utils;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @Date 2017/1/7
 * @Author wenzheng.liu
 * @Description
 */

public class HeartRate implements Serializable, Comparable<HeartRate> {
    public String time;
    public String value;

    @Override
    public int compareTo(HeartRate another) {
        Calendar calendar = Utils.strDate2Calendar(time, BTConstants.PATTERN_YYYY_MM_DD_HH_MM);
        Calendar anotherCalendar = Utils.strDate2Calendar(another.time, BTConstants.PATTERN_YYYY_MM_DD_HH_MM);
        if (calendar.getTime().getTime() > anotherCalendar.getTime().getTime()) {
            return -1;
        }
        if (calendar.getTime().getTime() < anotherCalendar.getTime().getTime()) {
            return 1;
        }
        return 0;
    }
}
