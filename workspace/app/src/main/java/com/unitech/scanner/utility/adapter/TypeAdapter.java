package com.unitech.scanner.utility.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.config.formatting.FormattingElement;
import com.unitech.scanner.utility.config.formatting.Rule;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.ui.fragment.FormattingRuleFragment;
import com.unitech.scanner.utility.weight.FormattingViewHolder;
import com.unitech.scanner.utility.weight.ViewBinderHelper;

import org.tinylog.Logger;

import java.util.ArrayList;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/27 下午 01:51
 * 修改人:user
 * 修改時間:2021/1/27 下午 01:51
 * 修改備註:
 */

public class TypeAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {
    //==============================================================================================
    public ArrayList <FormattingElement> typeList;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();//swipe

    //==============================================================================================
    public TypeAdapter(ArrayList <FormattingElement> articlesList) {
        this.typeList = articlesList;
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FormattingViewHolder) {
            viewBinderHelper.setOpenOnlyOne(true);//設置swipe只能有一個item被拉出
            viewBinderHelper.bind(((FormattingViewHolder) holder).swipeRevealLayout, String.valueOf(position));//綁定Layout (第三步)
            FormattingElement entity = typeList.get(position);

            int type = entity.getType();
            String name;
            try {
                name = BarcodeType.fromValue(type).name();
            } catch (Exception e) {
                name = "Error type";
            }

            boolean enable = entity.getEnable();

            int ruleCount;
            ArrayList <Rule> rules = entity.getRules();
            if (rules == null) {
                ruleCount = 0;
            } else {
                ruleCount = rules.size();
            }
            String summary = "Rule Count = " + ruleCount;
            //Set Menu Actions like:
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
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//關閉已被拉出的視窗
                showRule(position);
            }));
            ((FormattingViewHolder) holder).summary.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//關閉已被拉出的視窗
                showRule(position);
            }));


            ((FormattingViewHolder) holder).btn_delete.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);
                typeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, typeList.size());
            }));
        }
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    //==============================================================================================
    private void setEnable(int position, boolean enable) {
        if (typeList.size() > position) {
            typeList.get(position).setEnable(enable);
            MainActivity.handler.post(() -> notifyItemChanged(position));
        } else {
            Logger.error("updateImage list.size() = " + typeList.size() + " , position = " + position);
        }
    }

    public void showRule(int position) {
        if (typeList.size() > position) {
            FormattingElement formattingElement = typeList.get(position);
            int codeType = formattingElement.getType();
            ArrayList <Rule> rules = formattingElement.getRules();
            if (rules != null && rules.size() > 0) {
                FragmentManager mFragmentManager = MainActivity.getInstance().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

                fragmentTransaction
                        .replace(R.id.settingFrag, FormattingRuleFragment.newInstance(codeType), AllUITag.childFragment)
                        .addToBackStack(AllUITag.childFragment)
                        .commit();
            }
            notifyDataSetChanged();
        } else {
            Logger.error("showRule list.size() = " + typeList.size() + " , position = " + position);
        }
    }
    //==============================================================================================
}