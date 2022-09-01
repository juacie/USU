package com.unitech.scanner.utility.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.unitech.scanner.utility.R;

import java.util.List;


/**
 * 底部图标Adapter
 */
public class EmojiBottomAdapter extends RecyclerView.Adapter <EmojiBottomAdapter.BottomClassViewHolder> {
    private final LayoutInflater layoutInflater;
    private final List <Drawable> tips;
    private ItemOnClick itemOnClick;
    private int itemIndex = 0;
    private final Drawable baseDrawable;
    private final int itemSize;

    public EmojiBottomAdapter(Context context, List <Drawable> tips, int itemSize) {
        this.tips = tips;
        this.itemSize = itemSize;
        this.layoutInflater = LayoutInflater.from(context);
        baseDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_base, context.getTheme());
    }

    public void setItemOnClick(ItemOnClick itemOnClick) {
        this.itemOnClick = itemOnClick;
    }

    @NonNull
    @Override
    public BottomClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BottomClassViewHolder(layoutInflater.inflate(R.layout.keyboard_emoji_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BottomClassViewHolder holder, int position) {
        if (itemIndex == position) {
            holder.relBottomBg.setBackgroundColor(Color.parseColor("#f0f0f0"));
        } else {
            holder.relBottomBg.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        //针对为设置的icon添加默认图标
        if (tips.size() > position) {
            holder.imgBottomIcon.setImageDrawable(tips.get(position));
        } else {
            holder.imgBottomIcon.setImageDrawable(baseDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return itemSize;
    }

    public void changeBottomItem(int position) {
        itemIndex = position;
        notifyDataSetChanged();
    }

    public class BottomClassViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBottomIcon;
        RelativeLayout relBottomBg;

        BottomClassViewHolder(View view) {
            super(view);
            imgBottomIcon = view.findViewById(R.id.imagview_bottom_icon);
            relBottomBg = view.findViewById(R.id.relative_bottom_bg);
            view.setOnClickListener(view1 -> itemOnClick.itemOnClick(getAdapterPosition()));
        }
    }

    public interface ItemOnClick {
        void itemOnClick(int position);
    }
}