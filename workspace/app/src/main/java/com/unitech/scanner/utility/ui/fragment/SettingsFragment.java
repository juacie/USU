package com.unitech.scanner.utility.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.menu_ScannerConfig);
//        MainActivity.getInstance().bottomNavigationView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null) {
            int count = preferenceScreen.getPreferenceCount();
            for (int i = 0; i < count; i++)
                preferenceScreen.getPreference(i).setIconSpaceReserved(false);
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        try {
            try {
                addPreferencesFromResource(R.xml.settings_menu_scanner);
            }catch (Exception e){
                e.printStackTrace();
            }
            PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_menu_scanner, false);
            setRetainInstance(true);
            SharedPreferences prefA = getPreferenceScreen().getSharedPreferences();
            Map <String, ?> allEntries = prefA.getAll();
            for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
//            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                Preference preference = findPreference(entry.getKey());
                if (preference instanceof EditTextPreference)
                    preference.setSummaryProvider(preference1 ->  prefA.getString(entry.getKey(), ""));
                else if (preference instanceof CheckBoxPreference) {
                    ((CheckBoxPreference) preference).setChecked(prefA.getBoolean(entry.getKey(), false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        try {
            Logger.debug( "key = " + key + " sharedPreferences = " + sharedPreferences.getAll());
            Preference pref = findPreference(key);
            if (pref instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) pref;
                final int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
                Logger.debug( key + " value was updated to " + sharedPreferences.getString(key, ""));
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
                    editTextPreference.setSummaryProvider(preference ->  sharedPreferences.getString(key, ""));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
