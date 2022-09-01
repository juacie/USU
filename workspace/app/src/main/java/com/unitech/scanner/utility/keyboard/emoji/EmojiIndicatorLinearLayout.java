package com.unitech.scanner.utility.keyboard.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.utils.ViewUtils;

/**
 * emoji输入法中的指示器
 */

public class EmojiIndicatorLinearLayout extends LinearLayout {

    private final Context mContext;
    private int chooseItem = 0;
    private int maxCount;

    public EmojiIndicatorLinearLayout(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        initView();
    }

    public EmojiIndicatorLinearLayout(Context mContext, @Nullable AttributeSet attrs) {
        super(mContext, attrs);
        this.mContext = mContext;
        initView();
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        updateView();
    }

    private void updateView() {
        removeAllViews();
        for (int i = 0; i < maxCount; i++) {
            ImageView imageView = new ImageView(mContext);
            int paddingHorizontal = ViewUtils.dip2px(getContext(), 2);
            imageView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
            if (i == chooseItem) {
                imageView.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.emoji_selected, mContext.getTheme()));
            } else {
                imageView.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.emoji_unselected, mContext.getTheme()));
            }
            addView(imageView);
        }
    }

    public void setChoose(int chooseItem) {
        if (chooseItem < 0) {
            chooseItem = 0;
        } else if (chooseItem > maxCount) {
            chooseItem = maxCount;
        }
        this.chooseItem = chooseItem;

        updateView();
    }

    private void initView() {
        this.setOrientation(HORIZONTAL);
    }


}
