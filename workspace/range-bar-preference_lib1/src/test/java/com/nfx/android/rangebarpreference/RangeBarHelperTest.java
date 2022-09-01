package com.nfx.android.rangebarpreference;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * NFX Development
 * Created by nick on 15/02/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RangeBarHelperTest {
    private static final float LOW_VALUE = (float) (Math.random() * Float.MAX_VALUE);
    private static final float HIGH_VALUE = (float) (Math.random() * Float.MAX_VALUE);
    private static final String exampleJson =
            "{\"lowValue\":" + (double) LOW_VALUE + ",\"highValue\":" + (double) HIGH_VALUE + "}";

    public RangeBarHelperTest() throws JSONException {
    }

    @Test
    public void convertValuesToJsonString() throws Exception {
        String jsonString = RangeBarHelper.convertValuesToJsonString(LOW_VALUE, HIGH_VALUE);

        assertThat("Json objects not equal", jsonString, equalTo(exampleJson));
    }

    @Test
    public void getLowValueFromJsonString() throws Exception {
        assertEquals(LOW_VALUE, RangeBarHelper.getLowValueFromJsonString(exampleJson), 0);
    }

    @Test
    public void getHighValueFromJsonString() throws Exception {
        assertEquals(HIGH_VALUE, RangeBarHelper.getHighValueFromJsonString(exampleJson), 0);
    }
}