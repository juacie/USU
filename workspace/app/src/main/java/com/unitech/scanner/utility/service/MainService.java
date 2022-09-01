package com.unitech.scanner.utility.service;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.BuildConfig;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.config.ErrorMessage;
import com.unitech.scanner.utility.config.SupportScanner;
import com.unitech.scanner.utility.config.formatting.Converter;
import com.unitech.scanner.utility.config.formatting.Formatting;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.service.mainUsage.DecodeReplyPacket;
import com.unitech.scanner.utility.service.mainUsage.FormattingData;
import com.unitech.scanner.utility.service.mainUsage.PrefCmdTrans;
import com.unitech.scanner.utility.service.mainUsage.RemoteDeviceInfo;
import com.unitech.scanner.utility.service.mainUsage.SendDataType;
import com.unitech.scanner.utility.service.mainUsage.TargetScanner;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.utils.DocumentFileUtil;
import com.unitech.scanner.utility.utils.IOUtil;
import com.unitech.sppprotocol.mcu.McuCommand;
import com.unitech.sppprotocol.mcu.McuCommandException;
import com.unitech.sppprotocol.mcu.McuCommandID;
import com.unitech.sppprotocol.model.SE2707;
import com.unitech.sppprotocol.ssi.SsiCommand;
import com.unitech.sppprotocol.ssi.SsiCommandException;
import com.unitech.sppprotocol.ssi.SsiMsgSrc;
import com.unitech.sppprotocol.ssi.SsiOpcode;
import com.unitech.sppprotocol.ssi.SsiStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.ACTION_REBOOT;
import static android.content.Intent.ACTION_SHUTDOWN;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.unitech.scanner.utility.config.App.exportBundle;
import static com.unitech.scanner.utility.config.App.mUriArray;
import static com.unitech.scanner.utility.ui.MainActivity.executorService;
import static com.unitech.scanner.utility.ui.MainActivity.exitAPP;
import static com.unitech.scanner.utility.ui.MainActivity.handler;
import static com.unitech.scanner.utility.ui.MainActivity.isAbleToAcceptClientConnect;
import static com.unitech.scanner.utility.utils.AESEncryptUtil.decrypt;
import static com.unitech.scanner.utility.utils.AESEncryptUtil.encrypt;

/**
 * Created by Stanly Wang on 3/24/2018.
 */

public class MainService extends Service {
    //==============================================================================================
    //region Variable

