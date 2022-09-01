package com.unitech.scanner.utility.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.service.ApiLocal;
import com.unitech.scanner.utility.service.MainService;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.service.mainUsage.RemoteDeviceInfo;

import org.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;

public class SettingBroadcastReceiver extends BroadcastReceiver {

    public SettingBroadcastReceiver() {
    }

    private static class Task extends AsyncTask <Void, String, Void> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final WeakReference <Context> contextReference;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            contextReference = new WeakReference <>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null) return null;
            Logger.debug("SettingBroadcastReceiver action = " + action);

            switch (action) {
                case AllUsageAction.apiGetConfigReply: {
                    try {
                        if (bundle == null) break;
                        Set <String> bundleSet = bundle.keySet();

                        StringBuilder sb = new StringBuilder();
                        sb.append("Received parameter values:\n");
                        for (String key : bundleSet) {
                            if (bundle.get(key) == null) continue;
                            sb.append(key).append(":").append(Objects.requireNonNull(bundle.get(key)).toString()).append(" , ");
                        }
                        sb.append("\n");
                        Logger.debug(sb.toString());//Jack
                    } catch (Exception ex) {
                        Logger.error(ex.toString());
                    } finally {
                        publishProgress("hideProgressBar");
                        Logger.trace("hideProgressBar");
                        publishProgress("toast", "Download complete");
                    }
                    break;
                }
                case AllUsageAction.apiGetBatteryReply: {
                    MainActivity.executorService.execute(() -> {
                        final int batteryLevel = intent.getIntExtra("battery", -1);
                        String batteryLevelStr = "";
                        Logger.info("batteryLevel:" + batteryLevel);
                        switch (batteryLevel) {
                            case 0://charging
                                batteryLevelStr = "charging";
                                break;
                            case 1://very low
                                batteryLevelStr = "very low";
                                break;
                            case 2://low
                                batteryLevelStr = "low";
                                break;
                            case 3://ok
                                batteryLevelStr = "ok";
                                break;
                            case 4://full
                                batteryLevelStr = "full";
                                break;
                        }
                        try {
                            String sn = MainActivity.getBtSerialNo();
                            RemoteDeviceInfo deviceInfo = MainService.scannersMap.get(sn);
                            if (deviceInfo == null)
                                return;
                            deviceInfo.setBatteryLevel(batteryLevelStr);
                            Logger.trace("set device {} batteryLevel {}", sn, batteryLevelStr);
                        } catch (Exception ex) {
                            String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                            Logger.error(error);
                        }

                    });
                    break;
                }
                case AllUsageAction.apiGetFWReply: {
                    MainActivity.executorService.execute(() -> {
                        String fwVersion = intent.getStringExtra("fw");
                        Logger.info("fwVersion:" + fwVersion);
                        if(fwVersion==null)return;
                        String sn = MainActivity.getBtSerialNo();
                        if (sn.contentEquals(""))
                            return;
                        RemoteDeviceInfo deviceInfo = MainService.scannersMap.get(sn);
                        if (deviceInfo == null)
                            return;
                        deviceInfo.setFirmwareVersion(fwVersion);
                        Logger.trace("set device {} fwversion {}", sn, fwVersion);
                        String[] parts = fwVersion.split("\\."); // escape .
                        ApiLocal usuApi = new ApiLocal(contextReference.get());
                        if (parts.length >= 2) {
                            int version = Integer.parseInt(parts[1]);
//                            if (version > 43) {
//                                usuApi.setSSIMode();
//                            }
                            if(version > 46){
                                usuApi.setDataTerminator(0);
                            }
                        }
                    });
                    break;
                }
                case AllUsageAction.apiGetSNReply: {
                    String serialNumber = intent.getStringExtra("sn");
                    Logger.info("serialNumber:" + serialNumber);
                    break;
                }
                case AllUsageAction.apiGetAddressReply: {
                    String btRemoteAddress = Objects.requireNonNull(intent.getStringExtra("address")).replaceAll(".{2}(?=.)", "$0:");
                    Logger.info("btRemoteAddress:" + btRemoteAddress);
                    break;
                }
                case AllUsageAction.apiGetNameReply: {
                    String btRemoteName = intent.getStringExtra("name");
                    Logger.info("btRemoteName:" + btRemoteName);
                    break;
                }
                case AllUsageAction.apiGetAckReply: {
                    boolean ack = intent.getBooleanExtra("ack", false);
                    Logger.info("ack:" + ack);
                    break;
                }
                case AllUsageAction.serviceGetScannerReply: {
                    String[] scanners = intent.getStringArrayExtra("connectedScanners");
                    if (scanners == null) break;
                    StringBuilder sb = new StringBuilder();
                    for (String scanner : scanners) {
                        sb.append(scanner);
                        sb.append(":");
                    }
                    Logger.info(sb.toString());
                    break;
                }
                case AllUsageAction.apiGetAutoConnectionReply: {
                    boolean autoConn = intent.getBooleanExtra("autoConn", false);
                    Logger.info("autoConn:" + autoConn);
                    break;
                }
                case AllUsageAction.apiGetBtSignalCheckingLevelReply: {
                    int btSignalCheckingLevel = intent.getIntExtra("btSignalCheckingLevel", 0);
                    Logger.info("btSignalCheckingLevel:" + btSignalCheckingLevel);
                    break;
                }
                case AllUsageAction.apiGetDataTerminatorReply: {
                    MainActivity.executorService.execute(() -> {
                        int dataTerminator = intent.getIntExtra("DataTerminator", 0);
                        Logger.info("dataTerminator:" + dataTerminator);
                        try {
                            String sn = MainActivity.getBtSerialNo();
                            RemoteDeviceInfo deviceInfo = MainService.scannersMap.get(sn);
                            if (deviceInfo == null)
                                return;
                            deviceInfo.setDataTerminator(dataTerminator);
                            Logger.trace("set device {} dataTerminator {}", sn, dataTerminator);
                        } catch (Exception ex) {
                            String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                            Logger.error(error);
                        }
                    });
                    break;
                }
                case AllUsageAction.apiGetFormatReply: {
                    MainActivity.executorService.execute(() -> {
                        int format = intent.getIntExtra("format", 0);
                        Logger.info("format:" + format);
                        try {
                            String sn = MainActivity.getBtSerialNo();
                            RemoteDeviceInfo deviceInfo = MainService.scannersMap.get(sn);
                            if (deviceInfo == null)
                                return;
                            deviceInfo.setFormat(format);
                            Logger.trace("set device {} format {}", sn, format);
                        } catch (Exception ex) {
                            String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                            Logger.error(error);
                        }
                    });
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Logger.trace("onProgressUpdate({})", values[0]);
            switch (values[0]) {
                case "showProgressBar":
                    MainActivity.showProgressBar();
                    break;
                case "hideProgressBar":
                    MainActivity.hideProgressBar();
                    break;
                case "toast":
                    if (values.length > 1) {
                        App.toast(contextReference.get(), values[1]);
                    }
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pendingResult != null)
                pendingResult.finish();
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        Task asyncTask = new Task(pendingResult, intent, context);
        asyncTask.execute();
    }
}

