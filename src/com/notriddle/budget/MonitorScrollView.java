package com.notriddle.budget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MonitorScrollView extends ScrollView {
    public static interface OnScrollListener {
        public void onScrollChanged(int pos, int oldPos);
    };

    OnScrollListener mListener;

	public MonitorScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	public MonitorScrollView(Context context, AttributeSet attrs)
	{
	    super(context, attrs);
	}

	public MonitorScrollView(Context context)
	{
	    super(context);
	}

    public void setOnScrollListener(OnScrollListener listener) {
        mListener = listener;
    }
	
    @Override
    public void onScrollChanged(int l, int t, int oL, int oT) {
        super.onScrollChanged(l, t, oL, oT);
        if (mListener != null) {
            mListener.onScrollChanged(t, oT);
        }
    }

}
