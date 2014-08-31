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
import android.app.Fragment;
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
import android.util.Log;
import android.util.SparseArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PaycheckFragment extends OkFragment
                              implements LoaderCallbacks<Cursor>,
                                         PaycheckEnvelopesAdapter
                                         .DepositesChangeListener,
                                         TextWatcher,
                                         DeleteView.OnDeleteListener,
                                         View.OnClickListener,
                                         MonitorScrollView.OnScrollListener,
                                         TitleFragment,
                                         CustomActionBarFragment {
    PaycheckEnvelopesAdapter mEnvelopes;
    EditMoney mIncome;
    EditText mDescription;
    TextView mSpent;
    GridView mGrid;
    ProgressBar mProgress;
    MonitorScrollView mScroll;
    View mSpendingCard;
    long mSpentValue;
    long mIncomeValue;
    SharedPreferences mPrefs;

    @Override public View onCreateActionBarView(LayoutInflater inflater) {
        ActionBar ab = getActivity().getActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.paycheckactivity_income);
        mIncome = (EditMoney) ab.getCustomView().findViewById(R.id.amount);
        mIncome.setCents(mIncomeValue);
        mIncome.addTextChangedListener(this);
        mDescription = (EditText) ab.getCustomView().findViewById(R.id.description);
        return ab.getCustomView();
    }

    @Override public View onCreateInternalView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.paycheckactivity, cont, false);
        mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(getActivity().getBaseContext());
        DeleteView docs = (DeleteView) retVal.findViewById(R.id.docs);
        if (mPrefs.getBoolean("com.notriddle.budget.PaycheckActivity.docs.show",
                              true)) {
            docs.setOnClickListener(this);
            docs.setOnDeleteListener(this);
        } else {
            docs.setVisibility(View.GONE);
        }
        mGrid = (GridView) retVal.findViewById(R.id.grid);
        mProgress = (ProgressBar) retVal.findViewById(R.id.progress);
        mSpent = (TextView) retVal.findViewById(R.id.spent);
        Bundle deposites = state != null ? state.getBundle("deposites") : null;
        mIncomeValue = state != null ? state.getLong("income", 0) : 0;
        mEnvelopes = new PaycheckEnvelopesAdapter(
            getActivity(),
            null,
            deposites != null
            ? Util.unpackSparseLongArray(deposites)
            : new SparseArray<Long>()
        );
        mEnvelopes.setDepositesChangeListener(this);
        mGrid.setAdapter(mEnvelopes);
        getLoaderManager().initLoader(0, null, this);
        mScroll = (MonitorScrollView) retVal.findViewById(R.id.scroll);
        mSpendingCard = retVal.findViewById(R.id.spendingCard);
        mScroll.setOnScrollListener(this);
        return retVal;
    }

    @Override public void onClick(View v) {
        ((DeleteView)v).performDelete();
    }
    @Override public void onDelete(DeleteView v) {
        mPrefs.edit()
               .putBoolean("com.notriddle.budget.PaycheckActivity.docs.show",
                           false)
               .commit();
        v.setVisibility(View.GONE);
    }

    @Override public void onScrollChanged(int pos, int oldPos) {
        mSpendingCard.setTranslationY((pos*2)/3);
    }

    @Override public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBundle(
            "deposites",
            Util.packSparseLongArray(mEnvelopes.getDeposites())
        );
        state.putLong("income", mIncomeValue);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int currentBudget = ((EnvelopesActivity)getActivity()).getCurrentBudget();
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(), new EnvelopesOpenHelper(getActivity()), "envelopes",
            new String[] {
                "name", "lastPaycheckCents", "_id", "color"
            },
            "budget = ?",
            new String[] {
                Integer.toString(currentBudget)
            },
            null,
            null,
            "name"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onDepositesChange(SparseArray<Long> deposites) {
        recalcProgress();
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mEnvelopes.changeCursor(data);
        mProgress.postDelayed(new Runnable() {
            public void run() {
                recalcProgress();
            }
        }, 5);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        mEnvelopes.changeCursor(null);
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
        recalcProgress();
    }

    private void recalcProgress() {
        mIncomeValue = mIncome.getCents();
        SparseArray<Long> deposites = mEnvelopes.getDeposites();
        int l = deposites.size();
        mSpentValue = 0;
        for (int i = 0; i != l; ++i) {
            mSpentValue += deposites.valueAt(i);
        }
        if (mIncomeValue != 0 && mSpentValue != 0) {
            double progress = ((double)mSpentValue)/mIncomeValue;
            mProgress.setProgress((int)(progress*100000));
            mProgress.setIndeterminate(mIncomeValue < mSpentValue);
        } else {
            mProgress.setProgress(0);
        }
        mProgress.setMax(100000);
        mSpent.setText(EditMoney.toMoney(mSpentValue));
        getActivity().invalidateOptionsMenu();
    }

    @Override public boolean isOk() {
        return mIncomeValue != 0 && mIncomeValue == mSpentValue;
    }

    @Override public void ok() {
        SparseArray<Long> deposites = mEnvelopes.getDeposites();
        String description = mDescription.getText().toString();
        int l = deposites.size();
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity())).getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i != l; ++i) {
                int id = deposites.keyAt(i);
                long centsDeposited = deposites.valueAt(i);
                EnvelopesOpenHelper.deposite(db, id, centsDeposited, description);
                values.put("lastPaycheckCents", centsDeposited);
                db.update("envelopes", values, "_id = ?", new String[] {
                    Integer.toString(id)
                });
            }
            db.setTransactionSuccessful();
            getActivity().getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.paycheck_name);
    }

}
