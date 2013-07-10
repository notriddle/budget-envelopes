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
import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PaycheckActivity extends Activity
                              implements LoaderCallbacks<Cursor>,
                                         PaycheckEnvelopesAdapter
                                         .DepositesChangeListener,
                                         TextWatcher,
                                         View.OnClickListener,
                                         MonitorScrollView.OnScrollListener {
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

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.paycheckactivity);
        mPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        View docs = findViewById(R.id.docs);
        if (mPrefs.getBoolean("com.notriddle.budget.PaycheckActivity.docs.show",
                             true)) {
            docs.setOnClickListener(this);
        } else {
            docs.setVisibility(docs.GONE);
        }
        mGrid = (GridView) findViewById(R.id.grid);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mSpent = (TextView) findViewById(R.id.spent);
        Bundle deposites = state != null ? state.getBundle("deposites") : null;
        mEnvelopes = new PaycheckEnvelopesAdapter(
            this,
            null,
            deposites != null
            ? Util.unpackSparseLongArray(deposites)
            : new SparseArray()
        );
        mEnvelopes.setDepositesChangeListener(this);
        mGrid.setAdapter(mEnvelopes);
        getLoaderManager().initLoader(0, null, this);
        ActionBar ab = getActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.paycheckactivity_income);
        ab.setDisplayHomeAsUpEnabled(true);
        mIncome = (EditMoney) ab.getCustomView().findViewById(R.id.amount);
        mIncome.addTextChangedListener(this);
        mIncomeValue = state != null ? state.getLong("income", 0) : 0;
        mIncome.setCents(mIncomeValue);
        mDescription = (EditText) ab.getCustomView().findViewById(R.id.description);
        recalcProgress();
        mScroll = (MonitorScrollView) findViewById(R.id.scroll);
        mSpendingCard = findViewById(R.id.spendingCard);
        mScroll.setOnScrollListener(this);
    }

    @Override public void onClick(View v) {
        mPrefs.edit()
               .putBoolean("com.notriddle.budget.PaycheckActivity.docs.show",
                           false)
               .commit();
        v.setVisibility(v.GONE);
    }

    @Override public void onScrollChanged(int pos, int oldPos) {
        mSpendingCard.setTranslationY((pos*2)/3);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBundle(
            "deposites",
            Util.packSparseLongArray(mEnvelopes.getDeposites())
        );
        state.putLong("income", mIncomeValue);
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

    @Override public void onDepositesChange(SparseArray deposites) {
        recalcProgress();
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mEnvelopes.changeCursor(data);
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
        SparseArray deposites = mEnvelopes.getDeposites();
        int l = deposites.size();
        mSpentValue = 0;
        int accountsWithNonzero = 0;
        for (int i = 0; i != l; ++i) {
            long newValue = (Long) deposites.valueAt(i);
            if (newValue != 0) {
                mSpentValue += newValue;
                accountsWithNonzero += 1;
            }
        }
        if (accountsWithNonzero <= 1 && mIncomeValue < mSpentValue) {
            mIncomeValue = mSpentValue;
            mIncome.setCents(mSpentValue);
        }
        boolean tooBig = mIncomeValue > Integer.MAX_VALUE && mSpentValue != 0;
        int displaySpentValue = (int)(tooBig ? 1 : mSpentValue);
        int displayMaxValue = (int)(tooBig ? (mIncomeValue/mSpentValue) : mIncomeValue);
        mProgress.setProgress(displaySpentValue);
        mProgress.setMax(displayMaxValue);
        mSpent.setText(EditMoney.toMoney(mSpentValue));
        invalidateOptionsMenu();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.ok_menuItem).setEnabled(mIncomeValue != 0 && mIncomeValue == mSpentValue);
        return true;
    }

    private void depositePaycheck() {
        SparseArray deposites = mEnvelopes.getDeposites();
        String description = mDescription.getText().toString();
        int l = deposites.size();
        SQLiteDatabase db = (new EnvelopesOpenHelper(this)).getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i != l; ++i) {
                int id = deposites.keyAt(i);
                long centsDeposited = (Long) deposites.valueAt(i);
                EnvelopesOpenHelper.deposite(db, id, centsDeposited, description);
            }
            db.setTransactionSuccessful();
            getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, EnvelopesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.ok_menuItem:
                depositePaycheck();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
