package com.unitech.scanner.utility.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.utils.ViewUtils;
import com.unitech.scanner.utility.keyboard.emoji.EmojiCustomTextView;

import java.util.ArrayList;
import java.util.List;


public class EmojiAdapter extends PagerAdapter {
    private final Context context;
    private final List <List <String>> listSource;
    private final List <Integer> listInfo = new ArrayList <>();
    private final List <List <String>> lists = new ArrayList <>();
    private EmojiTextOnClick emojiOnClick;


    private int maxIndex = 0;       //展示的页数
    private final int showMaxLines;               //行数
    private final int showMaxColumns;             //列数
    private final int pageMaxCount;       //每个页面最多展示的emoji数量 此处不包括最后一个预留的删除
    private final int maxViewWidth;       //页面宽度
    private final int emojiSize;          //字体大小

    public List <Integer> getListInfo() {
        return listInfo;
    }

    public EmojiAdapter(Context context, List <List <String>> listSource, int maxViewWidth, int showMaxLines, int showMaxColumns, int emojiSize) {
        this.context = context;
        this.listSource = listSource;
        this.maxViewWidth = maxViewWidth;
        this.emojiSize = emojiSize;
        this.showMaxLines = showMaxLines;
        this.showMaxColumns = showMaxColumns;
        this.pageMaxCount = showMaxLines * showMaxColumns - 1;
        initList();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * 根据每个页面展示效果,序列化lists
     */
    private void initList() {
        for (List <String> list : listSource) {

            int listSize = list.size();
            int itemMaxIndex = listSize / pageMaxCount;
            if (listSize % pageMaxCount != 0) {
                //获取当前标签最大页数
                itemMaxIndex += 1;
            }
            listInfo.add(itemMaxIndex);
            //整个ViewPager最大页数
            maxIndex += itemMaxIndex;

            for (int i = 0; i < itemMaxIndex; i++) {
                List <String> tempList = new ArrayList <>();

                for (int j = 0; j < pageMaxCount; j++) {
                    int index = i * pageMaxCount + j;
                    if (index < listSize) {
                        tempList.add(list.get(index));
                    } else {
                        j = pageMaxCount;
                    }
                }
                //ViewPager展示的数据源
                lists.add(tempList);
            }
        }

    }

    @Override
    public int getCount() {
        return maxIndex;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setEmojiOnClick(EmojiTextOnClick emojiOnClick) {
        this.emojiOnClick = emojiOnClick;
    }


    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        List <String> list = lists.get(position);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(ViewUtils.dip2px(context, 2), ViewUtils.dip2px(context, 0),
                ViewUtils.dip2px(context, 2), ViewUtils.dip2px(context, 18));
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        //分行展示
        for (int index = 0; index < showMaxLines; index++) {
            LinearLayout linearLayoutIndex = new LinearLayout(context);
            linearLayout.addView(linearLayoutIndex);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayoutIndex.getLayoutParams();
            layoutParams.weight = 1;

            linearLayoutIndex.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutIndex.setGravity(Gravity.CENTER);

            for (int i = index * showMaxColumns; i < (index + 1) * showMaxColumns; i++) {
                //使用自定义View用于展示Emoji
                EmojiCustomTextView textView = new EmojiCustomTextView(context);
                textView.setGravity(Gravity.CENTER);
                textView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_select_bg, context.getTheme()));
                linearLayoutIndex.addView(textView);
                textView.setTextSize(emojiSize);
                textView.getLayoutParams().width = maxViewWidth / showMaxColumns;
                textView.setGravity(Gravity.CENTER);

                //即使没有内容,也要将空view填充占位,防止最后的删除按钮错位
                if (i < list.size()) {
                    textView.setText(list.get(i));
                    final String finaltext = list.get(i);
                    textView.setOnClickListener(v -> emojiOnClick.onClick(finaltext));
                } else {
                    if (i == pageMaxCount) {
                        //添加最后的删除按钮
                        linearLayoutIndex.removeView(textView);
                        LinearLayout backLinearLayout = new LinearLayout(context);
                        linearLayoutIndex.addView(backLinearLayout);
                        backLinearLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_select_bg, context.getTheme()));
                        backLinearLayout.getLayoutParams().width = maxViewWidth / showMaxColumns;
                        backLinearLayout.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;


                        backLinearLayout.setGravity(Gravity.CENTER);


                        TextView backTextView = new TextView(context);
                        backLinearLayout.addView(backTextView);

                        LinearLayout.LayoutParams textViewLayout = (LinearLayout.LayoutParams) backTextView.getLayoutParams();
                        textViewLayout.width = ViewUtils.dip2px(context, 28);
                        textViewLayout.height = ViewUtils.dip2px(context, 23);
                        backTextView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.emoji_delete, context.getTheme()));
                        backLinearLayout.setOnClickListener(v -> emojiOnClick.onClick("-1"));
                    }
                }
            }

        }


        ViewPager viewPager = (ViewPager) container;
        viewPager.addView(linearLayout);

        return linearLayout;
    }

    /**
     * emoji点击回调按钮
     */
    public interface EmojiTextOnClick {
        void onClick(String text);
    }
}