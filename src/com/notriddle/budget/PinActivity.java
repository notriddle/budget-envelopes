/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Anatolij Zelenin <az@azapps.de>
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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class PinActivity extends Activity {
    private EditText mPin;
    private SharedPreferences mPrefs;
    private PendingIntent mNextActivity;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(getApplicationContext());

        Intent i = getIntent();

        if (i != null && "com.notriddle.budget.LOCK".equals(i.getAction())) {
            lock();
        } else if (mPrefs.getString("com.notriddle.budget.pin", "").equals("")
                   || mPrefs.getBoolean("com.notriddle.budget.unlocked", false)) {
            finishSuccessful();
        } else {
            Parcelable nextActivity
             = i == null
               ? null
               : i.getParcelableExtra("com.notriddle.budget.NEXT_ACTIVITY");
            mNextActivity = (PendingIntent) nextActivity;
            Log.d("Budget", "mNextActivity="+mNextActivity);

            setContentView(R.layout.activity_pin);

            mPin = (EditText) findViewById(R.id.pin);
            mPin.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId,
                        KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        check();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok_menuItem:
                check();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void check() {
        if (mPin.getText()
                 .toString()
                  .equals(mPrefs.getString("com.notriddle.budget.pin", ""))) {
            unlock();
        } else {
            Toast.makeText(getApplicationContext(), R.string.pin_toast_wrong,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void finishSuccessful() {
        try {
            if (mNextActivity != null) {
                mNextActivity.send(this, 0, null);
            } else {
                if (getIntent() != null
                    && getIntent()
                       .hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    Intent i = new Intent();
                    i.putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        getIntent()
                        .getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                     AppWidgetManager.INVALID_APPWIDGET_ID)
                    );
                    setResult(RESULT_OK, i);
                } else {
                    setResult(RESULT_OK);
                }
            }
        } catch (PendingIntent.CanceledException e) {
            throw new Error(e);
        }
        finish();
    }

    private void unlock() {
        notify(this);
        mPrefs.edit()
               .putBoolean("com.notriddle.budget.unlocked", true)
               .apply();
        finishSuccessful();
    }

    private void lock() {
        NotificationManager nM = (NotificationManager)
                                 getSystemService(NOTIFICATION_SERVICE);
        nM.cancel(R.string.pin_notify);
        mPrefs.edit()
               .putBoolean("com.notriddle.budget.unlocked", false)
               .apply();
        finish();
    }

    private static void notify(Activity cntx) {
        Notification.Builder nB = new Notification.Builder(cntx);
        nB.setSmallIcon(R.drawable.ic_notification);
        nB.setContentTitle(cntx.getText(R.string.app_name));
        nB.setContentText(cntx.getText(R.string.pin_notify));
        Intent i = new Intent(cntx, PinActivity.class);
        i.setAction("com.notriddle.budget.LOCK");
        PendingIntent pI = PendingIntent.getActivity(cntx, 0, i, 0);
        nB.setContentIntent(pI);
        nB.setDeleteIntent(pI);
        nB.setAutoCancel(true);
        NotificationManager nM = (NotificationManager)
                                 cntx.getSystemService(NOTIFICATION_SERVICE);
        nM.notify(R.string.pin_notify, nB.getNotification());
    }

    public static boolean ensureUnlocked(Activity a) {
        SharedPreferences prefs
         = PreferenceManager
           .getDefaultSharedPreferences(a.getApplicationContext());
        
        boolean done
         = prefs.getString("com.notriddle.budget.pin", "").equals("")
           || prefs.getBoolean("com.notriddle.budget.unlocked", false);

        if (!done) {
            Intent i = new Intent(a, PinActivity.class);
            Intent j = new Intent(a.getApplicationContext(), a.getClass());
            //j.setData(a.getIntent().getData());
            //j.setAction(a.getIntent().getAction());
            j.putExtras(a.getIntent());
            PendingIntent pJ = PendingIntent.getActivity(a.getApplicationContext(), 0, j, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d("Budget", "pJ="+pJ);
            i.putExtra("com.notriddle.budget.NEXT_ACTIVITY",
                       (Parcelable)pJ);
            a.startActivity(i);
        } else {
            notify(a);
        }

        return done;
    }
}
