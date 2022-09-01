package com.unitech.scanner.utility.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.BarcodeType;
import com.unitech.scanner.utility.service.ApiLocal;
import com.unitech.scanner.utility.ui.MainActivity;

import java.util.Arrays;

public class TestFragment extends Fragment {
    private LinearLayout contentLayout;
    private SharedPreferences defaultPref;

    public static TestFragment newInstance() {
        return new TestFragment();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //-----------------------------------------------------------------------------------------
        ApiLocal usuLocal = new ApiLocal(MainActivity.getInstance());
        //-----------------------------------------------------------------------------------------
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);
        rootView.findViewById(R.id.btn_start_decode).setOnClickListener(view -> MainActivity.executorService.execute(usuLocal::startDecode));
        rootView.findViewById(R.id.btn_stop_decode).setOnClickListener(view -> MainActivity.executorService.execute(usuLocal::stopDecode));
        rootView.findViewById(R.id.btn_clear).setOnClickListener(view -> clear());
        contentLayout = rootView.findViewById(R.id.content_layout);
        //-----------------------------------------------------------------------------------------
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        defaultPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getInstance());
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null) return;
            if (bundle == null) return;
            String dataKey = defaultPref.getString(getString(R.string.setting_StringData), AllDefaultValue.setting_StringData);
            String dataByteKey = defaultPref.getString(getString(R.string.setting_StringDataByte), AllDefaultValue.setting_StringDataByte);
            String dataTypeKey = defaultPref.getString(getString(R.string.setting_StringDataType), AllDefaultValue.setting_StringDataType);
            String dataLengthKey = defaultPref.getString(getString(R.string.setting_StringDataLength), AllDefaultValue.setting_StringDataLength);
            if (dataKey == null) {
                dataKey = AllDefaultValue.setting_StringData;
            }
            if (dataByteKey == null) {
                dataByteKey =AllDefaultValue.setting_StringDataByte;
            }
            if (dataTypeKey == null) {
                dataTypeKey = AllDefaultValue.setting_StringDataType;
            }
            if (dataLengthKey == null) {
                dataLengthKey = AllDefaultValue.setting_StringDataLength;
            }
            String outputData = "";
            if (bundle.get(dataKey) != null) {
                String data = bundle.getString(dataKey);
                if (data == null) {
                    data = "";
                }
                outputData += "Data = " + data + "\n";
            }

            if (bundle.get(dataTypeKey) != null) {
                int dataType = bundle.getInt(dataTypeKey);
                try {
                    BarcodeType barcodeType = BarcodeType.fromValue(dataType);
                    outputData += "Data Type = " + barcodeType + " (" + dataType + ")" + "\n";
                } catch (Exception e) {
                    outputData += "Get Error Data Type!\n";
                }
            }
            if (bundle.get(dataLengthKey) != null) {
                int dataLength = bundle.getInt(dataLengthKey);
                outputData += "Data Length = " + dataLength + "\n";
            }
            if (bundle.get(dataByteKey) != null) {
                byte[] dataArray = bundle.getByteArray(dataByteKey);
                if (dataArray != null && dataArray.length > 0) {
                    outputData += "Data Byte array = " + Arrays.toString(dataArray) + "\n";
                }
            }
            outputData = "=====================\n" + outputData;
            addEditText(outputData);

        }
    };
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null) return;
            if (bundle == null) return;
            String dataKey = defaultPref.getString(getString(R.string.setting_StringData), AllDefaultValue.setting_StringData);
            String dataByteKey = defaultPref.getString(getString(R.string.setting_StringDataByte), AllDefaultValue.setting_StringDataByte);
            String dataTypeKey = defaultPref.getString(getString(R.string.setting_StringDataType), AllDefaultValue.setting_StringDataType);
            String dataLengthKey = defaultPref.getString(getString(R.string.setting_StringDataLength),AllDefaultValue.setting_StringDataLength);
            if (dataKey == null) {
                dataKey = AllDefaultValue.setting_StringData;
            }
            if (dataByteKey == null) {
                dataByteKey = AllDefaultValue.setting_StringDataByte;
            }
            if (dataTypeKey == null) {
                dataTypeKey =AllDefaultValue.setting_StringDataType;
            }
            if (dataLengthKey == null) {
                dataLengthKey = AllDefaultValue.setting_StringDataLength;
            }
            String outputData = "";
            if (bundle.get(dataKey) != null) {
                String data = bundle.getString(dataKey);
                if (data == null) {
                    data = "";
                }
                outputData += "Data = " + data + "\n";
            }
            if (bundle.get(dataTypeKey) != null) {
                int dataType = bundle.getInt(dataTypeKey);
                try {
                    BarcodeType barcodeType = BarcodeType.fromValue(dataType);
                    outputData += "Data Type = " + barcodeType + " (" + dataType + ")" + "\n";
                } catch (Exception e) {
                    outputData += "Get Error Data Type!\n";
                }
            }
            if (bundle.get(dataLengthKey) != null) {
                int dataLength = bundle.getInt(dataLengthKey);
                outputData += "Data Length = " + dataLength + "\n";
            }
            if (bundle.get(dataByteKey) != null) {
                byte[] dataArray = bundle.getByteArray(dataByteKey);
                if (dataArray != null && dataArray.length > 0) {
                    outputData += "Data Byte array = " + Arrays.toString(dataArray) + "\n";
                }
            }
            outputData = "=====================\n" + outputData;
            addEditText(outputData);
        }
    };

    private boolean tempPrefOfScan2Key = true;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setTitle(R.string.menu_DataTesting);
        enablePublicReceiver(true);
        enableLocalReceiver(true);
        tempPrefOfScan2Key = defaultPref.getBoolean(getString(R.string.setting_Scan2key), AllDefaultValue.setting_Scan2key);
        defaultPref.edit().putBoolean(getString(R.string.setting_Scan2key), false).apply();
    }

    @Override
    public void onPause() {
        enablePublicReceiver(false);
        enableLocalReceiver(false);
        clear();
        defaultPref.edit().putBoolean(getString(R.string.setting_Scan2key), tempPrefOfScan2Key).apply();
        super.onPause();
    }

    private void enablePublicReceiver(boolean enable) {
        String action = defaultPref.getString(getString(R.string.setting_DataAction), getString(R.string.defaultIntentAction));
        if (enable) {
            MainActivity.getInstance().registerReceiver(mBroadcastReceiver, new IntentFilter(action));
        } else {
            MainActivity.getInstance().unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void enableLocalReceiver(boolean enable) {
        String action = defaultPref.getString(getString(R.string.setting_DataAction), getString(R.string.defaultIntentAction));
        if (enable) {
            LocalBroadcastManager.getInstance(MainActivity.getInstance()).registerReceiver(localBroadcastReceiver, new IntentFilter(action));
        } else {
            LocalBroadcastManager.getInstance(MainActivity.getInstance()).unregisterReceiver(localBroadcastReceiver);
        }
    }

    private void addEditText(String text) {
        TextView textView = new TextView(MainActivity.getInstance(), null);
        textView.setTextColor(MainActivity.getInstance().getColor(R.color.testtext));
        textView.setTextSize(16);
        textView.setText(text);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.requestFocus();

//        contents.add(new Pair <>(textView, text));
//        contentLayout.addView(textView);
        contentLayout.addView(textView, 0);
    }

    private void clear() {
        if (contentLayout != null) {
            contentLayout.removeAllViews();
        }
    }

}
