package com.unitech.scanner.utility.service.mainUsage;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/8/14 下午 03:12
 * 修改人:user
 * 修改時間:2020/8/14 下午 03:12
 * 修改備註:
 */

public class SendDataType {
    public enum BroadcastType {
        PUBLIC(0),
        LOCAL(1);
        private final int type;

        BroadcastType(int opcode) {
            this.type = opcode;
        }

        public int get() {
            return type;
        }

        public static BroadcastType fromValue(int value) throws IllegalArgumentException {
            for (BroadcastType e : BroadcastType.values()) {
                if (e.type == value) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Unknown enum value :" + value);
        }
    }

    private byte[] data;
    private String commandName = null;
    private String packageName;
    private BroadcastType broadcast;

    public SendDataType(@NonNull byte[] data, String packageName, BroadcastType broadcast, String... commandName) {
        this.data = data.clone();
        this.packageName = packageName;
        this.broadcast = broadcast;
        if (commandName != null) if (commandName.length > 0) {
            this.commandName = commandName[0];
        }
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setBroadcast(BroadcastType broadcast) {
        this.broadcast = broadcast;
    }

    public byte[] getData() {
        return data;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getPackageName() {
        return packageName;
    }

    public BroadcastType getBroadcast() {
        return broadcast;
    }

    public String toString() {
        String str = "";
        str += "data = " + Arrays.toString(data) + "\n";
        str += "length = " + data.length + "\n";
        str += "packageName = " + packageName + "\n";
        str += "broadcast = " + broadcast + "\n";
        str += "commandName = " + commandName + "\n";

        return str;
    }
}
