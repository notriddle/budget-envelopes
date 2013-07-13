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
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class EnvelopesActivity extends Activity
                               implements LoaderCallbacks<Cursor>,
                                          GridView.OnItemClickListener,
                                          MonitorScrollView.OnScrollListener,
                                          AbsListView.MultiChoiceModeListener {
    GridView mGrid;
    EnvelopesAdapter mEnvelopes;
    TextView mTotal;
    View mTotalContainer;
    MonitorScrollView mScroll;
    SharedPreferences mPrefs;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.envelopesactivity);
        mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(getBaseContext());
        mGrid = (GridView) findViewById(R.id.grid);
        getLoaderManager().initLoader(0, null, this);
        mEnvelopes = new EnvelopesAdapter(this, null);
        mGrid.setAdapter(mEnvelopes);
        mGrid.setOnItemClickListener(this);
        mTotalContainer = findViewById(R.id.totalamount);
        mTotal = (TextView) mTotalContainer.findViewById(R.id.value);
        mScroll = (MonitorScrollView) findViewById(R.id.scroll);
        mScroll.setOnScrollListener(this);
        mGrid.setChoiceMode(mGrid.CHOICE_MODE_MULTIPLE_MODAL);
        mGrid.setMultiChoiceModeListener(this);
    }

    @Override public void onResume() {
        super.onResume();
        long now = System.currentTimeMillis();
        if (now > mPrefs.getLong("com.notriddle.budget.lastCheck", now)
                  +(1000*60*60*24)) {
            EnvelopesOpenHelper.playLog(this);
            mPrefs.edit().putLong("com.notriddle.budget.lastCheck", now)
                         .commit();
        }
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater()
             .inflate(R.menu.envelopesactivity_checked, menu);
        return true;
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        countItems(mode);
        return true;
    }

    @Override public void onItemCheckedStateChanged(ActionMode mode, int pos,
                                                    long id, boolean chk) {
        countItems(mode);
    }

    @Override public void onDestroyActionMode(ActionMode mode) {
        // Do nothing.
    }

    @Override public boolean onActionItemClicked(ActionMode mode,
                                                 MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_menuItem:
                long[] items = mGrid.getCheckedItemIds();
                int l = items.length;
                for (int i = 0; i != l; ++i) {
                    deleteEnvelope((int)items[i]);
                }
                mode.finish();
                return true;
        }
        return false;
    }

    private void countItems(ActionMode mode) {
        int count = Util.numberOf(mGrid.getCheckedItemPositions(),
                                  true);
        String titler = getResources().getQuantityString(
            R.plurals.envelopesChecked_name, count
        );
        mode.setTitle(String.format(titler, count));
    }

    private void deleteEnvelope(int id) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(this))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM envelopes WHERE _id = ?",
                       new String[] {Integer.toString(id)});
            db.execSQL("DELETE FROM log WHERE envelope = ?",
                       new String[] {Integer.toString(id)});
            db.setTransactionSuccessful();
            getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override public void onScrollChanged(int pos, int oldPos) {
        mTotalContainer.setTranslationY((pos*2)/3);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteLoader retVal = new SQLiteLoader(
            this, new EnvelopesOpenHelper(this), "envelopes",
            new String[] {
                "name", "cents", "_id"
            }
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        data.moveToFirst();
        int l = data.getCount();
        long total = 0;
        for (int i = 0; i != l; ++i) {
            total += data.getLong(data.getColumnIndexOrThrow("cents"));
            data.moveToNext();
        }
        mTotal.setText(EditMoney.toColoredMoney(this, total));
        mEnvelopes.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        mEnvelopes.changeCursor(null);
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        openEnvelope((int)id);
    }

    private void openEnvelope(int id) {
        Intent i = new Intent(this, EnvelopeDetailsActivity.class);
        i.putExtra("com.notriddle.budget.envelope", id);
        startActivity(i);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.envelopesactivity, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newEnvelope_menuItem:
                SQLiteDatabase db = (new EnvelopesOpenHelper(this))
                                    .getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", "");
                long id = db.insert("envelopes", null, values);
                db.close();
                getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
                openEnvelope((int)id);
                return true;
            case R.id.transfer_menuItem:
                DialogFragment f = TransferFragment.newInstance();
                f.show(getFragmentManager(), "dialog");
                return true;
            case R.id.export_menuItem:
                f = ExportFragment.newInstance();
                f.show(getFragmentManager(), "dialog");
                return true;
            case R.id.import_menuItem:
                f = ImportFragment.newInstance();
                f.show(getFragmentManager(), "dialog");
                return true;
            case R.id.paycheck_menuItem:
                Intent i = new Intent(this, PaycheckActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
