package com.unitech.scanner.utility.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.unitech.api.bluetooth.BluetoothCtrl;
import com.unitech.scanner.utility.BuildConfig;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllDefaultValue;
import com.unitech.scanner.utility.config.AllUITag;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.config.App;
import com.unitech.scanner.utility.receiver.SettingBroadcastReceiver;
import com.unitech.scanner.utility.service.ApiLocal;
import com.unitech.scanner.utility.service.ApiPublic;
import com.unitech.scanner.utility.service.CustomFloatingViewService;
import com.unitech.scanner.utility.service.MainService;
import com.unitech.scanner.utility.service.mainUsage.RemoteDeviceInfo;
import com.unitech.scanner.utility.service.mainUsage.TargetScanner;
import com.unitech.scanner.utility.ui.fragment.ApplicationSettingsFragment;
import com.unitech.scanner.utility.ui.fragment.LabelFormattingFragment;
import com.unitech.scanner.utility.ui.fragment.MainFragment;
import com.unitech.scanner.utility.ui.fragment.QuickSettingChartFragment;
import com.unitech.scanner.utility.ui.fragment.SettingsFragment;
import com.unitech.scanner.utility.ui.fragment.TestFragment;
import com.unitech.scanner.utility.utils.ExceptionHandler;
import com.unitech.scanner.utility.weight.floatingview.FloatingViewManager;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.unitech.api.bluetooth.BluetoothCtrl.RESULT_KEY_BT_MAC;
import static com.unitech.scanner.utility.config.App.exportBundle;
import static com.unitech.scanner.utility.config.App.mUriArray;


