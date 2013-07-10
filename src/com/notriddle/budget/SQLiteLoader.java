package com.notriddle.budget;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;

public class SQLiteLoader extends AsyncTaskLoader<Cursor> {
    Context mCntx;
    SQLiteDatabase mDatabase;
    String mTable;
    String[] mColumns;
    String mSelection;
    String[] mSelectionArgs;
    String mGroupBy;
    String mHaving;
    String mOrderBy;
    Cursor mResults;
    ContentObserver mObserver;
    Uri mNotificationUri;

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns, String selection, String[] args, 
                        String groupBy, String having, String orderBy) {
        super(cntx);
        mCntx = cntx;
        mDatabase = helper.getReadableDatabase();
        mTable = table;
        mColumns = columns;
        mSelection = selection;
        mSelectionArgs = args;
        mGroupBy = groupBy;
        mHaving = having;
        mOrderBy = orderBy;
        mObserver = new ForceLoadContentObserver();
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns, String selection, String[] args) {
        this(cntx, helper, table, columns, selection, args, null, null, null);
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns) {
        this(cntx, helper, table, columns, null, null, null, null, null);
    }

    public void setNotificationUri(Uri uri) {
        mNotificationUri = uri;
        if (mResults != null) {
            mResults.setNotificationUri(mCntx.getContentResolver(), uri);
        }
    }

    @Override public Cursor loadInBackground() {
        if (mResults != null) {
            mResults.unregisterContentObserver(mObserver);
        }
        mResults = mDatabase.query(
            mTable, mColumns, mSelection, mSelectionArgs,
            mGroupBy, mHaving, mOrderBy
        );
        mResults.registerContentObserver(mObserver);
        if (mNotificationUri != null) {
            mResults.setNotificationUri(mCntx.getContentResolver(), mNotificationUri);
        }
        return mResults;
    }

    @Override public void onStartLoading() {
        forceLoad();
    }

    @Override public void abandon() {
        super.abandon();
        mResults.unregisterContentObserver(mObserver);
        mResults = null;
        mDatabase.close();
    }
}

