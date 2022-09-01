package com.unitech.scanner.utility.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.unitech.scanner.utility.config.AllUsageAction;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/9/4 上午 11:19
 * 修改人:user
 * 修改時間:2020/9/4 上午 11:19
 * 修改備註:
 */

public class ApiLocal {
    //==============================================================================================
    private final Context context;
    private final String packageName;

    //==============================================================================================
    public ApiLocal(Context context) {
        this.context = context;
        this.packageName = null;
    }

    //==============================================================================================
    public void setScanToKey(boolean isEnable) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetScan2key).putExtra("scan2key", isEnable).putExtra("packageName", packageName));
    }

    public void setScanner(String serialNo) {
        Bundle mBundle = new Bundle();
        mBundle.putString("serialNo", serialNo);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.serviceSetScanner).putExtras(mBundle).setFlags(FLAG_RECEIVER_FOREGROUND));
    }

    public void getScannerReply(String[] deviceList) {
        Bundle mBundle = new Bundle();
        mBundle.putStringArray("connectedScanners", deviceList);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.serviceGetScannerReply).putExtras(mBundle));
    }

    public void getTargetScannerReply(String serialNo, boolean isConnected) {
        Bundle mBundle = new Bundle();
        mBundle.putString("serialNo", serialNo);
        mBundle.putBoolean("IsConnected", isConnected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTargetScannerStatusReply).putExtras(mBundle)
        );
    }

    public void importSettingsReply(int result, String message){
        Bundle mBundle = new Bundle();
        mBundle.putInt("result", result);
        mBundle.putString("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiImportSettingsReply).putExtras(mBundle)
        );
    }

    public void exportSettingsReply(int result, String message){
        Bundle mBundle = new Bundle();
        mBundle.putInt("result", result);
        mBundle.putString("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiExportSettingsReply).putExtras(mBundle)
        );
    }

    public void KEYBOARD_INPUT(byte[] rawData) {
        //push out using softkeyboard
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", rawData);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AllUsageAction.serviceKeyboardInput).putExtras(bundle));
    }

    public void enableFloatingButtonService(boolean enable){
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AllUsageAction.floatingServiceStart).putExtra("enable",enable));
    }
    //==============================================================================================
    public void getPairingBarcode() {//2.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetPairingBarcode).putExtra("packageName", packageName));
    }

    public void getPairingBarcodeReply(String PairingBarcodeContent) {//2.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetPairingBarcodeReply).putExtra("PairingBarcodeContent", PairingBarcodeContent));
    }

    public void getTargetScannerStatus() {//2.2
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTargetScannerStatus).putExtra("packageName", packageName));
    }

    public void setDisconnect() {//2.4
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiUnpaired).putExtra("packageName", packageName));
    }

    public void getSerialNumber() {//2.5
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetSN).setFlags(FLAG_RECEIVER_FOREGROUND).putExtra("packageName", packageName));
    }

    public void getRemoteBTName() {//2.6
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetName).putExtra("packageName", packageName));
    }

    public void getRemoteBTAddress() {//2.7
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAddress).putExtra("packageName", packageName));
    }

    public void getFirmwareVersion() {//2.8
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetFW).putExtra("packageName", packageName));
    }

    public void getBattery() {//2.9
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetBattery).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void getTrigger() {//3.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTrigger).putExtra("packageName", packageName));
    }

    public void setTrigger(boolean status) {//3.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetTrigger).putExtra("trig", status).putExtra("packageName", packageName));
    }

    public void startDecode() {//3.2
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiStartDecode).putExtra("packageName", packageName));
    }

    public void stopDecode() {//3.3
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiStopDecode).putExtra("packageName", packageName));
    }

    public void getACK() {//3.4
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAck).putExtra("packageName", packageName));
    }

    public void setACK(boolean ack) {//3.4
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetAck).putExtra("ack", ack).putExtra("packageName", packageName));
    }

    public void getAutoConnect() {//3.5
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetAutoConnection).putExtra("packageName", packageName));
    }

    public void setAutoConnect(boolean autoConn) {//3.5
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetAutoConnection).putExtra("autoConn", autoConn).putExtra("packageName", packageName));
    }

    public void getConfiguration() {//3.6
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetConfig).putExtra("packageName", packageName));
    }

    public void setConfiguration(String appendix, int value) {//3.6
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetConfig).putExtra(appendix, value).putExtra("packageName", packageName));
    }

    public void setConfiguration(Bundle bundle) {//3.6
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetConfig).putExtras(bundle).putExtra("packageName", packageName));
    }

    public void getBTSignalCheckingLevel() {//3.7
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetBtSignalCheckingLevel).putExtra("packageName", packageName));
    }

    public void setBTSignalCheckingLevel(int level) {//3.7
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetBtSignalCheckingLevel).putExtra("btSignalCheckingLevel", level).putExtra("packageName", packageName));
    }

    public void getDataTerminator() {//3.8
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetDataTerminator).putExtra("packageName", packageName));
    }

    public void setDataTerminator(int parseInt) {//3.8
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetDataTerminator).putExtra("DataTerminator", parseInt).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void getFormat() {//4.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiGetFormat).putExtra("packageName", packageName));
    }

    public void setSSIMode() {//4.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiChangeToFormatSsi).putExtra("packageName", packageName));
    }

    public void setRAWMode() {//4.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiChangeToFormatRaw).putExtra("packageName", packageName));
    }

    public void setIndicator(Bundle bundle) {//4.2
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiSetIndicator).putExtras(bundle).putExtra("packageName", packageName));
    }

    //==============================================================================================
    public void exportSettings(String filepath) {//5.1
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiExportSettings).putExtra("filepath", filepath).putExtra("packageName", packageName));
    }

    public void importSettings(String filepath, String password) {//5.2
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiImportSettings).putExtra("filepath", filepath).putExtra("passcode", password).putExtra("packageName", packageName));
    }

    public void uploadAllSettings() {//5.3
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiUploadSettings).putExtra("packageName", packageName));
    }

    public void resetAllSettings() {//5.4
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(AllUsageAction.apiResetSettings).putExtra("packageName", packageName));
    }
    //==============================================================================================
}
