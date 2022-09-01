package com.unitech.scanner.utility.service.mainUsage;

import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;

public class RemoteDeviceInfo {
    private final BluetoothSocket btSocket;
    private final String name;
    private final String sn;
    private final String address;
    private String batteryLevel;
    private String fw;
    private int format = -1;
    private int dataTerminator = -1;
    private boolean selected;
    private int version = 0;

    public RemoteDeviceInfo(BluetoothSocket socket, String address, String name, String serialNumber) {
        this.btSocket = socket;
        this.address = address;
        this.name = name;
        this.sn = serialNumber;
        this.selected = false;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setFirmwareVersion(String fwVersion) {
        this.fw = fwVersion;
        String[] parts = fwVersion.split("\\."); // escape .
        if (parts.length >= 2) {
             this.version = Integer.parseInt(parts[1]);
        }
    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getSn() {
        return sn;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public String getFw() {
        return fw;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public boolean getSelected(){
        return selected;
    }

    public void setFormat(int format){
        this.format = format;
    }

    public int getFormat(){
        return format;
    }

    public void setDataTerminator(int dataTerminator){
        this.dataTerminator = dataTerminator;
    }

    public int getDataTerminator(){
        return dataTerminator;
    }

    public int getVersion(){
        return version;
    }


    @NonNull
    public String toString() {
        String str = "[";
        str += "BluetoothSocket:" + btSocket;
        str += ", name:" + name;
        str += ", sn:" + sn;
        str += ", address:" + address;
        str += ", batteryLevel:" + batteryLevel;
        str += ", fwVersion:" + fw;
        str += ", isSelected:" + selected;
        str += "]";
        return str;
    }


}
