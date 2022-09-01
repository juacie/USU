package com.unitech.scanner.utility.ui.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.adapter.ActionAdapter;
import com.unitech.scanner.utility.callback.ActionButtonEnableCallback;
import com.unitech.scanner.utility.callback.ItemMoveCallback;
import com.unitech.scanner.utility.callback.ListItemDragCallback;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.config.ScannerSettingsFragment;
import com.unitech.scanner.utility.config.formatting.Action;
import com.unitech.scanner.utility.config.formatting.Converter;
import com.unitech.scanner.utility.config.formatting.Formatting;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.config.formatting.Rule;
import com.unitech.scanner.utility.ui.MainActivity;

import org.json.JSONException;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/28 下午 03:08
 * 修改人:user
 * 修改時間:2021/1/28 下午 03:08
 * 修改備註:
 */

public class FormattingActionFragment extends ScannerSettingsFragment implements ListItemDragCallback, ActionButtonEnableCallback {
    //==============================================================================================
    private SharedPreferences defaultPref = null;
    private LinearLayout layout_filterOn;
    private LinearLayout layout_input_switch_replace;
    private LinearLayout layout_regex_replace;
    private EditText edt_ruleName;
    private EditText edt_regex;
    private ActionAdapter adapter;
    private AlertDialog dialog = null;
    private CheckBox checkbox;
    private ItemTouchHelper touchHelper;
    //------------------------------------------------------------------------------------------
    private int thisFragmentType = 256;
    private boolean thisFragmentRuleEnable = true;
    private boolean thisFragmentRuleFilterOnly = false;
    private int thisFragmentRuleExistIndex = -1;
    private List <Action> list_action;
    private final String INPUT = "INPUT";
    private final String SWITCH = "SWITCH";
    private final String REPLACE = "REPLACE";
    private final String REGEX = "REGEX";
    //------------------------------------------------------------------------------------------
    public static Fragment newInstance(int codeType, Bundle args) {
        args.putInt("type", codeType);
        FormattingActionFragment fragment = new FormattingActionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(int codeType) {
        Bundle args = new Bundle();
        args.putInt("type", codeType);
        FormattingActionFragment fragment = new FormattingActionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //==============================================================================================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(getClass().toString() + ":" + "onCreateView");
        return inflater.inflate(R.layout.formating_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.info(getClass().toString() + ":" + "onViewCreated");
        //------------------------------------------------------------------------------------------
        //------------------------------------------------------------------------------------------
        RecyclerView recyclerView_action = view.findViewById(R.id.recyclerView_action);
        layout_filterOn = view.findViewById(R.id.layout_filterOn);
        layout_input_switch_replace = view.findViewById(R.id.layout_input_switch_replace);
        layout_regex_replace = view.findViewById(R.id.layout_regex_replace);
        checkbox = view.findViewById(R.id.checkbox);
        checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            disableEnableControls(b, layout_filterOn);
            list_action = adapter.getActionList();
            if (list_action != null && list_action.size() > 0) {
                boolean hasRegexReplace = false;
                for (Action action : list_action) {
                    String doAction = action.getActionDo().toLowerCase();
                    if (doAction.equals("regex")) {
                        hasRegexReplace = true;
                    }
                }
                requestActionsQuantity(list_action.size(), hasRegexReplace);
            } else {
                requestActionsQuantity(0, false);
            }
        });
        TextView txt_type = view.findViewById(R.id.txt_codeType);
        edt_ruleName = view.findViewById(R.id.edt_ruleName);
        edt_regex = view.findViewById(R.id.edt_regex);
        view.findViewById(R.id.btn_addReg).setOnClickListener(view1 -> actionDialog(REGEX, -1, -1, -1, null, null));
        view.findViewById(R.id.btn_addInput).setOnClickListener(view1 -> actionDialog(INPUT, -1, -1, -1, null, null));
        view.findViewById(R.id.btn_addSwitch).setOnClickListener(view1 -> actionDialog(SWITCH, -1, -1, -1, null, null));
        view.findViewById(R.id.btn_addReplace).setOnClickListener(view1 -> actionDialog(REPLACE, -1, -1, -1, null, null));
        view.findViewById(R.id.btn_confirm).setOnClickListener(view12 -> {
            String ruleName = edt_ruleName.getText().toString();
            if (ruleName.equals("")) {
                App.toast(MainActivity.getInstance(), "Please input the rule name.");
                return;
            }
            Rule rule = new Rule();
            rule.setName(ruleName);
            rule.setFilterOnly(checkbox.isChecked());
            rule.setRegex(edt_regex.getText().toString());
            rule.setEnable(thisFragmentRuleEnable);
            rule.setAction(adapter.getActionList());
            addRule(rule);
            MainActivity.getInstance().onBackPressed();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(view13 -> MainActivity.getInstance().onBackPressed());
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
        //------------------------------------------------------------------------------------------
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.get("type") != null) {
                thisFragmentType = bundle.getInt("type");
            }
            if (bundle.get("name") != null) {
                String thisFragmentRuleName = bundle.getString("name");
                if (thisFragmentRuleName != null) {
                    edt_ruleName.setText(thisFragmentRuleName);
                }
            }
            if (bundle.get("enable") != null) {
                thisFragmentRuleEnable = bundle.getBoolean("enable");
            }
            if (bundle.get("regex") != null) {
                String thisFragmentRuleRegEx = bundle.getString("regex");
                if (thisFragmentRuleRegEx != null) {
                    edt_regex.setText(thisFragmentRuleRegEx);
                }
            }
            if (bundle.get("filterOnly") != null) {
                thisFragmentRuleFilterOnly = bundle.getBoolean("filterOnly");
            }
            if (bundle.get("existIndex") != null) {
                thisFragmentRuleExistIndex = bundle.getInt("existIndex");
                getAction(thisFragmentRuleExistIndex);
            }
        }
        String name;
        try {
            name = BarcodeType.fromValue(thisFragmentType).name();
        } catch (Exception e) {
            name = "Error type";
        }
        txt_type.setText(name);
        //------------------------------------------------------------------------------------------
        if (list_action == null) {
            list_action = new ArrayList <>();
        }
        recyclerView_action.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        adapter = new ActionAdapter(list_action, this, this);
        recyclerView_action.setAdapter(adapter);
        recyclerView_action.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), DividerItemDecoration.VERTICAL));
        touchHelper = new ItemTouchHelper(new ItemMoveCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView_action);
        Logger.debug("thisFragmentRuleFilterOnly = " + thisFragmentRuleFilterOnly);
        new Handler().postDelayed(() -> {
            checkbox.setChecked(thisFragmentRuleFilterOnly);
            disableEnableControls(thisFragmentRuleFilterOnly, layout_filterOn);
            if(list_action.size()>0){
                boolean hasRegexReplace = false;
                for(Action action:list_action){
                    String doAction = action.getActionDo().toLowerCase();
                    if(doAction.equals("regex")){
                        hasRegexReplace = true;
                    }
                }
                requestActionsQuantity(list_action.size(),hasRegexReplace);
            }else{
                requestActionsQuantity(0,false);
            }
        }, 100);
        //------------------------------------------------------------------------------------------
    }


    //==============================================================================================
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        MainActivity.getInstance().setTitle(R.string.config_Rule);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void requestActionsQuantity(int quantity, boolean hasRegexReplacement) {
        if (!checkbox.isChecked()) {
            return;
        }
        if (quantity == 0) {
            disableEnableControls(true, layout_filterOn);
        } else {
            disableEnableControls(false,layout_regex_replace );
            disableEnableControls(!hasRegexReplacement, layout_input_switch_replace);
        }
    }

    //==============================================================================================
    private void disableEnableControls(boolean enable, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }
    private void getAction(int thisFragmentRuleExistIndex) {
        //------------------------------------------------------------------------------------------
        String jsonFormatting = defaultPref.getString(getString(R.string.setting_Formatting), "{}");
        //------------------------------------------------------------------------------------------
        try {
            Formatting formatting = Converter.fromJsonString(jsonFormatting);
            list_action = formatting.getActionList(thisFragmentType, thisFragmentRuleExistIndex);
            if(list_action==null){
                list_action = new ArrayList <>();
            }
            Logger.debug("getType = " + list_action + " , thisFragmentType = " + thisFragmentType + " , thisFragmentRuleExistIndex = " + thisFragmentRuleExistIndex);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        //------------------------------------------------------------------------------------------
    }

    private void addRule(Rule rule) {
        String jsonFormatting = defaultPref.getString(getString(R.string.setting_Formatting), AllDefaultValue.setting_Formatting);
        Logger.debug("addRule = " + jsonFormatting);
        int formattingIndex = -1;
        boolean updateFlag = false;
        try {
            Formatting formatting = Converter.fromJsonString(jsonFormatting);
            ArrayList <FormattingElement> formattingElements = formatting.getFormatting();
            FormattingElement formattingElement = null;
            if (formattingElements != null && formattingElements.size() > 0) {
                for (int i = 0; i < formattingElements.size(); i++) {
                    FormattingElement _formattingElement = formattingElements.get(i);
                    if (_formattingElement.getType() == thisFragmentType) {
                        formattingElement = _formattingElement;
                        formattingIndex = i;
                        break;
                    }
                }
            } else {
                formattingElements = new ArrayList <>();
            }

            if (formattingElement == null) {
                formattingElement = new FormattingElement();
                formattingElement.setEnable(true);
                formattingElement.setType(thisFragmentType);
            }

            ArrayList <Rule> rules = formatting.getRuleList(thisFragmentType);
            if (rules != null && rules.size() > 0) {
                if (thisFragmentRuleExistIndex != -1) {
                    rules.set(thisFragmentRuleExistIndex, rule);
                    updateFlag = true;
                }
            } else {
                rules = new ArrayList <>();
            }
            if (!updateFlag) {
                rules.add(rule);
            }
            formattingElement.setRule(rules);
            if (formattingIndex != -1) {
                formattingElements.set(formattingIndex, formattingElement);
            } else {
                formattingElements.add(formattingElement);
            }
            formatting.setFormatting(formattingElements);

            defaultPref.edit().putString(getString(R.string.setting_Formatting), Converter.toJsonString(formatting)).apply();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void actionDialog(String actionName, int index, int range, int symbol, String content, String regexReplace) {
        //------------------------------------------------------------------------------------------
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        //------------------------------------------------------------------------------------------
        final View v = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.dialog_formatting_action, null);
        LinearLayout layout_index = v.findViewById(R.id.layout_index);
        LinearLayout layout_range = v.findViewById(R.id.layout_range);
        LinearLayout layout_symbol = v.findViewById(R.id.layout_symbol);
        LinearLayout layout_content = v.findViewById(R.id.layout_content);
        LinearLayout layout_regex_replace = v.findViewById(R.id.layout_regex_replace);
        EditText edt_index = v.findViewById(R.id.edt_index);
        EditText edt_range = v.findViewById(R.id.edt_range);
        EditText edt_content = v.findViewById(R.id.edt_content);
        EditText edt_regexReplace = v.findViewById(R.id.edt_regexReplace);
        Spinner spinner = v.findViewById(R.id.spinner_symbol);
        String[] mItems = {"Lowercase", "Uppercase"};
        ArrayAdapter <String> _Adapter = new ArrayAdapter <>(MainActivity.getInstance(), android.R.layout.simple_list_item_1, mItems);
        spinner.setAdapter(_Adapter);
        String title = "";
        //------------------------------------------------------------------------------------------
        switch (actionName) {
            case INPUT:
                layout_index.setVisibility(View.VISIBLE);
                layout_range.setVisibility(View.GONE);
                layout_symbol.setVisibility(View.GONE);
                layout_content.setVisibility(View.VISIBLE);
                layout_regex_replace.setVisibility(View.GONE);
                title = "Insert Content.\n(Index 0 for suffix)";
                break;
            case SWITCH:
                layout_index.setVisibility(View.VISIBLE);
                layout_range.setVisibility(View.VISIBLE);
                layout_symbol.setVisibility(View.VISIBLE);
                layout_content.setVisibility(View.GONE);
                layout_regex_replace.setVisibility(View.GONE);
                title = "Switch the data case.\n(Index position to Length range)";
                break;
            case REPLACE:
                layout_index.setVisibility(View.VISIBLE);
                layout_range.setVisibility(View.VISIBLE);
                layout_symbol.setVisibility(View.GONE);
                layout_content.setVisibility(View.VISIBLE);
                layout_regex_replace.setVisibility(View.GONE);
                title = "Replace content.\n(Index position to Length range)";
                break;
            case REGEX:
                layout_index.setVisibility(View.GONE);
                layout_range.setVisibility(View.GONE);
                layout_symbol.setVisibility(View.GONE);
                layout_content.setVisibility(View.GONE);
                layout_regex_replace.setVisibility(View.VISIBLE);
                title = "Replace the group contents with \"Replace RegEx.\"";
                break;
        }
        //------------------------------------------------------------------------------------------
        if (index != -1) {
            edt_index.setText(String.valueOf(index));
        }
        if (range != -1) {
            edt_range.setText(String.valueOf(range));
        }
        if (symbol == 0 || symbol == 1) {
            spinner.setSelection(symbol);
        }
        if (content != null) {
            edt_content.setText(content);
        }
        if (regexReplace != null) {
            edt_regexReplace.setText(regexReplace);
        }
        //------------------------------------------------------------------------------------------
        dialog = new AlertDialog.Builder(MainActivity.getInstance())
                .setTitle(title)
                .setView(v)
                .setPositiveButton("OK", (dialog, which) -> {
                    try {
                        String _index = edt_index.getText().toString();
                        String _range = edt_range.getText().toString();
                        String _content = edt_content.getText().toString();
                        String _regexReplace = edt_regexReplace.getText().toString();
                        int iIndex;
                        int iRange;

                        Action newAction = new Action();
                        newAction.setActionDo(actionName);
                        newAction.setEnable(true);
                        switch (actionName.toUpperCase()) {
                            case INPUT:
                                iIndex = Integer.parseInt(_index);
                                if (iIndex < 0) {
                                    throw new Exception("index");
                                }
                                newAction.setIndex(iIndex);
                                newAction.setContent(_content);
                                break;
                            case SWITCH:
                                iIndex = Integer.parseInt(_index);
                                iRange = Integer.parseInt(_range);
                                if (iIndex <= 0) {
                                    throw new Exception("index");
                                }
                                if (iRange < 0) {
                                    throw new Exception("length");
                                }
                                newAction.setIndex(Integer.parseInt(_index));
                                newAction.setLength(Integer.parseInt(_range));
                                newAction.setSymbolCase(spinner.getSelectedItemPosition());
                                break;
                            case REPLACE:
                                iIndex = Integer.parseInt(_index);
                                iRange = Integer.parseInt(_range);
                                if (iIndex <= 0) {
                                    throw new Exception("index");
                                }
                                if (iRange < 0) {
                                    throw new Exception("length");
                                }
                                newAction.setIndex(iIndex);
                                newAction.setLength(iRange);
                                newAction.setContent(_content);
                                break;
                            case REGEX:
                                newAction.setRegexReplace(_regexReplace);
                                break;
                        }
                        adapter.addItem(newAction);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String eMessage = e.getMessage();
                        switch (Objects.requireNonNull(eMessage)) {
                            case "index":
                                App.toast(MainActivity.getInstance(), "Input input format out of range!");
                                break;
                            case "length":
                                App.toast(MainActivity.getInstance(), "Input length format out of range!");
                                break;
                            default:
                                App.toast(MainActivity.getInstance(), "Input format error!");
                                break;
                        }
                        actionDialog(actionName, index, range, symbol, content, regexReplace);
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                })
                .create();
        dialog.show();
        //------------------------------------------------------------------------------------------
    }


    //==============================================================================================
}
