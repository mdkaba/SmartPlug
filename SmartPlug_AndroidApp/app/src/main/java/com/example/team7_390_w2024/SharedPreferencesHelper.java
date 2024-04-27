package com.example.team7_390_w2024;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private SharedPreferences sharedPreferences;
    public static final int PLUG_STATUS_ENABLED = 1;
    public static final int PRESENT_CURRENT_DRAW_ENABLED = 1<<1;
    public static final int PRESENT_POWER_DRAW_ENABLED = 1<<2;
    public static final int POWER_DATA_LOG_ENABLED = 1<<3;
    public static final int CURRENT_DATA_LOG_ENABLED = 1<<4;
    private static final String VIEW_SETTINGS = "ViewSettings";
    private static final String DEVICE_NAME = "DeviceName";
    private static final String DEVICE_SSID = "DeviceSSID";
    private static final String DEVICE_PASSWORD = "DevicePassword";
    private static final String DEVICE_MAC_ADDRESS = "DeviceMacAddress";


    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
    }

    public void setViewSettings(int settings){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VIEW_SETTINGS, settings);
        editor.apply();
    }

    public int getViewSettings() {
        return sharedPreferences.getInt(VIEW_SETTINGS,0xFF);
    }

    public void setDeviceName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_NAME, name);
        editor.apply();
    }

    public String getDeviceName() { return sharedPreferences.getString(DEVICE_NAME, null); }

    public void setDeviceSsid(String ssid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_SSID, ssid);
        editor.apply();
    }

    public String getDeviceSsid() { return sharedPreferences.getString(DEVICE_SSID, null); }

    public void setDevicePassword(String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_PASSWORD, password);
        editor.apply();
    }

    public String getDevicePassword() { return sharedPreferences.getString(DEVICE_PASSWORD, null); }

    public void setDeviceMacAddress(String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_MAC_ADDRESS, address);
        editor.apply();
    }

    public String getDeviceMacAddress() { return sharedPreferences.getString(DEVICE_MAC_ADDRESS, null); }

    void resetDeviceStrings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_NAME, null);
        editor.putString(DEVICE_SSID, null);
        editor.putString(DEVICE_PASSWORD, null);

        editor.apply();
    }
}
