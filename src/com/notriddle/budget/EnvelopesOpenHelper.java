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
import java.util.GregorianCalendar;

public class EnvelopesOpenHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "envelopes.db";
    static final int DB_VERSION = 7;
    public static final Uri URI = Uri.parse("sqlite://com.notriddle.budget/envelopes");

    Context mCntx;
    public EnvelopesOpenHelper(Context cntx) {
        super(cntx, DB_NAME, null, DB_VERSION);
        mCntx = cntx;
    }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE 'envelopes' ( '_id' INTEGER PRIMARY KEY, 'name' TEXT, 'cents' INTEGER, 'projectedCents' INTEGER, 'lastPaycheckCents' INTEGER, 'color' INTEGER );");
        ContentValues values = new ContentValues();
        values.put("name", mCntx.getString(R.string.default_envelope_1));
        values.put("cents", 0);
        values.put("projectedCents", 0);
        values.put("lastPaycheckCents", 0);
        values.put("color", 0);
        db.insert("envelopes", null, values);
        values.put("name", mCntx.getString(R.string.default_envelope_2));
        values.put("cents", 0);
        values.put("projectedCents", 0);
        values.put("lastPaycheckCents", 0);
        values.put("color", 0);
        db.insert("envelopes", null, values);
        values.put("name", mCntx.getString(R.string.default_envelope_3));
        values.put("cents", 0);
        values.put("projectedCents", 0);
        values.put("lastPaycheckCents", 0);
        values.put("color", 0);
        db.insert("envelopes", null, values);
        db.execSQL("CREATE TABLE 'log' ( '_id' INTEGER PRIMARY KEY, 'envelope' INTEGER, 'time' TIMESTAMP, 'description' TEXT, 'cents' INTEGER, 'repeat' STRING )");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        if (oldVer < 3) {
            db.execSQL("ALTER TABLE 'envelopes' ADD COLUMN 'projectedCents' INTEGER");
            playLog(db);
        }
        if (oldVer < 4) {
            db.execSQL("ALTER TABLE 'envelopes' ADD COLUMN 'lastPaycheckCents' INTEGER");
            db.execSQL("UPDATE envelopes SET lastPaycheckCents = 0");
        }
        if (oldVer < 5) {
            db.execSQL("ALTER TABLE 'envelopes' ADD COLUMN 'color' INTEGER");
            db.execSQL("UPDATE envelopes SET color = 0");
        } else if (oldVer == 5) {
            db.execSQL("UPDATE envelopes SET color = 0 WHERE color = ?", new String[] {Integer.toString(0xFFEEEEEE)});
        }
        if (oldVer < 7){
            db.execSQL("ALTER TABLE 'log' ADD COLUMN 'repeat' STRING");
        } 
    }

    public static void deposite(SQLiteDatabase db, int envelope, long cents,
                                String description, String frequency) {
        if (cents != 0) {
            String envelopeString = Integer.toString(envelope);
            String[] envelopeStringArray = new String[] {envelopeString};
            ContentValues values = new ContentValues();
            Cursor csr
             = db.rawQuery("SELECT cents, projectedCents FROM envelopes WHERE _id = ?",
                           envelopeStringArray);
            csr.moveToFirst();
            long currentCents = csr.getLong(csr.getColumnIndexOrThrow("cents"));
            long currentProjectedCents = csr.getLong(csr.getColumnIndexOrThrow("projectedCents"));
            values.put("cents", currentCents+cents);
            values.put("projectedCents", currentProjectedCents+cents);
            db.update("envelopes", values, "_id = ?", envelopeStringArray);
            values = new ContentValues();
            values.put("envelope", envelope);
            values.put("time", System.currentTimeMillis());
            values.put("description", description);
            values.put("cents", cents);
            values.put("repeat", frequency);
            db.insert("log", null, values);
        }
    }
    public static void deposite(Context cntx, int envelope, long cents,
                                String description, String frequency) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(cntx))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
	        deposite(db, envelope, cents, description, frequency);
            db.setTransactionSuccessful();
            cntx.getContentResolver().notifyChange(URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public static void playLog(SQLiteDatabase db) {
        long currentTime = System.currentTimeMillis();
        /* First insert repeated transaction up to now */
        Cursor csr
	        = db.rawQuery("SELECT envelope, MAX(time) AS last, cents, description, repeat FROM log WHERE repeat IS NOT NULL AND time < ? GROUP BY envelope, cents, description, repeat", new String [] {Long.toString(currentTime)});
        if (csr.moveToFirst()) {
	        do {
		        long last = csr.getLong(csr.getColumnIndexOrThrow("last"));
		        int envelope = csr.getInt(csr.getColumnIndexOrThrow("envelope"));
		        long cents = csr.getLong(csr.getColumnIndexOrThrow("cents"));
		        String description = csr.getString(csr.getColumnIndexOrThrow("description"));
		        String repeat = csr.getString(csr.getColumnIndexOrThrow("repeat"));
		        if (repeat == null) continue;
		        
		        GregorianCalendar cal = new GregorianCalendar();
		        cal.setTimeInMillis(last);
		        while (cal.getTimeInMillis() < currentTime) {
			        if ( repeat.toLowerCase().equals("monthly") ){
				        cal.add((GregorianCalendar.MONTH), 1);
			        } else if ( repeat.toLowerCase().equals("daily") ){
				        cal.add((GregorianCalendar.DAY_OF_MONTH), 1);
			        } else if ( repeat.toLowerCase().equals("weekly") ){
				        cal.add((GregorianCalendar.WEEK_OF_YEAR), 1);
			        } else if ( repeat.toLowerCase().equals("yearly") ){
				        cal.add((GregorianCalendar.YEAR), 1);
			        } else if ( repeat.toLowerCase().equals("fortnightly") ){
				        cal.add((GregorianCalendar.WEEK_OF_YEAR), 2);
			        } else if ( repeat.toLowerCase().equals("quarterly") ){
				        cal.add((GregorianCalendar.MONTH), 3);
			        }
			        /* This will always deposit one payment in advance - by design */
			        depositeDelayed(db, envelope, cents, description, repeat, cal.getTimeInMillis());
			        db.execSQL("UPDATE log SET repeat = NULL WHERE time < ?", new String [] {Long.toString(currentTime)});	        
		        } 
	        } while(csr.moveToNext());
        }
        db.execSQL("UPDATE envelopes SET cents = (SELECT SUM(log.cents) FROM log WHERE log.envelope = envelopes._id AND log.time < ? GROUP BY log.envelope), projectedCents = (SELECT SUM(log.cents) FROM log WHERE log.envelope = envelopes._id GROUP BY log.envelope)", new String [] {Long.toString(currentTime)});
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
                                       String repeat, long delayUntil) {
        if (cents != 0) {
            String envelopeString = Integer.toString(envelope);
            String[] envelopeStringArray = new String[] {envelopeString};
            ContentValues values = new ContentValues();
            Cursor csr
             = db.rawQuery("SELECT projectedCents FROM envelopes WHERE _id = ?",
                           envelopeStringArray);
            csr.moveToFirst();
            long currentProjectedCents = csr.getLong(csr.getColumnIndexOrThrow("projectedCents"));
            values.put("projectedCents", currentProjectedCents+cents);
            if (delayUntil <= System.currentTimeMillis()) {
                values.put("cents", currentProjectedCents+cents);
            }
            db.update("envelopes", values, "_id = ?", envelopeStringArray);
            ContentValues lValues = new ContentValues();
            lValues.put("envelope", envelope);
            lValues.put("time", delayUntil);
            lValues.put("description", description);
            lValues.put("cents", cents);
            lValues.put("repeat", repeat);
            db.insert("log", null, lValues);
        }
    }
    public static void depositeDelayed(Context cntx, int envelope, long cents,
                                       String description, String repeat, long delayUntil ) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(cntx))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
	        depositeDelayed(db, envelope, cents, description, repeat, delayUntil);
            db.setTransactionSuccessful();
            cntx.getContentResolver().notifyChange(URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

