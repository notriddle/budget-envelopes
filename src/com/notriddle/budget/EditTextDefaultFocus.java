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