public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    //==============================================================================================
    //region Variable
    //--------------------------------------- Context ----------------------------------------------
    private static MainActivity instance;
    private SharedPreferences defaultPref = null;
    //-------------------------------------- Settings ----------------------------------------------
    public static SettingsFragment setting = null;
    //------------------------------------ Manager -------------------------------------------------
    public ApiLocal usu_api = null;
    private InputMethodManager inputMethodManager = null;
    public static BluetoothAdapter bluetoothAdapter = null;
    //-------------------------------------- Handler -----------------------------------------------
    public static Handler handler = null;
    //-------------------------------------- Receiver ----------------------------------------------
    private static SettingBroadcastReceiver settingBroadcastReceiver = null;
    private final BroadcastReceiver floatingButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null) return;
            if (bundle == null) return;
            boolean startFloatingService = bundle.getBoolean("enable",AllDefaultValue.setting_FloatingButton);
            Logger.debug("floatingButtonReceiver startFloatingService = "+startFloatingService);
            if(startFloatingService){
                showFloatingView(true);
            }else{
                stopFloatingService();
            }
        }
    };
    //-------------------------------------- UI ----------------------------------------------------
    private static AlertDialog LoadingDialog = null;
    //-------------------------------------- ThreadPoolExecutor ------------------------------------
    public static ThreadPoolExecutor executorService = null;
    //-------------------------------------- Semaphore ---------------------------------------------
    public static Semaphore btSentSemaphore = null;
    //-------------------------------------- public variable ---------------------------------------
    public static boolean isAbleToAcceptClientConnect = false;
    private String bluetoothAddress = null;
    //-------------------------------------- private variable ---------------------------------------
    private final int requestWriteStorageRequestCode = 112;
    private final int requestOverlayPermissionCode = 200;
    private final int requestOpenInputMethodPicker = 2000;
    private final int requestBluetoothDiscoverable = 4000;
    private final int REQUEST_EXT_STORAGE_ACCESS = 4010;
    private final int REQUEST_EXT_STORAGE_ACCESS_Q = 4011;
    private final int requestEnableBt = 909;
    private String dmServiceBTMac = null;
    //..............................................................................................
    private boolean isInMainFragment = true;
    private boolean isInChildOfSettingFragment = false;
    private boolean isRootDeviceFragmentDisplay = false;
    private boolean isRootSettingFragmentDisplay = false;
    private boolean isRootApplicationSettingFragmentDisplay = false;
    private boolean isRootQuickChartFragmentDisplay = false;
    private boolean isRootLabelFormattingDisplay = false;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean canCommit = false;
    private static boolean isMoveTaskToBack = false;
    //------------------------------------ protected variable --------------------------------------
    protected static String btSerialNo = "";
    //endregion
    //==============================================================================================
    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        super.onCreate(savedInstanceState);
        //------------------------------------------------------------------------------------------
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0.75f;
        getWindow().setAttributes(params);
        setContentView(R.layout.activity_main);
        Logger.debug("Vendor info MANUFACTURER = " + Build.MANUFACTURER + " , Model = " + Build.MODEL);
        //------------------------------------------------------------------------------------------
        mUriArray.clear();
        Intent intent = getIntent();
        if (intent.hasExtra("ismoveTaskToBack")) {
            isMoveTaskToBack = intent.getBooleanExtra("ismoveTaskToBack", false);
        }
        //------------------------------------------------------------------------------------------
        defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //------------------------------------------------------------------------------------------
        InitCreateCheckWrite();
        //------------------------------------------------------------------------------------------
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// must store the new intent unless getIntent() will return the old one
        if (intent.hasExtra("ismoveTaskToBack")) {
            if (intent.getBooleanExtra("ismoveTaskToBack", false)) {
                moveTaskToBack(true);
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isMoveTaskToBack)
            moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent.hasExtra("ismoveTaskToBack")) {
            if (intent.getBooleanExtra("ismoveTaskToBack", false)) {
                moveTaskToBack(true);
            }
        }

        if (setting == null) setting = new SettingsFragment();
        if (handler == null) handler = new Handler();
        if (settingBroadcastReceiver == null)
            settingBroadcastReceiver = new SettingBroadcastReceiver();
        if (usu_api == null) usu_api = new ApiLocal(getApplicationContext());
        if (LoadingDialog == null) LoadingDialog = progressDialog();

        instance = this;//存储引用


        canCommit = true;
        Map <String, ?> allEntries = defaultPref.getAll();
        if (!allEntries.isEmpty()) {
            int settingCount = allEntries.size();
            Logger.debug("settingCount = " + settingCount);
        }

    }

    @Override
    protected void onDestroy() {
        Logger.info("call onDestroy");
        enableFloatingButtonReceiver(false);
        super.onDestroy();
        try {
            exitAPP();
        } catch (Exception ex) {
            String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
            Logger.error(error);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == requestWriteStorageRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                InitHasWriteCheckBT();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Logger.debug(MainActivity.class.getSimpleName(), "Returning from input settings with code: " + requestCode);

        switch (requestCode) {
            case requestEnableBt: {
                InitHasBTCheckIme();
                break;
            }
            case requestOpenInputMethodPicker: {
                initCheckImeOpenIme();
                break;
            }
            case requestBluetoothDiscoverable: {
                Logger.trace("REQUEST_BLUETOOTH_DISCOVERABLE");
                int extraDiscoverableDurationValue = 300;
                if (resultCode == extraDiscoverableDurationValue) {
                    Logger.trace("REQUEST_BLUETOOTH_DISCOVERABLE");
                    Logger.trace("isAbleToAcceptClientConnect:{]", isAbleToAcceptClientConnect);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Logger.trace("RESULT_CANCELED");
                    isAbleToAcceptClientConnect = false;
                    Logger.trace("isAbleToAcceptClientConnect: false");
                    if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                                .putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, extraDiscoverableDurationValue);
                        startActivityForResult(mIntent, requestBluetoothDiscoverable);
                    }
                }
                break;
            }
            case requestOverlayPermissionCode: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        App.toast(getInstance(), "\n" + "Authorization failure");
                    } else {
                        App.toast(getInstance(), "Authorized success");
                        showFloatingView( false);
                    }
                }
                break;
            }
            case REQUEST_EXT_STORAGE_ACCESS:
            case REQUEST_EXT_STORAGE_ACCESS_Q: {
                String filepath = "";
                String customPackage = "";
                if (exportBundle != null) {
                    filepath = exportBundle.getString("filepath");
                    customPackage = exportBundle.getString("customPackage");
                    exportBundle = null;
                }
                if (resultCode == RESULT_CANCELED) {
                    sendBroadcast(new Intent(AllUsageAction.apiExportSettingsReply)
                            .putExtra("result", 1)
                            .putExtra("packageName", customPackage)
                            .putExtra("message", "No allow permission")
                            .setPackage(customPackage)
                    );
                }
                if (intent != null && intent.getData() != null) {
                    Uri uri = intent.getData();
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Logger.tag(AllUITag.mainActivity).debug("onActivityResult data= " + intent);
                    mUriArray.add(uri);
                    if (filepath != null) {
                        ApiPublic api = new ApiPublic(getInstance(), customPackage);
                        api.exportSettings(filepath);
                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        try {
            Bundle args = pref.getExtras();
            Class <?> cls = Class.forName(pref.getFragment());
            Fragment fragment = (Fragment) cls.newInstance();

            fragment.setArguments(args);
            fragment.setTargetFragment(caller, 0);
            // Replace the existing Fragment with the new Fragment
            if (canCommit) {
                isInChildOfSettingFragment = true;
                isRootSettingFragmentDisplay = false;
                Logger.trace("add {}", AllUITag.childFragment);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.settingFrag, fragment, AllUITag.childFragment)
                        .addToBackStack(AllUITag.childFragment)
                        .commit();
                findFragmentListInFragmentManager();
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        canCommit = false;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String password = "";
        String sPassword = defaultPref.getString(getString(R.string.setting_Password), AllDefaultValue.setting_Password);
        if (sPassword != null) {
            password = sPassword.trim();
        }
        int menu_itemID = item.getItemId();
        if (menu_itemID == R.id.menu_home || menu_itemID == R.id.menu_scanner_info || menu_itemID == R.id.menu_quick_settings_chart) {
            changeFragment(menu_itemID, AllDefaultValue.setting_Password);
        } else if (menu_itemID == R.id.menu_scanner_configurations ||
                menu_itemID == R.id.menu_application_settings ||
                menu_itemID == R.id.menu_label_formatting ||
                menu_itemID == R.id.menu_reset_all_settings ||
                menu_itemID == R.id.menu_import_settings
        ) {
            if (menu_itemID == R.id.menu_scanner_configurations && (MainService.scannersMap == null || MainService.scannersMap.isEmpty())) {
                App.toast(getInstance(), "Cannot find any connected device!");
                return true;
            }
            if (password.equals(AllDefaultValue.setting_Password)) {
                changeFragment(menu_itemID, AllDefaultValue.setting_Password);
            } else {
                showPasswordDialog(password, menu_itemID);
            }
        } else if (
                menu_itemID == R.id.menu_export_settings ||
                        menu_itemID == R.id.menu_download_settings ||
                        menu_itemID == R.id.menu_upload_settings
        ) {
            if (password.equals(AllDefaultValue.setting_Password)) {
                settingsIO(menu_itemID);
            } else {
                showPasswordDialog(password, menu_itemID);
            }
        } else if (menu_itemID == R.id.menu_about) {
            versionDialog().show();
        } else if (menu_itemID == R.id.menu_exit) {
            exitDialog().show();
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.executePendingTransactions()) {
            Logger.trace("there were any pending transactions to be executed.");
        }
        Fragment settingFragment = fragmentManager.findFragmentByTag(AllUITag.scannerSettingsFragment);
        Fragment deviceFragment = fragmentManager.findFragmentByTag(AllUITag.deviceFragment);
        Fragment applicationSettingsFragment = fragmentManager.findFragmentByTag(AllUITag.appSettingsFragment);
        Fragment labelFormattingFragment = fragmentManager.findFragmentByTag(AllUITag.labelFormattingFragment);
        Fragment quickChartFragment = fragmentManager.findFragmentByTag(AllUITag.chartFragment);

        isRootSettingFragmentDisplay = settingFragment != null && settingFragment.isAdded() && settingFragment.isVisible();
        isRootDeviceFragmentDisplay = deviceFragment != null && deviceFragment.isAdded() && deviceFragment.isVisible();
        isRootApplicationSettingFragmentDisplay = applicationSettingsFragment != null && applicationSettingsFragment.isAdded() && applicationSettingsFragment.isVisible();
        isRootLabelFormattingDisplay = labelFormattingFragment != null && labelFormattingFragment.isAdded() && labelFormattingFragment.isVisible();
        isRootQuickChartFragmentDisplay = quickChartFragment != null && quickChartFragment.isAdded() && quickChartFragment.isVisible();

        if ((!isInMainFragment && !isInChildOfSettingFragment) ||
                isRootSettingFragmentDisplay ||
                isRootDeviceFragmentDisplay ||
                isRootApplicationSettingFragmentDisplay ||
                isRootLabelFormattingDisplay ||
                isRootQuickChartFragmentDisplay) {
            changeFragment(R.id.menu_home, AllDefaultValue.setting_Password);
        } else if (isInMainFragment) {
            if (!doubleBackToExitPressedOnce) {
                this.doubleBackToExitPressedOnce = true;
                App.toast(getInstance(), "Please click BACK again to exit.");
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SettingsFragment) {
            setting = (SettingsFragment) fragment;
        }
    }
    //endregion
    //==============================================================================================
    //region Util

    /**
     * get App versionName
     *
     * @param context input the app's context
     * @return version name
     */
    public String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void exitAPP() {
        SharedPreferences targetScannerPref = MainActivity.getInstance().getSharedPreferences("TargetScanner", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = targetScannerPref.edit();
        editor.putBoolean("IsConnected", false);
        editor.apply();
        Logger.info("call exitAPP");
        MainService.isRunning = false;
        if (executorService != null)
            executorService.shutdown();
        File file = new File(Environment.getExternalStorageDirectory(), "com.unitech.scanner.utility");
        if (file.exists())
            if (file.delete())
                Logger.trace("file com.unitech.scanner.utility is delete");


        bluetoothAdapter.cancelDiscovery();
        enableSettingBroadcastReceiver(false);
        stopMainService();
        stopFloatingService();
        ActivityManager activityManager = (ActivityManager) MainActivity.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        List <ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
        System.exit(0);
    }

    private static void stopFloatingService() {
        Logger.debug("stopFloatingService");
        MainActivity.getInstance().stopService(new Intent(MainActivity.getInstance(), CustomFloatingViewService.class));
    }

    private static void stopMainService() {
        MainActivity.getInstance().stopService(new Intent(MainActivity.getInstance(), MainService.class));
    }

    private void download() {
        App.toast(getInstance(), "Start Download");
        usu_api.getConfiguration();
        usu_api.getBTSignalCheckingLevel();

        RemoteDeviceInfo scannerInfo = MainService.scannersMap.get(getBtSerialNo());
        if (scannerInfo != null && TargetScanner.readSN().contentEquals(MainActivity.getBtSerialNo()) && scannerInfo.getFw() != null) {
            String[] parts = scannerInfo.getFw().split("\\."); // escape .
            if (parts.length >= 2) {
                int version = Integer.parseInt(parts[1]);
                if (version > 43) {
                    usu_api.getFormat();
                }
                if (version > 46) {
                    usu_api.getDataTerminator();
                }
            }
        }

    }

    private void showPasswordDialog(String password, int menu_itemID) {
        AlertDialog mDialog = passwordDialog(password, menu_itemID);
        Window window = mDialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        mDialog.show();
    }

    private boolean settingsIO(int menu_ID) {
        if (menu_ID == R.id.menu_upload_settings) {
            usu_api.uploadAllSettings();
            return true;
        } else if (menu_ID == R.id.menu_download_settings) {
            if (MainService.scannersMap == null || MainService.scannersMap.isEmpty()) {
                executorService.execute(() -> App.toast(getInstance(), "Cannot find any connected device!"));

            } else {
                MainActivity.showProgressBar();
                executorService.execute(this::download);
            }
            return true;
        } else if (menu_ID == R.id.menu_export_settings) {
            usu_api.exportSettings(null);
            return true;
        }
        return false;
    }


    //endregion
    //==============================================================================================
    //region Initial

    public static MainActivity getInstance() {
        return instance;
    }

    public void InitCreateCheckWrite() {
        setting = new SettingsFragment();
        handler = new Handler();
        settingBroadcastReceiver = new SettingBroadcastReceiver();
        instance = this;//存储引用
        if (LoadingDialog == null) {
            LoadingDialog = progressDialog();
        }
        //---------------------------------- Manager -----------------------------------------------
        if (usu_api == null) {
            usu_api = new ApiLocal(getApplicationContext());
        }
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //------------------------------------------------------------------------------------------
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Logger.trace("availableProcessors={}", Runtime.getRuntime().availableProcessors());
        executorService = new ThreadPoolExecutor(10, 100, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue <>(100), new ThreadPoolExecutor.DiscardOldestPolicy());
        btSentSemaphore = new Semaphore(1);

        requestAppPermissions();
//        checkStorage();
    }

    private void InitHasWriteCheckBT() {
        File file = new File(Environment.getExternalStorageDirectory(), "com.unitech.scanner.utility");
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(file));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bluetoothAdapter.isEnabled()) {
            InitHasBTCheckIme();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, requestEnableBt);
        }
    }

    private  void enableFloatingButtonReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.floatingServiceStart);
            LocalBroadcastManager.getInstance(MainActivity.getInstance()).registerReceiver(floatingButtonReceiver, filter);
        } else {
            LocalBroadcastManager.getInstance(MainActivity.getInstance()).unregisterReceiver(floatingButtonReceiver);
        }
    }

    private static void enableSettingBroadcastReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.apiGetTriggerReply);
            filter.addAction(AllUsageAction.apiGetConfigReply);
            filter.addAction(AllUsageAction.apiGetBatteryReply);
            filter.addAction(AllUsageAction.apiGetFWReply);
            filter.addAction(AllUsageAction.apiGetSNReply);
            filter.addAction(AllUsageAction.apiGetAddressReply);
            filter.addAction(AllUsageAction.apiGetNameReply);
            filter.addAction(AllUsageAction.apiGetAckReply);
            filter.addAction(AllUsageAction.serviceGetScannerReply);
            filter.addAction(AllUsageAction.apiGetAutoConnectionReply);
            filter.addAction(AllUsageAction.apiGetBtSignalCheckingLevelReply);
            filter.addAction(AllUsageAction.apiGetDataTerminatorReply);
            filter.addAction(AllUsageAction.apiGetFormatReply);
            filter.addAction(AllUsageAction.apiUploadSettings);
            LocalBroadcastManager.getInstance(MainActivity.getInstance()).registerReceiver(settingBroadcastReceiver, filter);
        } else {
            if (settingBroadcastReceiver != null)
                LocalBroadcastManager.getInstance(MainActivity.getInstance()).unregisterReceiver(settingBroadcastReceiver);
        }
    }

    private void InitHasBTCheckIme() {
        //---------------------------------- Receiver ------------------------------------------
        enableSettingBroadcastReceiver(true);
        enableFloatingButtonReceiver(true);
        //---------------------------------- UI ----------------------------------------------------
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //-------------------------------------- BTShow --------------------------------------------
        executorService.execute(() -> {
            try {
                dmServiceBTMac = new BluetoothCtrl(getApplicationContext()).getMacAddress().getString(RESULT_KEY_BT_MAC);
            } catch (Exception e) {
                Logger.info("Dm service not get bt mac.");
            }
        });
        handler.postDelayed(this::GenerateLinkCode, 200);
        //------------------------------------------------------------------------------------------
    }



    private void enableService() {
        if (isMyServiceRunning()) {
//            toast("The service is already running");
            return;
        }
        //----------------------------------- Service ----------------------------------------------
        Intent startScanService = new Intent(getApplicationContext(), MainService.class);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (startForegroundService(startScanService) != null) {
                Logger.info("Service started");
                App.toast(getInstance(), "Service is running");
            } else {
                App.toast(getInstance(), "Service is not running");
            }
        } else {
            if (startService(startScanService) != null) {
                Logger.info("Service started");
                App.toast(getInstance(), "Service is running");
            } else {
                App.toast(getInstance(), "Service is not running");
            }
        }
    }


    /**
     * FloatingViewの表示
     * @param isShowOverlayPermission 表示できなかった場合に表示許可の画面を表示するフラグ
     */
    public void showFloatingView( boolean isShowOverlayPermission) {
        // 他のアプリの上に表示できるかチェック
        if (Settings.canDrawOverlays(this)) {
            startFloatingViewService(this);
            return;
        }

        // オーバレイパーミッションの表示
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, requestOverlayPermissionCode);
        }
    }

    /**
     * Start floating view service
     *
     * @param activity {@link Activity}
     */
    private static void startFloatingViewService(Activity activity) {
        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (activity.getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
        }

        // launch service
        final Class <? extends Service> service = CustomFloatingViewService.class;
        final String key = CustomFloatingViewService.EXTRA_CUTOUT_SAFE_AREA;

        final Intent intent = new Intent(activity, service);
        intent.putExtra(key, FloatingViewManager.findCutoutSafeArea(activity));
        ContextCompat.startForegroundService(activity, intent);
    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initCheckImeOpenIme() {
        String currentKeyboard = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        String packageName = getPackageName();
        Logger.info("currentKeyboard:" + currentKeyboard);
        Logger.info("packageName:" + packageName);
        if (currentKeyboard.contains(packageName)) {
            enableService();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_INPUT_METHODS)) {
            App.toast(getInstance(), "Please Select the Unitech SU Keyboard");
            return;
        }
        if (inputMethodManager == null) {
            enableService();
            return;
        }
        SharedPreferences dialogPref = MainActivity.getInstance().getSharedPreferences("Dialog", Context.MODE_PRIVATE);
        boolean show = dialogPref.getBoolean("InputMethodPickerDialog", true);
        if (show && !BuildConfig.BUILD_TYPE.equals("system")) {
            inputMethodPickerDialog().show();
        } else {
            enableService();
        }
    }

    private void requestAppPermissions() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            InitHasWriteCheckBT();
            Logger.debug("requestAppPermissions PASS");
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, requestWriteStorageRequestCode); // your request code
    }

    private void checkIme() {
        if (inputMethodManager == null) return;
        int checkTime = 1;
        while (checkTime > 0) {
            checkTime--;
            List <InputMethodInfo> list = inputMethodManager.getEnabledInputMethodList();
            String USU_PkgName = getPackageName();
            for (InputMethodInfo inputMethod : list) {
                if (inputMethod.getPackageName().equals(USU_PkgName)) {
                    initCheckImeOpenIme();
                    return;
                }
            }
            if (checkTime > 0) {
                List <InputMethodInfo> listAll = inputMethodManager.getInputMethodList();
                for (InputMethodInfo inputMethod : listAll) {
                    if (inputMethod.getPackageName().equals(USU_PkgName)) {
                        Settings.Secure.putString(getApplicationContext().getContentResolver(),
                                Settings.Secure.ENABLED_INPUT_METHODS,
                                "com.android.inputmethod.latin/.LatinIME:" + inputMethod.getId());
                        Settings.Secure.putString(getApplicationContext().getContentResolver(),
                                Settings.Secure.DEFAULT_INPUT_METHOD,
                                inputMethod.getId());
                        break;
                    }
                }
            }
        }
        if (!BuildConfig.BUILD_TYPE.equals("system")) {
            inputMethodDialog().show();
        } else {
            enableService();
        }

    }


    //endregion

    //==============================================================================================
    //region Bluetooth

    public static synchronized String getBtSerialNo() {
        Logger.debug("read sn=" + btSerialNo);
        return btSerialNo;
    }

    public static synchronized void setBtSerialNo(String btSerialNo) {
        MainActivity.btSerialNo = btSerialNo;
        Logger.debug("set sn=" + MainActivity.btSerialNo);
    }

    public static synchronized void changeSocket(BluetoothSocket socket) {
        if (socket == null) {
            Logger.trace("change socket to null");
        } else {
            Logger.trace("change socket to a value");
        }

    }
    //endregion
    //==============================================================================================
    //region Progress Bar

    public static void showProgressBar() {
        if (MainActivity.getInstance() == null) return;
        if (MainActivity.getInstance().isFinishing()) return;
        if (isMoveTaskToBack) return;
        if (LoadingDialog == null) return;
        if (LoadingDialog.isShowing()) return;
        if (handler == null) return;
        handler.post(() -> LoadingDialog.show());
    }

    public static void hideProgressBar() {
        if (isMoveTaskToBack) return;
        if (handler == null) return;
        if (LoadingDialog == null) return;
        handler.post(() -> LoadingDialog.dismiss());
    }

    //endregion
    //==============================================================================================
    //region AlertDialog

    public static AlertDialog btMacDialog(String address) {
        //------------------------------------------------------------------------------------------
        ApiPublic api = new ApiPublic(getInstance(), null);
        api.setSystemScanToKey(true);
        //------------------------------------------------------------------------------------------
        LayoutInflater inflater = LayoutInflater.from(MainActivity.getInstance());
        final View v = inflater.inflate(R.layout.dialog_bt_address, null);
        EditText editText = v.findViewById(R.id.editTextBtAddress);
        TextView textView = v.findViewById(R.id.txt_bt_txt);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String tempString = MainActivity.getInstance().getString(R.string.InputBtAddressTitle);
        SpannableString spanString = new SpannableString(tempString);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                MainActivity.getInstance().startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
            }
        };
        String clickHere = "click here.";
        int index_clear_here = tempString.indexOf(clickHere);
        spanString.setSpan(clickableSpan, index_clear_here, index_clear_here + clickHere.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanString.setSpan(new StyleSpan(Typeface.NORMAL), 0, spanString.length(), 0);
        //设置字体大小（相对值,单位：像素） 参数表示为默认字体大小的多少倍   ,0.5表示一半
        spanString.setSpan(new RelativeSizeSpan(1.3f), 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置字体前景色
        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanString.setSpan(new ForegroundColorSpan(Color.BLUE), index_clear_here, index_clear_here + clickHere.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spanString);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance(), R.style.Theme_Dialog)
                .setTitle("Please Enter Your Bluetooth Address")
                .setCancelable(false);
        if (address != null && !address.equals("")) {
            builder.setNegativeButton("Later", (dialog, which) -> {
            });
            editText.setText(address);
        }
        editText.requestFocus();
        builder.setView(v)
                .setPositiveButton("OK", (dialog, which) -> {
                    String macAddressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
                    String editAddress = editText.getText().toString();
                    if ((editAddress.length() > 0 && !Pattern.matches(macAddressPattern, editAddress)) || editAddress.length() == 0) {
                        //wrong format
                        Logger.warn("wrong bt format");
                        MainActivity.getInstance().GenerateLinkCode();
                        return;
                    }
                    //correct format
                    Logger.debug("correct bt format");
                    MainActivity.getInstance().buildBtMACImage(editAddress);
                    InputMethodManager imm = (InputMethodManager) getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //強制隱藏鍵盤
                });
        return builder.create();
    }


    private AlertDialog inputMethodDialog() {
        return new AlertDialog.Builder(MainActivity.this, R.style.Theme_Dialog)
                .setTitle("Setup Wizard")
                .setMessage("(1) Navigate to Language & Input.\n\n(2) Enable Unitech SU Keyboard.\n\n(3) Click back button.\n")
                .setCancelable(false)
                .setPositiveButton("Go to Language & Input", (dialog, which) ->
                        startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), requestOpenInputMethodPicker))
                .setNegativeButton("Later", (arg1, arg2) -> enableService())
                .create();
    }

    private AlertDialog inputMethodPickerDialog() {
        return new AlertDialog.Builder(MainActivity.this, R.style.Theme_Dialog)
                .setTitle("Setup Input Method")
                .setMessage("(1) Navigate to Input Method Picker.\n\n(2) Please select the Unitech SU Keyboard.\n")
                .setCancelable(false)
                .setPositiveButton("Go to Input Method Picker", (dialog, which) -> {
                    MainActivity.handler.postDelayed(() -> inputMethodManager.showInputMethodPicker(), 200);
                    enableService();
                })
                .setNegativeButton("Never show again", (arg1, arg2) -> {
                    SharedPreferences dialogPref = MainActivity.getInstance().getSharedPreferences("Dialog", Context.MODE_PRIVATE);
                    dialogPref.edit().putBoolean("InputMethodPickerDialog", false).apply();
                    enableService();
                })
                .setNeutralButton("Later", (arg1, arg2) -> enableService())
                .create();
    }

    private AlertDialog progressDialog() {
        return new AlertDialog.Builder(this, R.style.Theme_Dialog)
                .setView(R.layout.activity_progress)
                .setCancelable(false)
                .create();
    }

    private AlertDialog passwordDialog(String password, int menuID) {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.dialog_password, null);

        return new AlertDialog.Builder(MainActivity.this, R.style.Theme_Dialog)
                .setTitle("Please enter password:")
                .setView(v)
                .setPositiveButton("OK", (dialog, which) -> {
                    EditText editText = v.findViewById(R.id.editTextPassword);
                    String editTextValue = editText.getText().toString().trim();
                    if (password.equals(editTextValue)) {
                        boolean isDone = settingsIO(menuID);
                        if (!isDone) {
                            changeFragment(menuID, password);
                        }
                    } else {
                        App.toast(getInstance(), "You have entered an invalid password:" + editTextValue);
                    }
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                })
                .create();
    }

    private AlertDialog versionDialog() {
        String mMessage = getString(R.string.version) + getVersionName(this) + "\n"
                + getString(R.string.copyright);
        return new AlertDialog.Builder(this, R.style.Theme_Dialog)
                .setTitle("Unitech Scanner Utility")
                .setMessage(mMessage)
//                .setMessage("Version v." + BuildConfig.VERSION_NAME)
                .create();
    }

    private AlertDialog exitDialog() {
        return new AlertDialog.Builder(this, R.style.Theme_Dialog)
                .setTitle("Exit Unitech Scanner Utility")
                .setSingleChoiceItems(new CharSequence[]{"Hide", "Exit Scanner Utility"}, -1, (dialogInterface, item1) -> {
                    switch (item1) {
                        case 0://Hide
                            moveTaskToBack(true);
                            break;
                        case 1://Exit Scanner Utility
                            try {
                                exitAPP();
                            } catch (Exception ex) {
                                String error = ex.getStackTrace()[ex.getStackTrace().length - 1].getLineNumber() + ":" + ex.toString();
                                Logger.error(error);
                            }
                            break;
                    }
                })
                .create();
    }

    //endregion
    //==============================================================================================
    //region Fragment

    private void changeFragment(int fragmentID, String password) {
        if (!canCommit)
            return;
        isInMainFragment = false;
        isInChildOfSettingFragment = false;
        isRootSettingFragmentDisplay = false;
        isRootDeviceFragmentDisplay = false;
        isRootApplicationSettingFragmentDisplay = false;
        isRootQuickChartFragmentDisplay = false;
        isRootLabelFormattingDisplay = false;

        FragmentManager fm = getSupportFragmentManager();

        //*Clear fragment back stack*//
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStackImmediate();
        }
        List <Fragment> fmAll = fm.getFragments();
        if (fmAll.size() > 0) {
            for (int i = 0; i < fmAll.size(); i++) {
                String tag = fmAll.get(i).getTag();
                if (tag != null &&
                        !tag.equals(AllUITag.deviceFragment) &&
                        !tag.equals(AllUITag.scannerSettingsFragment) &&
                        !tag.equals(AllUITag.childFragment) &&
                        !tag.equals(AllUITag.appSettingsFragment) &&
                        !tag.equals(AllUITag.chartFragment) &&
                        !tag.equals(AllUITag.mainFragment) &&
                        !tag.equals(AllUITag.labelFormattingFragment)
                ) {
                    Fragment fragment = fm.findFragmentByTag(tag);
                    if (fragment != null) {
                        fm.beginTransaction().remove(fragment).commitNow();
                    }
                }

            }
        }

        try {
            if (fragmentID == R.id.menu_home) {
                fm.beginTransaction()
                        .replace(R.id.settingFrag, MainFragment.newInstance(), AllUITag.mainFragment)
                        .commitNow();
                isInMainFragment = true;
//                bottomNavigationView.setVisibility(View.GONE);
            } else if (fragmentID == R.id.menu_scanner_info) {
                fm.beginTransaction()
                        .replace(R.id.settingFrag, TestFragment.newInstance(), AllUITag.deviceFragment)
                        .commitNow();
                isRootDeviceFragmentDisplay = true;
//                bottomNavigationView.setVisibility(View.GONE);
            } else if (fragmentID == R.id.menu_scanner_configurations) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.settingFrag, SettingsFragment.newInstance(), AllUITag.scannerSettingsFragment)
                        .commitNow();
                isRootSettingFragmentDisplay = true;
