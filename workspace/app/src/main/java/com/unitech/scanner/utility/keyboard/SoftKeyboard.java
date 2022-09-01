/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.unitech.scanner.utility.keyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.content.res.ResourcesCompat;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.AllUsageAction;
import com.unitech.scanner.utility.keyboard.emoji.EmojiApplicationInit;
import com.unitech.scanner.utility.keyboard.emoji.EmojiKeyboard;
import com.unitech.scanner.utility.keyboard.emoji.EmojiRegex;
import com.unitech.scanner.utility.keyboard.emoji.EomjiSource;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;


//import android.util.Log;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;

    private KeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions = null;

    private final StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;
    private final double adjustRatio = 0.75;

    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mNumbersKeyboard;

    public LatinKeyboard mCurKeyboard;
    private int keyboardFlag = 0;

    private String mWordSeparators;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug("Keyboard onCreate");
        mWordSeparators = getResources().getString(R.string.word_separators);
        sendBroadcast(
                new Intent().setAction(AllUsageAction.serviceImeStatus)
                        .putExtra("status", true)
        );
        IntentFilter filter = new IntentFilter();
        filter.addAction(AllUsageAction.serviceKeyboardInput);
        filter.addAction(AllUsageAction.apiStartDecodeReply);
        filter.addAction(AllUsageAction.apiStopDecodeReply);
        registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(this, R.xml.keyboard_qwerty);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.keyboard_symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.keyboard_symbols_shift);
        mNumbersKeyboard = new LatinKeyboard(this, R.xml.keyboard_numbers);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mQwertyKeyboard.changeKeyHeight(adjustRatio);
            mSymbolsKeyboard.changeKeyHeight(adjustRatio);
            mSymbolsShiftedKeyboard.changeKeyHeight(adjustRatio);
            mNumbersKeyboard.changeKeyHeight(adjustRatio);
        }
    }

    private RadioButton radio_qwerty;
    private RadioButton radio_numbers;
    private RadioButton radio_symbols;
    private ImageView img_scan;
    private InputMethodManager mInputMethodManager;
    private EmojiKeyboard emojiKeyboard;
    private List <Drawable> tips = new ArrayList <>();
    private boolean isScanDown = false;
    private boolean isWaitStartDecodeReply = false;
    private boolean isWaitStopDecodeReply = false;
    private boolean isWaitingGetData = false;

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateInputView() {
        View root = getLayoutInflater().inflate(R.layout.keyboard_main, null);
        //    private Button btn_numbers, btn_qwerty, btn_symbols;
        //    private Button btn_switch_language, btn_hide_keyboard;
        RadioGroup radioGroup = root.findViewById(R.id.radio_group);
        RadioButton radio_language = root.findViewById(R.id.radio_language);
        radio_qwerty = root.findViewById(R.id.radio_qwerty);
        radio_numbers = root.findViewById(R.id.radio_numbers);
        radio_symbols = root.findViewById(R.id.radio_symbols);
        img_scan = root.findViewById(R.id.img_scan);
        img_scan.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                img_scan.setBackgroundResource(R.color.keyboard_press_background);
                if (!isScanDown) {
                    Logger.debug("scan on isWaitingGetData = "+isWaitingGetData+" , isWaitStartDecodeReply = "+isWaitStartDecodeReply+" , isWaitStopDecodeReply = "+isWaitStopDecodeReply);
                    if ((!isWaitingGetData || !isWaitStartDecodeReply)&&!isWaitStopDecodeReply) {
                        sendBroadcast(new Intent(AllUsageAction.apiStartDecode).putExtra("packageName", getPackageName()));
                        isWaitStartDecodeReply = true;
                        isWaitingGetData = true;
                    }
                    isScanDown = true;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                img_scan.setBackgroundResource(R.color.keyboard_press_inside_background);
                if (isScanDown) {
                    Logger.debug("scan off isWaitingGetData = "+isWaitingGetData+" , isWaitStartDecodeReply = "+isWaitStartDecodeReply+" , isWaitStopDecodeReply = "+isWaitStopDecodeReply);
                    if (isWaitingGetData && !isWaitStopDecodeReply) {
                        sendBroadcast(new Intent(AllUsageAction.apiStopDecode).putExtra("packageName", getPackageName()));
                        isWaitStopDecodeReply = true;
                    }
                    isScanDown = false;
                }
            }
            return true;
        });
        //底部图标(如不设置则使用默认图标)

        emojiKeyboard = root.findViewById(R.id.emojiKeyboard);
        tips = new ArrayList <>();
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_1, getTheme()));
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_2, getTheme()));
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_3, getTheme()));
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_4, getTheme()));
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_5, getTheme()));
        tips.add(ResourcesCompat.getDrawable(getResources(), R.drawable.emoji_6, getTheme()));

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // find which radio button is selected
            if (checkedId == R.id.radio_language) {
                onKey(KeyboardConfig.KEYCODE_LANGUAGE_SWITCH, null);
            } else if (checkedId == R.id.radio_qwerty) {
                ChangeKeyboard(0);
            } else if (checkedId == R.id.radio_numbers) {
                ChangeKeyboard(-1);
            } else if (checkedId == R.id.radio_symbols) {
                ChangeKeyboard(1);
            } else if (checkedId == R.id.radio_hide_keyboard) {
                onKey(-3, null);
            } else if (checkedId == R.id.radio_emoji_keyboard) {
                ChangeKeyboard(-2);
            } else if (checkedId == R.id.radio_scan) {
                ChangeKeyboard(-3);
            }
        });

        radio_qwerty.setOnClickListener(v -> {
            if (keyboardFlag == -2) {
                ChangeKeyboard(0);
            }
        });
        radio_symbols.setOnClickListener(v -> {
            if (keyboardFlag == -2) {
                ChangeKeyboard(1);
            }
        });


        EmojiApplicationInit.init(this);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        final boolean shouldSupportLanguageSwitchKey =
                mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        if (shouldSupportLanguageSwitchKey) {
            radio_language.setVisibility(View.VISIBLE);
        } else {
            radio_language.setVisibility(View.GONE);
        }

        mInputView = root.findViewById(R.id.ime_softkeyboard);
