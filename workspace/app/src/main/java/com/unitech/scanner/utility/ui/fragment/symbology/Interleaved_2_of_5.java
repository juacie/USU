package com.unitech.scanner.utility.ui.fragment.symbology;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.util.Map;

public class Interleaved_2_of_5 extends ScannerSettingsFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_sym_i_two_of_five);
        PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_sym_i_two_of_five, false);
        setRetainInstance(true);
        SharedPreferences prefA = getPreferenceScreen().getSharedPreferences();
        Map <String, ?> allEntries = prefA.getAll();
        for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
            Logger.debug("map values = "+ entry.getKey() + ": " + entry.getValue().toString());
            Preference preference = findPreference(entry.getKey());
            if (preference instanceof EditTextPreference)
                preference.setSummaryProvider(preference1->prefA.getString(entry.getKey(), ""));
            else if (preference instanceof CheckBoxPreference)
                ((CheckBoxPreference) preference).setChecked(prefA.getBoolean(entry.getKey(), false));
        }
        EditTextPreference min = this.findPreference(getString(R.string.setting_Interleaved_2_of_5_Length_Parameter22));
        EditTextPreference max = this.findPreference(getString(R.string.setting_Interleaved_2_of_5_Length_Parameter23));
        if (min != null) {
            min.setOnPreferenceChangeListener((preference, o) -> {
                if (o.toString().matches("^([0-9]|[1-4][0-9]|[5][0-5])$"))
                    return true;
                else {
                    App.toast(MainActivity.getInstance(),"The value of inputed out of range!");

                    return false;
                }
            });
        }
        if (max != null) {
            max.setOnPreferenceChangeListener((preference, o) -> {
                if (o.toString().matches("^([0-9]|[1-4][0-9]|[5][0-5])$"))
                    return true;
                else {
                    App.toast(MainActivity.getInstance(),"The value of inputed out of range!");

                    return false;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.config_Interleaved_2_of_5);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
