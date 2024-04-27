package com.example.team7_390_w2024;

public class Device {
    private String bleMacAddress, name;
    private int id;

    Device() {
        this.bleMacAddress = null;
        this.name = null;
        this.id = -1;
    }
    Device(String macAddress, String name) {
        this.bleMacAddress = macAddress;
        this.name = name;
        this.id = -1;
    }

    Device(String macAddress, String name, int id) {
        this.bleMacAddress = macAddress;
        this.name = name;
        this.id = id;
    }

    public String getBleMacAddress() {
        return bleMacAddress;
    }

    public void setBleMacAddress(String bleMacAddress) {
        this.bleMacAddress = bleMacAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
