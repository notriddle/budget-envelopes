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
import android.widget.GridView;

public class TACGridView extends GridView {

	public TACGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	public TACGridView(Context context, AttributeSet attrs)
	{
	    super(context, attrs);
	}

	public TACGridView(Context context)
	{
	    super(context);
	}
	
	boolean expanded = true;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded()) {         
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                        MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            android.view.ViewGroup.LayoutParams params =  getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
