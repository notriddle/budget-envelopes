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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

public class NavAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    Context mCntx;

    public NavAdapter(Context cntx) {
        mInflater = LayoutInflater.from(cntx);
        mCntx = cntx;
    }

    @Override public int getCount() {
        return 4;
    }

    @Override public Class<?> getItem(int pos) {
        return pos == 0 ? EnvelopesFragment.class :
               pos == 1 ? AllTransactionsFragment.class :
               pos == 2 ? SettingsFragment.class :
               pos == 3 ? AboutFragment.class : null;
    }

    public String getItemTitle(int pos) {
        return pos == 0 ? mCntx.getResources().getString(R.string.envelopes_menuItem) :
               pos == 1 ? mCntx.getResources().getString(R.string.allTransactions_menuItem) :
               pos == 2 ? mCntx.getResources().getString(R.string.settings_menuItem) :
               pos == 3 ? mCntx.getResources().getString(R.string.about_menuItem) : null;
    }

    @Override public long getItemId(int pos) {
        return pos;
    }

    @Override public boolean hasStableIds() {
        return true;
    }

    @Override public int getItemViewType(int pos) {
        return pos > 1 ? 1 : 0;
    }

    @Override public int getViewTypeCount() {
        return 2;
    }

    @Override public View getView(int pos, View conv, ViewGroup cont) {
        View retVal;
        if (conv == null) {
            retVal = mInflater.inflate(pos > 1 ? R.layout.naventry_small : R.layout.naventry, cont, false);
        } else {
            retVal = conv;
        }
        TextView text = (TextView) retVal.findViewById(android.R.id.text1);
        text.setText(getItemTitle(pos));
        return retVal;
    }
}
