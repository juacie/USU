package com.nfx.android.rangebarpreference;

import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import static com.nfx.android.rangebarpreference.RangeBarHelper.convertValuesToJsonString;

/**
 * NFX Development
 * Created by nick on 29/01/17.
 */
public class RangeBarPreference extends Preference implements PreferenceControllerDelegate.ViewStateListener , ChangeValueListener, PersistValueListener {

    private PreferenceControllerDelegate controllerDelegate;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public RangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public RangeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RangeBarPreference(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        setLayoutResource(R.layout.range_bar_view_layout);

        controllerDelegate = new PreferenceControllerDelegate(getContext());

        controllerDelegate.setViewStateListener(this);
        controllerDelegate.setPersistValueListener(this);
        controllerDelegate.setChangeValueListener(this);

        controllerDelegate.loadValuesFromXml(attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        controllerDelegate.onBind(view);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        String jsonString = convertValuesToJsonString(controllerDelegate.getCurrentLowValue(),
                controllerDelegate.getCurrentHighValue());

        controllerDelegate.setValues(getPersistedString(jsonString));
        controllerDelegate.persistValues();
    }

    @Override
    public boolean persistString(String value) {
        return super.persistString(value);
    }

    @Override
    public boolean onChangeValue(String jsonString) {
        return callChangeListener(jsonString);
    }

    public float getTickEnd() {
        return controllerDelegate.getTickEnd();
    }

    public void setTickEnd(float tickEnd) {
        controllerDelegate.setTickEnd(tickEnd);
    }

    public float getTickStart() {
        return controllerDelegate.getTickStart();
    }

    public void setTickStart(int tickStart) {
        controllerDelegate.setTickStart(tickStart);
    }

    public float getTickInterval() {
        return controllerDelegate.getTickInterval();
    }

    public void setTickInterval(int tickInterval) {
        controllerDelegate.setTickInterval(tickInterval);
    }

    public float getCurrentLowValue() {
        return controllerDelegate.getCurrentLowValue();
    }

    public void setCurrentLowValue(float currentLowValue) {
        controllerDelegate.setCurrentLowValue(currentLowValue);
    }

    public float getCurrentHighValue() {
        return controllerDelegate.getCurrentHighValue();
    }

    public void setCurrentHighValue(float currentHighValue) {
        controllerDelegate.setCurrentHighValue(currentHighValue);
    }

    public String getMeasurementUnit() {
        return controllerDelegate.getMeasurementUnit();
    }

    public void setMeasurementUnit(String measurementUnit) {
        controllerDelegate.setMeasurementUnit(measurementUnit);
    }

    public boolean isDialogEnabled() {
        return controllerDelegate.isDialogEnabled();
    }

    public void setDialogEnabled(boolean dialogEnabled) {
        controllerDelegate.setDialogEnabled(dialogEnabled);
    }

    public void setDialogStyle(int dialogStyle) {
        controllerDelegate.setDialogStyle(dialogStyle);
    }
}
