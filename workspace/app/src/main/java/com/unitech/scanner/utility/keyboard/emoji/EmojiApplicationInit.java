package com.unitech.scanner.utility.keyboard.emoji;

import android.content.Context;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;


/**
 * 初始化时调用
 */
public class EmojiApplicationInit {

    public static void init(Context context) {
        EmojiCompat.Config config = new BundledEmojiCompatConfig(context);
        EmojiCompat.init(config);
    }
}
