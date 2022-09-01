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


import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.tinylog.Logger;

public class LatinKeyboardView extends KeyboardView {

    Context c;

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        c = context;
    }

    @Override
    public void onClick(View v) {
        Logger.debug("LatinKeyBoardView Popup keyboard close button clicked");
    }

    @Override
    protected boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KeyboardConfig.KEYCODE_OPTIONS, null);
            return true;
        } else if (key.codes[0] == 32) {
            InputMethodManager imeManager = (InputMethodManager) c.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imeManager.showInputMethodPicker();
            return true;
        }  else {
            return super.onLongPress(key);
        }
    }


}
