package com.unitech.scanner.utility.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainActivityRunning extends Service {
    public MainActivityRunning() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myAidlInterface_isMainActivityRunning;
    }

    IMyAidlInterface_IsMainActiviyRunning.Stub myAidlInterface_isMainActivityRunning = new IMyAidlInterface_IsMainActiviyRunning.Stub() {
        @Override
        public int getPid() {
            return android.os.Process.myPid();
        }

        @Override
        public boolean isRunning() {
            try {
                return MainService.isRunning;
            } catch (Exception ex) {
                return false;
            }

        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) {

        }
    };
}
