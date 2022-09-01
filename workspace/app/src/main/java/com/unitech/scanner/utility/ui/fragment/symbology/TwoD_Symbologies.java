package com.unitech.scanner.utility.ui.fragment.symbology;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.util.Map;

public class TwoD_Symbologies extends ScannerSettingsFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_sym_two_d_symbologies);
        PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_sym_two_d_symbologies, false);
        setRetainInstance(true);
        SharedPreferences prefA = getPreferenceScreen().getSharedPreferences();
        Map <String, ?> allEntries = prefA.getAll();
        for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
            Logger.debug("map values = "+entry.getKey() + ": " + entry.getValue().toString());
            Preference preference = findPreference(entry.getKey());
            if (preference instanceof EditTextPreference)
                preference.setSummaryProvider(preference1->prefA.getString(entry.getKey(), ""));
            else if (preference instanceof CheckBoxPreference)
                ((CheckBoxPreference) preference).setChecked(prefA.getBoolean(entry.getKey(), false));
        }
    }





    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.config_TwoD);
    }



    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
