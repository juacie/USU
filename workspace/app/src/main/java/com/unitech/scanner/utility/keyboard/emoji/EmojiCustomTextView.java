package com.unitech.scanner.utility.keyboard.emoji;


import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.emoji.widget.EmojiTextViewHelper;


/**
 * 官方demo中自定义view 可以解决emoji展示颜色浅的问题
 * 详情见https://github.com/googlesamples/android-EmojiCompat/blob/master/app/src/main/java/com/example/android/emojicompat/CustomTextView.java
 */
public class EmojiCustomTextView extends AppCompatTextView {

    private EmojiTextViewHelper mEmojiTextViewHelper;

    public EmojiCustomTextView(Context context) {
        this(context, null);
    }

    public EmojiCustomTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmojiCustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getEmojiTextViewHelper().updateTransformationMethod();
    }

    @Override
    public void setFilters(InputFilter[] filters) {
        super.setFilters(getEmojiTextViewHelper().getFilters(filters));
    }

    @Override
    public void setAllCaps(boolean allCaps) {
        super.setAllCaps(allCaps);
        getEmojiTextViewHelper().setAllCaps(allCaps);
    }

    /**
     * Returns the {@link EmojiTextViewHelper} for this TextView.
     * <p>
     * <p>This method can be called from super constructors through {@link
     * #setFilters(InputFilter[])} or {@link #setAllCaps(boolean)}.</p>
     */
    private EmojiTextViewHelper getEmojiTextViewHelper() {
        if (mEmojiTextViewHelper == null) {
            mEmojiTextViewHelper = new EmojiTextViewHelper(this);
        }
        return mEmojiTextViewHelper;
    }

}
