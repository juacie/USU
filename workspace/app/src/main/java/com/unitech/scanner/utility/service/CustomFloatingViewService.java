package com.unitech.scanner.utility.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.ui.MainActivity;
import com.unitech.scanner.utility.weight.floatingview.FloatingViewListener;
import com.unitech.scanner.utility.weight.floatingview.FloatingViewManager;

import org.tinylog.Logger;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/18 下午 04:35
 * 修改人:user
 * 修改時間:2021/2/18 下午 04:35
 * 修改備註:
 */

public class CustomFloatingViewService extends Service implements FloatingViewListener {

    /**
     * Intent key (Cutout safe area)
     */
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 908114;

    /**
     * Prefs Key(Last position X)
     */
    private static final String PREF_KEY_LAST_POSITION_X = "last_position_x";

    /**
     * Prefs Key(Last position Y)
     */
    private static final String PREF_KEY_LAST_POSITION_Y = "last_position_y";

    /**
     * FloatingViewManager
     */
    private FloatingViewManager mFloatingViewManager;


    private boolean isScanDown = false;
    private boolean isWaitStartDecodeReply = false;
    private boolean isWaitStopDecodeReply = false;
    private boolean isWaitingGetData = false;

    private ApiPublic usuApi;

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 既にManagerが存在していたら何もしない
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }
        usuApi = new ApiPublic(getApplicationContext());
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final ImageView iconView = (ImageView) inflater.inflate(R.layout.widget_scan, null, false);

        mFloatingViewManager = new FloatingViewManager(this, this);
//        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
//        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
//        mFloatingViewManager.setSafeInsetRect(intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        // Setting Options(you can change options at any time)
        loadDynamicOptions();
        // Initial Setting Options (you can't change options after created.)
        final FloatingViewManager.Options options = loadOptions(metrics);
        mFloatingViewManager.addViewToWindow(iconView, options);

        // 常駐起動
        startForeground(NOTIFICATION_ID, createNotification(MainActivity.getInstance()));

        enableReceiver(true);
        enableLocalReceiver(true);
        return START_REDELIVER_INTENT;
    }


    private void enableReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.apiData);
            filter.addAction(AllUsageAction.apiStartDecodeReply);
            filter.addAction(AllUsageAction.apiStopDecodeReply);
            registerReceiver(mBroadcastReceiver, filter);
        } else {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void enableLocalReceiver(boolean enable) {
        if (enable) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AllUsageAction.apiStartDecodeReply);
            filter.addAction(AllUsageAction.apiStopDecodeReply);
            registerReceiver(mLocalBroadcastReceiver, filter);
        } else {
            unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent mIntent) {
            String action = mIntent.getAction();
            Bundle mBundle = mIntent.getExtras();
            if (action == null) return;
            if (mBundle == null) return;
            switch (action) {
                case AllUsageAction.apiData: {
                    isWaitingGetData = false;
                    isWaitStopDecodeReply = false;
                    break;
                }
                case AllUsageAction.apiStartDecodeReply: {
                    isWaitStartDecodeReply = false;
                    break;
                }
                case AllUsageAction.apiStopDecodeReply: {
                    isWaitStopDecodeReply = false;
                    isWaitingGetData = false;
                    break;
                }
            }
        }
    };
    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent mIntent) {
            String action = mIntent.getAction();
            Bundle mBundle = mIntent.getExtras();
            if (action == null) return;
            if (mBundle == null) return;
            switch (action) {
                case AllUsageAction.apiStartDecodeReply: {
                    isWaitStartDecodeReply = false;
                    break;
                }
                case AllUsageAction.apiStopDecodeReply: {
                    isWaitStopDecodeReply = false;
                    isWaitingGetData = false;
                    break;
                }
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {
        if (!isFinishing) {
            // Save the last position
            final SharedPreferences.Editor editor = MainActivity.getInstance().getSharedPreferences(getString(R.string.floatingInfo), Context.MODE_PRIVATE).edit();
            editor.putInt(PREF_KEY_LAST_POSITION_X, x);
            editor.putInt(PREF_KEY_LAST_POSITION_Y, y);
            editor.apply();
        }
    }

    @Override
    public void onTouchScanStart() {
        if (!isScanDown) {
            Logger.debug("scan on isWaitingGetData = "+isWaitingGetData+" , isWaitStartDecodeReply = "+isWaitStartDecodeReply+" , isWaitStopDecodeReply = "+isWaitStopDecodeReply);
            if ((!isWaitingGetData || !isWaitStartDecodeReply)&&!isWaitStopDecodeReply) {
                usuApi.startDecode();
                isWaitStartDecodeReply = true;
                isWaitingGetData = true;
            }
            isScanDown = true;
        }
    }

    @Override
    public void onTouchScanEnd() {
        if (isScanDown) {
            Logger.debug("scan off isWaitingGetData = "+isWaitingGetData+" , isWaitStartDecodeReply = "+isWaitStartDecodeReply+" , isWaitStopDecodeReply = "+isWaitStopDecodeReply);
            if (isWaitingGetData && !isWaitStopDecodeReply) {
                usuApi.stopDecode();
                isWaitStopDecodeReply = true;
            }
            isScanDown = false;
        }
    }

    /**
     * Viewを破棄します。
     */
    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }
        enableReceiver(false);
        enableLocalReceiver(false);
    }

    /**
     * 通知を表示します。
     */
    private static Notification createNotification(Context context) {
        String channelName = context.getString(R.string.default_floatingview_channel_id);
        String contentTitle = context.getString(R.string.floating_notification_title);
        String contentText = context.getString(R.string.floating_notification_content);
        int icon = R.mipmap.ic_launcher;
        long when = System.currentTimeMillis();
        Notification.Builder builder;
        NotificationManager   notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(context.getPackageName(), channelName, NotificationManager.IMPORTANCE_LOW);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setSound(null, null);
            notificationManager.createNotificationChannel(mChannel);
            builder = new Notification.Builder(context, context.getPackageName())
                    .setAutoCancel(true)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
            ;
            builder.setShowWhen(false);

        } else {
            builder = new Notification.Builder(context)
                    .setAutoCancel(true)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
            ;
        }

        // PendingIntent作成
