package com.unitech.scanner.utility.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.ui.MainActivity;

import java.util.EnumMap;
import java.util.Map;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/1 上午 09:47
 * 修改人:user
 * 修改時間:2021/2/1 上午 09:47
 * 修改備註:
 */

public class ChartFragment extends Fragment {
    //==============================================================================================
    public ChartFragment newInstance(int[] drawableResource) {
        Bundle args = new Bundle();
        args.putIntArray("resource", drawableResource);
        ChartFragment chartFragment = new ChartFragment();
        chartFragment.setArguments(args);
        return chartFragment;
    }

    public ChartFragment newInstance(String barcodeData, String barcodeType) {
        Bundle args = new Bundle();
        args.putString("barcodeData", barcodeData);
        args.putString("barcodeType", barcodeType);
        ChartFragment chartFragment = new ChartFragment();
        chartFragment.setArguments(args);
        return chartFragment;
    }

    //==============================================================================================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quick_setting_chart, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout layout_chart = view.findViewById(R.id.layout_chart);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.get("resource") != null) {
                int[] chartArray = bundle.getIntArray("resource");
                if (chartArray != null && chartArray.length > 0) {
                    for (int value : chartArray) {
                        // Read your drawable from somewhere
                        VectorDrawable dr = (VectorDrawable) getResources().getDrawable(value, null);

                        Bitmap bitmap = changeVector(dr);
                        if (bitmap == null) continue;
                        int width = MainActivity.getInstance().getResources().getDisplayMetrics().widthPixels;
                        int height = (width * bitmap.getHeight()) / bitmap.getWidth();
                        bitmap = Bitmap.createScaledBitmap(bitmap, width / 2, height / 3, true);
                        ImageView imageView = new ImageView(MainActivity.getInstance());
                        imageView.setImageBitmap(bitmap);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 30, 0, 0);
                        imageView.setLayoutParams(lp);
                        layout_chart.addView(imageView, 0);
                    }
                }
            }
            if (bundle.get("barcodeData") != null && bundle.get("barcodeType") != null) {
                String barcodeData = bundle.getString("barcodeData");
                String barcodeType = bundle.getString("barcodeType");
                if (barcodeData != null && barcodeType != null) {
                    int cWidth = barcodeType.equals(BarcodeFormat.QR_CODE.name()) ? 200 : 100;
                    try {
                        Bitmap bitmap = encodeAsBitmap(barcodeData.replace(":", "").toUpperCase(), BarcodeFormat.valueOf(barcodeType), cWidth, 40);
                        int width = MainActivity.getInstance().getResources().getDisplayMetrics().widthPixels;
                        int height = (width * bitmap.getHeight()) / bitmap.getWidth();
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        ImageView imageView = new ImageView(MainActivity.getInstance());
                        imageView.setImageBitmap(bitmap);
                        layout_chart.addView(imageView, 0);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //==============================================================================================
    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        if (contents == null) {
            return null;
        }
        Map <EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap <>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contents, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    private Bitmap changeVector(VectorDrawable drawable) {
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() * 5, drawable.getIntrinsicHeight() * 5, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }
    //==============================================================================================
}
