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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent i) {
        final int widgetId = i.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            -1
        );
        return new RemoteViewsFactory() {
            Cursor csr;
            SQLiteDatabase db;
            ContentObserver obs;
            public void onCreate() {
                Log.d("Budget", "WidgetService.onCreate()");
                db = (new EnvelopesOpenHelper(WidgetService.this))
                      .getReadableDatabase();
                csr = db.rawQuery("SELECT name, cents, _id, color FROM envelopes ORDER BY name", null);
                csr.setNotificationUri(
                    getContentResolver(),
                    EnvelopesOpenHelper.URI
                );
                obs = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    public void onChange(boolean selfChange, Uri uri) {
                        onChange(selfChange);
                    }
                    public void onChange(boolean selfChange) {
                        AppWidgetManager
                        .getInstance(WidgetService.this)
                         .notifyAppWidgetViewDataChanged(widgetId, R.id.grid);
                    }
                };
                csr.registerContentObserver(obs);
            }
            public void onDestroy() {
                Log.d("Budget", "WidgetService.onDestroy()");
                csr.unregisterContentObserver(obs);
                csr.close();
                csr = null;
                db.close();
                db = null;
            }
            public boolean hasStableIds() {
                return true;
            }
            public long getItemId(int pos) {
                csr.moveToPosition(pos);
                return csr.getLong(2);
            }
            public int getCount() {
                int count = csr.getCount();
                Log.d("Budget", "WidgetService.getCount(): "+count);
                return count;
            }
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.card_widget);
            }
            public RemoteViews getViewAt(int pos) {
                Log.d("Budget", "WidgetService.getViewAt("+pos+")");
                RemoteViews views = new RemoteViews(
                    getPackageName(), R.layout.card_widget
                );
                csr.moveToPosition(pos);
                views.setTextViewText(R.id.name, csr.getString(0));
                views.setTextViewText(R.id.value,
                                      EditMoney.toMoney(csr.getLong(1)));
                Intent act = new Intent();
                act.putExtra("com.notriddle.budget.envelope",
                             (int)getItemId(pos));
                views.setOnClickFillInIntent(R.id.card, act);
                int color = csr.getInt(3);
                if (color == 0xFFEEEEEE || color == 0) {
                    views.setInt(R.id.name, "setBackgroundColor", 0);
                } else {
                    views.setInt(R.id.name, "setBackgroundColor", color);
                }

                return views;
            }
            public int getViewTypeCount() {
                return 1;
            }
            public void onDataSetChanged() {
                Log.d("Budget", "WidgetService.onDataSetChanged()");
                onDestroy();
                onCreate();
            }
        };
    }
}

