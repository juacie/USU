package com.nfx.android.rangebarpreference;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * NFX Development
 * Created by nick on 1/02/17.
 */
class RangeBarValueJSON {
    private final static String LOW_VALUE_KEY = "lowValue";
    private final static String HIGH_VALUE_KEY = "highValue";

    private JSONObject jsonObject;

    RangeBarValueJSON() {
        jsonObject = new JSONObject();
    }

    RangeBarValueJSON(String jsonString) throws JSONException {
        this.jsonObject = new JSONObject(jsonString);
    }

    float getLowValue() throws JSONException {
        return (float) jsonObject.getDouble(LOW_VALUE_KEY);
    }

    void setLowValue(float lowValue) throws JSONException {
        jsonObject.put(LOW_VALUE_KEY, lowValue);
    }

    float getHighValue() throws JSONException {
        return (float) jsonObject.getDouble(HIGH_VALUE_KEY);
    }

    void setHighValue(float highValue) throws JSONException {
        jsonObject.put(HIGH_VALUE_KEY, highValue);
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
