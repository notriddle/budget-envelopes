package com.notriddle.budget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class OkFragment extends DialogFragment
                                 implements TextView.OnEditorActionListener {
    @Override public void onResume() {
        super.onResume();
        refreshOkButton();
    }

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup cont, Bundle state) {
        View retVal = null;
        if (getDialog() == null) {
            retVal = onCreateInternalView(inflater, null, state);
        }
        return retVal;
    }

    @Override public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_Dialog);
        LayoutInflater inflater = LayoutInflater.from(builder.getContext());
        View v = onCreateInternalView(inflater, null, state);
        View t = inflater.inflate(R.layout.custom_title, null, false);
        ((TextView)t.findViewById(R.id.title)).setText(getTitle());
        ((LinearLayout)t).addView(v);
        return builder
               .setView(t)
               //.setView(v)
               //.setTitle(getTitle())
               .setPositiveButton(android.R.string.ok,
                   new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface d, int button) {
                           ok();
                       }
                   }
               )
               .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int button) {
                        }
                    }
               )
               .create();
    }

    public void refreshOkButton() {
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null && d.getButton(d.BUTTON_POSITIVE) != null) {
            d.getButton(d.BUTTON_POSITIVE)
              .setEnabled(isOk());
        }
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu,
                                              MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.ok, menu);
    }

    @Override public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.ok_menuItem).setEnabled(isOk());
        menu.findItem(R.id.ok_menuItem).setVisible(getDialog() == null);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok_menuItem:
                ok();
                dismiss();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public boolean onEditorAction(TextView v, int a, KeyEvent e) {
        if (isOk()) {
            ok();
            dismiss();
        }
        return !isOk();
    }

    abstract public void ok();
    abstract public boolean isOk();
    abstract public String getTitle();
    abstract public View onCreateInternalView(LayoutInflater inflater,
                                              ViewGroup cont, Bundle state);

};

