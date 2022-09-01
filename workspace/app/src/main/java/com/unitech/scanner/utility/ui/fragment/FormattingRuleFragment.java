package com.unitech.scanner.utility.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.adapter.RuleAdapter;
import com.unitech.scanner.utility.callback.ItemMoveCallback;
import com.unitech.scanner.utility.callback.ListItemDragCallback;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.config.formatting.Converter;
import com.unitech.scanner.utility.config.formatting.Formatting;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.config.formatting.Rule;
import com.unitech.scanner.utility.ui.MainActivity;

import org.json.JSONException;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/28 下午 02:17
 * 修改人:user
 * 修改時間:2021/1/28 下午 02:17
 * 修改備註:
 */

public class FormattingRuleFragment extends ScannerSettingsFragment implements ListItemDragCallback {
    //==============================================================================================
    private SharedPreferences defaultPref = null;
    //------------------------------------------------------------------------------------------
    private RecyclerView recyclerView_rule;
    private RuleAdapter adapter;
    private ItemTouchHelper touchHelper;
    //------------------------------------------------------------------------------------------
    private int thisFragmentType = 256;
    private ArrayList <Rule> list_rule;
    //------------------------------------------------------------------------------------------
    public FormattingRuleFragment() {
    }

    public static Fragment newInstance(int codeType) {
        Bundle args = new Bundle();
        args.putInt("type", codeType);
        FormattingRuleFragment fragment = new FormattingRuleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //==============================================================================================
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(getClass().toString() + ":" + "onCreateView");
        return inflater.inflate(R.layout.formating_rule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.info(getClass().toString() + ":" + "onViewCreated");
        //------------------------------------------------------------------------------------------
        recyclerView_rule = view.findViewById(R.id.recyclerView_rule);
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        //------------------------------------------------------------------------------------------
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.get("type") != null) {
                thisFragmentType = bundle.getInt("type");
            }
        }
        view.findViewById(R.id.btn_addRule).setOnClickListener(view1 -> addRule());
        //------------------------------------------------------------------------------------------
    }

    //==============================================================================================

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug("onResume");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String name;
        try {
            name = BarcodeType.fromValue(thisFragmentType).name();
        } catch (Exception e) {
            name = "Error type";
        }
        MainActivity.getInstance().setTitle(name);
        getType();
        //------------------------------------------------------------------------------------------
        recyclerView_rule.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        adapter = new RuleAdapter(list_rule, thisFragmentType, this);
        recyclerView_rule.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), DividerItemDecoration.VERTICAL));
        recyclerView_rule.setAdapter(adapter);

        touchHelper = new ItemTouchHelper(new ItemMoveCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView_rule);


    }


    @Override
    public void onPause() {
        super.onPause();
        Logger.debug("onPause");
        updatePref();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //==============================================================================================
    private void updatePref() {
        try {
            if (adapter != null) {
                list_rule = adapter.ruleList;
                String jsonFormatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
                Logger.debug("Rule Fragment updatePref = " + jsonFormatting);
                Formatting formatting = Converter.fromJsonString(jsonFormatting);
                ArrayList <FormattingElement> formattingElements = formatting.getFormatting();
                int typeIndex = -1;
                if (formattingElements != null) {
                    FormattingElement formattingElement = null;
                    for (int i = 0; i < formattingElements.size(); i++) {
                        FormattingElement _formattingElement = formattingElements.get(i);
                        if (_formattingElement.getType() == thisFragmentType) {
                            formattingElement = _formattingElement;
                            typeIndex = i;
                            break;
                        }
                    }
                    if (formattingElement == null) {
                        formattingElement = new FormattingElement();
                        formattingElement.setType(thisFragmentType);
                        formattingElement.setEnable(true);
                    }
                    formattingElement.setRule(list_rule);

                    if (list_rule != null && list_rule.size() > 0) {
                        if (typeIndex == -1) {
                            formattingElements.add(formattingElement);
                        } else {
                            formattingElements.set(typeIndex, formattingElement);
                        }
                    } else {
                        formattingElements.remove(typeIndex);
                    }
                    Logger.debug("formattingElements.size() = " + formattingElements.size());
                    if (formattingElements.size() > 0) {
                        formatting.setFormatting(formattingElements);
                        Logger.debug("formattingElements = " + Converter.toJsonString(formatting));
                        defaultPref.edit().putString(getString(R.string.setting_Formatting), Converter.toJsonString(formatting)).apply();
                    } else {
                        defaultPref.edit().putString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting).apply();
                    }
                } else {
                    defaultPref.edit().putString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting).apply();
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getType() {
        //------------------------------------------------------------------------------------------
        String jsonFormatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
        //------------------------------------------------------------------------------------------
        try {
            Formatting formatting = Converter.fromJsonString(jsonFormatting);
            list_rule = formatting.getRuleList(thisFragmentType);
            if (list_rule == null) {
                list_rule = new ArrayList <>();
            }
            Logger.debug("getType = " + list_rule + " , thisFragmentType = " + thisFragmentType);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        //------------------------------------------------------------------------------------------
    }

    private void addRule() {
        Logger.debug("FormattingRuleFragment addRule");
        FragmentManager mFragmentManager = MainActivity.getInstance().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction
                .replace(R.id.settingFrag, FormattingActionFragment.newInstance(thisFragmentType), AllUITag.childFragment)
                .addToBackStack(AllUITag.childFragment)
                .commit();

    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    //==============================================================================================
}
