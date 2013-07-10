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
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

public class LogAdapter extends CursorAdapter {
    LayoutInflater mInflater;
    DateFormat mDate;

    public LogAdapter(Context cntx, Cursor csr) {
        super(cntx, csr, 0);
        mInflater = LayoutInflater.from(cntx);
        mDate = android.text.format.DateFormat.getLongDateFormat(cntx);
    }

    private static class CardContents {
        public CardContents(View v) {
            name = (TextView) v.findViewById(R.id.name);
            value = (TextView) v.findViewById(R.id.value);
            time = (TextView) v.findViewById(R.id.time);
            money = new StringBuilder(6);
            parent = v;
        }
        public View parent;
        public TextView name;
        public TextView value;
        public TextView time;
        public StringBuilder money;
    };

    /*@Override public boolean areAllItemsEnabled() {
        return false;
    }
    @Override public boolean isEnabled(int pos) {
        return false;
    }*/

    @Override public String convertToString(Cursor csr) {
        return csr.getString(
            csr.getColumnIndexOrThrow("description")
        );
    }

    @Override public void bindView(View v, Context cntx, Cursor csr) {
        CardContents contents = (CardContents) v.getTag();
        fillCardContents(contents, csr);
    }

    @Override public View newView(Context cntx, Cursor csr, ViewGroup par) {
        View retVal = mInflater.inflate(R.layout.logentry, par, false);
        CardContents contents = new CardContents(retVal);
        retVal.setTag(contents);
        fillCardContents(contents, csr);
        return retVal;
    }

    private void fillCardContents(CardContents contents, Cursor csr) {
        contents.name.setText(csr.getString(
            csr.getColumnIndexOrThrow("description")
        ));
        long cents = csr.getLong(csr.getColumnIndexOrThrow("cents"));
        StringBuilder money = contents.money;
        money.delete(0, money.length());
        if (cents > 0) {
            money.append("+");
        }
        contents.value.setText(EditMoney.toMoneyBuilder(cents, money).toString());
        long time = csr.getLong(csr.getColumnIndexOrThrow("time"));
        Date timeD = new Date(time);
        String formattedDate = mDate.format(timeD);
        contents.time.setText(formattedDate);
    }
}
