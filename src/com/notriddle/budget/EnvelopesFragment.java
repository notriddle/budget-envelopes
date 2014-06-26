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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class EnvelopesFragment extends Fragment
                               implements LoaderCallbacks<Cursor>,
                                          GridView.OnItemClickListener,
                                          MonitorScrollView.OnScrollListener,
                                          DeleteAdapter.Deleter,
                                          TitleFragment {
    GridView mGrid;
    EnvelopesAdapter mEnvelopes;
    DeleteAdapter mDeleteAdapter;
    TextView mTotal;
    View mTotalContainer;
    View mTotalLabel;
    MonitorScrollView mScroll;
    SharedPreferences mPrefs;

    public EnvelopesFragment() {
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.envelopesactivity, cont, false);
        mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(getActivity().getBaseContext());
        mGrid = (GridView) retVal.findViewById(R.id.grid);
        getLoaderManager().initLoader(0, null, this);
        mEnvelopes = new EnvelopesAdapter(getActivity(), null);
        mDeleteAdapter = new DeleteAdapter(getActivity(), this, mEnvelopes, 0);
        mGrid.setAdapter(mDeleteAdapter);
        mGrid.setOnItemClickListener(this);
        mTotalContainer = retVal.findViewById(R.id.totalamount);
        mTotal = (TextView) mTotalContainer.findViewById(R.id.value);
        mTotalLabel = mTotalContainer.findViewById(R.id.name);
        mScroll = (MonitorScrollView) retVal.findViewById(R.id.scroll);
        mScroll.setOnScrollListener(this);
        return retVal;
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.app_name);
    }

    @Override public void onPause() {
        super.onPause();
        mDeleteAdapter.performDelete();
    }

    @Override public void performDelete(long id) {
        deleteEnvelope((int)id);
    }
    @Override public void onDelete(long id) {
        loadEnvelopesData(mEnvelopes.getCursor());
    }
    @Override public void undoDelete(long id) {
        loadEnvelopesData(mEnvelopes.getCursor());
    }

    private void deleteEnvelope(int id) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM envelopes WHERE _id = ?",
                       new String[] {Integer.toString(id)});
            db.execSQL("DELETE FROM log WHERE envelope = ?",
                       new String[] {Integer.toString(id)});
            db.setTransactionSuccessful();
            getActivity().getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override public void onScrollChanged(int pos, int oldPos) {
        mTotal.setTranslationY((pos*2)/3);
        mTotalLabel.setTranslationY((pos*2)/3);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(), new EnvelopesOpenHelper(getActivity()), "envelopes",
            new String[] {
                "name", "cents", "color", "_id"
            },
            null,
            null,
            null,
            null,
            "name"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        loadEnvelopesData(data);
    }
    private void loadEnvelopesData(Cursor data) {
        data.moveToFirst();
        int l = data.getCount();
        long total = 0;
        boolean hasColor = false;
        for (int i = 0; i != l; ++i) {
            int id = data.getInt(data.getColumnIndexOrThrow("_id"));
            if (id != mDeleteAdapter.getDeletedId()) {
                total += data.getLong(data.getColumnIndexOrThrow("cents"));
                int color = data.getInt(data.getColumnIndexOrThrow("color"));
                if (color != 0) {
                    hasColor = true;
                }
            }
            data.moveToNext();
        }
        mTotal.setText(EditMoney.toColoredMoney(getActivity(), total));
        mEnvelopes.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        mEnvelopes.changeCursor(null);
    }

    @Override public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
        openEnvelope((int)id);
    }

    private void openEnvelope(int id) {
        Bundle args = new Bundle();
        args.putInt("com.notriddle.budget.envelope", id);
        ((EnvelopesActivity)getActivity()).switchFragment(EnvelopeDetailsFragment.class, "EnvelopeDetailsFragment", args);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.envelopesactivity, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newEnvelope_menuItem:
                SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                                    .getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", "");
                values.put("color", 0);
                long id = db.insert("envelopes", null, values);
                db.close();
                getActivity().getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
                openEnvelope((int)id);
                return true;
            case R.id.transfer_menuItem:
                DialogFragment f = TransferFragment.newInstance();
                f.show(getFragmentManager(), "dialog");
                return true;
            case R.id.paycheck_menuItem:
                ((EnvelopesActivity)getActivity()).switchFragment(
                    PaycheckFragment.class,
                    "PaycheckFragment",
                    null
                );
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
