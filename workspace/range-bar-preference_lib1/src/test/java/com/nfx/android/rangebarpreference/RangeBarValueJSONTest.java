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
 * Created by nick on 1/02/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RangeBarValueJSONTest {

    private static final float LOW_VALUE = (float) (Math.random() * Float.MAX_VALUE);
    private static final float HIGH_VALUE = (float) (Math.random() * Float.MAX_VALUE);
    private static final String exampleJson =
            "{\"lowValue\":" + LOW_VALUE + ",\"highValue\":" + HIGH_VALUE + "}";

    private final RangeBarValueJSON rangeBarValueJSON = new RangeBarValueJSON(exampleJson);

    public RangeBarValueJSONTest() throws JSONException {
    }

    @Test
    public void getLowValue() throws Exception {
        assertEquals(LOW_VALUE, rangeBarValueJSON.getLowValue(), 0);
    }

    @Test
    public void setLowValue() throws Exception {
        rangeBarValueJSON.setLowValue(0);
        assertEquals(0, rangeBarValueJSON.getLowValue(), 0);

        rangeBarValueJSON.setLowValue(-100);
        assertEquals(-100, rangeBarValueJSON.getLowValue(), 0);

        float setValue = (float) (Math.random() * Float.MAX_VALUE);
        rangeBarValueJSON.setLowValue(setValue);
        assertEquals(setValue, rangeBarValueJSON.getLowValue(), 0);
    }

    @Test
    public void getHighValue() throws Exception {
        assertEquals(HIGH_VALUE, rangeBarValueJSON.getHighValue(), 0);
    }

    @Test
    public void setHighValue() throws Exception {
        rangeBarValueJSON.setHighValue(0);
        assertEquals(0, rangeBarValueJSON.getHighValue(), 0);

        rangeBarValueJSON.setHighValue(-100);
        assertEquals(-100, rangeBarValueJSON.getHighValue(), 0);

        float setValue = (float) (Math.random() * Float.MAX_VALUE);
        rangeBarValueJSON.setHighValue(setValue);
        assertEquals(setValue, rangeBarValueJSON.getHighValue(), 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertThat("Json objects not equal", rangeBarValueJSON.toString(), equalTo(exampleJson));
    }

}