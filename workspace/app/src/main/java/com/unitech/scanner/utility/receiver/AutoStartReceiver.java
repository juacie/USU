package com.unitech.scanner.utility.receiver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.service.MainService;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/15 下午 05:32
 * 修改人:user
 * 修改時間:2021/1/15 下午 05:32
 * 修改備註:
 */

public class AutoStartReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        Logger.tag(AllUITag.autoStartReceiver).debug("AutoStartReceiver get intent = " + intent);
        if (defaultPref.getBoolean(context.getString(R.string.setting_StartUp), AllDefaultValue.setting_StartUp)) {
            if (isMyServiceRunning(context)) {
                Logger.tag(AllUITag.autoStartReceiver).trace("MainService is running, no need to restart the service.");
            } else {
                Logger.tag(AllUITag.autoStartReceiver).trace("Start MainService");
                context.startActivity(new Intent(context, MainActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }

    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
