package com.unitech.scanner.utility.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/11/13 下午 05:50
 * 修改人:user
 * 修改時間:2020/11/13 下午 05:50
 * 修改備註:
 */

public class ChartStateAdapter extends FragmentStateAdapter {
    private final List <Fragment> fragments;

    public ChartStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        // 定義我們想要放三個 fragments as tab，以及他們的位置
        fragments = new ArrayList <>();
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }


    public void clearFragment() {
        fragments.clear();
        notifyDataSetChanged();
    }
}
