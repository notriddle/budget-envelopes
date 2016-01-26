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
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class TransferFragment extends OkFragment
                              implements LoaderCallbacks<Cursor>,
                                         TextWatcher,
                                         AdapterView.OnItemSelectedListener {
    EditText mDescription;
    EditMoney mAmount;
    Spinner mFrom;
    Spinner mTo;
    SparseArray mCurrentCents;

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);
        mFrom.setOnItemSelectedListener(this);
        mTo.setOnItemSelectedListener(this);
    }

    @Override public View onCreateInternalView(LayoutInflater inflater,
                                               ViewGroup cont, Bundle state) {
        View retVal = inflater.inflate(R.layout.transferfragment, cont, false);
        mAmount = (EditMoney) retVal.findViewById(R.id.amount);
        mDescription = (EditText) retVal.findViewById(R.id.description);
        mFrom = (Spinner) retVal.findViewById(R.id.from);
        mTo = (Spinner) retVal.findViewById(R.id.to);
        return retVal;
    }

    @Override public boolean isOk() {
        return mAmount != null && mCurrentCents != null
               && mAmount.getCents() != 0
               && mFrom.getSelectedItemPosition() != mTo.getSelectedItemPosition();
    }

    @Override public void beforeTextChanged(CharSequence s, int start,
                                            int count, int after) {
        // Do nothing.
    }
    @Override public void onTextChanged(CharSequence s, int start,
                                        int before, int count) {
        // Do nothing.
    }
    @Override public void afterTextChanged(Editable s) {
        refreshOkButton();
    }
    @Override public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
        a.post(new Runnable() {
            public void run() {
                refreshOkButton();
            }
        });
    }
    @Override public void onNothingSelected(AdapterView<?> p) {
        refreshOkButton();
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(), new EnvelopesOpenHelper(getActivity()), "envelopes",
            new String[] {
                "name", "cents", "_id", "color"
            }
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        int l = data.getCount();
        mCurrentCents = new SparseArray(l);
        data.moveToFirst();
        for (int i = 0; i != l; ++i) {
            mCurrentCents.put(data.getInt(data.getColumnIndexOrThrow("_id")),
                              data.getLong(data.getColumnIndexOrThrow("cents")));
            data.moveToNext();
        }
        SimpleEnvelopesAdapter a = new SimpleEnvelopesAdapter(
            getActivity(),
            data,
            R.layout.dropdown_nothing
        );
        mFrom.setAdapter(a);
        mTo.setAdapter(a);
        refreshOkButton();
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        dismiss();
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.transfer_name);
    }

    @Override public void ok() {
        int fromId = (int) mFrom.getSelectedItemId();
        int toId = (int) mTo.getSelectedItemId();
        long transferCents = mAmount.getCents();
        String description = mDescription.getText().toString();
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
	        EnvelopesOpenHelper.deposite(db, fromId, -1*transferCents, description, null);
            EnvelopesOpenHelper.deposite(db, toId, transferCents, description, null);
            db.setTransactionSuccessful();
            getActivity()
            .getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

