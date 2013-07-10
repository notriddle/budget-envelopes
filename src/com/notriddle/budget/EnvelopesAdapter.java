package com.notriddle.budget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EnvelopesAdapter extends CursorAdapter {
    LayoutInflater mInflater;

    public EnvelopesAdapter(Context cntx, Cursor csr) {
        super(cntx, csr, 0);
        mInflater = LayoutInflater.from(cntx);
    }

    private static class CardContents {
        public CardContents(View v) {
            name = (TextView) v.findViewById(R.id.name);
            value = (TextView) v.findViewById(R.id.value);
            parent = v;
        }
        public View parent;
        public TextView name;
        public TextView value;
    };

    @Override public void bindView(View v, Context cntx, Cursor csr) {
        CardContents contents = (CardContents) v.getTag();
        fillCardContents(cntx, contents, csr);
    }

    @Override public View newView(Context cntx, Cursor csr, ViewGroup par) {
        View retVal = mInflater.inflate(R.layout.card, par, false);
        CardContents contents = new CardContents(retVal);
        retVal.setTag(contents);
        fillCardContents(cntx, contents, csr);
        return retVal;
    }

    private void fillCardContents(Context cntx, CardContents contents, Cursor csr) {
        contents.name.setText(csr.getString(csr.getColumnIndexOrThrow("name")));
        long cents = csr.getLong(csr.getColumnIndexOrThrow("cents"));
        contents.value.setText(EditMoney.toColoredMoney(cntx, cents));
    }
}
