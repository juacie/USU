package com.unitech.scanner.utility.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
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
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.service.ApiLocal;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences defaultPref = null;

    public static ApplicationSettingsFragment newInstance() {

        Bundle args = new Bundle();

        ApplicationSettingsFragment fragment = new ApplicationSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_menu_application);
        PreferenceManager.setDefaultValues(MainActivity.getInstance(), R.xml.settings_menu_application, false);
        setRetainInstance(true);
        SharedPreferences prefA = getPreferenceManager().getSharedPreferences();
        Map <String, ?> allEntries = prefA.getAll();
        for (Map.Entry <String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Preference preference = findPreference(key);
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                checkBoxPreference.setChecked(prefA.getBoolean(key, false));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        //------------------------------------------------------------------------------------------
        //region launchApp

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        @SuppressLint("QueryPermissionsNeeded") List <ResolveInfo> pkgAppsList = MainActivity.getInstance().getPackageManager().queryIntentActivities(mainIntent, 0);
        List <String> entries = new ArrayList <>();
        List <String> entryValues = new ArrayList <>();
        entries.add("None");
        entryValues.add("");
        for (ResolveInfo app : pkgAppsList) {
            entryValues.add(app.activityInfo.packageName);
            entries.add(app.loadLabel(MainActivity.getInstance().getPackageManager()).toString());
        }
        ListPreference listPreference = findPreference(getString(R.string.setting_LaunchApp));
        if (listPreference != null) {
            listPreference.setEntries(entries.toArray(new CharSequence[0]));
            listPreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
            String launchApp = defaultPref.getString(getString(R.string.setting_LaunchApp), AllDefaultValue.setting_LaunchApp);
            if(launchApp==null|| launchApp.equals("")){
                listPreference.setSummaryProvider(preference -> "None");
            }else{
                int prefIndex = listPreference.findIndexOfValue(launchApp);
                if (prefIndex < 0){
                    listPreference.setSummaryProvider(preference -> "None");
                }else {
                    MainActivity.handler.post(() -> {
                        listPreference.setSummaryProvider(preference -> listPreference.getEntries()[prefIndex]);
                        listPreference.setValueIndex(prefIndex);
                    });
                }
            }
        }
        //endregion
        //------------------------------------------------------------------------------------------
        //region btmac

        SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
        Preference pref_btmac = findPreference(getString(R.string.setting_BtAddress));
        if (pref_btmac != null) {
            String address = localInfoPref.getString(getString(R.string.setting_BtAddress), AllDefaultValue.setting_BtAddress);
            Logger.debug("address = " + address);
            pref_btmac.setSummaryProvider(preference -> address);
            pref_btmac.setOnPreferenceClickListener(preference -> {
                //code for what you want it to do
                MainActivity.executorService.execute(() ->
                        MainActivity.handler.post(() ->
                                MainActivity.btMacDialog(address).show()));
                return true;
            });
        }
        //endregion
        //------------------------------------------------------------------------------------------
        //region intent

        int[] intentPrefID = new int[]{R.string.setting_DataAction, R.string.setting_StringData, R.string.setting_StringDataType, R.string.setting_StringDataLength, R.string.setting_StringDataByte};
        String[] intentDefault = new String[]{AllDefaultValue.setting_DataAction,AllDefaultValue.setting_StringData,AllDefaultValue.setting_StringDataType,AllDefaultValue.setting_StringDataLength,AllDefaultValue.setting_StringDataByte};
        for (int i = 0; i < intentPrefID.length; i++) {
            String keyName = getString(intentPrefID[i]);
            String defaultValue = intentDefault[i];
            Preference targetPreference = findPreference(keyName);
            if (targetPreference != null) {
                String actionName = defaultPref.getString(keyName, defaultValue);
                Logger.debug("actionName = " + actionName);
                targetPreference.setSummaryProvider(preference -> actionName);
                targetPreference.setOnPreferenceClickListener(preference -> {
                    //code for what you want it to do
                    MainActivity.executorService.execute(() -> {
                                String name = defaultPref.getString(keyName, defaultValue);
                                MainActivity.handler.post(() -> intentDialog(keyName, name).show());
                            }
                    );
                    return true;
                });
            }
        }

        Preference multiEnable = findPreference(getString(R.string.setting_EnableDataIntent));
        if (multiEnable != null) {
            Set <String> set = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent), AllDefaultValue.setting_EnableDataIntent);
            if (set != null) {
                multiEnable.setSummaryProvider(preference -> set.toString());
            }
        }

        //endregion
    }


    private AlertDialog intentDialog(String key, String originValue) {
        //------------------------------------------------------------------------------------------
        Context context = MainActivity.getInstance();
        LayoutInflater inflater = LayoutInflater.from(MainActivity.getInstance());
        final View v = inflater.inflate(R.layout.dialog_input_string, null);
        EditText editText = v.findViewById(R.id.editText);
        editText.setText(originValue);
        editText.requestFocus();

        String title = "Intent Edit";
        String message = "Please enter a non-empty value.";
        if (key.equals(getString(R.string.setting_DataAction))) {
            title = "Edit Intent Action";
        } else if (key.equals(getString(R.string.setting_StringData))) {
            title = "Edit Intent Data Key";
        } else if (key.equals(getString(R.string.setting_StringDataType))) {
            title = "Edit Intent Data Type Key";
        } else if (key.equals(getString(R.string.setting_StringDataLength))) {
            title = "Edit Intent Data Length Key";
        } else if (key.equals(getString(R.string.setting_StringDataByte))) {
            title = "Edit Intent Data Byte Key";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Dialog)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setView(v)
                .setPositiveButton("OK", (dialog, which) -> {
                    String text = editText.getText().toString();
                    if (text.length() == 0) {
                        intentDialog(key, originValue).show();
                        App.toast(MainActivity.getInstance(), message);
                    } else {
                        defaultPref.edit().putString(key, text).apply();
                    }
                });
        return builder.create();
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
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.menu_AppSettings);
//        MainActivity.getInstance().bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref == null) return;
        if (pref instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) pref;
            final int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            if (prefIndex < 0) return;
            Logger.info(key + " value was updated to " + sharedPreferences.getString(key, ""));
            Logger.info("Summary:" + listPreference.getEntries()[prefIndex] + " " + "setValueIndex:" + prefIndex);
            MainActivity.handler.post(() -> {
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
            Logger.tag("ApplicationSettingsFragment").info(key + ":" + checkBoxPreference.isChecked());
            MainActivity.handler.post(() -> {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                checkBoxPreference.setChecked(sharedPreferences.getBoolean(key, false));
            });
            if (key.equals(getString(R.string.setting_FloatingButton))) {
                MainActivity.handler.post(() -> {
                    boolean checked = sharedPreferences.getBoolean(key, AllDefaultValue.setting_FloatingButton);
                    ApiLocal usuApi = new ApiLocal(MainActivity.getInstance());
                    usuApi.enableFloatingButtonService(checked);
                });
            }
        } else {
            MainActivity.handler.post(() -> {
                if (key.equals(getString(R.string.setting_DataAction))) {
                    pref.setSummaryProvider(preference -> sharedPreferences.getString(key, AllDefaultValue.setting_DataAction));
                } else if (key.equals(getString(R.string.setting_StringData))) {
                    pref.setSummaryProvider(preference -> sharedPreferences.getString(key, AllDefaultValue.setting_StringData));
                } else if (key.equals(getString(R.string.setting_StringDataType))) {
                    pref.setSummaryProvider(preference -> sharedPreferences.getString(key, AllDefaultValue.setting_StringDataType));
                } else if (key.equals(getString(R.string.setting_StringDataLength))) {
                    pref.setSummaryProvider(preference -> sharedPreferences.getString(key, AllDefaultValue.setting_StringDataLength));
                } else if (key.equals(getString(R.string.setting_StringDataByte))) {
                    pref.setSummaryProvider(preference -> sharedPreferences.getString(key, AllDefaultValue.setting_StringDataByte));
                } else if (key.equals(getString(R.string.setting_EnableDataIntent))) {
                    pref.setSummaryProvider(preference -> {
                                Set <String> set = defaultPref.getStringSet(getString(R.string.setting_EnableDataIntent),  AllDefaultValue.setting_EnableDataIntent);
                                return (set != null) ? set.toString() : "";
                            }
                    );
                }
            });
        }
    }
}
