package com.unitech.scanner.utility.ui.fragment.symbology;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.service.MainService;
import com.unitech.scanner.utility.service.mainUsage.TargetScanner;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.service.mainUsage.RemoteDeviceInfo;

import org.tinylog.Logger;

import java.util.Map;


public class Others extends ScannerSettingsFragment {

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.config_OtherSettings);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_sym_others);
        RemoteDeviceInfo remoteDeviceInfo = MainService.scannersMap.get(MainActivity.getBtSerialNo());
        if (remoteDeviceInfo != null && TargetScanner.readSN().contentEquals(MainActivity.getBtSerialNo()) && remoteDeviceInfo.getFw() != null) {
            String[] parts = remoteDeviceInfo.getFw().split("\\."); // escape .
            if (parts.length >= 2) {
                int version = Integer.parseInt(parts[1]);
                //PreferenceScreen others=(PreferenceScreen)findPreference("SettingsOfOther");
                //if(others!=null)
                {
                    Preference p = getPreferenceManager().findPreference(getString(R.string.setting_scanned_data_format));
                    Preference q = getPreferenceManager().findPreference(getString(R.string.setting_Data_terminator));
                    Logger.trace("p={}", p);
                    Logger.trace("q={}", q);
                    Logger.trace("version={}", version);
                    if (version > 43) {
                        if (p != null)
                            p.setEnabled(true);
                    } else {
                        if (p != null)
                            p.setEnabled(false);
                    }
                    if (version > 46) {
                        if (q != null)
                            q.setEnabled(true);
                    } else {
                        if (q != null)
                            q.setEnabled(false);
                    }
                }

            }
        }
        PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_sym_others, false);
        setRetainInstance(true);
        SharedPreferences prefA = getPreferenceScreen().getSharedPreferences();
        Map <String, ?> allEntries = prefA.getAll();
        for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
            Log.d("Others map values", entry.getKey() + ": " + entry.getValue().toString());
            Preference preference = findPreference(entry.getKey());
            if (preference == null) continue;
            try {
                if (preference instanceof EditTextPreference) {
                    preference.setSummaryProvider(preference12 -> prefA.getString(entry.getKey(), ""));
                    if (entry.getKey().equals(getString(R.string.setting_Decode_Session_Timeout))) {
                        preference.setOnPreferenceChangeListener((preference1, o) -> {
                            if (o.toString().matches("^([5-9]|[1-4][0-9])$"))
                                return true;
                            else {
                                App.toast(MainActivity.getInstance(), "The input value is out of range!");
                                return false;
                            }
                        });
                    } else if (entry.getKey().equals(getString(R.string.setting_Fixed_Exposure_Time))) {
                        preference.setOnPreferenceChangeListener((preference1, o) -> {
                            if (o.toString().matches("^([1-9]|[1-9][0-9]{1,4})$"))
                                return true;
                            else {
                                App.toast(MainActivity.getInstance(), "The input value is out of range!");
                                return false;
                            }
                        });
                    }
                } else if (preference instanceof CheckBoxPreference) {
                    ((CheckBoxPreference) preference).setChecked(prefA.getBoolean(entry.getKey(), false));
                }
            } catch (Exception e) {
                Log.e("map values", entry.getKey() + ": " + entry.getValue().toString());
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) pref;
            final int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            Logger.info(key + " value was updated to " + sharedPreferences.getString(key, ""));
            Logger.info("Summary:" + listPreference.getEntries()[prefIndex] + " " + "setValueIndex:" + prefIndex);

            MainActivity.handler.post(() -> {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                listPreference.setSummaryProvider(preference -> ((ListPreference) preference).getEntries()[prefIndex]);
                listPreference.setValueIndex(prefIndex);

            });

        } else if (pref instanceof EditTextPreference) {
            final EditTextPreference editTextPreference = (EditTextPreference) pref;
            Logger.trace(key + ":" + editTextPreference.getText());
            MainActivity.handler.post(() -> {

                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                editTextPreference.setSummaryProvider(preference -> sharedPreferences.getString(key, ""));
                editTextPreference.setText(sharedPreferences.getString(key, ""));
            });
        } else if (pref instanceof CheckBoxPreference) {
            final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) pref;
            Logger.trace(key + ":" + checkBoxPreference.isChecked());
            MainActivity.handler.post(() -> {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                checkBoxPreference.setChecked(sharedPreferences.getBoolean(key, false));
            });
        }
    }
}
