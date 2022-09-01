package com.unitech.scanner.utility.service.mainUsage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.ui.fragment.MainFragment;

import org.tinylog.Logger;

import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

public class TargetScanner {
    private static boolean isConnected = false;
    private static String sn = "";

    synchronized public static void setIsConnected(boolean IsConnected) {
        isConnected = IsConnected;
        Logger.debug("TargetScanner setIsConnected = "+IsConnected);
        MainFragment mainFragment = (MainFragment) MainActivity.getInstance().getSupportFragmentManager().findFragmentByTag(AllUITag.mainFragment);
        if (mainFragment != null) {
            mainFragment.setStatus(isConnected);
        }
        SharedPreferences targetScannerPref = MainActivity.getInstance().getSharedPreferences("TargetScanner", Context.MODE_PRIVATE);
        targetScannerPref.edit().putBoolean("IsConnected", IsConnected).apply();
        MainActivity.getInstance().sendBroadcast(new Intent().setAction(AllUsageAction.apiTargetScanner).setFlags(FLAG_RECEIVER_FOREGROUND).putExtra("IsConnected", isConnected));
    }

    synchronized public static boolean readIsConnected() {
        return isConnected;
    }

    synchronized public static void setSN(String SN) {
        sn = SN;
        Logger.debug("TargetScanner setSN = "+SN);
        MainFragment mainFragment = (MainFragment) MainActivity.getInstance().getSupportFragmentManager().findFragmentByTag(AllUITag.mainFragment);
        if (mainFragment != null) {
            mainFragment.setSN(sn);
        }
        SharedPreferences targetScannerPref = MainActivity.getInstance().getSharedPreferences("TargetScanner", Context.MODE_PRIVATE);
        targetScannerPref.edit().putString("SN", sn).apply();
        MainActivity.getInstance().sendBroadcast(new Intent().setAction(AllUsageAction.apiTargetScanner).setFlags(FLAG_RECEIVER_FOREGROUND).putExtra("serialNo", sn));
    }

    synchronized public static String readSN() {
        SharedPreferences targetScannerPref = MainActivity.getInstance().getSharedPreferences("TargetScanner", Context.MODE_PRIVATE);
        String resultSN = targetScannerPref.getString("SN", "");
        if (resultSN != null && !resultSN.equals(""))
            return resultSN;
        else
            return sn;
    }
}
