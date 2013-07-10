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
               && (Long)mCurrentCents.get((int)mFrom.getSelectedItemId())
                  >= mAmount.getCents()
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
                "name", "cents", "_id"
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
            EnvelopesOpenHelper.deposite(db, fromId, -1*transferCents, description);
            EnvelopesOpenHelper.deposite(db, toId, transferCents, description);
            db.setTransactionSuccessful();
            getActivity()
            .getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

