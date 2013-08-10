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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    private boolean isUnlocked(Context cntx) {
        SharedPreferences prefs
         = PreferenceManager
           .getDefaultSharedPreferences(cntx.getApplicationContext());
        return prefs.getBoolean("com.notriddle.budget.unlocked", false)
               || prefs.getString("com.notriddle.budget.pin", "").equals("");
    }
    @Override public void onEnabled(Context cntx) {
        if (!isUnlocked(cntx)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(cntx);
            Intent i = new Intent(cntx, PinActivity.class);
            Intent j = new Intent(cntx, WidgetProvider.class);
            j.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            j.putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                manager.getAppWidgetIds(
                    new ComponentName(cntx, this.getClass())
                )
            );
            PendingIntent pJ = PendingIntent.getBroadcast(cntx, 0, j, 0);
            i.putExtra("com.notriddle.budget.NEXT_ACTIVITY", (Parcelable)pJ);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            cntx.startActivity(i);
        }
    }
    @Override public void onUpdate(Context cntx, AppWidgetManager manager,
                                   int[] widgetIds) {
        final int l = widgetIds.length;
        boolean unlocked = isUnlocked(cntx);
        for (int i = 0; i != l; ++i) {
            int widgetId = widgetIds[i];
            Log.d("Budget", "WidgetProvider.id="+widgetId);
            RemoteViews views = new RemoteViews(
                cntx.getPackageName(),
                R.layout.widget
            );
            if (unlocked) {
                Intent srv = new Intent(cntx, WidgetService.class);
                srv.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetIds[i]);
                srv.setData(Uri.parse(srv.toUri(Intent.URI_INTENT_SCHEME)));
                views.setRemoteAdapter(widgetIds[i], R.id.grid, srv);
                views.setEmptyView(R.id.grid, R.id.empty);
            }
            Intent act = new Intent(cntx, EnvelopeDetailsActivity.class);
            act.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetIds[i]);
            act.setData(Uri.parse(act.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent actPending = PendingIntent.getActivity(
                cntx, 0, act, PendingIntent.FLAG_UPDATE_CURRENT
            );
            views.setPendingIntentTemplate(R.id.grid, actPending);
            manager.updateAppWidget(widgetIds[i], views);
        }
        super.onUpdate(cntx, manager, widgetIds);
    }
}

