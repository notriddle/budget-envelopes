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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

public class EditTransactionFragment extends OkFragment
                                     implements TextWatcher {
    int mId;
    EditText mDescription;
    EditMoney mAmount;

    public static EditTransactionFragment newInstance(int id, String desc, long cents) {
        EditTransactionFragment retVal = new EditTransactionFragment();

        Bundle args = new Bundle();
        args.putInt("com.notriddle.budget.log", id);
        args.putString("com.notriddle.budget.log.description", desc);
        args.putLong("com.notriddle.bugdget.log.cents", cents);
        retVal.setArguments(args);

        return retVal;
    }

    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);

        Bundle args = getArguments();
        mId = args.getInt("com.notriddle.budget.log");
        mDescription.setText(
            args.getString("com.notriddle.budget.log.description")
        );
        mAmount.setCents(args.getLong("com.notriddle.bugdget.log.cents"));
    }

    @Override public View onCreateInternalView(LayoutInflater inflater,
                                               ViewGroup cont, Bundle state) {
        View retVal = inflater.inflate(R.layout.spendfragment, cont, false);
        mAmount = (EditMoney) retVal.findViewById(R.id.amount);
        mAmount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        mDescription = (EditText) retVal.findViewById(R.id.description);
        retVal.findViewById(R.id.delayed).setVisibility(View.GONE);
        retVal.findViewById(R.id.delay).setVisibility(View.GONE);
        retVal.findViewById(R.id.repeat).setVisibility(View.GONE);
        retVal.findViewById(R.id.frequency).setVisibility(View.GONE);
        return retVal;
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

    @Override public String getTitle() {
        return getActivity().getString(R.string.edit_name);
    }

    @Override public void ok() {
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(
                "UPDATE log SET cents = ?, description = ? WHERE _id = ?",
                new String[] {
                    Long.toString(mAmount.getCents()),
                    mDescription.getText().toString(),
                    Integer.toString(mId)
                }
            );
            EnvelopesOpenHelper.playLog(db);
            db.setTransactionSuccessful();
            getActivity().getContentResolver()
                          .notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

