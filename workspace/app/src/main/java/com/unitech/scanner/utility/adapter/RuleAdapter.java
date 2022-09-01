package com.unitech.scanner.utility.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.callback.ItemMoveCallback;
import com.unitech.scanner.utility.callback.ListItemDragCallback;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.formatting.Action;
import com.unitech.scanner.utility.config.formatting.Rule;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.ui.fragment.FormattingActionFragment;
import com.unitech.scanner.utility.weight.FormattingViewHolder;
import com.unitech.scanner.utility.weight.ViewBinderHelper;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/28 下午 02:37
 * 修改人:user
 * 修改時間:2021/1/28 下午 02:37
 * 修改備註:
 */

public class RuleAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> implements ItemMoveCallback.FormattingViewHolderItemTouchHelperContract {
    //==============================================================================================
    private final int codeType;
    public ArrayList <Rule> ruleList;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();//swipe
    private final ListItemDragCallback mStartDragListener;

    //==============================================================================================
    public RuleAdapter( ArrayList <Rule> ruleList, int codeType, ListItemDragCallback listItemDragCallback) {
        this.ruleList = ruleList;
        this.codeType = codeType;
        this.mStartDragListener = listItemDragCallback;
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

            Rule entity = ruleList.get(position);

            String name = entity.getName();

            boolean enable = entity.getEnable();
            int actionCount;
            ArrayList <Action> actions = entity.getActions();
            if (actions != null) {
                actionCount = entity.getActions().size();
            } else {
                actionCount = 0;
            }
            String summary = "Action Count = " + actionCount;

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
            ((FormattingViewHolder) holder).item_switch.setOnCheckedChangeListener((compoundButton, b) ->
                    {
                        if (enable != b) {
                            setEnable(position, b);
                        }
                    }
            );
            ((FormattingViewHolder) holder).item_switch.setChecked(enable);
            ((FormattingViewHolder) holder).title.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//關閉已被拉出的視窗
                showAction(position);
            }));
            ((FormattingViewHolder) holder).summary.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);//關閉已被拉出的視窗
                showAction(position);
            }));

            ((FormattingViewHolder) holder).btn_delete.setOnClickListener((v -> {
                ((FormattingViewHolder) holder).swipeRevealLayout.close(true);
                ruleList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, ruleList.size());
            }));
        }

    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }

    private void setEnable(int position, boolean enable) {
        if (ruleList.size() > position) {
            ruleList.get(position).setEnable(enable);
            MainActivity.handler.post(() -> notifyItemChanged(position));
        } else {
            Logger.error("updateImage list.size() = " + ruleList.size() + " , position = " + position);
        }
    }

    public void showAction(int position) {
        if (ruleList.size() > position) {
            Rule rule = ruleList.get(position);
            Bundle args = new Bundle();
            args.putString("name", rule.getName());
            args.putBoolean("enable", rule.getEnable());
            args.putString("regex", rule.getRegex());
            args.putBoolean("filterOnly", rule.getFilterOnly());
            args.putInt("existIndex", position);

            FragmentManager mFragmentManager = MainActivity.getInstance().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

            fragmentTransaction
                    .replace(R.id.settingFrag, FormattingActionFragment.newInstance(codeType, args), AllUITag.childFragment)
                    .addToBackStack(AllUITag.childFragment)
                    .commit();
            notifyDataSetChanged();
        } else {
            Logger.error("showRule list.size() = " + ruleList.size() + " , position = " + position);
        }
    }

    @Override
    public void onRowMoved(int fromPos, int toPos) {
        notifyItemMoved(fromPos, toPos);
        Collections.swap(ruleList, fromPos, toPos);
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