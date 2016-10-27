package com.blestep.sportsbracelet.entity;

import java.io.Serializable;

public class Sleep implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String date;
    public String start;
    public String end;
    public String deep;
    public String light;
    public String awake;
    public String record;

    @Override
    public String toString() {
        return "Sleep{" +
                "date='" + date + '\'' +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", deep='" + deep + '\'' +
                ", light='" + light + '\'' +
                ", awake='" + awake + '\'' +
                ", record='" + record + '\'' +
                '}';
    }
}
