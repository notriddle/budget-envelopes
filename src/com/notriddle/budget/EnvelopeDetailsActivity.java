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

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;

public class EnvelopeDetailsActivity extends ListActivity
                                     implements LoaderCallbacks<Cursor>,
                                                TextWatcher,
                                            AbsListView.MultiChoiceModeListener,
                                               AdapterView.OnItemClickListener {
    EditTextDefaultFocus mName;
    int mId;
    SQLiteDatabase mDatabase;
    LogAdapter mAdapter;
    TextView mAmount;
    TextView mAmountName;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        mId = getIntent().getIntExtra("com.notriddle.budget.envelope", -1);
        mName = new EditTextDefaultFocus(this);
        mName.setHint(getText(R.string.envelopeName_hint));
        mName.setLayoutParams(new ActionBar.LayoutParams(
            ActionBar.LayoutParams.FILL_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        ));
        mName.setSingleLine(true);
        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().initLoader(1, null, this);
        ActionBar ab = getActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(mName);
        ab.setDisplayHomeAsUpEnabled(true);
        mName.addTextChangedListener(this);
        mAdapter = new LogAdapter(this, null);
        final ListView lV = getListView();
        //lV.setOverScrollMode(lV.OVER_SCROLL_NEVER);
        final View head = getLayoutInflater().inflate(
            R.layout.totalamount,
            lV,
            false
        );
        head.setBackgroundResource(R.color.windowBackground);
        final int basePadding = head.getPaddingTop();
        mAmount = (TextView) head.findViewById(R.id.value);
        mAmountName = (TextView) head.findViewById(R.id.name);
        lV.addHeaderView(head);
        setListAdapter(mAdapter);

        lV.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScroll(AbsListView lV, int first, int count, int total) {
                View c = lV.getChildAt(0);
                if (c == head) {
                    int move = (head.getTop()*-2)/3;
                    mAmount.setTranslationY(move);
                    mAmountName.setTranslationY(move);
                }
            }
            public void onScrollStateChanged(AbsListView lV, int state) {
                // Do nothing.
            }
        });
        lV.setBackgroundResource(R.color.cardBackground);
        lV.setOnItemClickListener(this);
        lV.setChoiceMode(lV.CHOICE_MODE_MULTIPLE_MODAL);
        lV.setMultiChoiceModeListener(this);
        getWindow().setBackgroundDrawable(null);
        lV.setSelector(android.R.color.transparent);
        lV.setDivider(getResources().getDrawable(R.color.cardDivider));
        lV.setDividerHeight((int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()
        ));
        lV.setOverscrollFooter(getResources().getDrawable(
            R.color.cardBackground
        ));
        lV.setOverscrollHeader(getResources().getDrawable(
            R.color.windowBackground
        ));
    }

    @Override public void onPause() {
        super.onPause();
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (mName.getText().length() == 0 && mAdapter.getCount() == 0) {
            deleteThis();
            mDatabase.close();
            mDatabase = null;
        }
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater()
             .inflate(R.menu.envelopedetailsactivity_checked, menu);
        return true;
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        countItems(mode);
        return true;
    }

    @Override public void onItemCheckedStateChanged(ActionMode mode, int pos,
                                                    long id, boolean chk) {
        if (pos == 0 && chk) {
            getListView().setItemChecked(0, false);
        }
        countItems(mode);
    }

    @Override public void onDestroyActionMode(ActionMode mode) {
        // Do nothing.
    }

    @Override public boolean onActionItemClicked(ActionMode mode,
                                                 MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_menuItem:
                long[] items = getListView().getCheckedItemIds();
                int l = items.length;
                for (int i = 0; i != l; ++i) {
                    deleteTransaction((int)items[i]);
                }
                mode.finish();
                return true;
        }
        return false;
    }

    private void countItems(ActionMode mode) {
        int count = Util.numberOf(getListView().getCheckedItemPositions(),
                                  true);
        String titler = getResources().getQuantityString(
            R.plurals.transactionsChecked_name, count
        );
        mode.setTitle(String.format(titler, count));
    }

    private void deleteTransaction(int id) {
        needDatabase();
        mDatabase.beginTransaction();
        try {
            mDatabase.execSQL("DELETE FROM log WHERE _id = ?",
                              new String[] {Integer.toString(id)});
            EnvelopesOpenHelper.playLog(mDatabase);
            mDatabase.setTransactionSuccessful();
            getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            mDatabase.endTransaction();
        }
    }

    private SQLiteDatabase needDatabase() {
        if (mDatabase == null) {
            mDatabase = (new EnvelopesOpenHelper(this)).getWritableDatabase();
        }
        return mDatabase;
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String table     = id == 0
                           ? "envelopes"
                           : "log";
        String[] columns = id == 0
                           ? new String[] { "name", "cents", "_id" }
                           : new String[] { "description", "cents", "time",
                                            "_id" };
        String where     = id == 0
                           ? "_id = ?"
                           : "envelope = ?";
        String sort      = id == 0
                           ? null
                           : "time * -1";
        SQLiteLoader retVal = new SQLiteLoader(
            this,
            new EnvelopesOpenHelper(this),
            table,
            columns,
            where,
            new String[] {Integer.toString(mId)},
            null,
            null,
            sort
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        if (ldr.getId() == 0) {
            if (data.getCount() == 0) {
                finish();
            } else {
                data.moveToFirst();
                String name = data.getString(data.getColumnIndexOrThrow("name"));
                if (!mName.hasFocus()) {
                    if (!mName.getText().toString().equals(name)) {
                        mName.setText(name);
                    }
                    mName.setDefaultFocus(name.equals(""));
                }
                mAmount.setText(EditMoney.toColoredMoney(
                    this, data.getLong(data.getColumnIndexOrThrow("cents"))
                ));
            }
        } else {
            mAdapter.changeCursor(data);
            ListView lV = getListView();
            if (lV.getLastVisiblePosition() == mAdapter.getCount()
                || lV.getLastVisiblePosition() <= 1) {
                lV.setBackgroundResource(R.color.cardBackground);
            } else {
                lV.setBackgroundDrawable(null);
            }
        }
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        finish();
    }

    @Override public void afterTextChanged(Editable s) {
        // Do nothing.
    }

    @Override public void beforeTextChanged(CharSequence s, int start,
                                            int count, int end) {
        // Do nothing.
    }

    @Override public void onTextChanged(CharSequence s, int start,
                                        int count, int end) {
        ContentValues values = new ContentValues();
        values.put("name", s.toString());
        needDatabase().update("envelopes", values, "_id = ?", new String[] {Integer.toString(mId)});
        getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        editLogEntry((int)id);
    }

    private void editLogEntry(int id) {
        Cursor csr = mAdapter.getCursor();
        int oldPos = csr.getPosition();
        csr.moveToFirst();
        int l = csr.getCount();
        for (int i = 0; i != l; ++i) {
            if (csr.getInt(csr.getColumnIndexOrThrow("_id")) == id) {
                EditTransactionFragment f = EditTransactionFragment.newInstance(
                    id,
                    csr.getString(csr.getColumnIndexOrThrow("description")),
                    csr.getLong(csr.getColumnIndexOrThrow("cents"))
                );
                f.show(getFragmentManager(), "dialog");
                break;
            }
            csr.moveToNext();
        }
        csr.moveToPosition(oldPos);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.envelopedetailsactivity, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, EnvelopesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.earn_menuItem:
                SpendFragment sF = SpendFragment.newInstance(mId, SpendFragment.EARN);
                sF.show(getFragmentManager(), "dialog");
                return true;
            case R.id.spend_menuItem:
                sF = SpendFragment.newInstance(mId, SpendFragment.SPEND);
                sF.show(getFragmentManager(), "dialog");
                return true;
            case R.id.delete_menuItem:
                deleteThis();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteThis() {
        needDatabase().delete("envelopes", "_id = ?",
                              new String[] {Integer.toString(mId)});
        needDatabase().delete("log", "envelope = ?",
                              new String[] {Integer.toString(mId)});
        getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
    }
}
