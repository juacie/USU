package com.unitech.scanner.utility.weight;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/2 下午 03:57
 * 修改人:user
 * 修改時間:2021/2/2 下午 03:57
 * 修改備註:
 */

public class FormattingViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView summary;
    public ImageView imageView;
    public LinearLayout container;
    public SwipeRevealLayout swipeRevealLayout;
    public Button btn_delete;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch item_switch;

    public FormattingViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.item_title);
        imageView = view.findViewById(R.id.item_icon);
        container = view.findViewById(R.id.item_container);
        summary = view.findViewById(R.id.item_summary);
        swipeRevealLayout = view.findViewById(R.id.swipeLayout);
        btn_delete = view.findViewById(R.id.btn_delete);
        item_switch = view.findViewById(R.id.item_switch);
    }
}