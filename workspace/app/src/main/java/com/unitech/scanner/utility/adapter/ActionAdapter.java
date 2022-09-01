package com.unitech.scanner.utility.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.callback.ActionButtonEnableCallback;
import com.unitech.scanner.utility.callback.ItemMoveCallback;
import com.unitech.scanner.utility.callback.ListItemDragCallback;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.config.formatting.Action;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.weight.FormattingViewHolder;
import com.unitech.scanner.utility.weight.ViewBinderHelper;

import org.tinylog.Logger;

import java.util.Collections;
import java.util.List;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/28 下午 03:52
 * 修改人:user
 * 修改時間:2021/1/28 下午 03:52
 * 修改備註:
 */

public class ActionAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>  implements ItemMoveCallback.FormattingViewHolderItemTouchHelperContract{
    //==============================================================================================
    private final List <Action> actionList;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();//swipe
    private final String INPUT = "INPUT";
    private final String SWITCH = "SWITCH";
    private final String REPLACE = "REPLACE";
    private final String REGEX = "REGEX";
    private AlertDialog dialog = null;
    private final ListItemDragCallback mStartDragListener;
    private final ActionButtonEnableCallback actionButtonEnableCallback;
    //==============================================================================================
    public ActionAdapter(List <Action> actionList, ListItemDragCallback listItemDragCallback, ActionButtonEnableCallback actionButtonEnableCallback) {
        this.actionList = actionList;
        this.mStartDragListener = listItemDragCallback;
        this.actionButtonEnableCallback = actionButtonEnableCallback;
    }
    //==============================================================================================

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recylerview_item, parent, false);
        return new FormattingViewHolder(v);

    }

    @SuppressLint({"UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FormattingViewHolder) {
            viewBinderHelper.setOpenOnlyOne(true);//設置swipe只能有一個item被拉出
            viewBinderHelper.bind(((FormattingViewHolder) holder).swipeRevealLayout, String.valueOf(position));//綁定Layout (第三步)
            Action entity = actionList.get(position);

            String name = entity.getActionDo().toLowerCase();
            boolean enable = entity.getEnable();

            String summary= "";
            switch (name){
                case "input":
                    summary += "Index = "+entity.getIndex()+" , Content = "+entity.getContent();
                    break;
                case "switch":
                    summary += "Index = "+entity.getIndex()+" , Range = "+entity.getLength()+" , Symbol = "+(entity.getSymbolCase()==1?"UpperCase":"LowerCase");
                    break;
                case "replace":
                    summary += "Index = "+entity.getIndex()+" , Range = "+entity.getLength()+" , Content = "+entity.getContent();
                    break;
                case "regex":
                    summary += "RegEx = "+entity.getRegexReplace();
                    break;
            }
            ((FormattingViewHolder) holder).imageView.setImageDrawable(MainActivity.getInstance().getDrawable(R.drawable.formatting_drag));
            ((FormattingViewHolder) holder).imageView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() ==
                        MotionEvent.ACTION_DOWN || motionEvent.getAction() ==
                        MotionEvent.ACTION_UP) {
                    mStartDragListener.requestDrag(holder);
                }
                return false;
            });
            ((FormattingViewHolder) holder).title.setText(name);
            ((FormattingViewHolder) holder).summary.setText(summary);

            ((FormattingViewHolder) holder).item_switch.setChecked(enable);
            ((FormattingViewHolder) holder).item_switch.setOnCheckedChangeListener((compoundButton, b) ->
                    {
                        if (enable != b) {
                            setEnable(position, b);
                        }
                    }
            );
            ((FormattingViewHolder) holder).title.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//Close the window that has been pulled out
                showAction(position);
            }));
            ((FormattingViewHolder) holder).summary.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//Close the window that has been pulled out
                showAction(position);
            }));

            ((FormattingViewHolder) holder).btn_delete.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);
                actionList.remove(position);
                changeButton();
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, actionList.size());
            }));
        }
    }

    private void changeButton(){
        if(actionList==null)
            return;
        if(actionList.size()==0){
            actionButtonEnableCallback.requestActionsQuantity(0,false);
        }
        boolean hasRegexReplace = false;
        for(Action action:actionList){
            String doAction = action.getActionDo().toLowerCase();
            if(doAction.equals("regex")){
                hasRegexReplace = true;
            }
        }
        actionButtonEnableCallback.requestActionsQuantity(actionList.size(),hasRegexReplace);
    }

    @Override
    public int getItemCount() {
        return actionList.size();
    }

    private void setEnable(int position, boolean enable) {
        if (actionList.size() > position) {
            actionList.get(position).setEnable(enable);
            MainActivity.handler.post(() -> notifyItemChanged(position));
        } else {
            Logger.error("updateImage list.size() = " + actionList.size() + " , position = " + position);
        }
    }

    public void showAction(int position) {
        if (actionList.size() > position) {
            Action action = actionList.get(position);
            actionDialog(action.getEnable(),action.getActionDo(),action.getIndex(),action.getLength(),action.getSymbolCase(),action.getContent(),action.getRegexReplace(),position);
            notifyDataSetChanged();
        } else {
            Logger.error("showRule list.size() = " + actionList.size() + " , position = " + position);
        }
    }

    public void addItem(Action action){
        actionList.add(action);
        changeButton();
        notifyDataSetChanged();
    }

    public void modifyItem(Action action,int position){
        actionList.set(position,action);
        notifyItemChanged(position);
    }

    public List<Action> getActionList(){
        return actionList;
    }

    private void actionDialog(boolean enable,String actionName, int index, int range, int symbol, String content,String regexReplace,int position) {
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
        switch (actionName.toUpperCase()) {
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
                edt_index.setHint("Based on 1");
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
                        Action newAction = new Action();
                        newAction.setActionDo(actionName);
                        newAction.setEnable(enable);
                        switch (actionName.toUpperCase()) {
                            case INPUT:
                                newAction.setIndex(Integer.parseInt(_index));
                                newAction.setContent(_content);
                                break;
                            case SWITCH:
                                newAction.setIndex(Integer.parseInt(_index));
                                newAction.setLength(Integer.parseInt(_range));
                                newAction.setSymbolCase(spinner.getSelectedItemPosition());
                                break;
                            case REPLACE:
                                newAction.setIndex(Integer.parseInt(_index));
                                newAction.setLength(Integer.parseInt(_range));
                                newAction.setContent(_content);
                                break;
                            case REGEX:
                                newAction.setRegexReplace(_regexReplace);
                                break;
                        }
                        modifyItem(newAction,position);
                    } catch (Exception e) {
                        e.printStackTrace();
                        App.toast(MainActivity.getInstance(), "Input format error!");
                        actionDialog(enable,actionName, index, range, symbol, content,regexReplace,position);
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                })
                .create();
        dialog.show();
        //------------------------------------------------------------------------------------------
    }

    @Override
    public void onRowMoved(int fromPos, int toPos) {
        Collections.swap(actionList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
        notifyItemChanged(fromPos);
        notifyItemChanged(toPos);
    }

    @Override
    public void onRowSelected(FormattingViewHolder myFormattingViewHolder) {

    }

    @Override
    public void onRowClear(FormattingViewHolder myFormattingViewHolder) {

    }
    //==============================================================================================

}