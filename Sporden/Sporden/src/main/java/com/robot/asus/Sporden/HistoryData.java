package com.robot.asus.Sporden;

public class HistoryData {
    private String time;
    private String sport_name;

    public HistoryData(String time, String sport_name) {
        this.time = time;
        this.sport_name = sport_name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSport_name() {
        return sport_name;
    }

    public void setSport_name(String sport_name) {
        this.sport_name = sport_name;
    }
}
