package com.unitech.scanner.utility.keyboard.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.unitech.scanner.utility.adapter.EmojiAdapter;
import com.unitech.scanner.utility.adapter.EmojiBottomAdapter;
import com.unitech.scanner.utility.keyboard.SoftKeyboard;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Emoji输入选择器
 */
public class EmojiKeyboard extends LinearLayout {

    private final Context mContext;

    private LinearLayout linearLayoutEmoji;
    private ViewPager viewpagerEmojiKeyboard;
    private RecyclerView recycleviewEmoji;
    private EmojiIndicatorLinearLayout emojiIndicatorLinearLayout;
    private EmojiBottomAdapter emojiBottomAdapter;
    private EmojiAdapter emojiAdapter;
    private SoftKeyboard softKeyboard;

    private List <List <String>> lists;           //数据源

    private List <Drawable> tips = new ArrayList <>();    //底部图标信息
    private List <Integer> listInfo = new ArrayList <>(); //输入器分页情况

    int maxLinex = 3;       //行数
    int maxColumns = 7;    //列数
    private int emojiSize = 24; //字体大小
    private int indicatorPadding = 0;  //底部指示器距离
    private int itemIndex = 0;          //当前选择页面
    private int minItemIndex;           //当前条目页面最小位置
    private int maxItemIndex;           //当前条目页面最大位置
    private int maxViewWidth;           //页面宽度


    public EmojiKeyboard(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    public EmojiKeyboard(Context mContext, @Nullable AttributeSet attrs) {
        super(mContext, attrs);
        this.mContext = mContext;
    }

    public void setLists(List <List <String>> lists) {
        this.lists = lists;
    }

    public void setTips(List <Drawable> tips) {
        this.tips = tips;
    }

    public void setEditText(EditText editText) {
    }

    boolean init = true;

    public void init() {
        initView();
    }

    public void init(double adjustKeySize,int height) {
        emojiSize *= adjustKeySize;
        LayoutParams params = (LayoutParams) this.getLayoutParams();
        params.height = height;
        this.setLayoutParams(params);
        initView();
    }

    public void setSoftKeyboard(SoftKeyboard softKeyboard) {
        this.softKeyboard = softKeyboard;
    }

    public void setEmojiSize(int emojiSize) {
        this.emojiSize = emojiSize;
    }

    public void setIndicatorPadding(int indicatorPadding) {
        this.indicatorPadding = indicatorPadding;
    }

    public void setMaxLines(int maxLinex) {
        this.maxLinex = maxLinex;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    public void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.keyboard_emoji, this);

        //获取根布局对象
        linearLayoutEmoji = findViewById(R.id.linearLayout_emoji);
        viewpagerEmojiKeyboard = findViewById(R.id.viewpager_emojikeyboard);
        emojiIndicatorLinearLayout = findViewById(R.id.emojiIndicatorLinearLayout_emoji);
        recycleviewEmoji = findViewById(R.id.recycleview_emoji_class);

        viewpagerEmojiKeyboard.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                if (init) {
                    init = false;
                    maxViewWidth = linearLayoutEmoji.getWidth();
                    emojiAdapter = new EmojiAdapter(mContext, lists, maxViewWidth, maxLinex, maxColumns, emojiSize);
                    //通过构建后的EmojiAdapter获取底部指示器的范围

                    listInfo = emojiAdapter.getListInfo();
                    viewpagerEmojiKeyboard.setAdapter(emojiAdapter);


                    minItemIndex = 0;
                    maxItemIndex = listInfo.get(itemIndex);

                    //初始化底部指示器信息
                    emojiIndicatorLinearLayout.setMaxCount(listInfo.get(itemIndex));
                    emojiIndicatorLinearLayout.setPadding(0, 0, 0, ViewUtils.dip2px(mContext, indicatorPadding));


                    //初始化底部icon
                    initBottomClass();
                    //为ViewPager添加滑动监听
                    initViewPageChangeListener();
                    //设置emoji点击的回调
                    initEmojiOnClick();
                }

                viewpagerEmojiKeyboard.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    private void initBottomClass() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleviewEmoji.setLayoutManager(linearLayoutManager);

        if (tips.size() != 0 && listInfo.size() < tips.size()) {
            tips = tips.subList(0, listInfo.size());
        }

        emojiBottomAdapter = new EmojiBottomAdapter(mContext, tips, listInfo.size());
        emojiBottomAdapter.setItemOnClick(this::clickChangeBottomClass);
        recycleviewEmoji.setAdapter(emojiBottomAdapter);
    }

