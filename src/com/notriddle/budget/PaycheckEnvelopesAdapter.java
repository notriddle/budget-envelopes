package com.notriddle.budget;

import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class PaycheckEnvelopesAdapter extends CursorAdapter {
    public static interface DepositesChangeListener {
        public void onDepositesChange(SparseArray deposites);
    };

    LayoutInflater mInflater;
    SparseArray mDeposites;
    DepositesChangeListener mListener;

    public PaycheckEnvelopesAdapter(Context cntx, Cursor csr,
                                    SparseArray deposites) {
        super(cntx, csr, 0);
        mInflater = LayoutInflater.from(cntx);
        mDeposites = deposites;
    }

    public void setDeposits(SparseArray deposites) {
        mDeposites = deposites;
        notifyDataSetChanged();
    }

    @Override public boolean areAllItemsEnabled() {
        return false;
    }
    @Override public boolean isEnabled(int pos) {
        return false;
    }

    public SparseArray getDeposites() {
        return mDeposites;
    }

    public void setDepositesChangeListener(DepositesChangeListener listener) {
        mListener = listener;
    }

    private static class CardContents {
        public CardContents(View v) {
            parent = v;
            name = (TextView) v.findViewById(R.id.name);
            value = (EditMoney) v.findViewById(R.id.value);
        }
        public View parent;
        public TextView name;
        public EditMoney value;
    };

    @Override public void bindView(View v, Context cntx, Cursor csr) {
        CardContents contents = (CardContents) v.getTag();
        fillCardContents(contents, csr);
    }

    @Override public View newView(Context cntx, Cursor csr, ViewGroup par) {
        final View retVal = mInflater.inflate(R.layout.card_edit, par, false);
        final CardContents contents = new CardContents(retVal);
        final EditMoney value = contents.value;
        retVal.setTag(contents);
        fillCardContents(contents, csr);
        value.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                // Do nothing.
            }

            @Override public void beforeTextChanged(CharSequence s, int start,
                                                    int count, int end) {
                // Do nothing.
            }

            @Override public void onTextChanged(CharSequence s, int start,
                                                int count, int end) {
                if (value.getTag() == null) return;
                long cents = value.getCents();
                int id = (Integer) value.getTag();
                Log.i("PaycheckEnvelopesAdapter.TextWatcher.onTextChanged",
                      "id="+id+", cents="+cents);
                mDeposites.put(id, Long.valueOf(cents));
                if (mListener != null) {
                    mListener.onDepositesChange(mDeposites);
                }
            }
        });
        return retVal;
    }

    private void fillCardContents(CardContents contents, Cursor csr) {
        contents.name.setText(csr.getString(csr.getColumnIndexOrThrow("name")));
        int id = csr.getInt(csr.getColumnIndexOrThrow("_id"));
        long cents = (Long) mDeposites.get(id, 0l);
        Log.i("PaycheckEnvelopesAdapter.fillCardContents",
              "id="+id+", cents="+cents);
        contents.value.setTag(null);
        contents.value.setCents(cents);
        contents.value.setTag(id);
    }
}
