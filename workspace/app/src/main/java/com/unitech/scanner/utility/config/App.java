package com.unitech.scanner.utility.config;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.tinylog.Logger;

import java.util.ArrayList;

public class App extends Application {
    private static Toast toast = null;
    private static final Handler toastHandler = new Handler();
    public static ArrayList <Uri> mUriArray = new ArrayList <>();
    public static Bundle exportBundle = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void toast(Context context, final String message) {
        toastHandler.post(() -> {
            Logger.tag(AllUITag.mainActivity).debug("message = " + message);
            if (toast != null) {
                toast.cancel();
                toast = null;
            }
            Toast tmpToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            if (tmpToast == null) return;
            toast = tmpToast;
            toast.show();
        });
    }
}
