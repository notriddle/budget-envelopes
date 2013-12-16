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
import android.database.DataSetObserver;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

public class DeleteAdapter extends BaseAdapter
                           implements WrapperListAdapter, View.OnClickListener,
                                      View.OnLongClickListener,
                                      DeleteView.OnDeleteListener,
                                      ActionMode.Callback {

    public static interface Deleter {
        public void performDelete(long id);
        public void onDelete(long id);
        public void undoDelete(long id);
    };

    Activity mCntx;
    ListAdapter mAdapter;
    long mDeletedId;
    int mSwipeBackgroundResource;
    int mObserverCount;
    DataSetObserver mObserver;
    Deleter mDeleter;
    ActionMode mUndoMode;

    public DeleteAdapter(Activity cntx, Deleter deleter, ListAdapter adapter, int background) {
        mCntx = cntx;
        mAdapter = adapter;
        mDeletedId = -1;
        mSwipeBackgroundResource = background;
        mObserverCount = 0;
        mObserver = new DataSetObserver() {
            public void onChanged() {
                super.onChanged();
                if (mDeletedId != -1) {
                    int l = mAdapter.getCount();
                    boolean found = false;
                    for (int i = 0; i != l; ++i) {
                        if (mAdapter.getItemId(i) == mDeletedId) {
                            found = true;
                        }
                    }
                    if (!found) {
                        mDeletedId = -1;
                    }
                }
                notifyDataSetChanged();
            }
            public void onInvalidated() {
                super.onInvalidated();
                notifyDataSetInvalidated();
            }
        };
        mDeleter = deleter;
    }

    private int myPosToSourcePos(int pos) {
        return mDeletedId == -1 || idToSourcePos(mDeletedId) > pos ? pos : pos+1;
    }
    private int sourcePosToMyPos(int pos) {
        return mDeletedId == -1 || idToSourcePos(mDeletedId) > pos ? pos : pos-1;
    }
    private int idToMyPos(long id) {
        return sourcePosToMyPos(idToSourcePos(id));
    }
    private int idToSourcePos(long id) {
        int l = mAdapter.getCount();
        for (int i = 0; i != l; ++i) {
            if (mAdapter.getItemId(i) == id) {
                return i;
            }
        }
        return getCount();
    }

    @Override public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }
    @Override public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }
    @Override public boolean isEnabled(int pos) {
        return mAdapter.isEnabled(myPosToSourcePos(pos));
    }
    @Override public int getCount() {
        return mAdapter.getCount()-(mDeletedId == -1 ? 0 : 1);
    }
    @Override public Object getItem(int pos) {
        return mAdapter.getItem(myPosToSourcePos(pos));
    }
    @Override public long getItemId(int pos) {
        return mAdapter.getItemId(myPosToSourcePos(pos));
    }
    @Override public int getItemViewType(int pos) {
        return mAdapter.getItemViewType(myPosToSourcePos(pos));
    }
    @Override public View getView(int pos, View conv, ViewGroup par) {
        DeleteView retVal;
        View innerView;
        if (conv != null) {
            retVal = (DeleteView) conv;
            innerView = mAdapter.getView(myPosToSourcePos(pos), retVal.getInnerView(), retVal);
            retVal.setInnerView(innerView);
            retVal.setTag(pos);
            retVal.setTag(R.id.value, getItemId(pos));
        } else {
            retVal = new DeleteView(mCntx);
            retVal.setSwipeBackgroundResource(mSwipeBackgroundResource);
            retVal.setTag(pos);
            retVal.setTag(R.id.value, getItemId(pos));
            innerView = mAdapter.getView(myPosToSourcePos(pos), null, retVal);
            retVal.setInnerView(innerView);
            retVal.setOnClickListener(this);
            retVal.setOnLongClickListener(this);
            retVal.setOnDeleteListener(this);
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
        super.registerDataSetObserver(observer);
        mObserverCount += 1;
        if (mObserverCount == 1) {
            mAdapter.registerDataSetObserver(mObserver);
        }
    }
    @Override public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        mObserverCount -= 1;
        if (mObserverCount == 0) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
    }

    @Override public void onClick(View v) {
        if (v.getParent() instanceof AdapterView) {
            AdapterView p = (AdapterView) v.getParent();
            int pos = (Integer) v.getTag();
            long id = getItemId(pos);
            AdapterView.OnItemClickListener l = p.getOnItemClickListener();
            if (l != null) {
                l.onItemClick(p, v, pos, id);
            }
        }
    }
    @Override public boolean onLongClick(View v) {
        if (v.getParent() instanceof AdapterView) {
            AdapterView p = (AdapterView) v.getParent();
            int pos = (Integer) v.getTag();
            long id = getItemId(pos);
            AdapterView.OnItemLongClickListener l = p.getOnItemLongClickListener();
            if (l != null) {
                return l.onItemLongClick(p, v, pos, id);
            }
        }
        ((DeleteView)v).performDelete();
        return true;
    }
    @Override public void onDelete(DeleteView v) {
        long id = (Long) v.getTag(R.id.value);
        long oldDeleted = mDeletedId;
        mDeletedId = id;
        if (oldDeleted != -1) {
            mDeleter.performDelete(oldDeleted);
        }
        mDeleter.onDelete(mDeletedId);
        notifyDataSetChanged();
        Log.d("Budget", "onDelete()");
        startUndoMode();
    }

    public long getDeletedId() {
        return mDeletedId;
    }

    public boolean performDelete() {
        if (mDeletedId != -1) {
            long del = mDeletedId;
            mDeletedId = -1;
            mDeleter.performDelete(del);
            stopUndoMode();
            return true;
        } else {
            return false;
        }
    }

    public void undoDelete() {
        long del = mDeletedId;
        mDeletedId = -1;
        mDeleter.undoDelete(del);
        notifyDataSetChanged();
        stopUndoMode();
    }

    private void startUndoMode() {
        if (mUndoMode == null) {
            mUndoMode = mCntx.startActionMode(this);
        }
    }
    private void stopUndoMode() {
        if (mUndoMode != null) {
            ActionMode undoMode = mUndoMode;
            mUndoMode = null;
            undoMode.finish();
        }
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(R.string.deleted_name);
        mode.getMenuInflater().inflate(R.menu.undo, menu);
        return mDeletedId != -1;
    }
    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
    @Override public void onDestroyActionMode(ActionMode mode) {
        mUndoMode = null;
        performDelete();
    }
    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.undo_menuItem) {
            undoDelete();
            return true;
        } else {
            return false;
        }
    }
}
