package com.blestep.sportsbracelet.entity;

import java.io.Serializable;

public class Alarm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String id;
    public String time;
    public String state;
    public String type;
    public String name;


    @Override
    public String toString() {
        return "Alarm{" +
                "time='" + time + '\'' +
                ", state='" + state + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
