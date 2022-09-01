package com.unitech.scanner.utility.ui.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.adapter.TypeAdapter;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.config.formatting.Converter;
import com.unitech.scanner.utility.config.formatting.Formatting;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.ui.MainActivity;

import org.json.JSONException;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/8 下午 05:10
 * 修改人:user
 * 修改時間:2021/1/8 下午 05:10
 * 修改備註:
 */

public class FormattingTypeFragment extends ScannerSettingsFragment {
    //==============================================================================================
    private SharedPreferences defaultPref = null;
    //------------------------------------------------------------------------------------------
    private RecyclerView recyclerView_type;
    private TypeAdapter adapter = null;
    private AlertDialog dialog = null;
    //------------------------------------------------------------------------------------------
    private ArrayList <FormattingElement> list_type;

    //==============================================================================================
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(getClass().toString() + ":" + "onCreateView");
        return inflater.inflate(R.layout.formatting_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.info(getClass().toString() + ":" + "onViewCreated");
        //------------------------------------------------------------------------------------------
        recyclerView_type = view.findViewById(R.id.recyclerView_type);
        view.findViewById(R.id.btn_addType).setOnClickListener(view1 -> addRuleDialog());
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        //------------------------------------------------------------------------------------------
    }
    //==============================================================================================

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.config_Formatting);
        //------------------------------------------------------------------------------------------
        getFormatting();
        //------------------------------------------------------------------------------------------

            recyclerView_type.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
            adapter = new TypeAdapter(list_type);
            recyclerView_type.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), DividerItemDecoration.VERTICAL));
            recyclerView_type.setAdapter(adapter);

        //------------------------------------------------------------------------------------------
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePref();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //==============================================================================================
    private void updatePref() {
        try {
            if (adapter != null) {
                list_type = adapter.typeList;
                Formatting formatting = new Formatting();
                formatting.setFormatting(list_type);
                defaultPref.edit().putString(getString(R.string.setting_Formatting), Converter.toJsonString(formatting)).apply();
            }
        } catch (JsonProcessingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getFormatting() {
        //------------------------------------------------------------------------------------------
        String jsonFormatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
//        String jsonFormatting = "{\"formatting\":[{\"enable\":true,\"rule\":[{\"action\":[{\"do\":\"SWITCH\",\"content\":null,\"enable\":true,\"index\":1,\"length\":0,\"symbolCase\":0}],\"enable\":true,\"filterOnly\":true,\"name\":\"yyy\",\"regex\":\"\\\\w\"}],\"type\":1}]}";
        //------------------------------------------------------------------------------------------
        try {
            Formatting formatting = Converter.fromJsonString(jsonFormatting);
            list_type = formatting.getFormatting();
            if (list_type == null) {
                list_type = new ArrayList <>();
            }
            Logger.debug("getFormatting = " + jsonFormatting + " ,formatting = " + formatting + " ,list_type = " + list_type);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        //------------------------------------------------------------------------------------------
    }

    private void addRuleDialog() {
        updatePref();
        getFormatting();
        String spiltOfItemName = "=";
        List <String> addCodeTypeName = new ArrayList <>();
        for (int barcodeTypeIndex = 0; barcodeTypeIndex < 256; barcodeTypeIndex++) {
            try {
                BarcodeType barcodeType = BarcodeType.fromValue(barcodeTypeIndex);
                boolean existCodeType = false;
                if (list_type != null) {
                    for (int j = 0; j < list_type.size(); j++) {
                        if (list_type.get(j).getType() == barcodeTypeIndex) {
                            existCodeType = true;
                            break;
                        }
                    }
                }
                if (!existCodeType) {
                    String itemName = barcodeTypeIndex+spiltOfItemName+barcodeType.name();
                    addCodeTypeName.add(itemName);
                }
            } catch (Exception ignore) {
            }
        }
        if (addCodeTypeName.size() <= 0) {
            App.toast(MainActivity.getInstance(), "Please swipe left to select an existing format and add rules.");
            return;
        }
        final View v = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.dialog_add_rule, null);
        Spinner spinner = v.findViewById(R.id.spinner_codeType);
        String[] mItems = new String[addCodeTypeName.size()];
        for (int i = 0; i < addCodeTypeName.size(); i++) {
            mItems[i] = addCodeTypeName.get(i);
        }
        ArrayAdapter <String> _Adapter = new ArrayAdapter <>(MainActivity.getInstance(), android.R.layout.simple_list_item_1, mItems);
        spinner.setAdapter(_Adapter);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new AlertDialog.Builder(MainActivity.getInstance())
                .setTitle("Choose code type and add rule.")
                .setView(v)
                .setPositiveButton("ADD", (dialog, which) -> {
                    String itemString = spinner.getSelectedItem().toString();
                    String codeTypeName = itemString.split(spiltOfItemName)[1];
                    Logger.debug("codeTypeName = " + codeTypeName);
                    FragmentManager mFragmentManager = MainActivity.getInstance().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

                    fragmentTransaction
                            .replace(R.id.settingFrag, FormattingActionFragment.newInstance(BarcodeType.valueOf(codeTypeName).get()), AllUITag.childFragment)
                            .addToBackStack(AllUITag.childFragment)
                            .commit();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                })
                .create();
        dialog.show();
    }
    //==============================================================================================
}
