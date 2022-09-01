package com.unitech.scanner.utility.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.unitech.scanner.utility.config.AllUsageAction;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

public class ApiPublic {
    //==============================================================================================
    private final Context context;
    private final String packageName;

    //==============================================================================================
    public ApiPublic(Context context) {
        this.context = context;
        this.packageName = null;
    }

    public ApiPublic(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
    }

    //==============================================================================================
    public void setSystemScanToKey(boolean enable) {
        context.sendBroadcast(new Intent().setAction(AllUsageAction.systemScan2key).putExtra("scan2key", enable).putExtra("packageName", packageName));
    }
    //==============================================================================================
    public void getPairingBarcode() {//2.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetPairingBarcode).putExtra("packageName", packageName));
    }

    public void getTargetScannerStatus() {//2.2
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTargetScannerStatus).putExtra("packageName", packageName));
    }

    public void setDisconnect() {//2.4
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiUnpaired).putExtra("packageName", packageName));
    }

    public void getSerialNumber() {//2.5
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetSN).setFlags(FLAG_RECEIVER_FOREGROUND).putExtra("packageName", packageName));
    }

    public void getRemoteBTName() {//2.6
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetName).putExtra("packageName", packageName));
    }

    public void getRemoteBTAddress() {//2.7
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAddress).putExtra("packageName", packageName));
    }

    public void getFirmwareVersion() {//2.8
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetFW).putExtra("packageName", packageName));
    }

    public void getBattery() {//2.9
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetBattery).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void getTrigger() {//3.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTrigger).putExtra("packageName", packageName));
    }

    public void setTrigger(boolean status) {//3.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetTrigger).putExtra("trig", status).putExtra("packageName", packageName));
    }

    public void startDecode() {//3.2
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiStartDecode).putExtra("packageName", packageName));
    }

    public void stopDecode() {//3.3
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiStopDecode).putExtra("packageName", packageName));
    }

    public void getACK() {//3.4
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAck).putExtra("packageName", packageName));
    }

    public void setACK(boolean ack) {//3.4
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetAck).putExtra("ack", ack).putExtra("packageName", packageName));
    }

    public void getAutoConnect() {//3.5
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAutoConnection).putExtra("packageName", packageName));
    }

    public void setAutoConnect(boolean autoConn) {//3.5
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetAutoConnection).putExtra("autoConn", autoConn).putExtra("packageName", packageName));
    }

    public void getConfiguration() {//3.6
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetConfig).putExtra("packageName", packageName));
    }

    public void setConfiguration(String appendix, int value) {//3.6
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetConfig).putExtra(appendix, value).putExtra("packageName", packageName));
    }

    public void setConfiguration(Bundle bundle) {//3.6
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetConfig).putExtras(bundle).putExtra("packageName", packageName));
    }

    public void getBTSignalCheckingLevel() {//3.7
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetBtSignalCheckingLevel).putExtra("packageName", packageName));
    }

    public void setBTSignalCheckingLevel(int level) {//3.7
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetBtSignalCheckingLevel).putExtra("btSignalCheckingLevel", level).putExtra("packageName", packageName));
    }

    public void getDataTerminator() {//3.8
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetDataTerminator).putExtra("packageName", packageName));
    }

    public void setDataTerminator(int parseInt) {//3.8
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetDataTerminator).putExtra("DataTerminator", parseInt).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void getFormat() {//4.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetFormat).putExtra("packageName", packageName));
    }

    public void setSSIMode() {//4.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiChangeToFormatSsi).putExtra("packageName", packageName));
    }

    public void setRAWMode() {//4.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiChangeToFormatRaw).putExtra("packageName", packageName));
    }

    public void setIndicator(Bundle bundle) {//4.2
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetIndicator).putExtras(bundle).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void exportSettings(String filepath) {//5.1
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiExportSettings).putExtra("filepath", filepath).putExtra("packageName", packageName));
    }

    public void importSettings(String filepath, String password) {//5.2
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiImportSettings).putExtra("filepath", filepath).putExtra("passcode", password).putExtra("packageName", packageName));
    }

    public void uploadAllSettings() {//5.3
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiUploadSettings).putExtra("packageName", packageName));
    }
    public void resetAllSettings() {//5.4
       context.sendBroadcast(new Intent().setAction(AllUsageAction.apiResetSettings).putExtra("packageName", packageName));
    }
    public void getScanToKey() {//5.5
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiGetScan2key).putExtra("packageName", packageName));
    }
    public void setScanToKey(boolean enable) {//5.5
        context.sendBroadcast(new Intent().setAction(AllUsageAction.apiSetScan2key).putExtra("scan2key", enable).putExtra("packageName", packageName));
    }
    //==============================================================================================
}
