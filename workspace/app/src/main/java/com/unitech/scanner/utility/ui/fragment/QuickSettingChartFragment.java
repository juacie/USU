package com.unitech.scanner.utility.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.adapter.ChartStateAdapter;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;

public class QuickSettingChartFragment extends Fragment {
    private ViewPager2 myViewPager;
    private TabLayout tabLayout;
    private ChartStateAdapter mChartStateAdapter;

    public static QuickSettingChartFragment newInstance() {

        Bundle args = new Bundle();

        QuickSettingChartFragment fragment = new QuickSettingChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(getClass().toString() + ":" + "onCreateView");
        return inflater.inflate(R.layout.quick_setting_chart_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setTitle(R.string.menu_QuickChart);

        final String[] tab_text_array = getResources().getStringArray(R.array.chart_text_array);
        TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy = (tab, position) -> tab.setText(tab_text_array[position]);
        mChartStateAdapter = new ChartStateAdapter(MainActivity.getInstance().getSupportFragmentManager(), this.getLifecycle());
        mChartStateAdapter.addFragment(new ChartFragment().newInstance(new int[]{R.drawable.chart_bt_keyboard, R.drawable.chart_bt_spp}));
        mChartStateAdapter.addFragment(new ChartFragment().newInstance(new int[]{R.drawable.chart_unpaired}));
        mChartStateAdapter.addFragment(new ChartFragment().newInstance(new int[]{R.drawable.chart_factory_default}));
        myViewPager.setOffscreenPageLimit(4);
        myViewPager.setAdapter(mChartStateAdapter);
        new TabLayoutMediator(tabLayout, myViewPager, tabConfigurationStrategy).attach();
    }

    @Override
    public void onPause() {
        super.onPause();
        mChartStateAdapter.clearFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.info(getClass().toString() + ":" + "onViewCreated");

        myViewPager = view.findViewById(R.id.quick_setting_chart_view_pager);
        tabLayout = view.findViewById(R.id.quick_settings_chart_tab_layout);


    }


}
