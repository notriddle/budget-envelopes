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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SimpleEnvelopesAdapter extends SimpleCursorAdapter {
    static final String[] FROM = new String[] {
        "name"
    };
    static final int[] TO = new int[] {
        android.R.id.text1
    };
    public SimpleEnvelopesAdapter(Context cntx, Cursor csr) {
        super(cntx, android.R.layout.simple_list_item_1, csr, FROM, TO, 0);
    }
    public SimpleEnvelopesAdapter(Context cntx, Cursor csr, int layout) {
        super(cntx, layout, csr, FROM, TO, 0);
    }
    @Override public View getDropDownView(int pos, View conv, ViewGroup par) {
        View retVal = super.getDropDownView(pos, conv, par);
        Cursor csr = getCursor();
        csr.moveToPosition(pos);
        int color = csr.getInt(csr.getColumnIndexOrThrow("color"));
        if (color == 0) {
            retVal.setBackgroundDrawable(null);
        } else {
            retVal.setBackgroundColor(color);
        }
        return retVal;
    }
    @Override public View newDropDownView(Context cntx, Cursor csr,
                                          ViewGroup parent) {
        View retVal = LayoutInflater.from(cntx).inflate(
            android.R.layout.simple_spinner_dropdown_item,
            parent,
            false
        );
        bindView(retVal, cntx, csr);
        return retVal;
    }
}
