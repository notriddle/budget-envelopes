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
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ImportFragment extends FileCreatorFragment {
    public static ImportFragment newInstance() {
        return new ImportFragment();
    }

    @Override protected void perform(Uri dest) throws Throwable{
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
        return R.string.import_menuItem;
    }
    @Override protected int getDialogTitle() {
        return R.string.import_name;
    }
};