    //----------------------------------------------------------------------------------------------
    private SharedPreferences defaultPref = null;
    private ApiLocal usuApi;
    private ConnectedObject connectThread = null;
    private NotificationManager notificationManager;
    //----------------------------------------------------------------------------------------------
    private final String allSharedPreferencesFileName = "/storage/emulated/0/Unitech/USU/USU.conf";
    public static boolean isRunning = false;
    private boolean isBTBondBonded = false;
    private boolean isSendDataQueue = false;
    private boolean isAcceptConnectionQueue = false;
    private boolean tryAcquireSemaphoreResult = false;
    //----------------------------------------------------------------------------------------------
    public static ConcurrentHashMap <String, String> scannerAddressMap = null;
    public static ConcurrentHashMap <String, RemoteDeviceInfo> scannersMap = null;
    public static ConcurrentHashMap <String, ConnectedObject> connectedThreadMap = null;
    private ConcurrentLinkedQueue <Runnable> callAcceptConnectionQueue;
    private ConcurrentLinkedQueue <SendDataType> sendDataQueue = null;
    //----------------------------------------------------------------------------------------------
    private ThreadPoolExecutor connectedThreadPoolExecutor = null;
    //----------------------------------------------------------------------------------------------
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            executorService.execute(() -> {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();
                if (action == null) return;
                Logger.info("localBroadcastReceiver action=" + action);
                switch (action) {
                    case AllUsageAction.apiGetPairingBarcode://2.1
                    {
                        SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
                        String PairingBarcodeContent = localInfoPref.getString("PairingBarcodeContent", AllDefaultValue.setting_BtAddress);
                        Logger.debug("PairingBarcodeContent = " + PairingBarcodeContent);
                        usuApi.getPairingBarcodeReply(PairingBarcodeContent);
                        break;
                    }
                    case AllUsageAction.apiGetTargetScannerStatus://2.2
                    {
                        usuApi.getTargetScannerReply(TargetScanner.readSN(), TargetScanner.readIsConnected());
                        break;
                    }
                    case AllUsageAction.apiSetConfig://3.6 local
                    {
                        if (bundle == null) return;
                        if (bundle.getBoolean("upload", false)) {
                            action = AllUsageAction.apiUploadSettings;
                            bundle.remove("upload");
                        }
                        if (connectThread != null && !connectThread.exit) {
                            List <McuCommand> list = getSettingConfigCommand(bundle);
                            if (list != null && list.size() > 0) {
                                int listSize = list.size();
                                for (int i = 0; i < listSize; i++) {
                                    sendDataLocal(list.get(i).getCommandArray(), (listSize > 1) ? action + "_" + i + "_" + (listSize - 1) : action);
                                }
                            }
                            Set <String> bundleSet = bundle.keySet();
                            Logger.debug("API_SET_CONFIG bundleSet.size() = " + bundleSet.size());
                            for (String key : bundleSet) {
                                try {
                                    String temp = defaultPref.getString(key, "0");
                                    int ibValue = bundle.getInt(key, 0);
                                    String sbValue = String.valueOf(ibValue);
                                    if (temp != null && !sbValue.equals(temp)) {
                                        defaultPref.edit().putString(key, sbValue).apply();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Logger.error("Error Key = " + key);
                                }

                            }
                        } else {
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                                    new Intent(action + "_apply")
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NOT_CONNECT)
                            );
                        }
                        break;
                    }
                    case AllUsageAction.apiUnpaired://2.4
                    case AllUsageAction.apiGetSN://2.5
                    case AllUsageAction.apiGetName://2.6
                    case AllUsageAction.apiGetAddress://2.7
                    case AllUsageAction.apiGetFW://2.8
                    case AllUsageAction.apiGetBattery://2.9
                    case AllUsageAction.apiGetTrigger://3.1
                    case AllUsageAction.apiStartDecode://3.2
                    case AllUsageAction.apiStopDecode://3.3
                    case AllUsageAction.apiGetAck://3.4
                    case AllUsageAction.apiGetAutoConnection://3.5
                    case AllUsageAction.apiGetConfig://3.6
                    case AllUsageAction.apiGetBtSignalCheckingLevel://3.7
                    case AllUsageAction.apiGetDataTerminator://3.8
                    case AllUsageAction.apiGetFormat://4.1
                    {
                        if (connectThread != null && !connectThread.exit) {
                            Logger.debug("Receiver localBroadcastReceiver ACTION = " + action);
                            sendDataLocal(cmd7(action), action);//local receiver
                        } else {
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                                    new Intent(action + "_apply")
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NOT_CONNECT)
                            );
                        }
                        break;
                    }
                    case AllUsageAction.apiSetTrigger://3.1
                    case AllUsageAction.apiSetAck://3.4
                    case AllUsageAction.apiSetAutoConnection://3.5
                    case AllUsageAction.apiSetBtSignalCheckingLevel://3.7
                    case AllUsageAction.apiSetDataTerminator://3.8
                    case AllUsageAction.apiChangeToFormatSsi://4.1
                    case AllUsageAction.apiChangeToFormatRaw://4.1
                    case AllUsageAction.apiSetIndicator://4.2
                    {
                        if (connectThread != null && !connectThread.exit) {
                            int value = 999;
                            switch (action) {
                                case AllUsageAction.apiSetIndicator:
                                    if (bundle == null) break;
                                    boolean dataAck = bundle.getBoolean("dataAck", AllDefaultValue.setting_DataAckWithIndicator);
                                    boolean vibrate = bundle.getBoolean("vibrate", Boolean.parseBoolean(AllDefaultValue.setting_IndicatorVibrator));
                                    int beepTimes = bundle.getInt("beepTime", Integer.parseInt(AllDefaultValue.setting_IndicatorBeep));
                                    String ledColor = bundle.getString("ledColor", AllDefaultValue.setting_IndicatorLed);

                                    if (beepTimes < 0 || beepTimes > 3) {
                                        beepTimes = 0;
                                    }
                                    defaultPref.edit().putString(getString(R.string.setting_IndicatorVibrator), String.valueOf(vibrate))
                                            .putString(getString(R.string.setting_IndicatorLed), ledColor)
                                            .putBoolean(getString(R.string.setting_DataAckWithIndicator), dataAck)
                                            .putString(getString(R.string.setting_IndicatorBeep), String.valueOf(beepTimes))
                                            .apply();
                                    Logger.debug("apiSetIndicator dataAck= " + dataAck + " vibrate = " + vibrate + " beepTimes = " + beepTimes + " ledColor = " + ledColor);
                                    value = setINDICATOR(beepTimes, vibrate, ledColor, dataAck);
                                    if (value == 1) {
                                        sendDataLocal(cmd7(AllUsageAction.serviceSendAck), action);//local receiver indicator ack
                                        break;
                                    }
                                    Logger.debug("apiSetIndicator = " + value);
                                    break;
                                case AllUsageAction.apiChangeToFormatRaw:
                                    value = 0;
                                    defaultPref.edit().putString(getString(R.string.setting_scanned_data_format), "0").apply();
                                    break;
                                case AllUsageAction.apiChangeToFormatSsi:
                                    value = 1;
                                    defaultPref.edit().putString(getString(R.string.setting_scanned_data_format), "1").apply();
                                    break;
                                case AllUsageAction.apiSetDataTerminator:
                                    if (bundle == null) break;
                                    value = bundle.getInt("DataTerminator", 0);
                                    defaultPref.edit().putString(getString(R.string.setting_Data_terminator), String.valueOf(value)).apply();
                                    break;
                                case AllUsageAction.apiSetBtSignalCheckingLevel:
                                    if (bundle == null) break;
                                    value = bundle.getInt("btSignalCheckingLevel", 0);
                                    defaultPref.edit().putString(getString(R.string.setting_BT_signal_checking_level), String.valueOf(value)).apply();
                                    break;
                                case AllUsageAction.apiSetAutoConnection:
                                    if (bundle == null) break;
                                    value = bundle.getBoolean("autoConn", true) ? 1 : 0;
                                    break;
                                case AllUsageAction.apiSetAck:
                                    if (bundle == null) break;
                                    value = bundle.getBoolean("ack", true) ? 1 : 0;
                                    break;
                                case AllUsageAction.apiSetTrigger:
                                    if (bundle == null) break;
                                    value = bundle.getBoolean("trig", true) ? 1 : 0;
                                    break;
                            }
                            if (value == 999) break;
                            sendDataLocal(cmd8(action, value), action);
                        } else {
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                                    new Intent(action + "_apply")
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NOT_CONNECT)
                            );
                        }
                        break;
                    }
                    case AllUsageAction.apiSetIntentAction://modify 4.1
                    {
                        if (bundle != null) {
                            SharedPreferences.Editor editor = defaultPref.edit();
                            if (bundle.get("actionName") != null) {
                                String setAction = bundle.getString("actionName", AllDefaultValue.setting_DataAction);
                                editor.putString(getString(R.string.setting_DataAction), setAction);
                            }
                            if (bundle.get("dataKey") != null) {
                                String dataKey = bundle.getString("dataKey", AllDefaultValue.setting_StringData);
                                editor.putString(getString(R.string.setting_StringData), dataKey);
                            }
                            if (bundle.get("dataTypeKey") != null) {
                                String dataTypeKey = bundle.getString("dataTypeKey", AllDefaultValue.setting_StringDataType);
                                editor.putString(getString(R.string.setting_StringDataType), dataTypeKey);
                            }
                            if (bundle.get("dataLengthKey") != null) {
                                String dataLengthKey = bundle.getString("dataLengthKey", AllDefaultValue.setting_StringDataLength);
                                editor.putString(getString(R.string.setting_StringDataLength), dataLengthKey);
                            }
                            if (bundle.get("dataByteKey") != null) {
                                String dataByteKey = bundle.getString("dataByteKey", AllDefaultValue.setting_StringDataByte);
                                editor.putString(getString(R.string.setting_StringDataByte), dataByteKey);
                            }

                            boolean enableType = false;
                            boolean enableLength = false;
                            boolean enableByte = false;
                            Set <String> enableSet = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
                            if (enableSet != null && enableSet.size() > 0) {
                                for (String set : enableSet) {
                                    String str = set.replace("[", "").replace("]", "").replace(" ", "").toLowerCase();
                                    switch (str) {
                                        case "type":
                                            enableType = true;
                                            break;
                                        case "length":
                                            enableLength = true;
                                            break;
                                        case "byte":
                                            enableByte = true;
                                            break;
                                    }
                                }
                                enableSet.clear();
                            } else {
                                enableSet = AllDefaultValue.setting_EnableDataIntent;
                            }

                            if (bundle.get("enableType") != null) {
                                enableType = bundle.getBoolean("enableType", enableType);
                            }
                            if (bundle.get("enableLength") != null) {
                                enableLength = bundle.getBoolean("enableLength", enableLength);
                            }
                            if (bundle.get("enableByte") != null) {
                                enableByte = bundle.getBoolean("enableByte", enableByte);
                            }
                            if (enableType) {
                                enableSet.add("type");
                            }
                            if (enableLength) {
                                enableSet.add("length");
                            }
                            if (enableByte) {
                                enableSet.add("byte");
                            }
                            editor.putStringSet(getString(R.string.setting_EnableDataIntent), enableSet);
                            editor.apply();
                        }
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.apiSetIntentActionReply).putExtra("result", 0));
                        break;
                    }
                    case AllUsageAction.apiGetIntentAction://modify 4.1
                    {
                        String actionName = AllDefaultValue.setting_DataAction;
                        String sActionName = defaultPref.getString(getString(R.string.setting_DataAction), actionName);
                        if (sActionName != null) {
                            actionName = sActionName;
                        }
                        String dataKey = AllDefaultValue.setting_StringData;
                        String sDataKey = defaultPref.getString(getString(R.string.setting_StringData), dataKey);
                        if (sDataKey != null) {
                            dataKey = sDataKey;
                        }
                        String dataTypeKey = AllDefaultValue.setting_StringDataType;
                        String sDataTypeKey = defaultPref.getString(getString(R.string.setting_StringDataType), dataTypeKey);
                        if (sDataTypeKey != null) {
                            dataTypeKey = sDataTypeKey;
                        }
                        String dataLengthKey = AllDefaultValue.setting_StringDataLength;
                        String sDataLengthKey = defaultPref.getString(getString(R.string.setting_StringDataLength), dataKey);
                        if (sDataLengthKey != null) {
                            dataLengthKey = sDataLengthKey;
                        }
                        String dataByteKey = AllDefaultValue.setting_StringDataByte;
                        String sDataByteKey = defaultPref.getString(getString(R.string.setting_StringDataByte), dataKey);
                        if (sDataByteKey != null) {
                            dataByteKey = sDataByteKey;
                        }

                        boolean enableType = false;
                        boolean enableLength = false;
                        boolean enableByte = false;
                        Set <String> enableSet = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
                        if (enableSet != null && enableSet.size() > 0) {
                            for (String set : enableSet) {
                                String str = set.replace("[", "").replace("]", "").replace(" ", "").toLowerCase();
                                switch (str) {
                                    case "type":
                                        enableType = true;
                                        break;
                                    case "length":
                                        enableLength = true;
                                        break;
                                    case "byte":
                                        enableByte = true;
                                        break;
                                }
                            }
                        } else {
                            enableType = true;
                            enableLength = true;
                            enableByte = false;
                        }

                        Bundle mBundle = new Bundle();
                        mBundle.putString("actionName", actionName);
                        mBundle.putString("dataKey", dataKey);
                        mBundle.putString("dataTypeKey", dataTypeKey);
                        mBundle.putString("dataLengthKey", dataLengthKey);
                        mBundle.putString("dataByteKey", dataByteKey);
                        mBundle.putBoolean("enableType", enableType);
                        mBundle.putBoolean("enableLength", enableLength);
                        mBundle.putBoolean("enableByte", enableByte);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.apiGetIntentActionReply).putExtras(mBundle));
                        break;
                    }
                    case AllUsageAction.apiExportSettings://5.1
                    {
                        String filepath = allSharedPreferencesFileName;
                        if (bundle != null) {
                            String temp = bundle.getString("filepath", allSharedPreferencesFileName);
                            if (temp != null && !temp.equals("")) {
                                filepath = temp;
                            }
                        }
                        ExportSettings(filepath, null, 1);
                        break;
                    }
                    case AllUsageAction.apiImportSettings://5.2
                    {
                        String filepath = allSharedPreferencesFileName;
                        String passcode = "";
                        if (bundle != null) {
                            String temp = bundle.getString("filepath", allSharedPreferencesFileName);
                            if (!temp.equals("")) {
                                filepath = temp;
                            }
                            passcode = bundle.getString("passcode", "");
                        }
                        ImportSettings(filepath, passcode, null, 1);
                        break;
                    }
                    case AllUsageAction.apiUploadSettings: //5.3
                    {
                        if (scannersMap.isEmpty()) {
                            executorService.execute(() -> App.toast(getApplicationContext(), "Cannot find any connected device!"));
                            break;
                        }
                        Upload(1, null);//local receiver
                        break;
                    }
                    case AllUsageAction.apiResetSettings: //5.4
                    {
                        Logger.debug("local apiResetSettings");
                        resetSettings(1, null);
                        break;
                    }
                    case AllUsageAction.serviceKeyboardInput: {
                        Logger.debug("KEYBOARD_INPUT");
                        if (bundle == null) return;
                        String data = bundle.getString("stringData", "");
//                        String terminator = bundle.getString("terminator", "");
                        Logger.debug("KEYBOARD_INPUT data = " + data);
                        executorService.execute(() -> {
                            String outputMethodType = defaultPref.getString(getString(R.string.setting_OutputMethod), AllDefaultValue.setting_OutputMethod);
                            if (outputMethodType == null || !outputMethodType.equals("1")) {
                                sendKeyCopyPaste(data);
                            } else {
                                String interChar = defaultPref.getString(getString(R.string.setting_InterCharTime), AllDefaultValue.setting_InterCharTime);
                                if (interChar == null || interChar.equals("0")) {
                                    sendKeyDownUpSync(data);
                                } else {
                                    int duration = Integer.parseInt(interChar);
                                    for (int i = 0; i < data.length(); i++) {
                                        sendKeyDownUpSync(String.valueOf(data.charAt(i)));
                                        try {
                                            Thread.sleep(duration);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
//                            if (!terminator.equals("")) {
//                                sendKeyDownUpSync(terminator);
//                            }
                        });
                        break;
                    }
                    case AllUsageAction.serviceGetScanner: {
                        Logger.debug("API_GET_SCANNERS");
                        ArrayList <String> deviceList = new ArrayList <>();
                        for (Map.Entry <String, RemoteDeviceInfo> deviceInfo : scannersMap.entrySet()) {
                            deviceList.add(deviceInfo.getKey());
                        }
                        usuApi.getScannerReply(deviceList.toArray(new String[0]));
                        break;
                    }
                    case AllUsageAction.serviceSetScanner: {
                        Logger.debug("API_SET_SCANNER");
                        if (bundle == null) return;
                        String serialNumber = bundle.getString("serialNo", "");
                        Logger.debug("serialNo:" + serialNumber);
                        if (scannersMap.containsKey(serialNumber)) {
                            for (RemoteDeviceInfo scannerInfo : scannersMap.values()) {
                                Logger.debug("RemoteDeviceInfo = " + scannerInfo);

                                if (scannerInfo.getSn().equals(serialNumber)) {
                                    Logger.info("deviceInfoEntry.getKey()==deviceInfoKey:" + scannerInfo.getSn() + ":" + serialNumber);
                                    scannerInfo.setSelected(true);
                                    MainActivity.setBtSerialNo(serialNumber);
                                    MainActivity.changeSocket(scannerInfo.getBtSocket());
                                    TargetScanner.setSN(serialNumber);
                                    TargetScanner.setIsConnected(true);

                                    boolean auto_upload_settings = defaultPref.getBoolean(getString(R.string.setting_AutoEnforceSettings), AllDefaultValue.setting_AutoEnforceSettings);
                                    Logger.debug("auto_upload_settings = " + auto_upload_settings);
                                    if (auto_upload_settings) {
                                        //Jack upload settings waiting for fw update

                                        handler.postDelayed(() -> {
                                            String password = "";
                                            String pPassword = defaultPref.getString(getString(R.string.setting_Password), AllDefaultValue.setting_Password);
                                            if (pPassword != null) {
                                                password = pPassword.trim();
                                            }
                                            boolean import_result = ImportSettings(allSharedPreferencesFileName, password, null, 1) == 0;
                                            Logger.debug("auto_upload_settings import settings = " + import_result);
                                            Upload(1, null);//auto_upload_settings
                                        }, 3000);
                                    }
                                } else {
                                    scannerInfo.setSelected(false);
                                }
                            }
                        } else {
                            for (RemoteDeviceInfo deviceInfoEntry : scannersMap.values()) {
                                deviceInfoEntry.setSelected(false);
                            }
                            MainActivity.changeSocket(null);
                            Logger.warn("Cannot find serial number:" + serialNumber);
                        }
                        break;
                    }
                }
            });
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @SuppressLint("StaticFieldLeak")
        class Task extends AsyncTask <Void, String, Void> {
            private final PendingResult pendingResult;
            private final Intent intent;
            private final WeakReference <Context> contextReference;

            private Task(PendingResult pendingResult, Intent intent, Context context) {
                this.pendingResult = pendingResult;
                this.intent = intent;
                this.contextReference = new WeakReference <>(context);
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                Logger.debug("onProgressUpdate = " + values[0]);
                switch (values[0]) {
                    case "showProgressBar":
                        MainActivity.showProgressBar();
                        setServiceNotification(2);
                        break;
                    case "hideProgressBar":
                        Logger.debug("hideProgressBar");
                        MainActivity.hideProgressBar();
                        setServiceNotification(1);
                        break;
                    case "toast":
                        if (values.length > 1) {
                            App.toast(getApplicationContext(), values[1]);
                        }
                        break;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pendingResult.finish();
            }


            @Override
            protected Void doInBackground(Void... voids) {
                {
                    String action = intent.getAction();
                    String customPackageName = intent.getStringExtra("packageName");
                    Bundle bundle = intent.getExtras();
                    Logger.info("mReceiver action=" + action + " , customPackageName = " + customPackageName);
                    switch (action) {
                        case AllUsageAction.apiGetPairingBarcode://2.1
                        {
                            SharedPreferences localInfoPref = getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
                            String PairingBarcodeContent = localInfoPref.getString("PairingBarcodeContent", AllDefaultValue.setting_BtAddress);
                            Logger.debug("PairingBarcodeContent=" + PairingBarcodeContent);
                            Bundle mBundle = new Bundle();
                            mBundle.putString("PairingBarcodeContent", PairingBarcodeContent);
                            mBundle.putString("packageName", customPackageName);
                            if (PairingBarcodeContent == null || PairingBarcodeContent.equals("")) {
                                mBundle.putInt("result", 1);
                                mBundle.putString("message", ErrorMessage.NO_PAIRING_BARCODE);
                            } else {
                                mBundle.putInt("result", 0);
                            }
                            contextReference.get().sendBroadcast(new Intent().setAction(AllUsageAction.apiGetPairingBarcodeReply)
                                    .putExtras(mBundle)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiGetTargetScannerStatus: //2.2
                        {
                            Bundle mBundle = new Bundle();
                            String sn = TargetScanner.readSN();
                            mBundle.putString("serialNo", sn);
                            mBundle.putBoolean("IsConnected", TargetScanner.readIsConnected());
                            mBundle.putString("packageName", customPackageName);
                            if (sn.equals("")) {
                                mBundle.putInt("result", 1);
                                mBundle.putString("message", ErrorMessage.NO_TARGET_SCANNER);
                            } else {
                                mBundle.putInt("result", 0);
                            }
                            contextReference.get().sendBroadcast(new Intent().setAction(AllUsageAction.apiGetTargetScannerStatusReply)
                                    .putExtras(mBundle)
                                    .setPackage(customPackageName)
                            );
                            break;
                        }
                        case AllUsageAction.apiSetConfig: //3.6 public
                        {
                            if (bundle == null) break;
                            if (bundle.getBoolean("upload", false)) {
                                action = AllUsageAction.apiUploadSettings;
                                bundle.remove("upload");
                            }
                            if (bundle.get("packageName") != null) {
                                bundle.remove("packageName");
                            }

                            if (connectThread != null && !connectThread.exit) {
                                List <McuCommand> list = getSettingConfigCommand(bundle);
                                if (list != null && list.size() > 0) {
                                    int listSize = list.size();
                                    for (int i = 0; i < listSize; i++) {
                                        sendDataPublic(list.get(i).getCommandArray(), customPackageName, (listSize > 1) ? action + "_" + i + "_" + (listSize - 1) : action);
                                    }
                                }
                                Set <String> bundleSet = bundle.keySet();
                                Logger.debug("API_SET_CONFIG bundleSet.size() = " + bundleSet.size());
                                for (String key : bundleSet) {
                                    try {
                                        String temp = defaultPref.getString(key, "0");
                                        int ibValue = bundle.getInt(key, 0);
                                        String sbValue = String.valueOf(ibValue);
                                        if (temp != null && !sbValue.equals(temp)) {
                                            defaultPref.edit().putString(key, sbValue).apply();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Logger.error("key name = " + key + " ,error = " + e.getMessage());
                                    }
                                }
                            } else {
                                sendBroadcast(
                                        new Intent(action + "_apply")
                                                .putExtra("result", 1)
                                                .putExtra("message", ErrorMessage.NOT_CONNECT)
                                                .putExtra("packageName", customPackageName)
                                                .setPackage(customPackageName)
                                );
                            }

                            break;
                        }
                        case AllUsageAction.apiGetDataTerminator://3.8
                        {
                            int terminator = Integer.parseInt(AllDefaultValue.setting_Terminator);
                            String sTerminator = defaultPref.getString(getString(R.string.setting_Terminator), AllDefaultValue.setting_Terminator);
                            if (sTerminator != null) {
                                try {
                                    terminator = Integer.parseInt(sTerminator);
                                }catch (Exception ignore){
                                }
                            }
                            sendBroadcast(new Intent(AllUsageAction.apiGetDataTerminatorReply)
                                    .putExtra("DataTerminator", terminator)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetDataTerminator://3.8
                        {
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetDataTerminatorReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    int terminator = bundle.getInt("DataTerminator", 2);
                                    if (terminator <= 4 && terminator >= 0) {
                                        defaultPref.edit().putString(getString(R.string.setting_Terminator), String.valueOf(terminator)).apply();
                                        sendBroadcast(new Intent(AllUsageAction.apiSetDataTerminatorReply)
                                                .putExtra("result", 0)
                                                .putExtra("packageName", customPackageName)
                                                .setPackage(customPackageName));
                                    } else {
                                        sendBroadcast(new Intent(AllUsageAction.apiSetDataTerminatorReply)
                                                .putExtra("result", 1)
                                                .putExtra("message", ErrorMessage.NO_CORRECT_VALUE)
                                                .putExtra("packageName", customPackageName)
                                                .setPackage(customPackageName));
                                    }
                                } catch (Exception e) {
                                    sendBroadcast(new Intent(AllUsageAction.apiSetDataTerminatorReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_TYPE)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiUnpaired://2.4
                        case AllUsageAction.apiGetSN://2.5
                        case AllUsageAction.apiGetName://2.6
                        case AllUsageAction.apiGetAddress://2.7
                        case AllUsageAction.apiGetFW://2.8
                        case AllUsageAction.apiGetBattery: //2.9
                        case AllUsageAction.apiGetTrigger://3.1
                        case AllUsageAction.apiStartDecode://3.2
                        case AllUsageAction.apiStopDecode://3.3
                        case AllUsageAction.apiGetAck://3.4
                        case AllUsageAction.apiGetAutoConnection://3.5
                        case AllUsageAction.apiGetConfig://3.6
                        case AllUsageAction.apiGetBtSignalCheckingLevel://3.7
//                        case AllUsageAction.apiGetFormat://4.1
                        {
                            if (connectThread != null && !connectThread.exit) {
                                Logger.debug("Receiver publicBroadcastReceiver ACTION = " + action);
                                sendDataPublic(cmd7(action), customPackageName, action);//public receiver
                            } else {
                                sendBroadcast(
                                        new Intent(action + "_apply")
                                                .putExtra("result", 1)
                                                .putExtra("message", ErrorMessage.NOT_CONNECT)
                                                .putExtra("packageName", customPackageName)
                                                .setPackage(customPackageName)
                                );
                            }
                            break;
                        }
                        case AllUsageAction.apiSetTrigger://3.1
                        case AllUsageAction.apiSetAck://3.4
                        case AllUsageAction.apiSetAutoConnection://3.5
                        case AllUsageAction.apiSetBtSignalCheckingLevel://3.7
//                        case AllUsageAction.apiChangeToFormatSsi://4.1
//                        case AllUsageAction.apiChangeToFormatRaw://4.1
                        case AllUsageAction.apiSetIndicator://4.2
                        {
                            if (connectThread != null && !connectThread.exit) {
                                int value = 999;
                                switch (action) {
                                    case AllUsageAction.apiSetIndicator:
                                        if (bundle == null) break;
                                        boolean dataAck = bundle.getBoolean("dataAck", false);
                                        boolean vibrate = bundle.getBoolean("vibrate", false);
                                        int beepTimes = bundle.getInt("beepTime", 0);
                                        String ledColor = bundle.getString("ledColor", "none");

                                        if (beepTimes < 0 || beepTimes > 3) {
                                            beepTimes = 0;
                                        }
                                        defaultPref.edit().putString(getString(R.string.setting_IndicatorVibrator), String.valueOf(vibrate))
                                                .putString(getString(R.string.setting_IndicatorLed), ledColor)
                                                .putBoolean(getString(R.string.setting_DataAckWithIndicator), dataAck)
                                                .putString(getString(R.string.setting_IndicatorBeep), String.valueOf(beepTimes))
                                                .apply();
                                        Logger.debug("apiSetIndicator dataAck= " + dataAck + " vibrate = " + vibrate + " beepTimes = " + beepTimes + " ledColor = " + ledColor);
                                        value = setINDICATOR(beepTimes, vibrate, ledColor, dataAck);
                                        if (value == 1) {
                                            sendDataPublic(cmd7(AllUsageAction.serviceSendAck), customPackageName, action);//public receiver indicator ack
                                            break;
                                        }
                                        Logger.debug("apiSetIndicator = " + value);
                                        break;
                                    case AllUsageAction.apiChangeToFormatRaw:
                                        value = 0;
                                        defaultPref.edit().putString(getString(R.string.setting_scanned_data_format), "0").apply();
                                        break;
                                    case AllUsageAction.apiChangeToFormatSsi:
                                        value = 1;
                                        defaultPref.edit().putString(getString(R.string.setting_scanned_data_format), "1").apply();
                                        break;
                                    case AllUsageAction.apiSetDataTerminator:
                                        value = bundle.getInt("DataTerminator", 0);
                                        defaultPref.edit().putString(getString(R.string.setting_Data_terminator), String.valueOf(value)).apply();
                                        break;
                                    case AllUsageAction.apiSetBtSignalCheckingLevel:
                                        value = bundle.getInt("btSignalCheckingLevel", 0);
                                        defaultPref.edit().putString(getString(R.string.setting_BT_signal_checking_level), String.valueOf(value)).apply();
                                        break;
                                    case AllUsageAction.apiSetAutoConnection:
                                        value = bundle.getBoolean("autoConn", true) ? 1 : 0;
                                        break;
                                    case AllUsageAction.apiSetAck:
                                        value = bundle.getBoolean("ack", true) ? 1 : 0;
                                        break;
                                    case AllUsageAction.apiSetTrigger:
                                        value = bundle.getBoolean("trig", true) ? 1 : 0;
                                        break;
                                }
                                if (value == 999) break;
                                sendDataPublic(cmd8(action, value), customPackageName, action);
                            } else {
                                sendBroadcast(
                                        new Intent(action + "_apply")
                                                .putExtra("result", 1)
                                                .putExtra("message", ErrorMessage.NOT_CONNECT)
                                                .putExtra("packageName", customPackageName)
                                                .setPackage(customPackageName)
                                );
                            }

                            break;
                        }
                        case AllUsageAction.apiSetIntentAction://modify 4.1
                        {
                            if (bundle != null) {
                                SharedPreferences.Editor editor = defaultPref.edit();
                                if (bundle.get("actionName") != null) {
                                    String setAction = bundle.getString("actionName", AllDefaultValue.setting_DataAction);
                                    editor.putString(getString(R.string.setting_DataAction), setAction);
                                }
                                if (bundle.get("dataKey") != null) {
                                    String dataKey = bundle.getString("dataKey", AllDefaultValue.setting_StringData);
                                    editor.putString(getString(R.string.setting_StringData), dataKey);
                                }
                                if (bundle.get("dataTypeKey") != null) {
                                    String dataTypeKey = bundle.getString("dataTypeKey", AllDefaultValue.setting_StringDataType);
                                    editor.putString(getString(R.string.setting_StringDataType), dataTypeKey);
                                }
                                if (bundle.get("dataLengthKey") != null) {
                                    String dataLengthKey = bundle.getString("dataLengthKey", AllDefaultValue.setting_StringDataLength);
                                    editor.putString(getString(R.string.setting_StringDataLength), dataLengthKey);
                                }
                                if (bundle.get("dataByteKey") != null) {
                                    String dataByteKey = bundle.getString("dataByteKey", AllDefaultValue.setting_StringDataByte);
                                    editor.putString(getString(R.string.setting_StringDataByte), dataByteKey);
                                }
                                boolean enableType = false;
                                boolean enableLength = false;
                                boolean enableByte = false;
                                Set <String> enableSet = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
                                if (enableSet != null && enableSet.size() > 0) {
                                    for (String set : enableSet) {
                                        String str = set.replace("[", "").replace("]", "").replace(" ", "").toLowerCase();
                                        switch (str) {
                                            case "type":
                                                enableType = true;
                                                break;
                                            case "length":
                                                enableLength = true;
                                                break;
                                            case "byte":
                                                enableByte = true;
                                                break;
                                        }
                                    }
                                    enableSet.clear();
                                }else{
                                    enableSet = AllDefaultValue.setting_EnableDataIntent;
                                }

                                if (bundle.get("enableType") != null) {
                                    enableType = bundle.getBoolean("enableType", enableType);
                                }
                                if (bundle.get("enableLength") != null) {
                                    enableLength = bundle.getBoolean("enableLength", enableLength);
                                }
                                if (bundle.get("enableByte") != null) {
                                    enableByte = bundle.getBoolean("enableByte", enableByte);
                                }
                                if(enableType){
                                    enableSet.add("type");
                                }
                                if(enableLength){
                                    enableSet.add("length");
                                }
                                if(enableByte){
                                    enableSet.add("byte");
                                }
                                editor.putStringSet(getString(R.string.setting_EnableDataIntent), enableSet);
                                editor.apply();
                            }
                            sendBroadcast(new Intent(AllUsageAction.apiSetIntentActionReply)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName)
                            );
                            break;
                        }
                        case AllUsageAction.apiGetIntentAction://modify 4.1
                        {
                            String actionName = AllDefaultValue.setting_DataAction;
                            String sActionName = defaultPref.getString(getString(R.string.setting_DataAction), actionName);
                            if (sActionName != null) {
                                actionName = sActionName;
                            }
                            String dataKey = AllDefaultValue.setting_StringData;
                            String sDataKey = defaultPref.getString(getString(R.string.setting_StringData), dataKey);
                            if (sDataKey != null) {
                                dataKey = sDataKey;
                            }
                            String dataTypeKey = AllDefaultValue.setting_StringDataType;
                            String sDataTypeKey = defaultPref.getString(getString(R.string.setting_StringDataType), dataTypeKey);
                            if (sDataTypeKey != null) {
                                dataTypeKey = sDataTypeKey;
                            }
                            String dataLengthKey = AllDefaultValue.setting_StringDataLength;
                            String sDataLengthKey = defaultPref.getString(getString(R.string.setting_StringDataLength), dataKey);
                            if (sDataLengthKey != null) {
                                dataLengthKey = sDataLengthKey;
                            }
                            String dataByteKey = AllDefaultValue.setting_StringDataByte;
                            String sDataByteKey = defaultPref.getString(getString(R.string.setting_StringDataByte), dataKey);
                            if (sDataByteKey != null) {
                                dataByteKey = sDataByteKey;
                            }
                            boolean enableType = false;
                            boolean enableLength = false;
                            boolean enableByte = false;
                            Set <String> enableSet = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
                            if (enableSet != null && enableSet.size() > 0) {
                                for (String set : enableSet) {
                                    String str = set.replace("[", "").replace("]", "").replace(" ", "").toLowerCase();
                                    switch (str) {
                                        case "type":
                                            enableType = true;
                                            break;
                                        case "length":
                                            enableLength = true;
                                            break;
                                        case "byte":
                                            enableByte = true;
                                            break;
                                    }
                                }
                            }else{
                                enableType = true;
                                enableLength = true;
                                enableByte = false;
                            }

                            Bundle mBundle = new Bundle();
                            mBundle.putString("actionName", actionName);
                            mBundle.putString("dataKey", dataKey);
                            mBundle.putString("dataTypeKey", dataTypeKey);
                            mBundle.putString("dataLengthKey", dataLengthKey);
                            mBundle.putString("dataByteKey", dataByteKey);
                            mBundle.putBoolean("enableType", enableType);
                            mBundle.putBoolean("enableLength", enableLength);
                            mBundle.putBoolean("enableByte", enableByte);
                            sendBroadcast(new Intent(AllUsageAction.apiGetIntentActionReply)
                                    .putExtras(mBundle)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName)
                            );
                            break;
                        }


                        case AllUsageAction.apiExportSettings://5.1
                        {
                            String filepath = allSharedPreferencesFileName;
                            if (bundle != null) {
                                String temp = bundle.getString("filepath", allSharedPreferencesFileName);
                                if (!temp.equals("")) {
                                    filepath = temp;
                                }
                            }
                            ExportSettings(filepath, customPackageName, 0);
                            break;
                        }
                        case AllUsageAction.apiImportSettings://5.2
                        {
                            String filepath = allSharedPreferencesFileName;
                            String passcode = "";
                            if (bundle != null) {
                                String temp = bundle.getString("filepath", allSharedPreferencesFileName);
                                if (!temp.equals("")) {
                                    filepath = temp;
                                }
                                passcode = bundle.getString("passcode", "");
                            }
                            ImportSettings(filepath, passcode, customPackageName, 0);
                            break;
                        }
                        case AllUsageAction.apiUploadSettings://5.3
                        {
                            if (scannersMap.isEmpty()) {
                                String message = "Cannot find any connected device!";
                                executorService.execute(() -> App.toast(getApplicationContext(), message));
                                Bundle mBundle = new Bundle();
                                mBundle.putInt("result", 1);
                                mBundle.putString("message", message);
                                mBundle.putString("packageName", customPackageName);
                                sendBroadcast(new Intent(AllUsageAction.apiUploadSettingsReply).putExtras(mBundle).setPackage(customPackageName));
                                break;
                            }
                            publishProgress("showProgressBar");
                            Upload(0, customPackageName);//public receiver
                            break;
                        }
                        case AllUsageAction.apiResetSettings://5.4
                        {
                            resetSettings(0, customPackageName);
                            break;
                        }
                        case AllUsageAction.usuExit: {
                            MainActivity.getInstance().finish();
                            onDestroy();
                            break;
                        }
                        case AllUsageAction.apiGetScan2key://5.5
                        {
                            boolean scan2key = defaultPref.getBoolean(getString(R.string.setting_Scan2key), AllDefaultValue.setting_Scan2key);
                            sendBroadcast(new Intent(AllUsageAction.apiGetScan2keyReply)
                                    .putExtra("scan2key", scan2key)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetScan2key://5.5
                        {
                            Logger.debug("SCAN2KEY_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetScan2keyReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    boolean scan2Key = bundle.getBoolean("scan2key", true);
                                    defaultPref.edit().putBoolean(getString(R.string.setting_Scan2key), scan2Key).apply();
                                    Logger.debug("scan2Key = " + scan2Key);
                                    sendBroadcast(new Intent(AllUsageAction.apiSetScan2keyReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetScan2keyReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiSetOutput://5.6
                        {
                            Logger.debug("OUTPUT_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetOutputReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("method") != null) {
                                        int method = bundle.getInt("method", -1);
                                        String sMethod = AllDefaultValue.setting_OutputMethod;
                                        if (method == 1) {
                                            sMethod = "1";
                                        }
                                        Logger.debug("apiSetOutput method = " + sMethod);
                                        defaultPref.edit().putString(getString(R.string.setting_OutputMethod), sMethod).apply();
                                    }
                                    if (bundle.get("enableClipboard") != null) {
                                        boolean clipboard = bundle.getBoolean("enableClipboard", AllDefaultValue.setting_Clipboard);
                                        Logger.debug("apiSetOutput clipboard = " + clipboard);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_Clipboard), clipboard).apply();

                                    }
                                    if (bundle.get("interCharDelay") != null) {
                                        int delay = bundle.getInt("interCharDelay", -1);
                                        String sDelay = AllDefaultValue.setting_InterCharTime;
                                        if (delay > 0) {
                                            sDelay = String.valueOf(delay);
                                        }
                                        Logger.debug("apiSetOutput interCharDelay = " + delay);
                                        defaultPref.edit().putString(getString(R.string.setting_InterCharTime), sDelay).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetOutputReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetOutputReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetOutput://5.6
                        {
                            String sMethod = defaultPref.getString(getString(R.string.setting_OutputMethod), AllDefaultValue.setting_OutputMethod);
                            String sDelay = defaultPref.getString(getString(R.string.setting_InterCharTime), AllDefaultValue.setting_InterCharTime);
                            boolean clipboard = defaultPref.getBoolean(getString(R.string.setting_Clipboard), AllDefaultValue.setting_Clipboard);
                            int method = Integer.parseInt(sMethod);
                            int delay = Integer.parseInt(sDelay);
                            sendBroadcast(new Intent(AllUsageAction.apiGetOutputReply)
                                    .putExtra("method", method)
                                    .putExtra("enableClipboard", clipboard)
                                    .putExtra("interCharDelay", delay)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetEncoding://5.7
                        {
                            Logger.debug("ENCODING_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetEncodingReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("encoding") != null) {
                                        int encoding = bundle.getInt("encoding", -1);
                                        String sEncoding = AllDefaultValue.setting_Encoding;
                                        if (encoding == 1 || encoding == 2 || encoding == 3) {
                                            sEncoding = String.valueOf(encoding);
                                        }
                                        Logger.debug("apiSetEncoding encoding = " + sEncoding);
                                        defaultPref.edit().putString(getString(R.string.setting_Encoding), sEncoding).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetEncodingReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetEncodingReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetEncoding://5.7
                        {
                            String sEncoding = defaultPref.getString(getString(R.string.setting_Encoding), AllDefaultValue.setting_Encoding);
                            int encoding = Integer.parseInt(sEncoding);
                            sendBroadcast(new Intent(AllUsageAction.apiGetEncodingReply)
                                    .putExtra("encoding", encoding)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetFormattingStatus://5.8
                        {
                            Logger.debug("FORMATTING_STATUS_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetFormattingStatusReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_UseFormatting);
                                        Logger.debug("apiSetFormattingStatus enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_UseFormatting), enable).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetFormattingStatusReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetFormattingStatusReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetFormattingStatus://5.8
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_UseFormatting), AllDefaultValue.setting_UseFormatting);
                            sendBroadcast(new Intent(AllUsageAction.apiGetFormattingStatusReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetAppend://5.9
                        {
                            Logger.debug("APPEND_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetAppendReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("prefix") != null) {
                                        String prefix = bundle.getString("prefix", AllDefaultValue.setting_Prefix);
                                        Logger.debug("apiSetOutput prefix = " + prefix);
                                        defaultPref.edit().putString(getString(R.string.setting_Prefix), prefix).apply();
                                    }
                                    if (bundle.get("suffix") != null) {
                                        String suffix = bundle.getString("suffix", AllDefaultValue.setting_Suffix);
                                        Logger.debug("apiSetOutput suffix = " + suffix);
                                        defaultPref.edit().putString(getString(R.string.setting_Suffix), suffix).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetAppendReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetAppendReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetAppend://5.9
                        {
                            String prefix = defaultPref.getString(getString(R.string.setting_Prefix), AllDefaultValue.setting_Prefix);
                            String suffix = defaultPref.getString(getString(R.string.setting_Suffix), AllDefaultValue.setting_Suffix);
                            sendBroadcast(new Intent(AllUsageAction.apiGetAppendReply)
                                    .putExtra("prefix", prefix)
                                    .putExtra("suffix", suffix)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetReplace://5.10
                        {
                            Logger.debug("REPLACE_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetReplaceReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    String find = "";
                                    String replacement = "";
                                    String replace = defaultPref.getString(getString(R.string.setting_Replace), AllDefaultValue.setting_Replace);
                                    if (replace != null && !replace.equals("") && replace.contains("->")) {
                                        String[] temp = replace.split("->");
                                        if (temp.length == 1) {
                                            find = temp[0];
                                        } else if (temp.length == 2) {
                                            find = temp[0];
                                            replacement = temp[1];
                                        }
                                    }

                                    if (bundle.get("find") != null) {
                                        find = bundle.getString("find");
                                        if (find == null) {
                                            throw new Exception();
                                        }
                                        Logger.debug("apiSetReplace find = " + find);
                                    }
                                    if (bundle.get("replacement") != null) {
                                        String temp = bundle.getString("replacement", "");
                                        if (temp != null) {
                                            replacement = temp;
                                        }
                                        Logger.debug("apiSetReplace replacement = " + replacement);
                                    }
                                    replace = find + "->" + replacement;
                                    Logger.debug("apiSetReplace replace = " + replace);
                                    defaultPref.edit().putString(getString(R.string.setting_Replace), replace).apply();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetReplaceReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetReplaceReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetReplace://5.10
                        {
                            String find = "";
                            String replacement = "";
                            String replace = defaultPref.getString(getString(R.string.setting_Replace), AllDefaultValue.setting_Replace);
                            if (replace != null && !replace.equals("") && replace.contains("->")) {
                                String[] temp = replace.split("->");
                                if (temp.length == 1) {
                                    find = temp[0];
                                } else if (temp.length == 2) {
                                    find = temp[0];
                                    replacement = temp[1];
                                }
                            }

                            sendBroadcast(new Intent(AllUsageAction.apiGetReplaceReply)
                                    .putExtra("find", find)
                                    .putExtra("replacement", replacement)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetRemoveNonPrintableChar://5.11
                        {
                            Logger.debug("REMOVE_NON_PRINTABLE_CHAR_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetRemoveNonPrintableCharReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_RemoveNonPrintableChar);
                                        Logger.debug("apiSetRemoveNonPrintableChar enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_RemoveNonPrintableChar), enable).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetRemoveNonPrintableCharReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetRemoveNonPrintableCharReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetRemoveNonPrintableChar://5.11
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_RemoveNonPrintableChar), AllDefaultValue.setting_RemoveNonPrintableChar);
                            sendBroadcast(new Intent(AllUsageAction.apiGetRemoveNonPrintableCharReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetFormatting://5.12
                        {
                            Logger.debug("FORMATTING_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetFormattingReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("formatting") != null) {
                                        String formatting = bundle.getString("formatting", AllDefaultValue.setting_Formatting);
                                        Logger.debug("apiSetFormatting formatting = " + formatting);
                                        try {
                                            Formatting formatting1 = Converter.fromJsonString(formatting);
                                            defaultPref.edit().putString(getString(R.string.setting_Formatting), formatting).apply();
                                            sendBroadcast(new Intent(AllUsageAction.apiSetFormattingReply)
                                                    .putExtra("result", 0)
                                                    .putExtra("packageName", customPackageName)
                                                    .setPackage(customPackageName));
                                        } catch (Exception e) {
                                            sendBroadcast(new Intent(AllUsageAction.apiSetFormattingReply)
                                                    .putExtra("result", 1)
                                                    .putExtra("message", ErrorMessage.NOT_CORRECT_FORMATTING_TYPE)
                                                    .putExtra("packageName", customPackageName)
                                                    .setPackage(customPackageName));
                                        }
                                    } else {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetFormattingReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetFormatting://5.12
                        {
                            String formatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
                            sendBroadcast(new Intent(AllUsageAction.apiGetFormattingReply)
                                    .putExtra("formatting", formatting)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiDeleteFormatting://5.12
                        {
                            Logger.debug("FORMATTING_DELETE_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("type") != null) {
                                        int type = bundle.getInt("type", -1);
                                        try {
                                            BarcodeType barcodeType = BarcodeType.fromValue(type);
                                            Logger.debug("apiSetFormatting type = " + type + " , barcodeType = " + barcodeType);

                                            String formatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
                                            Formatting formatting1 = Converter.fromJsonString(formatting);
                                            ArrayList <FormattingElement> formattingElements = formatting1.getFormatting();
                                            if (formattingElements == null) {
                                                sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                                        .putExtra("result", 1)
                                                        .putExtra("message", ErrorMessage.NOT_EXIST_FORMATTING_TYPE)
                                                        .putExtra("packageName", customPackageName)
                                                        .setPackage(customPackageName));
                                            } else {
                                                boolean findOut = false;
                                                ArrayList <FormattingElement> tmpFormattingElements = new ArrayList <>();
                                                for (FormattingElement formattingElement : formattingElements) {
                                                    int tmpType = formattingElement.getType();
                                                    if (tmpType == type) {
                                                        findOut = true;
                                                        continue;
                                                    }
                                                    tmpFormattingElements.add(formattingElement);
                                                }
                                                if (findOut) {
                                                    if (tmpFormattingElements.size() > 0) {
                                                        formatting1.setFormatting(tmpFormattingElements);
                                                        defaultPref.edit().putString(getString(R.string.setting_Formatting), Converter.toJsonString(formatting1)).apply();
                                                    } else if (tmpFormattingElements.size() == 0) {
                                                        defaultPref.edit().putString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting).apply();
                                                    }
                                                    sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                                            .putExtra("result", 0)
                                                            .putExtra("packageName", customPackageName)
                                                            .setPackage(customPackageName));
                                                } else {
                                                    sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                                            .putExtra("result", 1)
                                                            .putExtra("message", ErrorMessage.NOT_EXIST_FORMATTING_TYPE)
                                                            .putExtra("packageName", customPackageName)
                                                            .setPackage(customPackageName));
                                                }
                                            }
                                        } catch (Exception e) {
                                            sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                                    .putExtra("result", 1)
                                                    .putExtra("message", ErrorMessage.NO_CORRECT_VALUE)
                                                    .putExtra("packageName", customPackageName)
                                                    .setPackage(customPackageName));
                                        }
                                    } else {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiDeleteFormattingReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiSetFloatingButton://5.13
                        {
                            Logger.debug("FLOATING_BUTTON_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetFloatingButtonReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_FloatingButton);
                                        Logger.debug("apiSetFloatingButton enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_FloatingButton), enable).apply();
                                        enableFloatingButtonService(enable);
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetFloatingButtonReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetFloatingButtonReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetFloatingButton://5.13
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_FloatingButton), AllDefaultValue.setting_FloatingButton);
                            sendBroadcast(new Intent(AllUsageAction.apiGetFloatingButtonReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetSound://5.14
                        {
                            Logger.debug("SOUND_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetSoundReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_Sound);
                                        Logger.debug("apiSetSound enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_Sound), enable).apply();
                                    }
                                    if (bundle.get("freq") != null) {
                                        int freq = bundle.getInt("freq", -1);
                                        Logger.debug("apiSetSound freq = " + freq);
                                        if(freq==0||freq==1||freq==2){
                                            defaultPref.edit().putString(getString(R.string.setting_Frequency), String.valueOf(freq)).apply();
                                        }else{
                                            throw new Exception();
                                        }
                                    }
                                    if (bundle.get("duration") != null) {
                                        int duration = bundle.getInt("duration", -1);
                                        Logger.debug("apiSetSound duration = " + duration);
                                        if(duration==0||duration==1||duration==2){
                                            defaultPref.edit().putString(getString(R.string.setting_SoundDuration), String.valueOf(duration)).apply();
                                        }else{
                                            throw new Exception();
                                        }
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetSoundReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetSoundReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetSound://5.14
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_Sound), AllDefaultValue.setting_Sound);
                            String sFreq = defaultPref.getString(getString(R.string.setting_Frequency),AllDefaultValue.setting_Frequency);
                            String sDuration = defaultPref.getString(getString(R.string.setting_SoundDuration),AllDefaultValue.setting_SoundDuration);
                            int freq = Integer.parseInt(sFreq);
                            int duration = Integer.parseInt(sDuration);
                            sendBroadcast(new Intent(AllUsageAction.apiGetSoundReply)
                                    .putExtra("enable", enable)
                                    .putExtra("freq", freq)
                                    .putExtra("duration", duration)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetVibration://5.15
                        {
                            Logger.debug("VIBRATION_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetVibrationReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_Vibration);
                                        Logger.debug("apiSetVibration enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_Vibration), enable).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetVibrationReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetVibrationReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetVibration://5.15
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_Vibration), AllDefaultValue.setting_Vibration);
                            sendBroadcast(new Intent(AllUsageAction.apiGetVibrationReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetStartup://5.16
                        {
                            Logger.debug("STARTUP_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetStartupReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_StartUp);
                                        Logger.debug("apiSetVibration enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_StartUp), enable).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetStartupReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetStartupReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetStartup://5.16
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_StartUp), AllDefaultValue.setting_StartUp);
                            sendBroadcast(new Intent(AllUsageAction.apiGetStartupReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case AllUsageAction.apiSetAutoEnforceSettings://5.17
                        {
                            Logger.debug("AUTO_UPLOAD_SETTINGS_SETTING");
                            if (bundle == null) {
                                sendBroadcast(new Intent(AllUsageAction.apiSetAutoEnforceSettingsReply)
                                        .putExtra("result", 1)
                                        .putExtra("message", ErrorMessage.NO_BUNDLE)
                                        .putExtra("packageName", customPackageName)
                                        .setPackage(customPackageName));
                            } else {
                                try {
                                    if (bundle.get("enable") != null) {
                                        boolean enable = bundle.getBoolean("enable", AllDefaultValue.setting_AutoEnforceSettings);
                                        Logger.debug("apiSetAutoUploadSettings enable = " + enable);
                                        defaultPref.edit().putBoolean(getString(R.string.setting_AutoEnforceSettings), enable).apply();
                                    }
                                    sendBroadcast(new Intent(AllUsageAction.apiSetAutoEnforceSettingsReply)
                                            .putExtra("result", 0)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBroadcast(new Intent(AllUsageAction.apiSetAutoEnforceSettingsReply)
                                            .putExtra("result", 1)
                                            .putExtra("message", ErrorMessage.NO_CORRECT_COMMUNICATION_PROTOCOL)
                                            .putExtra("packageName", customPackageName)
                                            .setPackage(customPackageName));
                                }
                            }
                            break;
                        }
                        case AllUsageAction.apiGetAutoEnforceSettings://5.17
                        {
                            boolean enable = defaultPref.getBoolean(getString(R.string.setting_AutoEnforceSettings), AllDefaultValue.setting_AutoEnforceSettings);
                            sendBroadcast(new Intent(AllUsageAction.apiGetAutoEnforceSettingsReply)
                                    .putExtra("enable", enable)
                                    .putExtra("result", 0)
                                    .putExtra("packageName", customPackageName)
                                    .setPackage(customPackageName));
                            break;
                        }
                        case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                            int connectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                            switch (connectState) {
                                case BluetoothAdapter.STATE_DISCONNECTED:
                                    Logger.debug("STATE_DISCONNECTED");
                                    break;
                                case BluetoothAdapter.STATE_CONNECTING:
                                    Logger.debug("STATE_CONNECTING");
                                    break;
                                case BluetoothAdapter.STATE_CONNECTED:
                                    Logger.debug("STATE_CONNECTED");
                                    break;
                                case BluetoothAdapter.STATE_DISCONNECTING:
                                    Logger.debug("STATE_DISCONNECTING");
                                    break;
                            }
                            break;
                        }
                        case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED: {
                            int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                            switch (scanMode) {
                                case BluetoothAdapter.SCAN_MODE_NONE:
                                    /*Indicates that both inquiry scan and page scan are disabled on the local Bluetooth adapter. Therefore this device is neither discoverable nor connectable from remote Bluetooth devices.*/
                                    Logger.debug("SCAN_MODE_NONE");
                                    MainActivity.isAbleToAcceptClientConnect = false;
                                    break;
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                                    /*Indicates that inquiry scan is disabled, but page scan is enabled on the local Bluetooth adapter. Therefore this device is not discoverable from remote Bluetooth devices, but is connectable from remote devices that have previously discovered this device.*/
                                    Logger.debug("SCAN_MODE_CONNECTABLE");
                                    MainActivity.isAbleToAcceptClientConnect = false;
                                    break;
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                                    /*Indicates that both inquiry scan and page scan are enabled on the local Bluetooth adapter. Therefore this device is both discoverable and connectable from remote Bluetooth devices.*/
                                    Logger.debug("SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                                    MainActivity.isAbleToAcceptClientConnect = true;
                                    break;

                            }
                            break;
                        }
                        case BluetoothAdapter.ACTION_STATE_CHANGED: {
                            final int state = bundle.getInt(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    Logger.debug("Bluetooth off");
                                    try {
                                        for (Map.Entry <String, ConnectedObject> deviceInfoEntry : connectedThreadMap.entrySet()) {
                                            deviceInfoEntry.getValue().disconnect();
                                        }
                                        connectedThreadMap.clear();
                                        scannersMap.clear();
                                        //MainActivity.toast("Disconnect All Device!");
                                        publishProgress("toast", "Disconnect All Device!");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    Logger.debug("Turning Bluetooth off...");
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    Logger.debug("Bluetooth on");
                                    //startServer("");
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    Logger.debug("Turning Bluetooth on...");
                                    break;
                            }
                            break;
                        }
                        case BluetoothDevice.ACTION_ACL_CONNECTED: {
                            executorService.execute(() -> {
                                if (!BluetoothAdapter.getDefaultAdapter().isDiscovering())
                                    BluetoothAdapter.getDefaultAdapter().startDiscovery();
                                while (!BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                            });
                            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
                            Logger.info("[ACTION_ACL_CONNECTED] Name = " + device.getName() + " Address = " + device.getAddress());
                            int remoteDeviceType = device.getBluetoothClass().getMajorDeviceClass();
                            Logger.info("getMajorDeviceClass = " + remoteDeviceType);

                            publishProgress("showProgressBar");

                            try {
                                callAcceptConnectionQueue.add(() -> {
                                    Logger.debug("AcceptThread start");
                                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    if (mBluetoothAdapter == null) {
                                        Logger.debug("mBluetoothAdapter= null");
                                        return;
                                    }
                                    UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                                    BluetoothServerSocket mmServerSocket = null;
                                    BluetoothSocket socket = null;
                                    try {
                                        mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);
                                        socket = mmServerSocket.accept();

                                        isAbleToAcceptClientConnect = false;
                                        if (socket != null) {
                                            try {
                                                mmServerSocket.close();
                                            } catch (IOException ex) {
                                                String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                                                Logger.error(error);
                                            }
                                            String deviceName = socket.getRemoteDevice().getName();
                                            String deviceAddress = socket.getRemoteDevice().getAddress();
                                            Logger.debug("Accept Device:deviceName = " + deviceName + " deviceAddress = " + deviceAddress + " socket = " + socket);
                                            if (connectThread != null) {
                                                if(connectThread.deviceName.equals(deviceName)){
                                                    connectThread.disconnect();
                                                }else{
                                                    connectThread.setUnpaired();
                                                }
                                            }
                                            connectThread = new ConnectedObject(socket, deviceName, deviceAddress);
                                            connectThread.start();
                                        } else {
                                            Logger.debug("BluetoothSocket = NULL");
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        Logger.error("IOException = " + ex.getMessage());
                                        String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                                        Logger.warn(error);

                                        try {
                                            if (mmServerSocket != null) mmServerSocket.close();
                                            if (socket != null) socket.close();
                                        } catch (IOException exx) {
                                            error = exx.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                                            Logger.warn(error);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (Exception exx) {
                                Logger.error(exx);
                            }

                            if (remoteDeviceType == 1280) { //keyboard
                                publishProgress("hideProgressBar");
                            }
                            break;
                        }
                        case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                            executorService.execute(() -> {

                                Logger.debug("ACTION_PAIRING_REQUEST");
                                int type = bundle.getInt(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                                int pairingKey = bundle.getInt(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                                Logger.debug("type:" + type + " pairingKey:" + pairingKey);
                            });
                            break;
                        }
                        case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                            int state = bundle.getInt(BluetoothDevice.EXTRA_BOND_STATE, -1);
                            switch (state) {
                                case BluetoothDevice.BOND_BONDING:
                                    Logger.debug("BOND_BONDING");
                                    break;
                                case BluetoothDevice.BOND_BONDED:
                                    Logger.debug("BOND_BONDED");
//                                    isBTBondBonded = true;
                                    break;
                                case BluetoothDevice.BOND_NONE:
                                    Logger.debug("BOND_NONE");
                                    publishProgress("hideProgressBar");
                                    break;
                            }
                            break;
                        }
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                            Logger.debug("ACTION_ACL_DISCONNECTED");

                            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
                            if (device != null)
                                Logger.info(device.getName() + " send ACTION_ACL_DISCONNECTED event");

                            {
                                try {
                                    Logger.debug("Connected device = " + device.getName() + " , Address() = " + device.getAddress());
                                    Logger.debug("connectedThreadMap=" + connectedThreadMap);
                                    Logger.debug("scannerAddressMap=" + scannerAddressMap);
                                } catch (Exception ignored) {

                                }
                                if (connectedThreadMap != null && scannerAddressMap != null && device != null) {
                                    if (scannerAddressMap.containsKey(device.getAddress())) {
                                        if (connectedThreadMap.containsKey(scannerAddressMap.get(device.getAddress()))) {
                                            connectedThreadMap.remove(scannerAddressMap.get(device.getAddress()));
                                            Logger.debug("connectedThreadMap.remove:" + scannerAddressMap.get(device.getAddress()));
                                        }
                                    }
                                }

                                if (scannersMap != null) {
                                    if (scannerAddressMap.containsKey(device.getAddress())) {
                                        if (scannersMap.containsKey(scannerAddressMap.get(device.getAddress()))) {
                                            RemoteDeviceInfo scannerInfo = scannersMap.get(scannerAddressMap.get(device.getAddress()));
                                            if (scannerInfo != null) {
                                                if (scannerInfo.getSelected()) {
                                                    Logger.debug("Clear the selected scanner info due to disconnect event get");
                                                    MainActivity.setBtSerialNo("");
                                                    TargetScanner.setIsConnected(false);
                                                    MainActivity.changeSocket(null);
                                                    sendDataQueue.clear();
                                                }
                                            }
                                            BluetoothSocket sk = scannerInfo.getBtSocket();
                                            try {
                                                if (sk != null) {
                                                    OutputStream op = sk.getOutputStream();
                                                    op.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                if (sk != null) {
                                                    InputStream ip = sk.getInputStream();
                                                    ip.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (sk != null) {
                                                try {
                                                    sk.close();
                                                    Logger.debug("sk.close();");
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                            scannersMap.remove(scannerAddressMap.get(device.getAddress()));
                                            Logger.debug("btDeviceConcurrentHashMap.remove:" + scannerAddressMap.get(device.getAddress()));
                                        }
                                    }
                                }
                            }
                            publishProgress("hideProgressBar");
                            if (MainActivity.handler != null) {
                                Logger.debug(" send ACTION_ACL_DISCONNECTED event:publishProgress");
                                if (scannerAddressMap.get(device.getAddress()) != null)
                                    publishProgress("toast", scannerAddressMap.get(device.getAddress()) + " disconnected");

                                scannerAddressMap.remove(device.getAddress());
                            }

                            break;
                        }
                        case AllUsageAction.serviceExit: {
                            Logger.debug("EXIT");
                            onDestroy();
                            stopSelf();
                            break;
                        }
                        case ACTION_REBOOT:
                        case ACTION_SHUTDOWN: {
                            exitAPP();
                            break;
                        }
                    }
                }
                return null;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final PendingResult pendingResult = goAsync();
            new Task(pendingResult, intent, context).execute();
        }
    };


    //-------------------------- Thread-------------------------------------------------------------
    private final Thread popCallAcceptConnectionQueueThread = new Thread(() -> {
        while (isAcceptConnectionQueue) {
            try {
                if (!callAcceptConnectionQueue.isEmpty()) {
                    callAcceptConnectionQueue.remove().run();
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    private final Thread popSendDataQueueThenSendPackageThread = new Thread(() -> {
        final int timeout = 5000;
        int errorCount = 0;
        while (isSendDataQueue) {
            if (!sendDataQueue.isEmpty()) {
                //---------------------------SortACK------------------------------------------------
                SendDataType[] passArray = new SendDataType[sendDataQueue.size()];
                SendDataType[] array = sendDataQueue.toArray(passArray);
                int arrayIndex = 0;
                for (SendDataType a : passArray) {
                    if (a == null || a.getCommandName() == null || !a.getCommandName().contains("SEND_ACK")) {
                        arrayIndex++;
                    } else {
                        break;
                    }
                }
                if (arrayIndex != 0 && arrayIndex != array.length) {
                    SendDataType temp = array[0];
                    array[0] = array[arrayIndex];
                    array[arrayIndex] = temp;
                    sendDataQueue.clear();
                    sendDataQueue = new ConcurrentLinkedQueue <>(Arrays.asList(array));
                }
                //----------------------------------------------------------------------------------
                SendDataType mySendData = sendDataQueue.poll();
                byte[] data = new byte[0];

                String commandName = null;
                if (mySendData != null) {
                    data = mySendData.getData();
                    commandName = mySendData.getCommandName();
                    Logger.debug("SendDataType = " + mySendData.toString());
                }
                //----------------------------------------------------------------------------------
                try {
                    long start = System.nanoTime();
                    tryAcquireSemaphoreResult = MainActivity.btSentSemaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
                    long end = System.nanoTime();
                    Logger.debug("btSentSemaphore write :" + showPacket(data));
                    long elapsedTime = end - start;
                    // 1 second = 1_000_000_000 nano seconds
                    double elapsedTimeInMillisecond = (double) elapsedTime / 1_000_000.0;
                    Logger.debug("elapsedTimeInMillisecond:" + elapsedTimeInMillisecond);
                    if (elapsedTimeInMillisecond > timeout) {
                        String timeoutMessage = "Sending command(s) to " + MainActivity.getBtSerialNo() + " timeout occurred, please try again!\n";
                        Logger.debug(timeoutMessage + "elapsedTimeInMillisecond over " + timeout + "Millisecond:" + elapsedTimeInMillisecond);
                        executorService.execute(() -> {
                            Logger.debug("hideProgressBar");
                            MainActivity.hideProgressBar();
                            setServiceNotification(1);
                        });
                        if (MainActivity.btSentSemaphore.availablePermits() == 0) {
                            MainActivity.btSentSemaphore.release();
                            Logger.debug("btSentSemaphore.release() due to timeout");
                        }
                        sendDataQueue.clear();
                    }

                } catch (Exception ex) {
                    Logger.error(ex);
                    continue;
                }
                //----------------------------------------------------------------------------------
                try {

                    final byte[] chunk = Arrays.copyOfRange(data, 0, data.length);
                    if (connectedThreadMap.get(MainActivity.getBtSerialNo()) != null) {
                        try {
                            ConnectedObject tmpConnectThread = connectedThreadMap.get(MainActivity.getBtSerialNo());
                            if (tmpConnectThread != null) {
                                tmpConnectThread.write(mySendData);
                            }
                            Logger.info("write data to device:" + MainActivity.getBtSerialNo() + " , action = " + commandName + ", data = " + showPacket(chunk) + ", Thread = " + connectedThreadMap.get(MainActivity.getBtSerialNo()));
                            sendDataQueue.remove(mySendData);
                        } catch (Exception ex) {
                            Logger.error(ex.toString());
                            errorCount++;
                            if (errorCount > 2) {
                                sendDataQueue.remove(mySendData);
                                Logger.warn("Remove send data from sendDataQueue due to fail 3 times");
                            }
                        }
                    }

                    if (commandName != null && commandName.contains("SEND_ACK") && tryAcquireSemaphoreResult && MainActivity.btSentSemaphore.availablePermits() == 0) {
                        MainActivity.btSentSemaphore.release();
                        Logger.debug("btSentSemaphore.release() due to send ack");
                    }


                } catch (Exception ex) {
                    Logger.error(ex);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    //----------------------------------------------------------------------------------------------
    //endregion
    //==============================================================================================
    //region LifeCycle

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.debug("onStartCommand in MainService");
        usuApi = new ApiLocal(getApplicationContext());
        scannersMap = new ConcurrentHashMap <>();//key:serial number
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(MainActivity.NOTIFICATION_SERVICE);
        startForeground(1, showNotification(0));
        callAcceptConnectionQueue = new ConcurrentLinkedQueue <>();//ConcurrentLinkedQueue Runnable
        sendDataQueue = new ConcurrentLinkedQueue <>();
        connectedThreadMap = new ConcurrentHashMap <>();
        scannerAddressMap = new ConcurrentHashMap <>();
        isRunning = true;
        isSendDataQueue = true;
        isAcceptConnectionQueue = true;
        connectedThreadPoolExecutor = new ThreadPoolExecutor(2, 7, 5L, TimeUnit.SECONDS, new ArrayBlockingQueue <>(7), new ThreadPoolExecutor.DiscardOldestPolicy());

        if (defaultPref == null)
            defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!popCallAcceptConnectionQueueThread.isAlive()) {
            popCallAcceptConnectionQueueThread.start();
        }
        if (!popSendDataQueueThenSendPackageThread.isAlive()) {
            popSendDataQueueThenSendPackageThread.start();
        }
        //------------------------------------------------------------------------------------------
        enablePublicReceiver(true);
        enableLocalReceiver(true);
        setAllNoneValuePreference();

        boolean startFloatingService = defaultPref.getBoolean(getString(R.string.setting_FloatingButton), AllDefaultValue.setting_FloatingButton);
        enableFloatingButtonService(startFloatingService);
        //------------------------------------------------------------------------------------------

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.debug("onDestroy()");
        isRunning = false;
        if (isSendDataQueue) isSendDataQueue = false;
        if (isAcceptConnectionQueue) isAcceptConnectionQueue = false;
        if (connectedThreadPoolExecutor != null) connectedThreadPoolExecutor.shutdown();
        try {
            for (Map.Entry <String, ConnectedObject> deviceInfoEntry : connectedThreadMap.entrySet()) {
                deviceInfoEntry.getValue().disconnect();
            }
            connectedThreadMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        enablePublicReceiver(false);
        enableLocalReceiver(false);
        super.onDestroy();
    }

    //endregion


    //==============================================================================================
    //region Util

    private Notification showNotification(int notificationStatus) {
        //------------------------------------------------------------------------------------------
        String channelName = "USU Service Notification";
        String contentTitle = "USU Now Status";
        String contentText = "Unitech Scanner Utility Service";
        int icon = R.mipmap.ic_launcher;
        long when = System.currentTimeMillis();
        Notification.Builder builder;
        //------------------------------------------------------------------------------------------
        switch (notificationStatus) {
            case 0:
                contentTitle = "USU has not be paired yet.";
                contentText = "Please scan the pairing barcode to connect.";
                icon = R.drawable.main_disconnected;
                break;
            case 1:
                contentTitle = "USU is paired.";
                contentText = "";
                icon = R.drawable.main_connected;
                break;
            case 2:
                contentTitle = "USU is paired.";
                contentText = "USU is now setting up settings.";
                icon = R.drawable.main_progress;
                break;
        }
        //------------------------------------------------------------------------------------------
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(getPackageName(), channelName, NotificationManager.IMPORTANCE_LOW);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setSound(null, null);
            notificationManager.createNotificationChannel(mChannel);
            builder = new Notification.Builder(getApplicationContext(), getPackageName())
                    .setAutoCancel(true)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setWhen(when)
            ;
            builder.setVibrate(null);
            builder.setSound(null);
            builder.setShowWhen(false);

        } else {
            builder = new Notification.Builder(getApplicationContext())
                    .setAutoCancel(true)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setWhen(when)
            ;
        }
        //------------------------------------------------------------------------------------------
        return builder.build();
    }

    private void setServiceNotification(int notificationStatus) {
        notificationManager.notify(1, showNotification(notificationStatus));
    }

    private void sendKeyDownUpSync(String data) {
        new Instrumentation().sendStringSync(data);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendKeyCopyPaste(String data) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        Instrumentation inst = new Instrumentation();
        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", data));
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_PASTE);
    }

    //endregion
    //==============================================================================================
    //region Preferences

    public void resetSettings(int broadcast, String packageName) {
        try {
            defaultPref.edit().clear().apply();

            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_menu_scanner, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_menu_application, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_menu_label_formatting, false);

            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_upc_ean_jan, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_code_128, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_code_39, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_code_93, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_code_11, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_i_two_of_five, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_discrete2of5, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_codabar, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_msi, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_matrix_2_of_5, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_gs1_data_bar, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_composite, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_two_d_symbologies, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_postal_codes, false);
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings_sym_others, false);
            //8 specific preferences require additional settings
            defaultPref.edit()
                    .putString(getString(R.string.setting_Password), AllDefaultValue.setting_Password)
                    .putString(getString(R.string.setting_DataAction), AllDefaultValue.setting_DataAction)
                    .putString(getString(R.string.setting_StringData), AllDefaultValue.setting_StringData)
                    .putString(getString(R.string.setting_StringDataType), AllDefaultValue.setting_StringDataType)
                    .putString(getString(R.string.setting_StringDataLength), AllDefaultValue.setting_StringDataLength)
                    .putString(getString(R.string.setting_StringDataByte), AllDefaultValue.setting_StringDataByte)
                    .putString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting)
                    .putString(getString(R.string.setting_Replace), AllDefaultValue.setting_Replace)
                    .apply();

            Logger.debug("resetSettings");
            enableFloatingButtonService(false);
            executorService.execute(() -> App.toast(getApplicationContext(), "Reset All Settings Success!"));
            Bundle bundle = new Bundle();
            bundle.putInt("result", 0);
            if (broadcast == 0) {
                sendBroadcast(new Intent(AllUsageAction.apiResetSettingsReply).putExtras(bundle).setPackage(packageName));
            } else {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.apiResetSettingsReply).putExtras(bundle));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bundle bundle = new Bundle();
            bundle.putInt("result", 1);
            bundle.putString("message", "Reset fail");
            if (broadcast == 0) {
                sendBroadcast(new Intent(AllUsageAction.apiResetSettingsReply).putExtras(bundle).setPackage(packageName));
            } else {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.apiResetSettingsReply).putExtras(bundle));
            }
        }

    }

    public void Upload(int broadcast, String packageName) {
        String tempKey = "";
        String tempValue = "";
        try {
            Map <String, ?> allEntries = defaultPref.getAll();
            Bundle bundle = new Bundle();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
                tempKey = entry.getKey();
                tempValue = entry.getValue().toString();
                if (isInteger(tempValue)) {
                    sb.append(entry.getKey()).append(":").append(Integer.parseInt(entry.getValue().toString())).append("\n");
                    bundle.putInt(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
                }
            }
            Logger.debug("Upload get integer settings = \n" + sb.toString());

            App.toast(getApplicationContext(), "Start Upload");
            int btSignalLevel = 0;
            String bt_signal_level = defaultPref.getString(getString(R.string.setting_BT_signal_checking_level), "0");
            if (bt_signal_level != null) {
                btSignalLevel = Integer.parseInt(bt_signal_level);
            }
            usuApi.setBTSignalCheckingLevel(btSignalLevel);//Normal

            bundle.putBoolean("upload", true);
            if (broadcast == 0) {
                ApiPublic api = new ApiPublic(getApplicationContext(), packageName);
                api.setConfiguration(bundle);
            } else {
                usuApi.setConfiguration(bundle);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
            Logger.debug("Error key = " + tempKey + " , Error value = " + tempValue + "\n" + error);
            executorService.execute(() -> {
                Logger.debug("hideProgressBar");
                MainActivity.hideProgressBar();
                setServiceNotification(1);
            });
            String message = "Upload fail";
            Bundle bundle = new Bundle();
            bundle.putInt("result", 1);
            bundle.putString("message", "Upload fail");
            if (broadcast == 1) {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.apiUploadSettingsReply).putExtras(bundle));
            } else {
                bundle.putString("packageName", packageName);
                sendBroadcast(new Intent(AllUsageAction.apiUploadSettingsReply).putExtras(bundle).setPackage(packageName));
            }
            App.toast(getApplicationContext(), message + "\n" + error);

        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void ExportSettings(String filepath, String customPackage, int broadcast) {
        setAllNoneValuePreference();
        boolean success = true;
        String message = null;
        String settings = getAllSettings();
        Logger.debug("filepath = " + filepath + "\nExportSettings = " + settings);
        String encryptSettings = encrypt(settings);
        if (filepath == null || filepath.equals("")) {
            message = "File not found or no settings.";
            success = false;
        }
        if (success) {
            success = isExternalStorageWritable();
            if (!success) {
                message = "External Storage isn't Writable";
            }
        }
        if (success) {
            if (encryptSettings == null) {
                message = "Encryption failed.";
                success = false;
            }
        }
        if (success) {
            try {
                if (!savePreferenceToFile(filepath, encryptSettings)) {
                    Logger.debug("Save with activity result.");
                    exportBundle = new Bundle();
                    exportBundle.putString("filepath", filepath);
                    exportBundle.putString("customPackage", customPackage);
                    return;
                }
            } catch (Exception e) {
                Logger.error("Save file fail. Error message = " + e.getMessage());
                success = false;
                message = "Export settings fail." + e.getMessage();
            }
        }
        int result = success ? 0 : 1;
        Logger.debug("ExportSettingsReply "
                + "result = " + result + ", "
                + "packageName = " + customPackage + ", "
                + "message = " + message
        );
        if (broadcast == 1) {
            usuApi.exportSettingsReply(result, message);
        } else {
            sendBroadcast(new Intent(AllUsageAction.apiExportSettingsReply)
                    .putExtra("result", result)
                    .putExtra("packageName", customPackage)
                    .putExtra("message", message)
                    .setPackage(customPackage)
            );
        }
        if (success) {
            message = "Export settings success.";
        }
        Logger.debug(message);
        App.toast(getApplicationContext(), message);
    }

    public int ImportSettings(String filepath, String passcode, String customPackage, int broadcast) {
        boolean check_v1;
        boolean success;
        String message = null;

        success = isExternalStorageReadable();
        if (!success) {
            message = "External Storage isn't Readable";
        }
        if (success) {
            success = IOUtil.fileExist(filepath);
            if (!success) {
                message = "File is not exist.";
            }
        }
        String importContent = IOUtil.readFile(filepath);
        if (success) {
            if (importContent == null || importContent.equals("")) {
                message = "Storage has no data.";
                success = false;
            }
        }
        String jsonSetting = "";
        if (success) {
            if (IOUtil.isJSONValid(importContent)) {
                String password = Objects.requireNonNull(defaultPref.getString(getString(R.string.setting_Password), AllDefaultValue.setting_Password)).trim();
                if (Objects.requireNonNull(password).equals(passcode)) {
                    jsonSetting = importContent;
                } else {
                    message = "Wrong password.";
                    success = false;
                }
            } else {
                String decryptedSettings = decrypt(importContent);
                if (decryptedSettings == null || !IOUtil.isJSONValid(decryptedSettings)) {
                    message = "Decryption failed.";
                    success = false;
                }
                jsonSetting = decryptedSettings;
            }
        }
        Logger.debug("ImportSettingsReply jsonSetting = " + jsonSetting);
        if (success) {
            success = json2Pref(jsonSetting);
            if (!success) {
                message = "ImportSettings failed.";
                success = false;
            }
        }
        if (!success) {
            check_v1 = loadSharedPreferencesFromFile(new File(Environment.getExternalStorageDirectory(), "USUConfig.001"), "");
            check_v1 &= loadSharedPreferencesFromFile(new File(Environment.getExternalStorageDirectory(), "USUConfig.002"), getPackageName());
            if (check_v1) {
                success = true;
            }
        }
        if (success) {
            setAllNoneValuePreference();
        }
        int result = success ? 0 : 1;
        Logger.debug("ImportSettingsReply "
                + "result = " + result + ", "
                + "packageName = " + customPackage + ", "
                + "message = " + message
        );
        if (broadcast == 1) {
            usuApi.importSettingsReply(result, message);

        } else {
            sendBroadcast(new Intent(AllUsageAction.apiImportSettingsReply)
                    .putExtra("result", result)
                    .putExtra("packageName", customPackage)
                    .putExtra("message", message)
                    .setPackage(customPackage)
            );
        }
        if (success) {
            message = "Import settings success.";
        }
        Logger.debug(message);
        App.toast(getApplicationContext(), message);


        return result;

    }

    private boolean savePreferenceToFile(String filePath, String writeDown) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (filePath == null) {
                Logger.error("savePreferenceToFile filepath == null");
                throw new Exception("The filepath is null.");
            }
            if (saveFileTargetOfInternal(filePath, writeDown)) {
                return true;
            }
            int checkUriStorage = checkStorage(filePath);

            switch (checkUriStorage) {
                case -1:
                    throw new Exception("Not found storage path");
                case 0:
                    //Has uri
                    break;
                case 1:
                    //Check uri
                    return false;
            }

            if (IOUtil.fileWriteFromURI(getApplicationContext(), filePath, writeDown)) {
                return true;
            }
        } else {
            if (saveFileTargetOfInternal(filePath, writeDown)) {
                return true;
            }
        }
        throw new Exception("Save file not success");
    }

    private int checkStorage(String targetFile) {
        ArrayList <String> allPath = DocumentFileUtil.getExtSdCardPaths(getApplicationContext());
        Logger.tag(AllUITag.mainActivity).debug("allPath = " + allPath);
        for (String path : allPath) {
            String[] locationArray = path.split("/");
            if (locationArray.length > 2) {
                String location = locationArray[locationArray.length - 1];
                if (targetFile.contains(location)) {
                    if (mUriArray != null && mUriArray.size() > 0) {
                        boolean hasPermission = false;
                        for (Uri uri : mUriArray) {
                            if (uri.getPath() != null && uri.getPath().contains(location)) {
                                hasPermission = true;
                            }
                        }
                        if (!hasPermission) {
                            continue;
                        }
                        return 0;
                    } else {
                        if (getPersistablePermission(path)) {
                            return 1;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private boolean getPersistablePermission(String storagePath) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            return false;
        }
        File file = new File(storagePath);
        if (file.canWrite()) {
            return false;
        }
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume = storageManager.getStorageVolume(file);

        if (volume == null) {
            String path = file.getPath();
            List <StorageVolume> vols = storageManager.getStorageVolumes();
            if (vols.size() > 0) {
                for (StorageVolume sv : vols) {
                    Logger.tag(AllUITag.mainActivity).debug(String.format("sv %s : %s, removable: %b, primary: %b, isEmulated: %b",
                            sv.getUuid(), sv.getState(), sv.isRemovable(), sv.isPrimary(), sv.isEmulated()));

                    if (sv.getUuid() != null && sv.isRemovable() && path.contains(sv.getUuid())) {
                        Logger.tag(AllUITag.mainActivity).debug(String.format("Found target volume %s", sv.getUuid()));
                        volume = sv;
                    }
                }
            }
        }

        if (volume != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Logger.tag(AllUITag.mainActivity).debug("ASK REQUEST_EXT_STORAGE_ACCESS_Q");
                Intent intent = volume.createOpenDocumentTreeIntent();
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                int REQUEST_EXT_STORAGE_ACCESS_Q = 4011;
                startActivityForResult(MainActivity.getInstance(), intent, REQUEST_EXT_STORAGE_ACCESS_Q, null);
            } else {
                Logger.tag(AllUITag.mainActivity).debug("ASK REQUEST_EXT_STORAGE_ACCESS");
                Intent intent = volume.createAccessIntent(null);
                if (intent == null) {
                    Logger.tag(AllUITag.mainActivity).error("getPersistablePermission intent = null");
                    return false;
                }
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                int REQUEST_EXT_STORAGE_ACCESS = 4010;
                startActivityForResult(MainActivity.getInstance(), intent, REQUEST_EXT_STORAGE_ACCESS, null);
            }
        } else {
            Logger.tag(AllUITag.mainActivity).error("takeCardUriPermission: still cannot get correct storage volume");
            return false;
        }
        return true;
    }

    private boolean saveFileTargetOfInternal(String filePath, String writeDown) {
        File file = new File(filePath);
        try {
            if (!IOUtil.mkdir(file)) {
                return false;
            }
            return IOUtil.fileWrite(filePath, writeDown, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setAllNoneValuePreference() {
        String defaultSettings = "+5jC2C/HkRsaFOTNXBfEzcP4QYP7yf0Zaqowj/VlGslt/2qWFiHiqQcF0N0SGgXElKwsJsYiWLvE\n" +
                "DLgnjco+kxWx1FVnKHVwsUyFfossQr0kwWhW368PajEnVukAW+3cPvC6eSZY7OHkZn1RnapBtUIL\n" +
                "uTr99xp8NVXvsFOz7g8ONbZIq0q7rVuOMJbvnyAOb84aoalPX9VEOCTLju2nCaMjPQFzqcJN4sOL\n" +
                "WiYJVcFZgKx7NJ3d26SxOljUXkui5sIUcBwLOjKkbSI1xPEaK3xcJCil65IADOpsbzdXVsDR9sEU\n" +
                "lmGwHQ9nzW+451wJYmUP0aaznfiZGYjn9/WVxkxDKjJZirgopny3xZZk1kOxdpMxy/1CZiKlLtc0\n" +
                "d4TTpC1I4G1c6X1n86vTBJByIXTHvbGiM+IOM8AtcmYYHMe8V8MG7g9p1BZbuGtiJGQQz0c+dPA7\n" +
                "A2Bpw95xvxyZo4ixasrebrCOkQxTN+SL15ZvsEWHc8ty1sGOY1UrdoV3tFZ8bGBOgx70xY15X9fu\n" +
                "ap2m47+hP7pPTxAxyWiRGA7fH2Hk2FgS/Gj4zOWQOmy7fGHr0JXhJNnJ1PHIACpBSS+mP11Ao+VX\n" +
                "g3gTnd9R/AuiAQYS8HF44xM085UPmudtmCV9J9gOXE9yOvpdS2oLSQ5wiUFW3crZz0F4wVfbiKye\n" +
                "rzLDQDYAHng8MeiEpmnoJUyvpFUnqFNT/61dJ9RdMuY76+rwBNmy1E7qGNrXFYIPwUo0xwR1pRf4\n" +
                "wiPpC2XjhWE3XmJGfxHFQ6ZG+I9nNAmoEqRiuAbDkcSn+8SLXJVRgSRJTf+IcDMd2FFuDakq/SFF\n" +
                "BiKh4Bx/OMgowHIJCVuMHMFxhuiRrYvWdJr/as9ydrt8GDAfCO7xJgCm68svSiGkiYe6Byl9Z3zc\n" +
                "Rlj6ZI7It2vSIMXInqC5OdrLw/kP0bwIlyuD+hdMLS9DcK+KuEo0YBfITjeUIqey6md2PYeiFayZ\n" +
                "7MAeYf3fB7noA5XtnF2/hskGqTf/a//H+lkdh9yLFQuNUCtEIbRXivBX04oBiRnL0xv3vJioRlyx\n" +
                "C15b7IULpzX8dVYvnidq3Oa9OfMjwx3G7F4Paho2gO8qfDcdIrYNlaoTQh53QssvrbjHsHHAxiGv\n" +
                "qNBgWuWpeQ1Xl5mjRlbLMVRUH4nYGSEIbnii2pqsCx4e00Qjkam25S0ru8Si+fFMvAv4Q6yndXvS\n" +
                "BSRSS0mKedHZW9FOXRetXPtsXu5BDgjrJauIxCg/PbLSy7ekddO0pOxsLkc5w3CBTnS+nhmb3BWu\n" +
                "xNdkb0Akn0YnTIEqcEDH/BxaDfbjpN1AyfQf0GCO9zoVJR5Zc3gxOB3QpUGYUVg+pzht50zFZy00\n" +
                "p7hTVuwzlUI+iEqwJRQT3kNn7fu5LJjZgUtwWF7Xj+cGAY1NfPnSA70c5CRJg6Bu5P5VXO4bwy4b\n" +
                "nr3P1dC8ChSqy01x6uyd4+fWBKRv5AVrfohO7oZuyiT6cJ0BkfuVbbnodWs1Ti7aqY+KiVpWryOq\n" +
                "vzFujld5iSIFhXmRaZ3JQ8rN0kkRXMsPUUSYRBTF4VZxM3TnfuNTJGPRBuZWZsKc/q14YZk0XW7z\n" +
                "mt0KcIv1UoXWM1UPeVogt9HCG4iMOtgF9WRMP2pbFjZ4K7d/bMGgyB+lzV+KOpYxD9n0wE9gBE+x\n" +
                "mYW3Wij/QaXZDH/m37DjdRRZupYctbH1kTs8S8zjInpfRlxX8hIS4MCRqrwEwiRs+IjsXmkicDe8\n" +
                "FcrcEcEyHd9Nksil3TBCJJ7zL41OHE6IZm1RI+PcEPRvoOOoVV1cB02XlLXRr4Iq9IDqoU8EV6Fc\n" +
                "AzeZut7jejMsjvjjuL78jkowRlFwI1DCNyoJ2VwNAO/VKM2IN49bKLrTXopSobmABgGOdgr+PSk6\n" +
                "8OOB993oZ2rT1R3CvVHy98cFhVE4lTmq1tNT3EqTVwjdiJC+mE4NS8zDcilbVn5HOw57bdvw6gJu\n" +
                "jkJotn9Pq00hb3dW62VOc0U5BuI2qdB3xsnxFAWjpDvymmvduTLENZEAbiqJXEHj2xf5cgnjNsuE\n" +
                "STmgqtiLDkyVlIqONubpJ/aIKOuH4JOJvK+doIsDyWzONvEnf3TcrCJgoXIxogi8d47w9N5FnJXJ\n" +
                "kWs9gnWtBsFdKI3GTZl7DpAUU6FQwED4w6M4g6ZeP+oVyNKgi2EFnUr34Esf0RbVFy5NLBbOe8mN\n" +
                "3C4z7kkHkrlt2+KrF02+TGzR9x9qhTn2pvRcRhCKdS5panVP7ab37OLXiLkKSqU6+HvQkCNAB2TU\n" +
                "zTOtCLpxJ1SCW6rSpkRrXw36y78F97HN+xvXa1x0u0rMMFAS48e4rbZGmoQJBPsJXPwfYwCsLJqj\n" +
                "2d/ps3HXoMbUT6qXly29jh8sdfckiBB2kdI0GfAzIuOg7PsrjdOz3CneIlW8ZqFmRAc0RlAg+D0t\n" +
                "/0qSpztLm5ay8iXghPMEOho3uRChmHcl7KZcvIKbAnvVBFc7kL+K0SIbkHHwT2E7MJRUbk4+APHG\n" +
                "kiSA6hXF9FR+SwH0Ak+7ZuOCAkHvaJ2G4hbOo6hqaJBesBvaJnI8Rav6pxhQaPPN8ox1s8zOV4nl\n" +
                "YD7TZBAkQZcEcDm6HhLHjs7QcltWJCT+ii3ATHy0OJuK2F43UhypxBiiC3GXWy684HwNcjGkbXgH\n" +
                "rb71d2RRAH2Qk873CYg5Nb8Ko7FJwVOx3wdPQvIHc+VHyEDklh0InQGKtpBKT8tK1hXQCmFiKHEs\n" +
                "iNxYeVwOME0YIqHd8Tqkbn6nhuVnpUJyt5aAk64rEI1yZIxtTRyjf3bwsHuAlh5uZQo36VKit5hL\n" +
                "HpYI2SXmzYduUhBTcM9gigWURuotQ110K0vajJNHIbvrbm0OSSmDdteglypg0caM7/BRr5llitvk\n" +
                "0ihNfs3V2HwsVyswFUpYawTqXS9hyypZX5X0docXP/uHhHUApS6ce20qui/menEoWdrtHJureKki\n" +
                "KLGL7HTMVPQ5LTU92oWfD+gm4MffpyEfYkXgxshiwTc6mpWGNgLwFXLwP+LZGB20OguEpjTA0wYy\n" +
                "0i0NPds02SlCV2bPkGJ5rLCJISq4XUKb8hM6FTEQAMimJ36KcUzciXSva8Ebl1zDItSDV3hTqjuZ\n" +
                "d7ooweCQR9ypk9muv8da1/X/w3YiV0maZ8uIvo1u5K/IJMqCvJ7PLM78lQNB4PoxvzI3cZij4Z/m\n" +
                "0ZSx0u5F0w0TKyVfK8ZjyWk6ZMzF4x+x5hIeJl0vllEo+BmP4wOYsSL3ViLxLVEcfZidMSjdpq1W\n" +
                "VDFT9eMGxfog1RkWo8lfQHeiTDj+UIMB+9x48zu0mzrQoovw5p/KgZdAsRJhpRM3cokRUG+cdOkv\n" +
                "qH5DpvSf7wEy5M+q73RQyjGIC5P/+c2hSWtouacYOuUidtTyS3LFZO3GAQadX72AHPmgRkRQioiv\n" +
                "GLJ9YEbog3Y8AVB4YbVlrj+xuQhKm+7VZBN5eW6PKHJMJTal8Vqfhj/O+4IzXb2noqfeDLrV3vxu\n" +
                "+OujcPx/MhYh2aRlnU5FW9QHiex3LrANLuXivKDX5Q6QpujNeW0jb8FPfFPj/yMtaIlu985BZrOa\n" +
                "eQQKDF5F/yKdK0rp7iI7W+dxRh919HKSYJejxcKaDaUHoxawqkZkduf39mFOndLLH3Vc6K6pzJGc\n" +
                "io8LScRDqv1SY5ptCyRTmHIPmec3Q7flSxUzgm+tuzaI4SHpPc9giwSGKEVtfy8dqaXNKvRcJlFR\n" +
                "Pg4jD+7q/hsX4TA4vgeIVEyQOidJQxxiAEMqionz4C+4dQvFVhPKS7D+Awsu2MgRDiTNYflZJ1/A\n" +
                "nqQozPWyvb7L2Dnlf6vlYWUkVHv+UKsrFgQ/qfuFwl8+aZ76zOS5PvNTuLXo4Ze0S4CJ+NSXdHdp\n" +
                "zmeg2xCTxappxc3u/bJdC9ph4EDUmsC/K3Yyyh6BySHwnyCR4IRsIjZTb3PoyRHjnH96N8NQZUYV\n" +
                "NeJmGYiklzAr1NgoQLZFKbsSBxP49VMJyDVw5G1lMcPq3Ho19pp6p922ifwaXv4cI/dyVPNA0CEL\n" +
                "NKsPanLY2lroe7SpUJ6lkHwfIj5738hmMHwHh+pxvMfkEj4PR5XojfyIjiHB+o9gJ6Cj8HYtTYZr\n" +
                "IKU3ZQtt/QgTmQ==";
        try {
            String decryptedSettings = decrypt(defaultSettings);
            if (decryptedSettings == null) {
                Logger.error("setAllNoneValuePreference decrypt default value error");
                return;
            }
            JSONObject jsonObject = new JSONObject(decryptedSettings);
            SharedPreferences.Editor editor;
            Iterator <String> keys;
            //--------------------------------------------------------------------------------------
            editor = defaultPref.edit();
            keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object v = jsonObject.get(key);
                //json only boolean and string
                if (v instanceof Boolean) {
                    boolean bValue = defaultPref.getBoolean(key, (boolean) v);
                    editor.putBoolean(key, bValue);
                } else if (v instanceof String) {
                    if (key.equals(getString(R.string.setting_EnableDataIntent))) {
                        if (defaultPref.getStringSet(key, null) == null) {
                            editor.putStringSet(key, AllDefaultValue.setting_EnableDataIntent);
                        }
                    } else {
                        if (defaultPref.getString(key, null) == null) {
                            editor.putString(key, ((String) v));
                        }
                    }
                }
            }
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getAllSettings() {
        JSONObject jsonObject = new JSONObject();
        try {
            Map <String, ?> defaultSettings = defaultPref.getAll();

            for (Map.Entry <String, ?> entry : defaultSettings.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                jsonObject.put(key, v);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private boolean json2Pref(String settings) {
        try {
            JSONObject jsonObject = new JSONObject(settings);
            SharedPreferences.Editor editor;
            Iterator <String> keys;
            //--------------------------------------------------------------------------------------
            editor = defaultPref.edit();
            keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object v = jsonObject.get(key);
                if (v instanceof Boolean) {
                    boolean value = (Boolean) v;
                    editor.putBoolean(key, value);
                    if (key.equals(getString(R.string.setting_FloatingButton))) {
                        enableFloatingButtonService(value);
                    }
                } else if (v instanceof String) {
                    if (key.equals(getString(R.string.setting_EnableDataIntent))) {
                        String temp = ((String) v).replace("[", "").replace("]", "").replace(" ", "");
                        String[] spiltValue = temp.split(",");
                        HashSet <String> hashSet = new HashSet <>();
                        if (spiltValue.length > 0) {
                            hashSet.addAll(Arrays.asList(spiltValue));
                        }
                        editor.putStringSet(key, hashSet);
                    } else {
                        editor.putString(key, ((String) v));
                    }
                }
            }
            editor.apply();
            //--------------------------------------------------------------------------------------
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @SuppressWarnings({"unchecked"})
    private boolean loadSharedPreferencesFromFile(File src, String prefName) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences pref;
            if (prefName.trim().equals("")) {
                pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
            } else {
                pref = getSharedPreferences(prefName, Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = pref.edit();
//            editor.clear();
            Object object = input.readObject();
            Map <String, ?> entries = (Map <String, ?>) object;
            for (Map.Entry <String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    editor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    editor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    editor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    editor.putLong(key, (Long) v);
                else if (v instanceof String)
                    editor.putString(key, ((String) v));
            }
            editor.apply();
            Logger.debug("loadSharedPreferencesFromFile = " + pref.getAll());
            res = true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Logger.error(e.toString());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.error(ex.toString());
            }
        }
        return res;
    }
    //endregion
    //==============================================================================================
    //region Function

    private void enableFloatingButtonService(boolean enable) {
        Logger.debug("enableFloatingButtonService enable = " + enable);
        usuApi.enableFloatingButtonService(enable);
    }

    private void enablePublicReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.apiGetPairingBarcode);//2.1
            filter.addAction(AllUsageAction.apiGetTargetScannerStatus);//2.2
            filter.addAction(AllUsageAction.apiUnpaired);//2.4
            filter.addAction(AllUsageAction.apiGetSN);//2.5
            filter.addAction(AllUsageAction.apiGetName);//2.6
            filter.addAction(AllUsageAction.apiGetAddress);//2.7
            filter.addAction(AllUsageAction.apiGetFW);//2.8
            filter.addAction(AllUsageAction.apiGetBattery);//2.9
            filter.addAction(AllUsageAction.apiGetTrigger);//3.1
            filter.addAction(AllUsageAction.apiSetTrigger);//3.1
            filter.addAction(AllUsageAction.apiStartDecode);//3.2
            filter.addAction(AllUsageAction.apiStopDecode);//3.3
            filter.addAction(AllUsageAction.apiGetAck);//3.4
            filter.addAction(AllUsageAction.apiSetAck);//3.4
            filter.addAction(AllUsageAction.apiGetAutoConnection);//3.5
            filter.addAction(AllUsageAction.apiSetAutoConnection);//3.5
            filter.addAction(AllUsageAction.apiGetConfig);//3.6
            filter.addAction(AllUsageAction.apiSetConfig);//3.6
            filter.addAction(AllUsageAction.apiGetBtSignalCheckingLevel);//3.7
            filter.addAction(AllUsageAction.apiSetBtSignalCheckingLevel);//3.7
            filter.addAction(AllUsageAction.apiGetDataTerminator);//3.8
            filter.addAction(AllUsageAction.apiSetDataTerminator);//3.8
//            filter.addAction(AllUsageAction.apiChangeToFormatSsi);//4.1
//            filter.addAction(AllUsageAction.apiChangeToFormatRaw);//4.1
//            filter.addAction(AllUsageAction.apiGetFormat);//4.1
            filter.addAction(AllUsageAction.apiSetIntentAction);//modify 4.1
            filter.addAction(AllUsageAction.apiGetIntentAction);//modify 4.1
            filter.addAction(AllUsageAction.apiSetIndicator);//4.2
            filter.addAction(AllUsageAction.apiExportSettings);//5.1
            filter.addAction(AllUsageAction.apiImportSettings);//5.2
            filter.addAction(AllUsageAction.apiUploadSettings);//5.3
            filter.addAction(AllUsageAction.apiResetSettings);//5.4
            filter.addAction(AllUsageAction.apiGetScan2key);//5.5
            filter.addAction(AllUsageAction.apiSetScan2key);//5.5
            filter.addAction(AllUsageAction.apiSetOutput);//5.6
            filter.addAction(AllUsageAction.apiGetOutput);//5.6
            filter.addAction(AllUsageAction.apiSetEncoding);//5.7
            filter.addAction(AllUsageAction.apiGetEncoding);//5.7
            filter.addAction(AllUsageAction.apiSetFormattingStatus);//5.8
            filter.addAction(AllUsageAction.apiGetFormattingStatus);//5.8
            filter.addAction(AllUsageAction.apiSetAppend);//5.9
            filter.addAction(AllUsageAction.apiGetAppend);//5.9
            filter.addAction(AllUsageAction.apiSetReplace);//5.10
            filter.addAction(AllUsageAction.apiGetReplace);//5.10
            filter.addAction(AllUsageAction.apiSetRemoveNonPrintableChar);//5.11
            filter.addAction(AllUsageAction.apiGetRemoveNonPrintableChar);//5.11
            filter.addAction(AllUsageAction.apiSetFormatting);//5.12
            filter.addAction(AllUsageAction.apiGetFormatting);//5.12
            filter.addAction(AllUsageAction.apiDeleteFormatting);//5.12
            filter.addAction(AllUsageAction.apiSetFloatingButton);//5.13
            filter.addAction(AllUsageAction.apiGetFloatingButton);//5.13
            filter.addAction(AllUsageAction.apiSetSound);//5.14
            filter.addAction(AllUsageAction.apiGetSound);//5.14
            filter.addAction(AllUsageAction.apiSetVibration);//5.15
            filter.addAction(AllUsageAction.apiGetVibration);//5.15
            filter.addAction(AllUsageAction.apiSetStartup);//5.16
            filter.addAction(AllUsageAction.apiGetStartup);//5.16
            filter.addAction(AllUsageAction.apiSetAutoEnforceSettings);//5.17
            filter.addAction(AllUsageAction.apiGetAutoEnforceSettings);//5.17
            filter.addAction(AllUsageAction.usuExit);
            filter.addAction(AllUsageAction.serviceExit);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_REBOOT);
            filter.addAction(Intent.ACTION_SHUTDOWN);
            registerReceiver(mReceiver, filter);
        } else {
            unregisterReceiver(mReceiver);
        }
    }

    private void enableLocalReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.apiGetPairingBarcode);//2.1
            filter.addAction(AllUsageAction.apiGetTargetScannerStatus);//2.2
            filter.addAction(AllUsageAction.apiUnpaired);//2.4
            filter.addAction(AllUsageAction.apiGetSN);//2.5
            filter.addAction(AllUsageAction.apiGetName);//2.6
            filter.addAction(AllUsageAction.apiGetAddress);//2.7
            filter.addAction(AllUsageAction.apiGetFW);//2.8
            filter.addAction(AllUsageAction.apiGetBattery);//2.9
            filter.addAction(AllUsageAction.apiGetTrigger);//3.1
            filter.addAction(AllUsageAction.apiSetTrigger);//3.1
            filter.addAction(AllUsageAction.apiStartDecode);//3.2
            filter.addAction(AllUsageAction.apiStopDecode);//3.3
            filter.addAction(AllUsageAction.apiGetAck);//3.4
            filter.addAction(AllUsageAction.apiSetAck);//3.4
            filter.addAction(AllUsageAction.apiGetAutoConnection);//3.5
            filter.addAction(AllUsageAction.apiSetAutoConnection);//3.5
            filter.addAction(AllUsageAction.apiGetConfig);//3.6
            filter.addAction(AllUsageAction.apiSetConfig);//3.6
            filter.addAction(AllUsageAction.apiGetBtSignalCheckingLevel);//3.7
            filter.addAction(AllUsageAction.apiSetBtSignalCheckingLevel);//3.7
            filter.addAction(AllUsageAction.apiGetDataTerminator);//3.8
            filter.addAction(AllUsageAction.apiSetDataTerminator);//3.8
            filter.addAction(AllUsageAction.apiChangeToFormatSsi);//4.1
            filter.addAction(AllUsageAction.apiChangeToFormatRaw);//4.1
            filter.addAction(AllUsageAction.apiGetFormat);//4.1
            filter.addAction(AllUsageAction.apiSetIntentAction);//modify 4.1
            filter.addAction(AllUsageAction.apiGetIntentAction);//modify 4.1
            filter.addAction(AllUsageAction.apiSetIndicator);//4.2
            filter.addAction(AllUsageAction.apiExportSettings);//5.1
            filter.addAction(AllUsageAction.apiImportSettings);//5.2
            filter.addAction(AllUsageAction.apiUploadSettings);//5.3
            filter.addAction(AllUsageAction.apiResetSettings);//5.4
            filter.addAction(AllUsageAction.serviceKeyboardInput);
            filter.addAction(AllUsageAction.serviceGetScanner);
            filter.addAction(AllUsageAction.serviceSetScanner);
            filter.addAction(AllUsageAction.serviceSendAck);
            LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter);
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
        }
    }


    //broadcast 0=public,1=local
    private void sendDataPublic(byte[] data, String packageName, String... commandName) {
        sendData(data, data.length, packageName, 0, commandName);
    }

    public void sendDataLocal(byte[] data, String... commandName) {
        sendData(data, data.length, null, 1, commandName);
    }

    private void sendData(byte[] data, int length, String packageName, int broadcast, String... commandName) {
        try {
            if (!MainActivity.getBtSerialNo().contentEquals(""))
                sendDataQueue.add(new SendDataType(data, packageName, SendDataType.BroadcastType.fromValue(broadcast), commandName));
        } catch (Exception ex) {
            Logger.error(ex);
        }
    }

    private byte[] listToArray(ArrayList <Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private ArrayList <Byte> arrayToList(byte[] array) {
        ArrayList <Byte> list = new ArrayList <>();
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }

    public String showPacket(byte[] packet) {
        StringBuilder logString = new StringBuilder();
        for (int x = 0; x < packet.length; x++) {
            logString.append("[").append(x).append("]0x").append(Integer.toString((packet[x] & 0xFF), 16).toUpperCase()).append(" ");
        }
        return logString.toString();
    }

    private List <McuCommand> getSettingConfigCommand(Bundle mBundle) {
        Logger.debug("API_SET_CONFIG");
        //----------------------------------------------------------------------------------
        if (mBundle == null) return null;
        //----------------------------------------------------------------------------------
        //set the return byte command
        List <McuCommand> listMcuCommand = new ArrayList <>();
        PrefCmdTrans prefCmdTrans = new PrefCmdTrans(getApplicationContext(), SupportScanner.ms652Plus);
        ArrayList <Byte> ssiCommandTemp = new ArrayList <>();
        for (String key : mBundle.keySet()) {
            String parameter = prefCmdTrans.getCmd(key);
            if (prefCmdTrans.getCmd(key) != null) {
                SE2707 se2707 = SE2707.valueOf(parameter);
                byte[] paramLength = se2707.getBytes();
                byte value = (byte) mBundle.getInt(key, 0);
                if (ssiCommandTemp.size() > 234) { //mcu 7, ssi 10 , data 4
                    byte[] ssiCommandArray = listToArray(ssiCommandTemp);
                    SsiCommand ssiCommand = new SsiCommand(SsiOpcode.PARAM_SEND, SsiStatus.PACKET_PERMANENT_TOTAL, ssiCommandArray);
                    ssiCommandArray = ssiCommand.byteArray;
                    Logger.debug("ssiCommandArray = " + Arrays.toString(ssiCommandArray));
                    ssiCommandTemp.clear();
                    McuCommand mcuCommand = new McuCommand(McuCommandID.SET_SSI_CONFIG, ssiCommandArray);
                    listMcuCommand.add(mcuCommand);
                }
                ssiCommandTemp.addAll(arrayToList(paramLength));
                ssiCommandTemp.add(value);
            }
        }
        if (ssiCommandTemp.size() > 0) {
            byte[] ssiCommandArray = listToArray(ssiCommandTemp);
            SsiCommand ssiCommand = new SsiCommand(SsiOpcode.PARAM_SEND, SsiStatus.PACKET_PERMANENT_TOTAL, ssiCommandArray);
            ssiCommandArray = ssiCommand.byteArray;
            Logger.debug("ssiCommandArray = " + Arrays.toString(ssiCommandArray));
            ssiCommandTemp.clear();
            McuCommand mcuCommand = new McuCommand(McuCommandID.SET_SSI_CONFIG, ssiCommandArray);
            listMcuCommand.add(mcuCommand);
        }
        return listMcuCommand;
    }

    private void lunchSelectedAPP() {
        String packageName = defaultPref.getString(getString(R.string.setting_LaunchApp), "");
        if (packageName != null && !packageName.equals("")) {
            Intent launchIntent = MainActivity.getInstance().getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                MainActivity.getInstance().startActivity(launchIntent); //null pointer check in case package name was not found
            }
        }
    }
    //endregion
    //==============================================================================================
    //region Command

    private byte[] cmd7(String command) {
        McuCommandID commandID = McuCommandID.ACK;
        switch (command) {//3
            case AllUsageAction.serviceSendAck:
                commandID = McuCommandID.ACK;
                break;
            case AllUsageAction.apiGetFW:
                commandID = McuCommandID.GET_FW;
                break;
            case AllUsageAction.apiGetSN:
                commandID = McuCommandID.GET_SN;
                break;
            case AllUsageAction.apiGetAddress:
                commandID = McuCommandID.GET_BT_MAC;
                break;
            case AllUsageAction.apiGetName:
                commandID = McuCommandID.GET_BT_NAME;
                break;
            case AllUsageAction.apiUnpaired:
                commandID = McuCommandID.UNPAIRED;
                break;
            case AllUsageAction.apiGetBattery:
                commandID = McuCommandID.GET_BATTERY;
                break;
            case AllUsageAction.apiStartDecode:
                commandID = McuCommandID.START_DECODE;
                break;
            case AllUsageAction.apiStopDecode:
                commandID = McuCommandID.STOP_DECODE;
                break;
            case AllUsageAction.apiGetTrigger:
                commandID = McuCommandID.GET_TRIGGER;
                break;
            case AllUsageAction.apiGetConfig:
                commandID = McuCommandID.GET_CONFIG;
                break;
            case AllUsageAction.apiGetAck:
                commandID = McuCommandID.GET_ACK;
                break;
            case AllUsageAction.apiGetAutoConnection:
                commandID = McuCommandID.GET_AUTO_CONNECTION;
                break;
            case AllUsageAction.apiGetBtSignalCheckingLevel:
                commandID = McuCommandID.GET_SIGNAL_CHECKING_LEVEL;
                break;
            case AllUsageAction.apiGetDataTerminator:
                commandID = McuCommandID.GET_DATA_TERMINATOR;
                break;
            case AllUsageAction.apiGetFormat:
                commandID = McuCommandID.GET_DATA_FORMAT;
                break;
        }
        McuCommand mcuCommand = new McuCommand(commandID);
        return mcuCommand.getCommandArray();
    }

    private byte[] cmd8(String command, int value) {
        McuCommandID commandID = McuCommandID.SET_ACK;
        switch (command) {//3
            case AllUsageAction.apiSetIndicator:
                commandID = McuCommandID.SET_INDICATOR;
                break;
            case AllUsageAction.apiSetTrigger:
                commandID = McuCommandID.SET_TRIGGER;
                break;
            case AllUsageAction.apiSetAck:
                commandID = McuCommandID.SET_ACK;
                break;
            case AllUsageAction.apiSetAutoConnection:
                commandID = McuCommandID.SET_AUTO_CONNECTION;
                break;
            case AllUsageAction.apiSetBtSignalCheckingLevel:
                commandID = McuCommandID.SET_SIGNAL_CHECKING_LEVEL;
                break;
            case AllUsageAction.apiSetDataTerminator:
                commandID = McuCommandID.SET_DATA_TERMINATOR;
                break;
            case AllUsageAction.apiChangeToFormatSsi:
            case AllUsageAction.apiChangeToFormatRaw:
                commandID = McuCommandID.SET_DATA_FORMAT;
                break;
        }
        McuCommand mcuCommand = new McuCommand(commandID, value);
        return mcuCommand.getCommandArray();
    }

    public int setINDICATOR(int beepTimes, boolean vibrate, String ledColor, boolean dataAck) {
        BitSet bitSet = new BitSet(8);
        bitSet.clear(0, 7);
        switch (beepTimes) {
            case 0:
                break;
            case 1:
                bitSet.set(0);
                break;
            case 2:
                bitSet.set(1);
                break;
            case 3:
                bitSet.set(0);
                bitSet.set(1);
                break;
        }
        if (vibrate) bitSet.set(3);
//        Logger.info("ledColor:" + ledColor);
        switch (ledColor) {
            case "none":
                break;
            case "red":
                bitSet.set(4);
                break;
            case "green":
                bitSet.set(5);
                break;
            case "blue":
                bitSet.set(6);
                break;
        }
        if (dataAck) bitSet.set(7);
//        Logger.info("bitSet = " + bitSet.toString());
        byte[] byteArray = bitSet.toByteArray();
        if (byteArray.length == 0) {
            return 0;
        }
        return byteArray[0];
    }

    //endregion
    //==============================================================================================
    public class ConnectedObject extends Thread {
        //==========================================================================================
        private final BluetoothSocket mmSocket;
        private final DecodeReplyPacket decodeReplyPacket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ConcurrentLinkedQueue <SendDataType> writeSocketCommand;
        private SendDataType sendDataType = null;
        private ToneGenerator toneGenerator = null;
        private Vibrator vibrator = null;
        //------------------------------------------------------------------------------------------
        public String scannerSerialNumber = null;
        public boolean isNeedToCheckUnpairedTimeOut = true;
        private boolean isCheckWriteSocketQueue = true;
        private boolean exit = false;
        private boolean isSelected = false;
        private boolean isWaitForSetConfig = false;
        private boolean uploadFlag = true;
        private final String scannerAddress;
        private final String deviceName;
        private long startCommandTime = 0;
        //------------------------------------------------------------------------------------------
        private final Runnable writeRunnable = () -> {
            while (isCheckWriteSocketQueue) {
                if (!writeSocketCommand.isEmpty()) {
                    boolean canWrite = true;
                    SendDataType sendDataTypePeek = writeSocketCommand.peek();
                    if (sendDataTypePeek != null && sendDataTypePeek.getCommandName() != null && sendDataTypePeek.getCommandName().contains(AllUsageAction.apiUploadSettings)) {
                        String command = sendDataTypePeek.getCommandName();
                        if (command.contains("_")) {
                            if (isWaitForSetConfig) {
                                canWrite = false;
                            } else {
                                String[] spilt = command.replace(AllUsageAction.apiUploadSettings, "").split("_");
                                try {
                                    int nowUploadIndex = Integer.parseInt(spilt[1]);
                                    int totalIndex = Integer.parseInt(spilt[2]);
                                    if (!uploadFlag) {
                                        canWrite = false;
                                        writeSocketCommand.remove();
                                    }
                                    if (nowUploadIndex == totalIndex) {
                                        if (!uploadFlag) {
                                            executorService.execute(() -> {
                                                Intent intent = new Intent(AllUsageAction.apiUploadSettingsReply)
                                                        .putExtra("result", 1)
                                                        .putExtra("message", ErrorMessage.RECEIVE_NAK)
                                                        .putExtra("packageName", sendDataTypePeek.getPackageName())
                                                        .setPackage(sendDataTypePeek.getPackageName());
                                                SendDataType.BroadcastType broadcastType = sendDataTypePeek.getBroadcast();
                                                if (broadcastType == SendDataType.BroadcastType.LOCAL) {
                                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                                } else {
                                                    sendBroadcast(intent);
                                                }
                                                App.toast(MainActivity.getInstance(), "Upload setting(s) failed");
                                            });
                                            uploadFlag = true;
                                        }
                                    } else {
                                        isWaitForSetConfig = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (canWrite) {
                        sendDataType = writeSocketCommand.poll();
                        if (sendDataType != null && sendDataType.getData() != null) {
                            if (sendDataType.getCommandName() != null && !sendDataType.getCommandName().equals("SEND_ACK")) {
                                executorService.execute(() -> {
                                    MainActivity.showProgressBar();
                                    setServiceNotification(2);
                                });

                            }
                            byte[] data = sendDataType.getData();
                            try {
                                if (outputStream != null) {
                                    outputStream.write(data);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                disconnect();
                            }
                        }
                    }

                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        private final Runnable readRunnable = () -> {
            try {
                Logger.debug("ConnectedThread run()");
                // Keep listening to the InputStream until an exception occurs
                startCommandTime = System.nanoTime();
                while (!exit) {
                    try {
                        int count = 0;
                        while (count == 0) {
                            count = inputStream.available();
                            if (isNeedToCheckUnpairedTimeOut)
                                if (checkTimeout()) {
                                    Logger.debug("UnpairedIfTimeOut");
                                    throw new Exception("UnpairedIfTimeOut");
                                }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        int readByte = inputStream.read();
                        final String intent_action = sendDataType.getCommandName();
                        final String customPackage = sendDataType.getPackageName();
                        boolean isPublic = sendDataType.getBroadcast() == SendDataType.BroadcastType.PUBLIC;
                        if (readByte != -1) {
                            if (scannerSerialNumber != null && scannersMap.containsKey(scannerSerialNumber)) {
                                RemoteDeviceInfo scannerInfo = scannersMap.get(scannerSerialNumber);
                                if (scannerInfo != null) {
                                    isSelected = scannerInfo.getSelected();
                                    Logger.debug(scannerInfo.getSn() + ":" + scannerSerialNumber + " :" + (isSelected ? "" : "not") + " Selected");
                                }
                            }
                            try {
                                if (readByte == McuCommandID.STX.get()) {
                                    rawFormatReply(intent_action, customPackage, isPublic);
                                } else {
                                    ssiFormatReply(readByte, customPackage);
                                }
                                executorService.execute(() -> {
                                    MainActivity.hideProgressBar();
                                    setServiceNotification(1);
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                        Logger.error(error);
                        // Here you know that you are disconnected
                        break;
                    }
                }
                disconnect();
                Logger.trace("ConnectedThread end");
            } catch (Exception ex) {
                String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                Logger.error(error);
                App.toast(getApplicationContext(), "A master BR/EDR Bluetooth device can communicate with a maximum of seven devices in a piconet");
            }

        };

        private final Runnable checkDeviceRunnable = () -> {
            usuApi.getBattery();
            usuApi.getFirmwareVersion();
            usuApi.getFormat();
            usuApi.getDataTerminator();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean needToReCheck = true;
            while (scannersMap != null && scannersMap.containsKey(scannerSerialNumber) && needToReCheck) {
                RemoteDeviceInfo remoteDeviceInfo = scannersMap.get(scannerSerialNumber);
                boolean redo = false;
                if (remoteDeviceInfo != null) {
                    String batteryInfo = remoteDeviceInfo.getBatteryLevel();
                    String firmware = remoteDeviceInfo.getFw();
                    int format = remoteDeviceInfo.getFormat();
                    int version = remoteDeviceInfo.getVersion();
                    int dataTerminator = remoteDeviceInfo.getDataTerminator();
                    if (batteryInfo == null) {
                        usuApi.getBattery();
                        redo = true;
                    }
                    if (firmware == null) {
                        usuApi.getFirmwareVersion();
                        redo = true;
                    }
                    if (version > 43 && format != 1) {//0:raw,1:ssi
                        usuApi.setSSIMode();
                        usuApi.getFormat();
                        redo = true;
                    }
                    if (version > 46 && dataTerminator != 0) {
                        usuApi.setDataTerminator(0);
                        usuApi.getDataTerminator();
                        redo = true;
                    }
                }
                if (redo) {
                    needToReCheck = true;
                } else {
                    needToReCheck = false;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        //==========================================================================================
        public ConnectedObject(BluetoothSocket socket, String deviceName, String scannerAddress) throws IOException {
            this.scannerAddress = scannerAddress;
            this.deviceName = deviceName;
            // Get the input and output streams, using temp objects because member streams are final
            this.mmSocket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            Logger.debug("initialSocket(socket);");
            writeSocketCommand = new ConcurrentLinkedQueue <>();
            decodeReplyPacket = new DecodeReplyPacket(getApplicationContext(), SupportScanner.ms652Plus);
        }

        //==========================================================================================

        public void run() {
            Logger.info(deviceName + "connect thread started.");
            //--------------------------------------------------------------------------------------
            try {
                new Thread(writeRunnable).start();
                //---------------------------------------------------------------------------------
                String serialNumber = "";
                try {
                    serialNumber = getSerialNumber();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (serialNumber.equals("")) {
                    Logger.debug("Disconnect device:" + deviceName);
                    App.toast(getApplicationContext(), "Disconnect " + deviceName + " due to cannot get serial number");
                    disconnect();
                    return;
                }
                scannerSerialNumber = serialNumber;
                //----------------------------------Connect thread----------------------------------
                if (connectedThreadMap.containsKey(serialNumber)) {
                    ConnectedObject tmpConnectThread = connectedThreadMap.get(serialNumber);
                    if (tmpConnectThread != null) {
                        tmpConnectThread.disconnect();
                    }
                    connectedThreadMap.remove(serialNumber);
                }
                connectedThreadMap.put(serialNumber, this);
                Logger.debug("connectedThreadMap = " + connectedThreadMap + ", put[" + serialNumber + ":" + this + "]");
                //-----------------------------------scannerAddressMap------------------------------
                scannerAddressMap.remove(scannerAddress);
                scannerAddressMap.put(scannerAddress, serialNumber);
                Logger.debug("btMacAddressKeyAndSerialNoValue=" + scannerAddressMap + " ,put[" + scannerAddress + ":" + serialNumber + "]");
                //------------------------------------scannersMap-----------------------------------
                scannersMap.remove(serialNumber);
                scannersMap.put(serialNumber, new RemoteDeviceInfo(mmSocket, scannerAddress, deviceName, serialNumber));
                //----------------------------------------------------------------------------------
                Logger.debug("serialNumber:" + serialNumber + " , TargetScanner.readSN():" + TargetScanner.readSN() + " , isEqual = " + serialNumber.contentEquals(TargetScanner.readSN()));
                if (serialNumber.contentEquals(TargetScanner.readSN())) {
                    usuApi.setScanner(serialNumber);
                    isNeedToCheckUnpairedTimeOut = false;
                    lunchSelectedAPP();
                }

                new Thread(checkDeviceRunnable).start();
                //----------------------------------------------------------------------------------
                App.toast(getApplicationContext(), "Device:" + serialNumber + " connected");
                Logger.info(serialNumber + " connected");
                //----------------------------------------------------------------------------------
            } catch (Exception exx) {
                exx.printStackTrace();
            }
            //--------------------------------------------------------------------------------------
            if (connectedThreadPoolExecutor == null) return;
            connectedThreadPoolExecutor.execute(readRunnable);
            //--------------------------------------------------------------------------------------
        }
        //==========================================================================================

        public void sendMcuACK() {
            McuCommand mcuCommand = new McuCommand(McuCommandID.ACK);
            sendDataLocal(mcuCommand.getCommandArray(), "SEND_ACK");
        }

        public void sendSsiACK() {
            SsiCommand ssiCommand = new SsiCommand(SsiOpcode.CMD_ACK);
            write(ssiCommand.byteArray);
        }

        public void sendIndicator(boolean sendAck) {
            int beepTime = 0;
            String sBeepTime = defaultPref.getString(getString(R.string.setting_IndicatorBeep), AllDefaultValue.setting_IndicatorBeep);
            if (sBeepTime != null) {
                beepTime = Integer.parseInt(sBeepTime);
            } else {
                defaultPref.edit().putString(getString(R.string.setting_IndicatorBeep), AllDefaultValue.setting_IndicatorBeep).apply();
            }
            boolean vibrate = false;
            String sVibrate = defaultPref.getString(getString(R.string.setting_IndicatorVibrator), AllDefaultValue.setting_IndicatorVibrator);
            if (sVibrate != null) {
                vibrate = sVibrate.toLowerCase().equals("true");
            } else {
                defaultPref.edit().putString(getString(R.string.setting_IndicatorVibrator), AllDefaultValue.setting_IndicatorVibrator).apply();
            }
            String ledColor = defaultPref.getString(getString(R.string.setting_IndicatorLed), AllDefaultValue.setting_IndicatorLed);
            if (ledColor == null) {
                defaultPref.edit().putString(getString(R.string.setting_IndicatorLed), AllDefaultValue.setting_IndicatorLed).apply();
                ledColor = AllDefaultValue.setting_IndicatorLed;
            }
            int value = setINDICATOR(beepTime, vibrate, ledColor, true);
            Logger.debug("DataAck beepTime = " + beepTime + ", vibrate = " + vibrate + ", ledColor = " + ledColor + ", value = " + value);
            McuCommand mcuCommand;
            if (value != 1) {
                mcuCommand = new McuCommand(McuCommandID.SET_INDICATOR, value);
            } else {
                mcuCommand = new McuCommand(McuCommandID.ACK);
            }
            sendDataLocal(mcuCommand.getCommandArray(), sendAck ? "SEND_ACK" : null);
        }

        public void setUnpaired() throws IOException, InterruptedException {
            Logger.trace("{} call UnPair function", scannerSerialNumber);
            McuCommand mcuCommand = new McuCommand(McuCommandID.UNPAIRED);
            write(mcuCommand.getCommandArray());
            int count = 0;
            int timeout = 0;
            int maxTimeout = 5; // leads to a timeout of 5 seconds
            while (count == 0 && timeout < maxTimeout) {
                count = inputStream.available();
                timeout++;
                Thread.sleep(100);
            }
            int readByte = inputStream.read();
            if (readByte != 0x02) return;
            byte[] fullPackage = isReceiveMcuCommand();
            Logger.debug("Unpaired reply = " + showPacket(fullPackage));
        }


        private String getSerialNumber() throws McuCommandException, SsiCommandException, IOException, InterruptedException {
            McuCommand mcuCommand = new McuCommand(McuCommandID.GET_SN);
            write(mcuCommand.getCommandArray());
            int count = 0;
            int timeout = 0;
            int maxTimeout = 5; // leads to a timeout of 0.5 seconds
            while (count == 0 && timeout < maxTimeout) {
                count = inputStream.available();
                timeout++;
                Thread.sleep(100);
            }
            int readByte = inputStream.read();
            if (readByte != 2) return "";

            byte[] fullPackage = isReceiveMcuCommand();
            Logger.debug("getSerialNumber reply = " + showPacket(fullPackage));
            mcuCommand = new McuCommand(fullPackage);
            if (mcuCommand.getCommandID() == McuCommandID.GET_SN.get()) {
                return new String(mcuCommand.getDataParameter(), StandardCharsets.UTF_8);
            }
            return "";

        }

        //==========================================================================================

        private byte[] isReceiveMcuCommand() throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(McuCommandID.STX.get());
            int[] length = new int[2];
            length[0] = inputStream.read();
            length[1] = inputStream.read();
            byteArrayOutputStream.write(length[0]);
            byteArrayOutputStream.write(length[1]);
            int remainDataNotReceived = (length[0] << 8) + length[1] + 1;
            int index = 0;
            byte[] tmp = new byte[remainDataNotReceived];
            do {
                int result = inputStream.read(tmp);
                byteArrayOutputStream.write(tmp, 0, result);
                index += result;
            } while (index < remainDataNotReceived);
            byte[] mcuPacket = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return mcuPacket;
        }


        private byte[] isReceiveSsiCommand(byte first) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(first);
            int remainDataNotReceived = ((first < 0) ? first + 256 : first) + 1;//-1+2
            int index = 0;
            byte[] tmp = new byte[remainDataNotReceived];
            do {
                int result = inputStream.read(tmp);
                byteArrayOutputStream.write(tmp, 0, result);
                index += result;
            } while (index < remainDataNotReceived);
            byte[] mcuPacket = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
            return mcuPacket;
        }

        //==========================================================================================
        private void rawFormatReply(String intent_action, String customPackage, boolean isPublic) throws IOException, McuCommandException, SsiCommandException {
            byte[] fullPackage = isReceiveMcuCommand();
            McuCommand mcuCommand = new McuCommand(fullPackage);
            McuCommandID commandID = McuCommandID.fromValue(mcuCommand.getCommandID());
            Logger.debug("mcuCommand ID= " + commandID.name() + "\nget rawFormatReply = " + showPacket(mcuCommand.getCommandArray()));

            if (commandID == McuCommandID.RESPOND_DATA) {
                executorService.execute(() -> {
                    String data = encodingData(mcuCommand.getDataParameter());
                    Logger.debug("rawFormatReply data = " + data);
                    String[] split = data.split(",");
                    if (split.length == 4) {
                        if (MainActivity.btSentSemaphore.availablePermits() == 0) {
                            MainActivity.btSentSemaphore.release();
                            Logger.debug("btSentSemaphore.release() due to receive ID: " + commandID.name());
                        }
                        if (split[1].contentEquals("USUCommand") && split[2].contentEquals("TargetME")) {
                            String decode = split[3].toLowerCase();
                            SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
                            String bluetoothMac = localInfoPref.getString(getString(R.string.setting_BtAddress), AllDefaultValue.setting_BtAddress);
                            bluetoothMac = bluetoothMac == null ? AllDefaultValue.setting_BtAddress : bluetoothMac;
                            String macAddress = bluetoothMac.replace(":", "").toLowerCase();
                            Logger.trace("decode={},macAddress={}", decode, macAddress);
                            if (decode.contentEquals(macAddress)) {
                                if (!isSelected) {
                                    isNeedToCheckUnpairedTimeOut = false;
                                    //unpaired other connected devices
                                    for (ConnectedObject tempConnectObject : connectedThreadMap.values()) {
                                        if (!tempConnectObject.scannerSerialNumber.contentEquals(scannerSerialNumber)) {
                                            Logger.trace("start unpaired:{}", tempConnectObject.scannerSerialNumber);
                                            try {
                                                tempConnectObject.setUnpaired();
                                            } catch (Exception ex) {
                                                Logger.error(ex.toString());
                                            }
                                            try {
                                                tempConnectObject.disconnect();
                                            } catch (Exception ex) {
                                                Logger.error(ex.toString());
                                            }
                                            Logger.trace("end unpaired:{}", tempConnectObject.scannerSerialNumber);
                                        }
                                    }
                                    Logger.trace("SetScanners:{}", scannerSerialNumber);
                                    usuApi.setScanner(scannerSerialNumber);
                                    lunchSelectedAPP();
                                }
                            }
                        }
                    } else {
                        boolean dataAck = defaultPref.getBoolean(getString(R.string.setting_DataAckWithIndicator), false);
                        if (dataAck) {
                            sendIndicator(true);
                        } else {
                            sendMcuACK();
                        }
                        sendDataWithCodeType((byte) 0xff, mcuCommand.getDataParameter(), customPackage);
                    }

                });
            } else {
                executorService.execute(() -> {
                    if (tryAcquireSemaphoreResult) {
                        if (MainActivity.btSentSemaphore.availablePermits() == 0) {
                            MainActivity.btSentSemaphore.release();
                            Logger.debug("btSentSemaphore.release() due to receive ID: " + commandID.name());
                        }
                    }
                    Bundle bundle = decodeReplyPacket.syncMcuCommand(mcuCommand, intent_action, customPackage, isPublic);
                    String action = bundle.getString("action");
                    String replyPackage = bundle.getString("packageName");
                    boolean isPublicReturn = bundle.getBoolean("isPublic", false);
                    Logger.debug("rawFormatReply action = " + action +
                            " , broadcast = " + (isPublicReturn ? SendDataType.BroadcastType.PUBLIC.name() : SendDataType.BroadcastType.LOCAL.name()) +
                            " , package = " + replyPackage);

                    if (action != null && !action.equals("") && !action.contains("null")) {
                        boolean canSendBroadcast = true;
                        if (action.contains(AllUsageAction.apiUploadSettings + "_")) {
                            try {
                                isWaitForSetConfig = false;
                                String[] spilt = action.replace(AllUsageAction.apiUploadSettings, "").split("_");
                                int nowUploadIndex = Integer.parseInt(spilt[1]);
                                int totalUploadIndex = Integer.parseInt(spilt[2]);
                                Logger.debug("spilt = " + Arrays.toString(spilt));
                                if (nowUploadIndex != totalUploadIndex) {
                                    canSendBroadcast = false;
                                    if (bundle.getInt("result") != 0) {
                                        uploadFlag = false;
                                    }
                                } else {
                                    uploadFlag = true;
                                    action = AllUsageAction.apiUploadSettingsReply;
                                    if (bundle.getInt("result") != 0) {
                                        App.toast(MainActivity.getInstance(), "Upload setting(s) failed");
                                    } else {
                                        App.toast(MainActivity.getInstance(), "Upload setting(s) complete");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (canSendBroadcast) {
                            bundle.remove("action");
                            bundle.remove("isPublic");
                            StringBuilder str = new StringBuilder();
                            for (String key : bundle.keySet()) {
                                str.append(key).append(" => ").append(bundle.get(key)).append("\n");
                            }
                            Logger.debug("rawFormatReply rBundle = \n" + str);
                            if (!isPublicReturn) {
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(action).putExtras(bundle));
                            } else {
                                sendBroadcast(new Intent(action).putExtras(bundle).setPackage(replyPackage));
                            }
                        }
                    }
                });
            }
        }

        private void ssiFormatReply(int readByte, String customPackage) throws IOException, SsiCommandException {
            byte[] fullPackage = isReceiveSsiCommand((byte) readByte);
            SsiCommand ssiCommand = new SsiCommand(fullPackage);
            Logger.debug("get ssiFormatReply = " + showPacket(ssiCommand.byteArray));
            if (isSelected &&
                    ssiCommand.msgSource == SsiMsgSrc.DECODER.get() &&
                    ssiCommand.commandOpcode == SsiOpcode.DECODE_DATA.get()
            ) {
                sendSsiACK();
                byte[] dataParameter = ssiCommand.dataParameter;
                byte dataType = dataParameter[0];
                Logger.debug("dataType = " + (int) dataType);
                byte[] data = new byte[dataParameter.length - 1];
                System.arraycopy(dataParameter, 1, data, 0, data.length);
                executorService.execute(() -> {
                    boolean dataAck = defaultPref.getBoolean(getString(R.string.setting_DataAckWithIndicator), false);
                    if (dataAck) {
                        handler.postDelayed(() -> sendIndicator(false), 700);
                    }
                    sendDataWithCodeType(dataType, data, customPackage);
                });

            }
        }

        private String encodingData(byte[] respondData) {
            int iEncoding = 0;

            String sEncoding = defaultPref.getString(getString(R.string.setting_Encoding), AllDefaultValue.setting_Encoding);
            if (sEncoding != null) {
                iEncoding = Integer.parseInt(sEncoding);
            }

            String data = "";
            try {
                switch (iEncoding) {
                    case 0://UTF-8
                        data = new String(respondData, StandardCharsets.UTF_8);
                        break;
                    case 1://GBK
                        data = new String(respondData, "GBK");
                        break;
                    case 2://BIG5
                        data = new String(respondData, "BIG5");
                        break;
                    case 3: //Shift_JIS
                        data = new String(respondData, "Shift_JIS");
                        break;
                    case 4://Unicode
                        data = new String(respondData, StandardCharsets.ISO_8859_1);
                        break;
                }
            } catch (UnsupportedEncodingException e) {
                data = new String(respondData, StandardCharsets.UTF_8);
            }

            return data;
        }

        private void sendDataWithCodeType(byte codeType, byte[] rawData, String customPackage) {
            //--------------------------------------------------------------------------------------
            //region beep/vibrate

            doBeeper();
            doVibrator();
            //endregion
            //--------------------------------------------------------------------------------------
            //region encoding

            String data = encodingData(rawData);
            //endregion
            //--------------------------------------------------------------------------------------
            FormattingData formattingData = new FormattingData(getApplicationContext());
            try {
                String lastData = formattingData.getLastData(data, codeType);

                //--------------------------------------------------------------------------------------
                //region keyEvent

                boolean bScan2Key = defaultPref.getBoolean(getString(R.string.setting_Scan2key), AllDefaultValue.setting_Scan2key);
                if (bScan2Key) {
                    String ime = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                    Logger.trace("ime={}", ime);

                    String outputMethod = defaultPref.getString(getString(R.string.setting_OutputMethod), AllDefaultValue.setting_OutputMethod);
                    String interCharDelay = defaultPref.getString(getString(R.string.setting_InterCharTime), AllDefaultValue.setting_InterCharTime);
                    if (outputMethod == null || outputMethod.equals("")) {
                        defaultPref.edit().putString(getString(R.string.setting_OutputMethod), AllDefaultValue.setting_OutputMethod).apply();
                        outputMethod = AllDefaultValue.setting_OutputMethod;
                    }

                    if (interCharDelay == null || interCharDelay.equals("")) {
                        defaultPref.edit().putString(getString(R.string.setting_InterCharTime), AllDefaultValue.setting_InterCharTime).apply();
                        interCharDelay = AllDefaultValue.setting_InterCharTime;
                    } else {
                        try {
                            Integer.parseInt(interCharDelay);
                        } catch (Exception e) {
                            defaultPref.edit().putString(getString(R.string.setting_InterCharTime), AllDefaultValue.setting_InterCharTime).apply();
                            interCharDelay = AllDefaultValue.setting_InterCharTime;
                        }
                    }

                    Bundle bundle = new Bundle();
                    if (BuildConfig.BUILD_TYPE.equals("system")) {
                        bundle.putString("stringData", lastData);
//                        bundle.putString("terminator", formattingData.getTerminator());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(AllUsageAction.serviceKeyboardInput).putExtras(bundle));
                    } else {
                        bundle.putString("stringData", lastData);
                        bundle.putString("outputMethod", outputMethod);
                        bundle.putString("interCharDelay", interCharDelay);
                        sendBroadcast(new Intent(AllUsageAction.serviceKeyboardInput)
                                .putExtras(bundle)
                                .setPackage(getPackageName()));
                    }
                }
                //endregion
                //--------------------------------------------------------------------------------------
                //region intent

                {//version 1.3

                    boolean enableDataType = false;
                    boolean enableDataLength = false;
                    boolean enableDataByte = false;
                    Set <String> enableList = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
                    if (enableList == null) {
                        enableList = AllDefaultValue.setting_EnableDataIntent;
                    }
                    for (String item : enableList) {
                        item = item.toLowerCase().replace("[", "").replace("]", "").replace(" ", "");
                        Logger.debug("enableList item =" + item);
                        switch (item) {
                            case "type":
                                enableDataType = true;
                                break;
                            case "length":
                                enableDataLength = true;
                                break;
                            case "byte":
                                enableDataByte = true;
                                break;
                        }
                    }
                    Logger.debug("enableList item enableDataType= " + enableDataType + " ,enableDataLength = " + enableDataLength + " ,enableDataByte = " + enableDataByte);
                    String intentAction = defaultPref.getString(getString(R.string.setting_DataAction), AllDefaultValue.setting_DataAction);
                    String intentDataKey = defaultPref.getString(getString(R.string.setting_StringData), AllDefaultValue.setting_StringData);
                    String intentDataTypeKey = defaultPref.getString(getString(R.string.setting_StringDataType), AllDefaultValue.setting_StringDataType);
                    String intentDataLengthKey = defaultPref.getString(getString(R.string.setting_StringDataLength), AllDefaultValue.setting_StringDataLength);
                    String intentDataByteKey = defaultPref.getString(getString(R.string.setting_StringDataByte), AllDefaultValue.setting_StringDataByte);

                    Bundle bundle = new Bundle();
                    if (enableDataType) {
                        bundle.putInt(intentDataTypeKey, codeType);
                    }
                    if (enableDataLength) {
                        bundle.putInt(intentDataLengthKey, rawData.length);
                    }
                    if (enableDataByte) {
                        bundle.putByteArray(intentDataByteKey, rawData);
                    }
                    Logger.debug("intentDataKey = " + intentDataKey + " , lastData = " + lastData);
                    bundle.putString(intentDataKey, lastData);
                    bundle.putString("packageName", customPackage);
                    sendBroadcast(new Intent(intentAction).putExtras(bundle).setPackage(customPackage));
                }
                {//version 1.2
                    sendBroadcast(new Intent(AllUsageAction.apiData)
                            .putExtra("text", lastData)
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.apiDataCodeType)
                            .putExtra("codeType", codeType)
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.apiDataAll)
                            .putExtra("databyte", rawData)
                            .putExtra("databytelength", rawData.length)
                            .putExtra("datatype", codeType)
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.apiDataByte)
                            .putExtra("text", rawData)
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.apiDataByteLength)
                            .putExtra("text", rawData.length)
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.apiDataLength)
                            .putExtra("text", lastData.length())
                            .setPackage(customPackage)
                    );
                    sendBroadcast(new Intent(AllUsageAction.utcRequestApiData)
                            .putExtra("ZL_BAR_VALUE", lastData)
                            .setPackage(customPackage)
                    );
                }
                //endregion
                //--------------------------------------------------------------------------------------
                //region clipboard

                if (defaultPref.getBoolean(getString(R.string.setting_Clipboard), AllDefaultValue.setting_Clipboard)) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", lastData));
                }
                //endregion
                //--------------------------------------------------------------------------------------
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doBeeper() {
            executorService.execute(() -> {
                if (toneGenerator != null) {
                    toneGenerator.release();
                    toneGenerator = null;
                }
                boolean soundOn = defaultPref.getBoolean(getString(R.string.setting_Sound), AllDefaultValue.setting_Sound);
                if (soundOn) {
                    int freq = 1;
                    int duration = 1;
                    String sFreq = defaultPref.getString(getString(R.string.setting_Frequency), AllDefaultValue.setting_Frequency);
                    if (sFreq != null) {
                        freq = Integer.parseInt(sFreq);
                    }
                    String sDuration = defaultPref.getString(getString(R.string.setting_SoundDuration), AllDefaultValue.setting_SoundDuration);
                    if (sDuration != null) {
                        duration = Integer.parseInt(sDuration);
                    }
                    int toneDuration;
                    switch (duration) {
                        case 0:
                            toneDuration = 25;
                            break;
                        case 2:
                            toneDuration = 100;
                            break;
                        default:
                            toneDuration = 50;
                            break;
                    }
                    int toneFreq;
                    switch (freq) {
                        case 0:
                            toneFreq = ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK;
                            break;
                        case 2:
                            toneFreq = ToneGenerator.TONE_CDMA_CALLDROP_LITE;
                            break;
                        default:
                            toneFreq = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                            break;
                    }
                    Logger.debug("toneFreq = " + toneFreq + " , toneDuration = " + toneDuration);
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(toneFreq, toneDuration);
                }
            });
        }

        private void doVibrator() {
            executorService.execute(() -> {
                boolean vibration = defaultPref.getBoolean(getString(R.string.setting_Vibration), AllDefaultValue.setting_Vibration);
                if (vibration) {
                    if (vibrator == null) {
                        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    } else {
                        vibrator.cancel();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, 150));
                    } else {
                        vibrator.vibrate(100);
                    }
                }
            });
        }

        private boolean checkTimeout() {
            long endCheckTargetMeTime = System.nanoTime();
            long elapsedTime = endCheckTargetMeTime - startCommandTime;
            // 1 second = 1_000_000_000 nano seconds
            double elapsedTimeInMillisecond = (double) elapsedTime / 1_000_000.0;

            SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
            String pairingBT = localInfoPref.getString("PairingBarcodeContent", AllDefaultValue.setting_BtAddress);
            double timeOutForUnpaired = 20000;
            if (elapsedTimeInMillisecond > timeOutForUnpaired || pairingBT == null) {
                Logger.debug("UnpairedIfTimeOut = TRUE");
                App.toast(getApplicationContext(), "Timeout disconnection");
                try {
                    setUnpaired();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(SendDataType data) {
            writeSocketCommand.add(data);
        }

        public void write(byte[] bytes) {
            writeSocketCommand.add(new SendDataType(bytes, null, SendDataType.BroadcastType.LOCAL));
        }

        /* Call this from the main activity to shutdown the connection */
        public boolean disconnect() {
            executorService.execute(() -> setServiceNotification(0));
            try {
                if (toneGenerator != null) {
                    toneGenerator.release();
                    toneGenerator = null;
                }
                if (vibrator != null) {
                    vibrator.cancel();
                }
                isCheckWriteSocketQueue = false;
                Logger.debug(scannerSerialNumber + " call disconnect function");
                exit = true;
                BluetoothSocket sk = mmSocket;
                OutputStream op = outputStream;
                InputStream ip = inputStream;
                if (op != null) {
                    try {
                        op.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (ip != null) {
                    try {
                        ip.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Thread.sleep(1000);
                if (sk != null) {
                    try {
                        sk.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (scannerAddressMap != null && scannerAddress != null && !scannerAddress.equals(""))
                scannerAddressMap.remove(scannerAddress);
            if (connectedThreadMap != null && scannerSerialNumber != null && !scannerSerialNumber.equals(""))
                connectedThreadMap.remove(scannerSerialNumber);
            if (scannersMap != null && scannerSerialNumber != null && !scannerSerialNumber.equals(""))
                if (scannersMap.containsKey(scannerSerialNumber)) {
                    RemoteDeviceInfo scannerInfo = scannersMap.get(scannerSerialNumber);
                    if (scannerInfo != null) {
                        if (scannerInfo.getSelected()) {
                            Logger.debug("Clear the selected scanner info due to disconnect event get");
                            MainActivity.setBtSerialNo("");
                            TargetScanner.setIsConnected(false);
                            MainActivity.changeSocket(null);
                            sendDataQueue.clear();
                            isCheckWriteSocketQueue = false;
                            writeSocketCommand.clear();
                        }
                    }
                    scannersMap.remove(scannerSerialNumber);
                }
            return true;
        }
    }
    //==============================================================================================

}