//        final Intent notifyIntent = new Intent(context, DeleteActionActivity.class);
//        PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }

    /**
     * 動的に変更可能なオプションを読み込みます。
     */
    private void loadDynamicOptions() {
        final SharedPreferences sharedPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.floatingInfo), Context.MODE_PRIVATE);

        final String displayModeSettings = sharedPref.getString("settings_display_mode", "");
        if ("Always".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        } else if ("FullScreen".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN);
        } else if ("Hide".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_ALWAYS);
        }

    }

    /**
     * FloatingViewのオプションを読み込みます。
     *
     * @param metrics X/Y座標の設定に利用するDisplayMetrics
     * @return Options
     */
    private FloatingViewManager.Options loadOptions(DisplayMetrics metrics) {
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        final SharedPreferences sharedPref = MainActivity.getInstance().getSharedPreferences(getString(R.string.floatingInfo), Context.MODE_PRIVATE);

        // Shape
        final String shapeSettings = sharedPref.getString("settings_shape", "Circle");
        if ("Circle".equals(shapeSettings)) {
            options.shape = FloatingViewManager.SHAPE_CIRCLE;
        } else if ("Rectangle".equals(shapeSettings)) {
            options.shape = FloatingViewManager.SHAPE_RECTANGLE;
        }

        // Margin
        final String marginSettings = sharedPref.getString("settings_margin", String.valueOf(options.overMargin));
        if(marginSettings!=null){
            options.overMargin = Integer.parseInt(marginSettings);
        }

        // MoveDirection
        final String moveDirectionSettings = sharedPref.getString("settings_move_direction", "Default");
        if ("Default".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_DEFAULT;
        } else if ("Left".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_LEFT;
        } else if ("Right".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_RIGHT;
        } else if ("Nearest".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_NEAREST;
        } else if ("Fix".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_NONE;
        } else if ("Thrown".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN;
        }

        options.usePhysics = sharedPref.getBoolean("settings_use_physics", true);

        // Last position
        final boolean isUseLastPosition = sharedPref.getBoolean("settings_save_last_position", false);
        if (isUseLastPosition) {
            final int defaultX = options.floatingViewX;
            final int defaultY = options.floatingViewY;
            options.floatingViewX = sharedPref.getInt(PREF_KEY_LAST_POSITION_X, defaultX);
            options.floatingViewY = sharedPref.getInt(PREF_KEY_LAST_POSITION_Y, defaultY);
        } else {
            // Init X/Y
            final String initXSettings = sharedPref.getString("settings_init_x", "0");
            final String initYSettings = sharedPref.getString("settings_init_y", "0.6");
            if (initXSettings!=null && initYSettings!=null) {
                final int offset = (int) (48 + 8 * metrics.density);
                options.floatingViewX = (int) (metrics.widthPixels * Float.parseFloat(initXSettings) - offset);
                options.floatingViewY = (int) (metrics.heightPixels * Float.parseFloat(initYSettings) - offset);
            }
        }

        // Initial Animation
        options.animateInitialMove = sharedPref.getBoolean("settings_animation", options.animateInitialMove);

        return options;
    }
}
