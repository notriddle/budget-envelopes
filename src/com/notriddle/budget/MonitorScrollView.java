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
