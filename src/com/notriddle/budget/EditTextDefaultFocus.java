/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Michael Howell <michael@notriddle.com>
 *
 * Budget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Budget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Budget. If not, see <http://www.gnu.org/licenses/>.
 */

package com.notriddle.budget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

public class EditTextDefaultFocus extends EditText {
    boolean mDefaultFocus;

    public EditTextDefaultFocus(Context cntx) {
        super(cntx);
    }
    public EditTextDefaultFocus(Context cntx, AttributeSet attrs) {
        super(cntx, attrs);
        setDefaultFocus(attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/com.notriddle.budget", "defaultFocus", false));
    }
    public EditTextDefaultFocus(Context cntx, AttributeSet attrs,
                                int defStyle) {
        super(cntx, attrs, defStyle);
        TypedArray a = cntx.obtainStyledAttributes(attrs, R.styleable.EditTextDefaultFocus, defStyle, 0);
        setDefaultFocus(a.getBoolean(R.styleable.EditTextDefaultFocus_defaultFocus, false));
        a.recycle();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDefaultFocus) {
            if (getContext() instanceof Activity) {
                ((Activity)getContext())
                .getWindow()
                 .setSoftInputMode(WindowManager
                                   .LayoutParams
                                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            requestFocus();
        } else {
            ViewGroup parent = (ViewGroup)getParent();
            //while (parent.getParent() instanceof ViewGroup) {
            //    parent = (ViewGroup)parent.getParent();
            //}
            parent.setFocusableInTouchMode(true);
            parent.requestFocus();
            clearFocus();
        }
    }

    public void setDefaultFocus(boolean ex) {
        Log.d("EditTextDefaultFocus.setDefaultFocus", "ex="+ex);
        if (ex) {
            requestFocus();
        } else {
            clearFocus();
        }
        mDefaultFocus = ex;
    }
}
