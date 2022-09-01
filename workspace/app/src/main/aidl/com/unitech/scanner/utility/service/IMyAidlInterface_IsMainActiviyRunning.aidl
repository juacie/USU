// IMyAidlInterface_IsMainActiviyRunning.aidl
package com.unitech.scanner.utility.service;

// Declare any non-default types here with import statements

 interface IMyAidlInterface_IsMainActiviyRunning {
/** Request the process ID of this service, to do evil things with it. */
    int getPid();
    boolean isRunning();
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