    private void initViewPageChangeListener() {
        viewpagerEmojiKeyboard.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //滑动后更新底部指示器
                touchChangeBottomClass(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * @param clickItemIndex 点击后更新指示器
     */
    private void clickChangeBottomClass(int clickItemIndex) {
        itemIndex = clickItemIndex;
        maxItemIndex = 0;
        minItemIndex = 0;
        for (int i = 0; i <= itemIndex; i++) {
            maxItemIndex += listInfo.get(i);
        }

        for (int i = 0; i < itemIndex; i++) {
            minItemIndex += listInfo.get(i);
        }


        viewpagerEmojiKeyboard.setCurrentItem(minItemIndex);
        changeBottomClassIcon();


        emojiIndicatorLinearLayout.setMaxCount(listInfo.get(itemIndex));
        emojiIndicatorLinearLayout.setChoose(0);
    }

    /**
     * @param position 滑动后更新底部指示器
     */
    private void touchChangeBottomClass(int position) {
        //判断滑动是否越界
        if (position >= maxItemIndex) {
            itemIndex++;

            maxItemIndex = 0;
            minItemIndex = 0;
            for (int i = 0; i <= itemIndex; i++) {
                maxItemIndex += listInfo.get(i);
            }

            for (int i = 0; i < itemIndex; i++) {
                minItemIndex += listInfo.get(i);
            }

            emojiIndicatorLinearLayout.setMaxCount(listInfo.get(itemIndex));
            emojiIndicatorLinearLayout.setChoose(0);
        } else if (position < minItemIndex) {
            itemIndex--;

            maxItemIndex = 0;
            minItemIndex = 0;
            for (int i = 0; i <= itemIndex; i++) {
                maxItemIndex += listInfo.get(i);
            }

            for (int i = 0; i < itemIndex; i++) {
                minItemIndex += listInfo.get(i);
            }

            emojiIndicatorLinearLayout.setMaxCount(listInfo.get(itemIndex));
            emojiIndicatorLinearLayout.setChoose(listInfo.get(itemIndex) - 1);
        } else {
            position -= minItemIndex;

            emojiIndicatorLinearLayout.setChoose(position);
        }

        changeBottomClassIcon();
    }

    private void changeBottomClassIcon() {
        emojiBottomAdapter.changeBottomItem(itemIndex);


        int firstItem = recycleviewEmoji.getChildLayoutPosition(recycleviewEmoji.getChildAt(0));
        int lastItem = recycleviewEmoji.getChildLayoutPosition(recycleviewEmoji.getChildAt(recycleviewEmoji.getChildCount() - 1));


        if (itemIndex <= firstItem || itemIndex >= lastItem) {
            recycleviewEmoji.smoothScrollToPosition(itemIndex);
        }
    }


    private void initEmojiOnClick() {
        emojiAdapter.setEmojiOnClick(text -> {
            softKeyboard.onKeyEmoji(text);
//            //获取坐标位置及文本内容
//            int index = editText.getSelectionStart();
//            Editable edit = editText.getEditableText();
//
//            //当点击删除按钮时text为-1
//            if (text.equals("-1")) {
//                String str = editText.getText().toString();
//                if (!str.equals("")) {
//
//                    //只有一个字符
//                    if (str.length() < 2) {
//                        editText.getText().delete(index - 1, index);
//                    } else if (index > 0) {
//                        String lastText = str.substring(index - 2, index);
//                        //检测最后两个字符是否为一个emoji(emoji可能存在一个字符的情况 需要进行正则校验)
//                        if (EmojiRegexUtil.checkEmoji(lastText)) {
//                            editText.getText().delete(index - 2, index);
//                        } else {
//                            editText.getText().delete(index - 1, index);
//                        }
//                    }
//
//                }
//            } else {
//                //插入你内容
//                if (index < 0 || index >= edit.length()) {
//                    edit.append(text);
//                } else {
//                    edit.insert(index, text);
//                }
//            }
        });
    }

}
