package com.nfx.android.rangebarpreference;

import org.json.JSONException;

/**
 * NFX Development
 * Created by nick on 1/02/17.
 */
@SuppressWarnings("ALL")
public class RangeBarHelper {
    static String formatFloatToString(float value) {
        String valueString;
        if (value == Math.ceil(value)) {
            valueString = String.valueOf((int) value);
        } else {
            valueString = String.valueOf(value);
        }

        return valueString;
    }

    public static String convertValuesToJsonString(float lowValue, float highValue) {
        String jsonString = null;
        try {
            RangeBarValueJSON rangeBarValueJSON = new RangeBarValueJSON();
            rangeBarValueJSON.setLowValue(lowValue);
            rangeBarValueJSON.setHighValue(highValue);

            jsonString = rangeBarValueJSON.toString();
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    public static float getLowValueFromJsonString(String jsonString) {
        try {
            RangeBarValueJSON rangeBarValueJSON = new RangeBarValueJSON(jsonString);
            return rangeBarValueJSON.getLowValue();
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static float getHighValueFromJsonString(String jsonString) {
        try {
            RangeBarValueJSON rangeBarValueJSON = new RangeBarValueJSON(jsonString);
            return rangeBarValueJSON.getHighValue();
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