//        mInputView = (KeyboardView) getLayoutInflater().inflate(R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        ChangeKeyboard(0);
        mInputView.setPreviewEnabled(false);
        return root;
    }


    public void onKeyEmoji(String text) {
        handleEmoji(text);
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(KEYBOARD_INPUT);
        //this.registerReceiver(mBroadcastReceiver, filter);

        // We are now going to initialize our state based on the type of
        // text being edited.
        Logger.debug("onStartInput = " + (attribute.inputType & EditorInfo.TYPE_MASK_CLASS));
        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
//                mCurKeyboard = mSymbolsKeyboard;
//                keyboardFlag = 1;
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mNumbersKeyboard;
                keyboardFlag = -1;
                break;

            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                keyboardFlag = 0;
                mPredictionOn = false;
                // We now look for a few special variations of text that will
                // modify our behavior.
                // Do not display predictions / what the user is typing
                // when they are entering a password.

                // Our predictions are not useful for e-mail addresses
                // or URIs.

                if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);//onStartInput
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);//onStartInput
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), getApplication().getTheme(), attribute.imeOptions);


    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override
    public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        mCurKeyboard = mQwertyKeyboard;
        keyboardFlag = 0;
        if (mInputView != null) {
            mInputView.closing();
        }

    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.

        ChangeKeyboard(keyboardFlag);
        mInputView.closing();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(AllUsageAction.serviceKeyboardInput);
