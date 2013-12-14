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
import android.database.DataSetObserver;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

public class DeleteAdapter implements WrapperListAdapter {
    Context mCntx;
    ListAdapter mAdapter;
    SparseBooleanArray mDeleted;
    int mSwipeBackgroundResource;

    public DeleteAdapter(Context cntx, ListAdapter adapter, int background) {
        mCntx = cntx;
        mAdapter = adapter;
        mDeleted = new SparseBooleanArray();
        mSwipeBackgroundResource = background;
    }

    private int alignPos(int pos) {
        int resultingPos = pos;
        for (int i = 0; i != pos; ++i) {
            if (mDeleted.get(pos)) {
                resultingPos += 1;
            }
        }
        return resultingPos;
    }

    @Override public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }
    @Override public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }
    @Override public boolean isEnabled(int pos) {
        return mAdapter.isEnabled(alignPos(pos));
    }
    @Override public int getCount() {
        return mAdapter.getCount()-mDeleted.size();
    }
    @Override public Object getItem(int pos) {
        return mAdapter.getItem(alignPos(pos));
    }
    @Override public long getItemId(int pos) {
        return mAdapter.getItemId(alignPos(pos));
    }
    @Override public int getItemViewType(int pos) {
        return mAdapter.getItemViewType(alignPos(pos));
    }
    @Override public View getView(int pos, View conv, ViewGroup par) {
        DeleteView retVal;
        View innerView;
        if (conv != null && conv.getTag() == this) {
            retVal = (DeleteView) conv;
            innerView = mAdapter.getView(pos, retVal.getInnerView(), retVal);
            retVal.setInnerView(innerView);
        } else {
            retVal = new DeleteView(mCntx);
            retVal.setSwipeBackgroundResource(mSwipeBackgroundResource);
            retVal.setTag(this);
            innerView = mAdapter.getView(pos, null, retVal);
            retVal.setInnerView(innerView);
        }
        return retVal;
    }
    @Override public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }
    @Override public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }
    @Override public boolean isEmpty() {
        return mAdapter.isEmpty();
    }
    @Override public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }
    @Override public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }
}
