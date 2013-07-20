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
import android.net.Uri;
import android.os.Build;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.GregorianCalendar;

public class SpendFragment extends OkFragment
                           implements LoaderCallbacks<Cursor>,
                                      TextWatcher,
                                      AdapterView.OnItemClickListener,
                                      DatePicker.OnDateChangedListener,
                                      CompoundButton.OnCheckedChangeListener {
    public static final boolean EARN = false;
    public static final boolean SPEND = true;

    int mId;
    AutoCompleteTextView mDescription;
    EditMoney mAmount;
    SimpleLogAdapter mLogAdapter;
    EnvelopesOpenHelper mHelper;
    boolean mNegative;
    DatePicker mDelay;
    CheckBox mDelayed;

    public static SpendFragment newInstance(int id, boolean negative) {
        SpendFragment retVal = new SpendFragment();

        Bundle args = new Bundle();
        args.putInt("com.notriddle.budget.envelope", id);
        args.putBoolean("com.notriddle.budget.negative", negative);
        retVal.setArguments(args);

        return retVal;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        mHelper = new EnvelopesOpenHelper(getActivity());
        Bundle args = getArguments();
        mId = args.getInt("com.notriddle.budget.envelope");
        mNegative = args.getBoolean("com.notriddle.budget.negative");
        getLoaderManager().initLoader(0, null, this);
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
                    null, null, "time * -1"
                );
                retVal.setNotificationUri(getActivity().getContentResolver(),
                                          EnvelopesOpenHelper.URI);
                return retVal;
            }
        });
        mDescription.setAdapter(mLogAdapter);
        mDescription.setOnItemClickListener(this);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);

        mDelayed = (CheckBox) retVal.findViewById(R.id.delayed);
        mDelayed.setOnCheckedChangeListener(this);
        mDelay = (DatePicker) retVal.findViewById(R.id.delay);
        GregorianCalendar min = new GregorianCalendar();
        min.add(min.SECOND, -1);
        min.add(min.DAY_OF_MONTH, 1);
        mDelay.setMinDate(min.getTimeInMillis());
        GregorianCalendar tomorrow = new GregorianCalendar();
        tomorrow.add(min.DAY_OF_MONTH, 1);
        mDelay.init(tomorrow.get(tomorrow.YEAR), tomorrow.get(tomorrow.MONTH), tomorrow.get(tomorrow.DAY_OF_MONTH), this);

        if (state == null) {
            Bundle args = getArguments();
            mDescription.setText(args.getString("com.notriddle.budget.description", ""));
            mAmount.setCents(args.getLong("com.notriddle.budget.cents", 0));
            mDelayed.setChecked(args.getBoolean("com.notriddle.budget.delayed", false));
        }
        onCheckedChanged(mDelayed, mDelayed.isChecked());

        return retVal;
    }

    @Override protected void writeArgs(Bundle args) {
        args.putString("com.notriddle.budget.description",
                       mDescription.getText().toString());
        args.putLong("com.notriddle.budget.cents", mAmount.getCents());
        args.putBoolean("com.notriddle.budget.delayed",
                        mDelayed.isChecked());
    }

    @Override public void onCheckedChanged(CompoundButton b, boolean checked) {
        if (getActivity().getResources()
                          .getDimensionPixelOffset(R.dimen.tabletBool) == 0
             && getShowsDialog() && checked) {
            changeToActivity();
        } else {
            mDelay.setVisibility(checked ? View.VISIBLE : View.GONE);
        }
    }

    @Override public void onDateChanged(DatePicker view, int year, int month,
                                        int day) {
        // Do nothing.
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        Cursor c = mLogAdapter.getCursor();
        int oldPos = c.getPosition();
        c.moveToPosition(pos);
        mDescription.setText(c.getString(
            c.getColumnIndexOrThrow("description")
        ));
        if (mAmount.getCents() == 0) {
            mAmount.setCents((mNegative?-1:1) * c.getLong(c.getColumnIndexOrThrow("cents")));
        }
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
            "time * -1"
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
        return getActivity().getString(mNegative?R.string.spend_name:R.string.earn_name);
    }

    @Override public void ok() {
        if (mDelayed.isChecked()) {
            long time = new GregorianCalendar(
                mDelay.getYear(),
                mDelay.getMonth(),
                mDelay.getDayOfMonth()
            ).getTimeInMillis();
            EnvelopesOpenHelper.depositeDelayed(
                getActivity(), mId,
                (mNegative?-1:1)*mAmount.getCents(),
                mDescription.getText().toString(),
                time
            );
        } else {
            EnvelopesOpenHelper.deposite(getActivity(), mId,
                                         (mNegative?-1:1)*mAmount.getCents(),
                                         mDescription.getText().toString());
        }
    }
};

