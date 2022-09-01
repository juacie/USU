package com.nfx.android.rangebarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;

import org.json.JSONException;

import static com.nfx.android.rangebarpreference.RangeBarHelper.convertValuesToJsonString;
import static com.nfx.android.rangebarpreference.RangeBarHelper.formatFloatToString;

/**
 * NFX Development
 * Created by nick on 29/01/17.
 */
class PreferenceControllerDelegate implements RangeBar.OnRangeBarChangeListener, RangeBar
        .OnTouchListener {

    private static final float DEFAULT_CURRENT_LOW_VALUE = 20;
    private static final float DEFAULT_CURRENT_HIGH_VALUE = 80;
    private static final float DEFAULT_TICK_START = 0;
    private static final float DEFAULT_TICK_END = 100;
    private static final float DEFAULT_TICK_INTERVAL = 1;
    private static final boolean DEFAULT_DIALOG_ENABLED = true;
    private static final boolean DEFAULT_IS_ENABLED = true;
    private static final int DEFAULT_DIALOG_STYLE = R.style.Range_Bar_Dialog_Default;
    private final Context context;
    private String measurementUnit;
    private boolean dialogEnabled = DEFAULT_DIALOG_ENABLED;
    private int dialogStyle;
    private TextView currentLowValueView;
    private TextView currentLowMeasurementView;
    private FrameLayout currentLowBottomLineView;
    private LinearLayout lowValueHolderView;
    private TextView currentHighValueView;
    private TextView currentHighMeasurementView;
    private FrameLayout currentHighBottomLineView;
    private LinearLayout highValueHolderView;
    private RangeBar rangeBarView;
    //view stuff
    private boolean isEnabled = DEFAULT_IS_ENABLED;
    //controller stuff
    private float currentLowValue = DEFAULT_CURRENT_LOW_VALUE;
    private float currentHighValue = DEFAULT_CURRENT_HIGH_VALUE;
    private float tempLowValue = DEFAULT_CURRENT_LOW_VALUE;
    private float tempHighValue = DEFAULT_CURRENT_HIGH_VALUE;
    private float tickStart = DEFAULT_TICK_START;
    private float tickEnd = DEFAULT_TICK_END;
    private float tickInterval = DEFAULT_TICK_INTERVAL;
    private ViewStateListener viewStateListener;
    private PersistValueListener persistValueListener;
    private ChangeValueListener changeValueListener;
    private final View.OnClickListener currentLowValueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            new CustomValueDialog(context, dialogStyle, getTickStart(), getTickEnd(),
                    getCurrentLowValue())
                    .setOnChangeListener(new CustomValueDialogListener() {
                        @Override
                        public void onChangeValue(float value) {
                            setLocalLowValue(value);
                            persistValues();
                            rangeBarView.setOnRangeBarChangeListener(null);
                            rangeBarView.setRangePinsByValue(currentLowValue, currentHighValue);
                            rangeBarView.setOnRangeBarChangeListener(
                                    PreferenceControllerDelegate.this);
                        }
                    })
                    .show();
        }
    };
    private final View.OnClickListener currentHighValueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            new CustomValueDialog(context, dialogStyle, getTickStart(), getTickEnd(),
                    getCurrentHighValue())
                    .setOnChangeListener(new CustomValueDialogListener() {
                        @Override
                        public void onChangeValue(float value) {
                            setLocalHighValue(value);
                            persistValues();
                            rangeBarView.setOnRangeBarChangeListener(null);
                            rangeBarView.setRangePinsByValue(currentLowValue, currentHighValue);
                            rangeBarView.setOnRangeBarChangeListener(
                                    PreferenceControllerDelegate.this);
                        }
                    })
                    .show();
        }
    };
    private boolean dragEvent = false;

    PreferenceControllerDelegate(Context context) {
        this.context = context;
    }

    void setPersistValueListener(PersistValueListener persistValueListener) {
        this.persistValueListener = persistValueListener;
    }

    void setViewStateListener(ViewStateListener viewStateListener) {
        this.viewStateListener = viewStateListener;
    }

    void setChangeValueListener(ChangeValueListener changeValueListener) {
        this.changeValueListener = changeValueListener;
    }

    void loadValuesFromXml(AttributeSet attrs) {
        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RangeBarPreference);
            try {
                tickStart = a.getFloat(R.styleable.RangeBarPreference_rbp_tickStart, DEFAULT_TICK_START);
                tickEnd = a.getFloat(R.styleable.RangeBarPreference_rbp_tickEnd, DEFAULT_TICK_END);
                tickInterval = a.getFloat(R.styleable.RangeBarPreference_rbp_tickInterval, DEFAULT_TICK_INTERVAL);
                dialogEnabled = a.getBoolean(R.styleable.RangeBarPreference_rbp_dialogEnabled, DEFAULT_DIALOG_ENABLED);

                measurementUnit = a.getString(R.styleable.RangeBarPreference_rbp_measurementUnit);
                currentLowValue = a.getFloat(R.styleable
                        .RangeBarPreference_rbp_view_defaultLowValue, DEFAULT_CURRENT_LOW_VALUE);
                currentHighValue = a.getFloat(R.styleable
                        .RangeBarPreference_rbp_view_defaultHighValue, DEFAULT_CURRENT_HIGH_VALUE);
//                TODO make it work:
//                dialogStyle = a.getInt(R.styleable.RangeBarPreference_rbp_dialogStyle,
// DEFAULT_DIALOG_STYLE);

                dialogStyle = DEFAULT_DIALOG_STYLE;
            }
            finally {
                a.recycle();
            }
        }
    }

    @Override
    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex,
                                      String leftPinValueString, String rightPinValueString) {
        setLocalLowValue(Float.valueOf(leftPinValueString));
        setLocalHighValue(Float.valueOf(rightPinValueString));
        if(!dragEvent) {
            persistValues();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            dragEvent = true;
            tempLowValue = currentLowValue;
            tempHighValue = currentHighValue;
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            if(dragEvent) {
                persistValues();
            }
            dragEvent = false;
        }
        return false;
    }

    void onBind(View view) {
        rangeBarView = (RangeBar) view.findViewById(R.id.range_bar);

        currentLowMeasurementView = (TextView) view.findViewById(R.id.current_low_value_measurement_unit);
        currentLowValueView = (TextView) view.findViewById(R.id.current_low_value);

        currentHighMeasurementView = (TextView) view.findViewById(R.id.current_high_value_measurement_unit);
        currentHighValueView = (TextView) view.findViewById(R.id.current_high_value);

        // This enables the rangebar to catch drag left when in a navigation drawer
        rangeBarView.setOnTouchListener(new RangeBar.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getAction();
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle rangebar touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        rangeBarView.setPinTextFormatter(new PreferencePinFormatterText());
        rangeBarView.setTickEnd(tickEnd);
        rangeBarView.setTickStart(tickStart);
        rangeBarView.setTickInterval(tickInterval);
        rangeBarView.setDrawTicks(false);

        currentLowMeasurementView.setText(measurementUnit);
        currentHighMeasurementView.setText(measurementUnit);

        setLocalLowValue(currentLowValue);
        setLocalHighValue(currentHighValue);
        rangeBarView.setRangePinsByValue(currentLowValue, currentHighValue);

        rangeBarView.setOnRangeBarChangeListener(this);
        rangeBarView.setOnTouchListener(this);

        currentLowBottomLineView = (FrameLayout) view.findViewById(R.id.current_low_value_bottom_line);
        currentHighBottomLineView = (FrameLayout) view.findViewById(R.id.current_high_value_bottom_line);
        lowValueHolderView = (LinearLayout) view.findViewById(R.id.low_value_holder);
        highValueHolderView = (LinearLayout) view.findViewById(R.id.high_value_holder);

        setDialogEnabled(dialogEnabled);
        setEnabled(isEnabled());
    }

    private boolean isEnabled() {
        if(viewStateListener != null) {
            return viewStateListener.isEnabled();
        }
        else return isEnabled;
    }

    private void setEnabled(boolean enabled) {
        isEnabled = enabled;

        if(viewStateListener != null) {
            viewStateListener.setEnabled(enabled);
        }

        if(rangeBarView != null) { //theoretically might not always work
            rangeBarView.setEnabled(enabled);
            currentLowValueView.setEnabled(enabled);
            currentHighValueView.setEnabled(enabled);
            lowValueHolderView.setClickable(enabled);
            lowValueHolderView.setEnabled(enabled);
            highValueHolderView.setClickable(enabled);
            highValueHolderView.setEnabled(enabled);

            currentHighMeasurementView.setEnabled(enabled);
            currentLowMeasurementView.setEnabled(enabled);
            currentLowBottomLineView.setEnabled(enabled);
            currentHighBottomLineView.setEnabled(enabled);
        }

    }

    float getTickEnd() {
        return rangeBarView.getTickEnd();
    }

    void setTickEnd(float tickEnd) {
        rangeBarView.setTickEnd(tickEnd);
    }

    float getTickStart() {
        return rangeBarView.getTickStart();
    }

    void setTickStart(float tickStart) {
        rangeBarView.setTickStart(tickStart);
    }

    float getTickInterval() {
        return (float) rangeBarView.getTickInterval();
    }

    void setTickInterval(float interval){
        rangeBarView.setTickInterval(interval);
    }

    float getCurrentLowValue() {
        return currentLowValue;
    }

    void setCurrentLowValue(float value) {
        setLocalLowValue(value);
        persistValues();
    }

    private void setLocalLowValue(float value) {
        float lowValue;
        float highValue;

        if(value < tempHighValue) {
            lowValue = value;
            highValue = tempHighValue;
        } else {
            lowValue = tempHighValue;
            highValue = value;
        }

        setLocalValues(lowValue, highValue);
    }

    float getCurrentHighValue() {
        return currentHighValue;
    }

    void setCurrentHighValue(float value) {
        setLocalHighValue(value);
        persistValues();
    }

    private void setLocalHighValue(float value) {
        float lowValue;
        float highValue;

        if(value > tempLowValue) {
            lowValue = tempLowValue;
            highValue = value;
        } else {
            lowValue = value;
            highValue = tempLowValue;
        }

        setLocalValues(lowValue, highValue);
    }

    private void setLocalValues(float lowValue, float highValue) {
        tempLowValue = (lowValue > tickStart) ? lowValue : tickStart;
        tempHighValue = (highValue < tickEnd) ? highValue : tickEnd;

        if(currentLowValueView != null) {
            currentLowValueView.setText(formatFloatToString(tempLowValue));
        }
        if(currentHighValueView != null) {
            currentHighValueView.setText(formatFloatToString(tempHighValue));
        }
    }

    void persistValues() {
        String jsonString = convertValuesToJsonString(tempLowValue, tempHighValue);

        if (changeValueListener != null) {
            if (!changeValueListener.onChangeValue(jsonString)) {
                return;
            }
        }

        currentLowValue = tempLowValue;
        currentHighValue = tempHighValue;

        if(persistValueListener != null) {
            persistValueListener.persistString(jsonString);
        }
    }

    void setValues(String jsonString) {
        try {
            RangeBarValueJSON rangeBarValueJSON = new RangeBarValueJSON(jsonString);

            setLocalHighValue(rangeBarValueJSON.getHighValue());
            setLocalLowValue(rangeBarValueJSON.getLowValue());
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    String getMeasurementUnit() {
        return measurementUnit;
    }

    void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
        if(currentLowMeasurementView != null) {
            currentLowMeasurementView.setText(measurementUnit);
        }
        if(currentHighMeasurementView != null) {
            currentHighMeasurementView.setText(measurementUnit);
        }
    }

    boolean isDialogEnabled() {
        return dialogEnabled;
    }

    void setDialogEnabled(boolean dialogEnabled) {
        this.dialogEnabled = dialogEnabled;

        if(lowValueHolderView != null &&
                highValueHolderView != null &&
                currentLowBottomLineView != null &&
                currentHighBottomLineView != null) {
            lowValueHolderView.setOnClickListener(
                    dialogEnabled ? currentLowValueClickListener : null);
            lowValueHolderView.setClickable(dialogEnabled);
            highValueHolderView.setOnClickListener(
                    dialogEnabled ? currentHighValueClickListener : null);
            highValueHolderView.setClickable(dialogEnabled);
            currentLowBottomLineView.setVisibility(dialogEnabled ? View.VISIBLE : View.INVISIBLE);
            currentHighBottomLineView.setVisibility(dialogEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    void setDialogStyle(int dialogStyle) {
        this.dialogStyle = dialogStyle;
    }

    interface ViewStateListener {
        boolean isEnabled();

        void setEnabled(boolean enabled);
    }
}