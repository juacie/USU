package com.unitech.scanner.utility.utils;

import android.os.Build;

import androidx.annotation.NonNull;

import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public ExceptionHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(@NonNull Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        //Intent intent = new Intent(myContext, CrashActivity.class); //start a new activity to show error message
        //intent.putExtra("error", errorReport.toString());
        //myContext.startActivity(intent);
        String lineSeparator = "\n";
        String errorReport = "************ CAUSE OF ERROR ************\n\n" +
                stackTrace.toString() +
                "\n************ DEVICE INFORMATION ***********\n" +
                "Brand: " +
                Build.BRAND +
                lineSeparator +
                "Device: " +
                Build.DEVICE +
                lineSeparator +
                "Model: " +
                Build.MODEL +
                lineSeparator +
                "Id: " +
                Build.ID +
                lineSeparator +
                "Product: " +
                Build.PRODUCT +
                lineSeparator +
                "\n************ FIRMWARE ************\n" +
                "SDK: " +
                Build.VERSION.SDK_INT +
                lineSeparator +
                "Release: " +
                Build.VERSION.RELEASE +
                lineSeparator +
                "Incremental: " +
                Build.VERSION.INCREMENTAL +
                lineSeparator;
        Logger.error(exception, errorReport);
        MainActivity.exitAPP();
        defaultUEH.uncaughtException(thread, exception);

        //android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(10);
    }
}
