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

import android.app.Activity;
import android.app.ActivityOptions;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
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
                                 implements TextView.OnEditorActionListener,
                                            TitleFragment {

    boolean mRanOk;

    public static interface OnDismissListener {
        public void onDismiss();
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);
        mRanOk = false;
    }

    @Override public void onResume() {
        super.onResume();
        refreshOkButton();
    }

    @Override public void dismiss() {
        if (!getShowsDialog()) {
            OnDismissListener l = (OnDismissListener)getActivity();
            l.onDismiss();
        } else {
            super.dismiss();
        }
    }

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup cont, Bundle state) {
        View retVal = null;
        if (!getShowsDialog()) {
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
        menu.findItem(R.id.ok_menuItem).setVisible(!getShowsDialog());
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.ok_menuItem:
                ok();
                dismiss();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public boolean onEditorAction(TextView v, int a, KeyEvent e) {
        if (!mRanOk && isOk()) {
            mRanOk = true;
            ok();
            dismiss();
        }
        return false;
    }

    protected void changeToActivity() {
        EnvelopesActivity a = (EnvelopesActivity) getActivity();
        Bundle args = (Bundle) getArguments().clone();
        writeArgs(args);
        a.switchFragment(getClass(), getClass().getName(), args);
        dismiss();
    }

    protected void writeArgs(Bundle args) {
    }

    abstract public void ok();
    abstract public boolean isOk();
    abstract public String getTitle();
    abstract public View onCreateInternalView(LayoutInflater inflater,
                                              ViewGroup cont, Bundle state);

};

