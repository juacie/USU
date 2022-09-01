package com.unitech.scanner.utility.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.util.Map;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/8 下午 03:28
 * 修改人:user
 * 修改時間:2021/1/8 下午 03:28
 * 修改備註:
 */

public class LabelFormattingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences defaultPref = null;

    public static LabelFormattingFragment newInstance() {

        Bundle args = new Bundle();

        LabelFormattingFragment fragment = new LabelFormattingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            if (pref instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) pref;
                final int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
                if (prefIndex < 0) return;
                Logger.info(key + " value was updated to " + sharedPreferences.getString(key, ""));
                Logger.info("Summary:" + listPreference.getEntries()[prefIndex] + " " + "setValueIndex:" + prefIndex);

                MainActivity.handler.post(() -> {
                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                    listPreference.setSummaryProvider(preference -> listPreference.getEntries()[prefIndex]);
                    listPreference.setValueIndex(prefIndex);

                });
            } else if (pref instanceof EditTextPreference) {
                final EditTextPreference editTextPreference = (EditTextPreference) pref;
                Logger.info(key + ":" + editTextPreference.getText());
                MainActivity.handler.post(() -> {
                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                    editTextPreference.setSummaryProvider(preference -> sharedPreferences.getString(key, ""));
                    editTextPreference.setText(sharedPreferences.getString(key, ""));
                });
            } else if (pref instanceof CheckBoxPreference) {
                final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) pref;
                Logger.tag("LabelFormattingFragment").info(key + ":" + checkBoxPreference.isChecked());
                MainActivity.handler.post(() -> {
                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                    checkBoxPreference.setChecked(sharedPreferences.getBoolean(key, false));
                });
                if(key.equals(getString(R.string.setting_UseFormatting))){
                   Preference preference =  findPreference(getString(R.string.setting_Formatting));
                   if(preference!=null){
                       preference.setEnabled(checkBoxPreference.isChecked());
                   }
                }
            } else {
                if (key.equals(getString(R.string.setting_Replace))) {
                    MainActivity.handler.post(() -> {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        pref.setSummaryProvider(preference -> sharedPreferences.getString(key, ""));
                    });
                }
            }
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_menu_label_formatting);
        PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_menu_label_formatting, false);
        setRetainInstance(true);
        SharedPreferences prefA = getPreferenceManager().getSharedPreferences();
        Map <String, ?> allEntries = prefA.getAll();
        for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
//            Logger.debug("map values = " + entry.getKey() + ": " + entry.getValue().toString());
            if ((findPreference(entry.getKey()) instanceof CheckBoxPreference)) {
                CheckBoxPreference checkBoxPreference = findPreference(entry.getKey());
                if (checkBoxPreference != null) {
                    checkBoxPreference.setChecked(prefA.getBoolean(entry.getKey(), false));
                }
            }
        }
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        //------------------------------------------------------------------------------------------
        String keyName;
        String defaultValue;
        Preference targetPreference;
        //------------------------------------------------------------------------------------------
        targetPreference = findPreference(getString(R.string.setting_Formatting));
        boolean status = defaultPref.getBoolean(getString(R.string.setting_UseFormatting), false);
        if (targetPreference != null) {
            targetPreference.setEnabled(status);
        }
        //------------------------------------------------------------------------------------------
        keyName = getString(R.string.setting_Replace);
        defaultValue = "";
        targetPreference = findPreference(keyName);
        if (targetPreference != null) {
            String actionName = defaultPref.getString(keyName, defaultValue);
            Logger.debug("actionName = " + actionName);
            targetPreference.setSummaryProvider(preference -> actionName);
            targetPreference.setOnPreferenceClickListener(preference -> {
                //code for what you want it to do
                MainActivity.executorService.execute(() -> {
                            String value = defaultPref.getString(keyName, defaultValue);
                            if (value == null) {
                                value = "";
                            }
                            String finalValue = value;
                            MainActivity.handler.post(() -> replaceDialog(keyName, finalValue).show());
                        }
                );
                return true;
            });
        }
        //------------------------------------------------------------------------------------------
    }


    private AlertDialog replaceDialog(String key, String originValue) {
        //------------------------------------------------------------------------------------------
        Context context = MainActivity.getInstance();
        LayoutInflater inflater = LayoutInflater.from(MainActivity.getInstance());
        final View v = inflater.inflate(R.layout.dialog_replace, null);
        EditText edt_find = v.findViewById(R.id.edt_find);
        EditText edt_replacement = v.findViewById(R.id.edt_replacement);
        String find = "";
        String replacement = "";
        if (originValue.contains("->")) {
            String[] temp = originValue.split("->");
            find = temp[0];
            if (temp.length > 1) {
                replacement = temp[1];
            }
        }
        edt_find.setText(find);
        edt_replacement.setText(replacement);

        String title = getString(R.string.config_Replace);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(getString(R.string.config_Replace_dialog_msg))
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    String findValue = edt_find.getText().toString();
                    String replacementValue = edt_replacement.getText().toString();

                    if (findValue.contains("->") || replacementValue.contains("->")) {
                        replaceDialog(key, originValue).show();
                        App.toast(MainActivity.getInstance(), "Please do not enter a string containing \"->\"");
                    } else if (findValue.length() == 0 && replacementValue.length() == 0) {
                        defaultPref.edit().putString(key, "").apply();
                    } else if (findValue.length() == 0) {
                        replaceDialog(key, originValue).show();
                        App.toast(MainActivity.getInstance(), "Please enter the string you need to find.");
                    } else {
                        defaultPref.edit().putString(key, findValue + "->" + replacementValue).apply();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.menu_LabelFormatting);
//        MainActivity.getInstance().bottomNavigationView.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
