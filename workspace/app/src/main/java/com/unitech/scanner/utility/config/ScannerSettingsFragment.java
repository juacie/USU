package com.unitech.scanner.utility.config;

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
import androidx.preference.PreferenceScreen;

import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/11/19 下午 02:56
 * 修改人:user
 * 修改時間:2020/11/19 下午 02:56
 * 修改備註:
 */

public class ScannerSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        RecyclerView list = getListView();
//        list.addItemDecoration(new DividerItemDecoration(list.getContext(), LinearLayout.VERTICAL));
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
}
