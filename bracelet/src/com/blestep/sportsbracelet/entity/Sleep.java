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


    public Sleep(String start, String end, String deep, String light, String awake) {
        this.start = start;
        this.end = end;
        this.deep = deep;
        this.light = light;
        this.awake = awake;
    }

    public Sleep() {
    }

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
