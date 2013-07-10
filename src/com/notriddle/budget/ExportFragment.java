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
import com.notriddle.budget.csv.CSVWriter;
import java.io.FileWriter;

public class ExportFragment extends FileCreatorFragment {
    public static ExportFragment newInstance() {
        return new ExportFragment();
    }

    @Override protected void perform(Uri dest) throws Throwable {
        FileWriter f = new FileWriter(dest.getPath());
        CSVWriter c = new CSVWriter(f);
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getReadableDatabase();
        Cursor csr = db.rawQuery("SELECT l.time, e.name, l.cents, l.description FROM log AS l LEFT JOIN envelopes AS e ON (l.envelope = e._id)", null);
        int l = csr.getCount();
        csr.moveToFirst();
        for (int i = 0; i != l; ++i) {
            c.writeNext(new String[] {
                csr.getString(0), csr.getString(1), csr.getString(2),
                csr.getString(3)
            });
            csr.moveToNext();
        }
        db.close();
        f.close();
    }

    @Override protected int getButtonTitle() {
        return R.string.export_menuItem;
    }
    @Override protected int getDialogTitle() {
        return R.string.export_name;
    }
};

