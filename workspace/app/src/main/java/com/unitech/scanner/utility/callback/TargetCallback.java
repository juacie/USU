package com.unitech.scanner.utility.callback;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/11/18 上午 11:11
 * 修改人:user
 * 修改時間:2020/11/18 上午 11:11
 * 修改備註:
 */

public interface TargetCallback {
    void setStatus(boolean status);
    void setSN(String serialNumber);
}