//        registerReceiver(mBroadcastReceiver, filter);
//        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List <String> stringList = new ArrayList <>();
            for (CompletionInfo ci : completions) {
                if (ci != null)
                    stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.debug("onKeyDown");
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        Logger.debug("onKeyUp");
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        if (keyCode == '\n') {
            keyDownUp(KeyEvent.KEYCODE_ENTER);
        } else {
            if (keyCode >= '0' && keyCode <= '9') {
                keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
            } else {
                getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
            }
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) {
//        Log.d(TAG, "primaryCode = " + primaryCode + "; keyCodes = " + Arrays.toString(keyCodes));
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);

            updateShiftKeyState(getCurrentInputEditorInfo());//onKey
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
        } else if (primaryCode == KeyboardConfig.KEYCODE_OPTIONS) {
            // Show a menu or something'
        } else if (primaryCode == KeyboardConfig.KEYCODE_EMOJI) {
//            showEmoji(true);
            ChangeKeyboard(-2);
        } else if (primaryCode == KeyboardConfig.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                ChangeKeyboard(0);
            } else {
                ChangeKeyboard(1);
            }
        } else {
            handleCharacter(primaryCode);
        }
    }


    private void ChangeKeyboard(int keyboardValue) {//0:qwerty,1:symbols,2:symbolsL,-1:123
        keyboardFlag = keyboardValue;

        switch (keyboardValue) {
            case -3:
                int w = mInputView.getWidth();
                int h = mInputView.getHeight();
                img_scan.setMinimumWidth(w);
                img_scan.setMinimumHeight(h);
                img_scan.setVisibility(View.VISIBLE);
                emojiKeyboard.setVisibility(View.GONE);
                mInputView.setVisibility(View.GONE);
                radio_numbers.setChecked(false);
                radio_qwerty.setChecked(false);
                radio_symbols.setChecked(false);
                break;
            case -2:
                //设置輸入
                emojiKeyboard.setSoftKeyboard(this);
                //设置底部图标
                emojiKeyboard.setTips(tips);
                //设置行数 默认为3
                emojiKeyboard.setMaxLines(3);
                //设置列数 默认为7
                emojiKeyboard.setMaxColumns(7);
                //设置图标数据源
                emojiKeyboard.setLists(EomjiSource.getLists());
                //设置指示器距底部边界
                emojiKeyboard.setIndicatorPadding(3);
//                //初始化需要
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    emojiKeyboard.init(adjustRatio, mInputView.getHeight());
                } else {
                    emojiKeyboard.init(1, mInputView.getHeight());
                }
                emojiKeyboard.init();
                img_scan.setVisibility(View.GONE);
                emojiKeyboard.setVisibility(View.VISIBLE);
                mInputView.setVisibility(View.GONE);
                break;
            case -1:
                img_scan.setVisibility(View.GONE);
                emojiKeyboard.setVisibility(View.GONE);
                mInputView.setVisibility(View.VISIBLE);
                mInputView.setKeyboard(mNumbersKeyboard);
                radio_numbers.setChecked(true);
                radio_qwerty.setChecked(false);
                radio_symbols.setChecked(false);
                break;
            case 0:
                img_scan.setVisibility(View.GONE);
                emojiKeyboard.setVisibility(View.GONE);
                mInputView.setVisibility(View.VISIBLE);
                mInputView.setKeyboard(mQwertyKeyboard);
                radio_numbers.setChecked(false);
                radio_qwerty.setChecked(true);
                radio_symbols.setChecked(false);
                break;
            case 1:
                img_scan.setVisibility(View.GONE);
                emojiKeyboard.setVisibility(View.GONE);
                mInputView.setVisibility(View.VISIBLE);
                mInputView.setKeyboard(mSymbolsKeyboard);
                radio_numbers.setChecked(false);
                radio_qwerty.setChecked(false);
                radio_symbols.setChecked(true);
                mSymbolsShiftedKeyboard.setShifted(false);
                mSymbolsKeyboard.setShifted(false);
                break;
            case 2:
                img_scan.setVisibility(View.GONE);
                emojiKeyboard.setVisibility(View.GONE);
                mInputView.setVisibility(View.VISIBLE);
                mInputView.setKeyboard(mSymbolsShiftedKeyboard);
                radio_numbers.setChecked(false);
                radio_qwerty.setChecked(false);
                radio_symbols.setChecked(true);
                mSymbolsKeyboard.setShifted(true);
                mSymbolsShiftedKeyboard.setShifted(true);
                break;
        }

    }


    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());//onText
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList <String> list = new ArrayList <>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List <String> suggestions, boolean completions, boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());//handleBackspace
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
            mCurKeyboard.ShiftKeyLock(getApplication().getResources(), getApplication().getTheme(), mInputView.isShifted() && mCapsLock);

        } else if (currentKeyboard == mSymbolsKeyboard) {
            ChangeKeyboard(2);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            ChangeKeyboard(1);
        }
    }

    private void handleCharacter(int primaryCode) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (primaryCode == 32) {
            commitTyped(getCurrentInputConnection());
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());//handleCharacter
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
            updateShiftKeyState(getCurrentInputEditorInfo());//handleCharacter
        }
    }

    private void handleEmoji(String text) {
        if (text.equals("-1")) {
            final int length = mComposing.length();
            if (length > 2) {
                String lastText = mComposing.substring(length - 2, length);
                if (EmojiRegex.checkEmoji(lastText)) {
                    mComposing.delete(length - 2, length);
                } else {
                    mComposing.delete(length - 1, length);
                }
                getCurrentInputConnection().setComposingText(mComposing, 1);
//                updateCandidates();
            } else if (length > 0) {
                mComposing.setLength(0);
                getCurrentInputConnection().commitText("", 0);
//                updateCandidates();
            } else {
                keyDownUp(KeyEvent.KEYCODE_DEL);
            }
            updateShiftKeyState(getCurrentInputEditorInfo());//handleBackspace
        } else {
            mComposing.append(text);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());//handleCharacter
//            updateCandidates();
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        unregisterReceiver(mBroadcastReceiver);
        sendBroadcast(
                new Intent().setAction(AllUsageAction.serviceImeStatus)
                        .putExtra("status", false)
        );
        mInputView.closing();
    }

    private void handleLanguageSwitch() {
        commitTyped(getCurrentInputConnection());
        unregisterReceiver(mBroadcastReceiver);
        sendBroadcast(
                new Intent().setAction(AllUsageAction.serviceImeStatus)
                        .putExtra("status", false)
        );
        mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }


    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 400 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            if (mCapsLock) {
                mCapsLock = false;
                mLastShiftTime = 0;
            } else {
                mLastShiftTime = now;
            }
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());//pickSuggestionManually
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
    }

    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent mIntent) {
            String action = mIntent.getAction();
            Bundle mBundle = mIntent.getExtras();
            if (action == null) return;
            if (mBundle == null) return;
            switch (action) {
                case AllUsageAction.serviceKeyboardInput: {
                    isWaitingGetData = false;
                    isWaitStopDecodeReply = false;

                    boolean outputFlag = true;
                    String stringData = mBundle.getString("stringData");
                    String outputMethod = mBundle.getString("outputMethod");
                    String interCharDelay = mBundle.getString("interCharDelay");
                    Logger.debug("Software Keyboard package = " + getPackageName());
                    if (stringData != null) {
                        int length = stringData.length();
                        if (outputMethod == null || !outputMethod.equals("1")) {
                            if (!getCurrentInputConnection().commitText(stringData, length)) {
                                outputFlag = false;
                            }
                        } else {
                            int delay = 0;
                            if (interCharDelay != null) {
                                delay = Integer.parseInt(interCharDelay);
                            }
                            for (int i = 0; i < length; i++) {
                                if (!getCurrentInputConnection().commitText(String.valueOf(stringData.charAt(i)), 1)) {
                                    outputFlag = false;
                                    break;
                                }
                                try {
                                    Thread.sleep(delay);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if (outputFlag) {
                        Logger.debug("send key event use commitText success");
                    } else {
                        Logger.debug("send key event use commitText fail");
                    }
                    break;
                }
                case AllUsageAction.apiStartDecodeReply:
                    isWaitStartDecodeReply = false;
                    break;
                case AllUsageAction.apiStopDecodeReply:
                    isWaitStopDecodeReply = false;
                    isWaitingGetData = false;
                    break;
            }

        }
    };
}
