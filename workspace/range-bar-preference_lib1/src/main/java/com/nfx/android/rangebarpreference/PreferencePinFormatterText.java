package com.nfx.android.rangebarpreference;

import com.appyvet.rangebar.RangeBar;

/**
 * NFX Development
 * Created by nick on 5/02/17.
 */
class PreferencePinFormatterText implements RangeBar.PinTextFormatter {

    @Override
    public String getText(String value) {
        return value;
    }
}
