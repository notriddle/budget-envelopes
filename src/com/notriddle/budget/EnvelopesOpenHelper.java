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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;
import android.util.SparseArray;

public class EnvelopesOpenHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "envelopes.db";
    static final int DB_VERSION = 2;
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
        db.execSQL("CREATE TABLE 'log' ( '_id' INTEGER PRIMARY KEY, 'envelope' INTEGER, 'time' TIMESTAMP, 'description' TEXT, 'cents' INTEGER )");
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
    public static void playLog(SQLiteDatabase db) {
        SparseArray centsMap = new SparseArray();
        db.execSQL("UPDATE envelopes SET cents = 0");
        Cursor csr = db.rawQuery("SELECT cents, envelope, time FROM log", null);
        csr.moveToFirst();
        int l = csr.getCount();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i != l; ++i) {
            long time = csr.getLong(2);
            if (time <= currentTime) {
                int envelope = csr.getInt(1);
                Long centsObject = (Long)(centsMap.get(envelope));
                long cents = centsObject == null ? 0 : centsObject;
                centsMap.put(envelope, cents+csr.getLong(0));
            }
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
    public static void playLog(Context cntx) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(cntx))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            playLog(db);
            db.setTransactionSuccessful();
            cntx.getContentResolver().notifyChange(URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public static void depositeDelayed(SQLiteDatabase db, int envelope,
                                       long cents, String description,
                                       long delayUntil) {
        if (cents != 0) {
            ContentValues values = new ContentValues();
            values.put("envelope", envelope);
            values.put("time", delayUntil);
            values.put("description", description);
            values.put("cents", cents);
            db.insert("log", null, values);
        }
    }
    public static void depositeDelayed(Context cntx, int envelope, long cents,
                                       String description, long delayUntil) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(cntx))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            depositeDelayed(db, envelope, cents, description, delayUntil);
            db.setTransactionSuccessful();
            cntx.getContentResolver().notifyChange(URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

