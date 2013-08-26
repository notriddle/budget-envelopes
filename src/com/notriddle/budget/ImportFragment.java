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

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.notriddle.budget.csv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ImportFragment extends FileCreatorFragment {
    public static ImportFragment newInstance() {
        return new ImportFragment();
    }

    @Override protected void perform(Uri src) throws Throwable {
        if (src.toString().endsWith(".db")) {
            performOnDB(src);
        } else {
            performOnCSV(src);
        }
    }

    protected void performOnDB(Uri src) throws Throwable {
        String srcPath = src.getPath();
        SQLiteDatabase importDb = SQLiteDatabase.openDatabase(
            srcPath, null, SQLiteDatabase.OPEN_READONLY
        );
        Cursor chkbook = importDb.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='Accounts'", null);
        chkbook.moveToFirst();
        boolean exists = chkbook.getInt(0) == 1;
        chkbook.close();
        if (exists) {
            ContentValues envelopeValues = new ContentValues();
            ContentValues logValues = new ContentValues();
            SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                                .getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM envelopes");
                db.execSQL("DELETE FROM log");

                Cursor accounts = importDb.rawQuery("SELECT _id, _name FROM Accounts", null);
                int l = accounts.getCount();
                accounts.moveToFirst();
                for (int i = 0; i != l; ++i) {
                    envelopeValues.put("_id", accounts.getInt(0));
                    envelopeValues.put("name", accounts.getString(1));
                    db.insert("envelopes", null, envelopeValues);
                    accounts.moveToNext();
                }
                accounts.close();

                Cursor transactions = importDb.rawQuery("SELECT _account, _value, _transtype, description, year, month, day, hour, minute, _expensetype FROM Transactions", null);
                l = transactions.getCount();
                transactions.moveToFirst();
                for (int i = 0; i != l; ++i) {
                    int account = transactions.getInt(0);
                    long cents = (long)(transactions.getDouble(1)*100);
                    if (transactions.getString(2).equals("W")) {
                        cents = cents*-1;
                    }
                    String description = transactions.getString(3);
                    if (description == null || description.equals("")) {
                        description = transactions.getString(9);
                    }
                    int year = transactions.getInt(4);
                    int month = transactions.getInt(5);
                    int day = transactions.getInt(6);
                    int hour = transactions.getInt(7);
                    int minute = transactions.getInt(8);
                    Date d = new Date(year-1900, month-1, day, hour, minute);
                    long time = d.getTime();
                    logValues.put("envelope", account);
                    logValues.put("time", time);
                    logValues.put("description", description);
                    logValues.put("cents", cents);
                    db.insert("log", null, logValues);
                    transactions.moveToNext();
                }
                transactions.close();

                EnvelopesOpenHelper.playLog(db);
                db.setTransactionSuccessful();
                getActivity().getContentResolver()
                              .notifyChange(EnvelopesOpenHelper.URI, null);
            } finally {
                db.endTransaction();
                db.close();
            }
        } else {
            importDb.close();
            File destPath = getActivity().getDatabasePath(
                EnvelopesOpenHelper.DB_NAME
            );
            FileInputStream srcS = new FileInputStream(srcPath);
            FileOutputStream destS = new FileOutputStream(destPath);
            Util.pump(srcS, destS);
        }
    }

    protected void performOnCSV(Uri dest) throws Throwable {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        ContentValues envelopeValues = new ContentValues();
        ContentValues logValues = new ContentValues();
        FileReader f = new FileReader(dest.getPath());
        CSVReader c = new CSVReader(f);
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM envelopes");
            db.execSQL("DELETE FROM log");
            String[] list = c.readNext();
            while (list != null) {
                long time = Long.parseLong(list[0]);
                String envelopeName = list[1];
                long cents = Long.parseLong(list[2]);
                String description = list[3];
                int envelopeId;
                if (map.containsKey(envelopeName)) {
                    envelopeId = map.get(envelopeName);
                } else {
                    envelopeValues.put("name", envelopeName);
                    envelopeId = (int) db.insert("envelopes", null,
                                                 envelopeValues);
                    map.put(envelopeName, envelopeId);
                }
                logValues.put("envelope", envelopeId);
                logValues.put("time", time);
                logValues.put("description", description);
                logValues.put("cents", cents);
                db.insert("log", null, logValues);
                list = c.readNext();
            }
            EnvelopesOpenHelper.playLog(db);
            db.setTransactionSuccessful();
            getActivity().getContentResolver()
                          .notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
            f.close();
        }
    }

    @Override protected int getButtonTitle() {
        return R.string.import_name;
    }
    @Override protected int getDialogTitle() {
        return R.string.import_name;
    }
};

