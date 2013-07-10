package com.notriddle.budget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * A special variation of RelativeLayout that can be used as a checkable object.
 * This allows it to be used as the top-level view of a list view item, which
 * also supports checking.  Otherwise, it works identically to a RelativeLayout.
 *
 * http://alvinalexander.com/java/jwarehouse/apps-for-android/RingsExtended/src/com/example/android/rings_extended/CheckableRelativeLayout.java.shtml
 */

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private boolean mChecked;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    public void toggle() {
        setChecked(!mChecked);
    }
    
    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            int s = getChildCount();
            for (int i = 0; i != s; ++i) {
                View v = getChildAt(i);
                if (v instanceof Checkable) {
                    Checkable c = (Checkable)v;
                    c.setChecked(mChecked);
                }
            }
            refreshDrawableState();
        }
    }
}

