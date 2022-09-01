package com.unitech.scanner.utility.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.zxing.BarcodeFormat;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.adapter.ChartStateAdapter;
import com.unitech.scanner.utility.callback.TargetCallback;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.service.ApiLocal;
import com.unitech.scanner.utility.service.MainService;
import com.unitech.scanner.utility.service.mainUsage.RemoteDeviceInfo;
import com.unitech.scanner.utility.service.mainUsage.TargetScanner;
import com.unitech.scanner.utility.ui.MainActivity;

import org.tinylog.Logger;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p>
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

//{@link MainFragment.OnFragmentInteractionListener} interface
public class MainFragment extends Fragment implements TargetCallback {
    //==============================================================================================
    private TextView txt_targetScanner_sn;
    private ImageView img_find_scanner, img_scanner_info;
    private ImageView img_connection_status, img_disconnection_status;
    private  ChartStateAdapter mChartStateAdapter = null;
    //==============================================================================================
    public MainFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.info(getClass().toString() + ":" + "onCreateView");

        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //------------------------------------------------------------------------------------------
        view.invalidate();
        Logger.info(getClass().toString() + ":" + "onViewCreated");
        //------------------------------------------------------------------------------------------
        InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //強制隱藏鍵盤
        //------------------------------------------------------------------------------------------
        ViewPager2 myViewPager = view.findViewById(R.id.viewpager);
        txt_targetScanner_sn = view.findViewById(R.id.txt_scanner_sn);
        img_connection_status = view.findViewById(R.id.img_connection_status);
        img_disconnection_status = view.findViewById(R.id.img_disconnection_status);
        img_find_scanner = view.findViewById(R.id.img_find_scanner);
        img_scanner_info = view.findViewById(R.id.img_scanner_info);
       TextView txt_change_image = view.findViewById(R.id.txt_change_image);
        //------------------------------------------------------------------------------------------
        SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
        String bluetoothMac = localInfoPref.getString(getString(R.string.setting_BtAddress), AllDefaultValue.setting_BtAddress);
        bluetoothMac = bluetoothMac == null ? AllDefaultValue.setting_BtAddress : bluetoothMac;
        String barcode_data = "//.USU" + bluetoothMac.replace(":", "").toUpperCase();

        myViewPager.setOffscreenPageLimit(2);
        mChartStateAdapter = new ChartStateAdapter(MainActivity.getInstance().getSupportFragmentManager(), MainActivity.getInstance().getLifecycle());
        mChartStateAdapter.addFragment(new ChartFragment().newInstance(barcode_data, BarcodeFormat.CODE_39.name()));
        mChartStateAdapter.addFragment(new ChartFragment().newInstance(barcode_data, BarcodeFormat.QR_CODE.name()));
        myViewPager.setAdapter(mChartStateAdapter);
        myViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String str = "[1D]";
                if(position == 1){
                    str = "[2D]";
                }
                txt_change_image.setText(str);
            }
        });
        //------------------------------------------------------------------------------------------
        SharedPreferences targetScannerPref = MainActivity.getInstance().getSharedPreferences("TargetScanner", Context.MODE_PRIVATE);
        setSN(targetScannerPref.getString("SN", ""));
        setStatus(TargetScanner.readIsConnected());
        //------------------------------------------------------------------------------------------
        img_find_scanner.setOnClickListener(view1 -> {
            if (!TargetScanner.readIsConnected()) {
                return;
            }
            if (MainService.scannersMap == null || MainService.scannersMap.isEmpty()) {
                App.toast(MainActivity.getInstance(), "Cannot find any connected device!");
            } else {
                try {
                    ApiLocal usu_api = new ApiLocal(MainActivity.getInstance());
                    Bundle bundle = new Bundle();
                    bundle.clear();
                    bundle.putInt("beepTime", 3);
                    bundle.putBoolean("vibrate", true);
                    bundle.putString("ledColor", "green");
                    bundle.putBoolean("dataAck", false);
                    usu_api.setIndicator(bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        img_scanner_info.setOnClickListener(view12 -> {
            if (!TargetScanner.readIsConnected()) {
                return;
            }
            LayoutInflater inflater = LayoutInflater.from(MainActivity.getInstance());
            final View v = inflater.inflate(R.layout.dialog_scanner_info, null);
            TextView txt_name = v.findViewById(R.id.txt_name);
            TextView txt_sn = v.findViewById(R.id.txt_sn);
            TextView txt_mac = v.findViewById(R.id.txt_mac);
            TextView txt_battery = v.findViewById(R.id.txt_battery);
            TextView txt_fw = v.findViewById(R.id.txt_fw);
            RemoteDeviceInfo deviceInfo = MainService.scannersMap.get(txt_targetScanner_sn.getText().toString());
            if (deviceInfo != null) {
                txt_name.setText(deviceInfo.getName());
                txt_sn.setText(deviceInfo.getSn());
                txt_mac.setText(deviceInfo.getAddress());
                txt_battery.setText(deviceInfo.getBatteryLevel());
                txt_fw.setText(deviceInfo.getFw());
            }

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.getInstance(), R.style.Theme_Dialog)
                    .setTitle("Scanner Info")
                    .setView(v)
                    .create();
            dialog.show();

        });
        //------------------------------------------------------------------------------------------
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setTitle(R.string.app_name);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mChartStateAdapter!=null){
            mChartStateAdapter.clearFragment();
        }

    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setStatus(boolean status) {
        MainActivity.handler.post(() -> {
                    if (status) {
                        img_connection_status.setVisibility(View.VISIBLE);
                        img_disconnection_status.setVisibility(View.GONE);
                        img_find_scanner.setEnabled(true);
                        img_scanner_info.setEnabled(true);
                    } else {
                        img_connection_status.setVisibility(View.GONE);
                        img_disconnection_status.setVisibility(View.VISIBLE);
                        img_find_scanner.setEnabled(false);
                        img_scanner_info.setEnabled(false);
                    }
                }
        );
    }

    @Override
    public void setSN(String sn) {
        MainActivity.handler.post(() -> txt_targetScanner_sn.setText(sn));
    }
}
