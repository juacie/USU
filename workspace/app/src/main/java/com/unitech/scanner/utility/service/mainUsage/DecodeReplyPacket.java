package com.unitech.scanner.utility.service.mainUsage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.config.ErrorMessage;
import com.unitech.sppprotocol.mcu.McuCommand;
import com.unitech.sppprotocol.mcu.McuCommandID;
import com.unitech.sppprotocol.model.SE2707;

import org.tinylog.Logger;

import java.nio.charset.StandardCharsets;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/11 下午 03:29
 * 修改人:user
 * 修改時間:2021/1/11 下午 03:29
 * 修改備註:
 */

public class DecodeReplyPacket {
    private final Context context;
    private final SharedPreferences defaultPref;
    private final int scannerName;

    public DecodeReplyPacket(Context context, int scannerName) {
        this.context = context;
        this.scannerName = scannerName;
        defaultPref = PreferenceManager.getDefaultSharedPreferences(context);

    }


    public Bundle syncMcuCommand(McuCommand mcuCommand, String action, String customPackage, boolean isPublic) {
        Bundle returnBundle = new Bundle();
        returnBundle.putString("action", action + "_reply");
        returnBundle.putString("packageName", customPackage);
        returnBundle.putBoolean("isPublic", isPublic);
        returnBundle.putInt("result", 0);

        McuCommandID mcuCommandID = McuCommandID.fromValue(mcuCommand.getCommandID());
        String syncCommandName = mcuCommandID.name();
        Logger.debug("syncMcuCommand = " + syncCommandName);
        switch (syncCommandName) {
            case "ACK":
                if (action == null) {
                    returnBundle.putString("action", "");
                    break;
                }
                break;
            case "NAK":
                returnBundle.putInt("result", 1);
                returnBundle.putString("message", ErrorMessage.RECEIVE_NAK);
                break;
            case "GET_FW":
                String fw = new String(mcuCommand.getDataParameter(), StandardCharsets.UTF_8);
                Logger.debug("fw:" + fw);
                returnBundle.putString("fw", fw);
                returnBundle.putString("action", AllUsageAction.apiGetFWReply);
                break;
            case "GET_SN":
                String sn = new String(mcuCommand.getDataParameter(), StandardCharsets.UTF_8);
                Logger.debug("sn:" + sn);
                returnBundle.putString("sn", sn);
                returnBundle.putString("action", AllUsageAction.apiGetSNReply);
                break;
            case "GET_BT_MAC":
                String address = new String(mcuCommand.getDataParameter(), StandardCharsets.UTF_8);
                Logger.debug("address:" + address);
                returnBundle.putString("address", address);
                returnBundle.putString("action", AllUsageAction.apiGetAddressReply);
                break;
            case "GET_BT_NAME":
                String name = new String(mcuCommand.getDataParameter(), StandardCharsets.UTF_8);
                Logger.debug("name:" + name);
                returnBundle.putString("name", name);
                break;
            case "GET_BATTERY":
                int battery = mcuCommand.getDataParameter()[0];
                Logger.debug("battery:" + battery);
                returnBundle.putInt("battery", battery);
                returnBundle.putString("action", AllUsageAction.apiGetBatteryReply);
                break;
            case "GET_TRIGGER":
                int trigger = mcuCommand.getDataParameter()[0];
                Logger.debug("trigger:" + trigger);
                returnBundle.putBoolean("trig", trigger == 1);
                returnBundle.putString("action", AllUsageAction.apiGetTriggerReply);
                break;
            case "GET_CONFIG":
                PrefCmdTrans prefCmdTrans = new PrefCmdTrans(context, scannerName);
                Bundle bundle = mcuCommand.getSsiSettings();
                for (String key : bundle.keySet()) {
                    try {
                        SE2707 se2707 = SE2707.fromValue(Integer.parseInt(key));
                        String prefName = prefCmdTrans.getPref(se2707.name());
                        Object value = bundle.get(key);
                        if (prefName != null && value != null) {
                            if (prefName.equals(context.getString(R.string.setting_Decode_Session_Timeout)) && (int) value > 49) {
                                returnBundle.putInt(prefName, 49);
                                defaultPref.edit().putString(prefName, String.valueOf(49)).apply();
                            } else {
                                returnBundle.putInt(prefName, (int) value);
                                defaultPref.edit().putString(prefName, String.valueOf(bundle.get(key))).apply();
                            }
//                            Logger.debug("GET_CONFIG prefName = "+prefName+" , value = "+value);
//                            str.append(prefName).append(" => ").append(bundle.get(key)).append("\n");
                        } else {
//                            no_define.append(key).append(" => ").append(bundle.get(key)).append("\n");
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
//                        error.append(key).append(" => ").append(bundle.get(key)).append("\n");
                    }
                }
                break;
            case "GET_ACK":
                int ack = mcuCommand.getDataParameter()[0];
                returnBundle.putBoolean("ack", ack == 1);
                returnBundle.putString("action", AllUsageAction.apiGetAckReply);
                break;
            case "GET_AUTO_CONNECTION":
                int autoConn = mcuCommand.getDataParameter()[0];
                Logger.debug("autoConn:" + autoConn);
                returnBundle.putBoolean("autoConn", autoConn == 0x01);
                returnBundle.putString("action", AllUsageAction.apiGetAutoConnectionReply);
                break;
            case "GET_SIGNAL_CHECKING_LEVEL":
                int level = mcuCommand.getDataParameter()[0];
                Logger.debug("btSignalCheckingLevel:" + level);
                returnBundle.putInt("btSignalCheckingLevel", level);
                returnBundle.putString("action", AllUsageAction.apiGetBtSignalCheckingLevelReply);
                defaultPref.edit().putString(context.getString(R.string.setting_BT_signal_checking_level), String.valueOf(level)).apply();
                break;
            case "GET_DATA_TERMINATOR":
                int dataTerminator = mcuCommand.getDataParameter()[0];
                Logger.debug("DataTerminator:" + dataTerminator);
                returnBundle.putInt("DataTerminator", dataTerminator);
                returnBundle.putString("action", AllUsageAction.apiGetDataTerminatorReply);
                defaultPref.edit().putString(context.getString(R.string.setting_Data_terminator), String.valueOf(dataTerminator)).apply();
                break;
            case "GET_DATA_FORMAT":
                int format = mcuCommand.getDataParameter()[0];
                Logger.debug("format:" + format);
                returnBundle.putInt("format", format);
                returnBundle.putString("action", AllUsageAction.apiGetFormatReply);
                defaultPref.edit().putString(context.getString(R.string.setting_scanned_data_format), String.valueOf(format)).apply();
                break;
        }

        return returnBundle;
    }
}
