package com.example.team7_390_w2024;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentTimestamp {
    private String timestamp;
    private double current;

    //Use when you have a timestamp
    CurrentTimestamp(double current, String timestamp) {
        this.timestamp = timestamp;
        this.current = current;
    }

    //Use when you want to create a current timestamp with the current time
    CurrentTimestamp(double current) {
        setTimestamp();
        this.current = current;
    }

    public String getTimestamp() {
        return timestamp;
    }

    //Use when you want to set timestamp to current time
    public void setTimestamp() {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd '@' HH:mm:ss");
        this.timestamp = df.format(Calendar.getInstance().getTime());
    }

    //Used when you want to set timestamp to a timestamp String
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }
}
