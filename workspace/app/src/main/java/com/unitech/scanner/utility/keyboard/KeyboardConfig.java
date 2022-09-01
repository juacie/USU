package com.unitech.scanner.utility.keyboard;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/11 上午 10:05
 * 修改人:user
 * 修改時間:2021/1/11 上午 10:05
 * 修改備註:
 */

public interface KeyboardConfig {
    int KEYCODE_EMOJI = -102;
    int KEYCODE_LANGUAGE_SWITCH = -101;
    int KEYCODE_OPTIONS = -100;

    int OUT_OF_BOUNDS = -1;

    int MAX_SUGGESTIONS = 32;
    int SCROLL_PIXELS = 20;
}