//                bottomNavigationView.setVisibility(View.VISIBLE);
            } else if (fragmentID == R.id.menu_application_settings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.settingFrag, ApplicationSettingsFragment.newInstance(), AllUITag.appSettingsFragment)
                        .commitNow();
                isRootApplicationSettingFragmentDisplay = true;
//                bottomNavigationView.setVisibility(View.VISIBLE);
            } else if (fragmentID == R.id.menu_label_formatting) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.settingFrag, LabelFormattingFragment.newInstance(), AllUITag.labelFormattingFragment)
                        .commitNow();
                isRootLabelFormattingDisplay = true;
//                bottomNavigationView.setVisibility(View.GONE);
            } else if (fragmentID == R.id.menu_quick_settings_chart) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.settingFrag, QuickSettingChartFragment.newInstance(), AllUITag.chartFragment)
                        .commitNow();
                isRootQuickChartFragmentDisplay = true;
//                bottomNavigationView.setVisibility(View.GONE);
            } else if (fragmentID == R.id.menu_reset_all_settings) {
                fm.beginTransaction()
                        .replace(R.id.settingFrag, MainFragment.newInstance(), AllUITag.mainFragment)
                        .commitNow();
                isInMainFragment = true;
                usu_api.resetAllSettings();
//                bottomNavigationView.setVisibility(View.GONE);
            } else if (fragmentID == R.id.menu_import_settings) {
                fm.beginTransaction()
                        .replace(R.id.settingFrag, MainFragment.newInstance(), AllUITag.mainFragment)
                        .commitNow();
                isInMainFragment = true;
                usu_api.importSettings(null, password);
            }
            Logger.debug("getFragments = " + getSupportFragmentManager().getFragments());

        } catch (Exception e) {
            e.printStackTrace();
            App.toast(getInstance(), "Change Fragment error");
        }
    }


    private void findFragmentListInFragmentManager() {
        List <Fragment> fragmentList = getSupportFragmentManager().getFragments();
        StringBuilder sb = new StringBuilder();
        for (Fragment tmp : fragmentList) {
            sb.append(tmp.getTag());
            sb.append(" ");
        }
        sb.append("BackStackEntryList:");
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager().getBackStackEntryAt(i);
            sb.append(backStackEntry.getName());
            sb.append(" ");
        }
        Logger.trace("fragmentList count={},{}", fragmentList.size(), sb.toString());

    }

    //endregion
    //==============================================================================================
    //region Bitmapping

    private void GenerateLinkCode() {
        //------------------------------------------------------------------------------------------
        // if device does not support Bluetooth
        if (bluetoothAdapter == null) {
            Logger.error("Mobile does not support bluetooth.");
            App.toast(getInstance(), "\"Your mobile does not have Bluetooth.\"");
            handler.postDelayed(MainActivity::exitAPP, 2000);
            return;
        }
        //------------------------------------------------------------------------------------------
        //get local bt address from shared preferences
        SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
        String prefBT = localInfoPref.getString(getString(R.string.setting_BtAddress), AllDefaultValue.setting_BtAddress);
        if (prefBT != null && !prefBT.equals(AllDefaultValue.setting_BtAddress)) {
            bluetoothAddress = prefBT;
            buildBtMACImage(prefBT);
            return;
        }
        //------------------------------------------------------------------------------------------
        String address = getBTAddress();
        Logger.debug("MAC:" + address);
        if (address != null && !address.equals(AllDefaultValue.setting_BtAddress)) {
            buildBtMACImage(address);
        } else {
            Logger.debug("Show MAC address dialog");
            btMacDialog(null).show();
        }
        //------------------------------------------------------------------------------------------

    }

    private String getBTAddress() {
        String address;
        String btAdapterDefault = AllDefaultValue.setting_BtAddress;

        //Get bt from DMService
        if (dmServiceBTMac != null && !dmServiceBTMac.equals("") && !dmServiceBTMac.equals(btAdapterDefault)) {
            return dmServiceBTMac;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                mServiceField.setAccessible(true);

                Object btManagerService = mServiceField.get(bluetoothAdapter);

                if (btManagerService != null) {
                    address = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
                    if (address != null && !address.trim().equals(btAdapterDefault)) {
                        return address;
                    }
                }
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

            }
        }
        address = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
        if (address == null || address.equals("null") || address.equals("")) {
            address = btAdapterDefault;
        }
        return address;
    }

    private void buildBtMACImage(@NonNull String btMAC) {
        //------------------------------------------------------------------------------------------
        bluetoothAddress = btMAC.trim().toUpperCase();
        String barcode_data = "//.USU" + btMAC.replace(":", "");
        Logger.debug("MAC = " + bluetoothAddress + " , barcode_data = " + barcode_data);
        //------------------------------------------------------------------------------------------
        // barcode data
        SharedPreferences localInfoPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.localInfo), Context.MODE_PRIVATE);
        localInfoPref.edit()
                .putString("PairingBarcodeContent", barcode_data)
                .putString(getString(R.string.setting_BtAddress), bluetoothAddress)
                .apply();
        // barcode image
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingFrag, new MainFragment(), AllUITag.mainFragment)
                .commitAllowingStateLoss();
        //------------------------------------------------------------------------------------------
        checkDmiScannerStatus();
        checkIme();
        //------------------------------------------------------------------------------------------
    }

    private void checkDmiScannerStatus() {
        String dmiScannerStatus = "";
        Process ifc = null;
        BufferedReader bis = null;
        try {
            ifc = Runtime.getRuntime().exec("getprop persist.sys.product.serialno");
            bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
            dmiScannerStatus = bis.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (ifc != null) {
                    ifc.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("doDecode", "dmiScannerStatus = " + dmiScannerStatus);
    }
    //endregion
    //==============================================================================================
}
