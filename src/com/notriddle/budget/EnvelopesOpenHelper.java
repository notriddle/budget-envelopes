package com.notriddle.budget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;
import android.util.SparseArray;

public class EnvelopesOpenHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "envelopes.db";
    static final int DB_VERSION = 1;
    public static final Uri URI = Uri.parse("sqlite://com.notriddle.budget/envelopes");

    Context mCntx;
    public EnvelopesOpenHelper(Context cntx) {
        super(cntx, DB_NAME, null, DB_VERSION);
        mCntx = cntx;
    }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE 'envelopes' ( '_id' INTEGER PRIMARY KEY, 'name' TEXT, 'cents' INTEGER );");
        ContentValues values = new ContentValues();
        values.put("name", mCntx.getString(R.string.default_envelope_1));
        values.put("cents", 0);
        db.insert("envelopes", null, values);
        values.put("name", mCntx.getString(R.string.default_envelope_2));
        values.put("cents", 0);
        db.insert("envelopes", null, values);
        values.put("name", mCntx.getString(R.string.default_envelope_3));
        values.put("cents", 0);
        db.insert("envelopes", null, values);
        db.execSQL("CREATE TABLE 'log' ( '_id' INTEGER PRIMARY KEY AUTOINCREMENT, 'envelope' INTEGER, 'time' TIMESTAMP, 'description' TEXT, 'cents' INTEGER )");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
    }

    public static void deposite(SQLiteDatabase db, int envelope, long cents,
                                String description) {
        if (cents != 0) {
            String envelopeString = Integer.toString(envelope);
            String[] envelopeStringArray = new String[] {envelopeString};
            ContentValues values = new ContentValues();
            Cursor csr
             = db.rawQuery("SELECT cents FROM envelopes WHERE _id = ?",
                           envelopeStringArray);
            csr.moveToFirst();
            long currentCents = csr.getLong(csr.getColumnIndexOrThrow("cents"));
            values.put("cents", currentCents+cents);
            db.update("envelopes", values, "_id = ?", envelopeStringArray);
            values.put("envelope", envelope);
            values.put("time", System.currentTimeMillis());
            values.put("description", description);
            values.put("cents", cents);
            db.insert("log", null, values);
        }
    }
    public static void playLog(SQLiteDatabase db) {
        SparseArray centsMap = new SparseArray();
        db.execSQL("UPDATE envelopes SET cents = 0");
        Cursor csr = db.rawQuery("SELECT cents, envelope FROM log", null);
        csr.moveToFirst();
        int l = csr.getCount();
        for (int i = 0; i != l; ++i) {
            int envelope = csr.getInt(1);
            Long centsObject = (Long)(centsMap.get(envelope));
            long cents = centsObject == null ? 0 : centsObject;
            centsMap.put(envelope, cents+csr.getLong(0));
            csr.moveToNext();
        }
        l = centsMap.size();
        for (int i = 0; i != l; ++i) {
            int envelope = centsMap.keyAt(i);
            long cents = (Long) centsMap.valueAt(i);
            db.execSQL("UPDATE envelopes SET cents = ? WHERE _id = ?",
                       new String[] {Long.toString(cents),
                                     Integer.toString(envelope)});
        }
    }
    public static void deposite(Context cntx, int envelope, long cents,
                                String description) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(cntx))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            deposite(db, envelope, cents, description);
            db.setTransactionSuccessful();
            cntx.getContentResolver().notifyChange(URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

