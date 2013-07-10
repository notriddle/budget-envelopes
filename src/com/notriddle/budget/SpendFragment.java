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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.PopupMenu;
import android.widget.TextView;

public class SpendFragment extends OkFragment
                           implements LoaderCallbacks<Cursor>,
                                      TextWatcher,
                                      AdapterView.OnItemClickListener {
    int mId;
    AutoCompleteTextView mDescription;
    EditMoney mAmount;
    SimpleLogAdapter mLogAdapter;
    EnvelopesOpenHelper mHelper;

    public static SpendFragment newInstance(int id) {
        SpendFragment retVal = new SpendFragment();

        Bundle args = new Bundle();
        args.putInt("com.notriddle.budget.envelope", id);
        retVal.setArguments(args);

        return retVal;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        mHelper = new EnvelopesOpenHelper(getActivity());
        mId = getArguments().getInt("com.notriddle.budget.envelope");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);
    }

    @Override public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog retVal = super.onCreateDialog(state);
        retVal
        .getWindow()
         .setSoftInputMode(WindowManager.LayoutParams
                                         .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return retVal;
    }

    @Override public View onCreateInternalView(LayoutInflater inflater,
                                               ViewGroup cont, Bundle state) {
        View retVal = inflater.inflate(R.layout.spendfragment, cont, false);
        mAmount = (EditMoney) retVal.findViewById(R.id.amount);
        mDescription = (AutoCompleteTextView) retVal.findViewById(R.id.description);
        mDescription.requestFocus();
        mLogAdapter = new SimpleLogAdapter(getActivity(), null);
        mLogAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                Cursor retVal = mHelper.getReadableDatabase().query(
                    "log",
                    new String[] { "description", "cents", "time", "_id" },
                    "envelope = ? AND cents < 0 AND UPPER(description) LIKE ?",
                    new String[] {Integer.toString(mId),
                                  constraint.toString().toUpperCase()+"%"},
                    null, null, "_id * -1"
                );
                retVal.setNotificationUri(getActivity().getContentResolver(),
                                          EnvelopesOpenHelper.URI);
                return retVal;
            }
        });
        mDescription.setAdapter(mLogAdapter);
        mDescription.setOnItemClickListener(this);
        return retVal;
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        Cursor c = mLogAdapter.getCursor();
        int oldPos = c.getPosition();
        c.moveToPosition(pos);
        mDescription.setText(c.getString(
            c.getColumnIndexOrThrow("description")
        ));
        mAmount.setCents(-1 * c.getLong(c.getColumnIndexOrThrow("cents")));
        c.moveToPosition(oldPos);
    }

    @Override public boolean isOk() {
        return mAmount != null && mAmount.getCents() != 0;
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

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(),
            mHelper,
            "log",
            new String[] { "description", "cents", "time", "_id" },
            "envelope = ? AND cents < 0",
            new String[] {Integer.toString(mId)},
            null,
            null,
            "_id * -1"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mLogAdapter.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        dismiss();
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.spend_name);
    }

    @Override public void ok() {
        EnvelopesOpenHelper.deposite(getActivity(), mId, -1*mAmount.getCents(),
                                     mDescription.getText().toString());
    }
};

