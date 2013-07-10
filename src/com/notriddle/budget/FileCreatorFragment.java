package com.notriddle.budget;

import android.app.Activity;
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
import android.os.Environment;
import android.widget.Toast;
import com.notriddle.budget.csv.CSVWriter;
import java.io.FileWriter;
import java.io.File;

public abstract class FileCreatorFragment extends DialogFragment {
    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        Uri dest = Uri.fromFile(new File(
            Environment.getExternalStorageDirectory(),
            "budget.csv"
        ));
        Intent i = new Intent("org.openintents.action.PICK_FILE");
        i.putExtra("org.openintents.extra.TITLE",
                   getActivity().getString(getButtonTitle()));
        i.setData(dest);
        if (getActivity().getPackageManager()
                          .queryIntentActivities(i, 0).size() == 0) {
            act(dest);
        } else {
            startActivityForResult(i, 42);
        }
    }

    @Override public void onActivityResult(int req, int res, Intent data) {
        if (req == 42) {
            if (res == Activity.RESULT_OK) {
                act(data.getData());
            } else {
                dismiss();
            }
        } else {
            super.onActivityResult(req, res, data);
        }
    }

    private void act(Uri uri) {
        ProgressDialog prog = (ProgressDialog) getDialog();
        prog.setMessage(uri.getPath());
        (new AsyncTask<Uri, Object, Throwable>() {
            protected Throwable doInBackground(Uri... dests) {
                try {
                    perform(dests[0]);
                    return null;
                } catch (Throwable e) {
                    return e;
                }
            }
            protected void onPostExecute(Throwable e) {
                dismiss();
                if (e != null) {
                    Toast.makeText(
                        getActivity(),
                        e.toString(),
                        Toast.LENGTH_LONG
                    ).show();
                }
            }
        }).execute(uri);
    }

    abstract protected void perform(Uri uri) throws Throwable;
    abstract protected int getButtonTitle();
    abstract protected int getDialogTitle();

    @Override public ProgressDialog onCreateDialog(Bundle state) {
        ProgressDialog retVal = new ProgressDialog(getActivity());
        retVal.setTitle(getActivity().getString(getDialogTitle()));
        return retVal;
    }
};

