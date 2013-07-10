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
import android.graphics.Rect;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import java.io.IOException;

public class EditMoney extends EditText {
    public EditMoney(Context cntx) {
        super(cntx);
        init();
    }
    public EditMoney(Context cntx, AttributeSet attrs) {
        super(cntx, attrs);
        init();
    }
    public EditMoney(Context cntx, AttributeSet attrs, int defStyle) {
        super(cntx, attrs, defStyle);
        init();
    }

    private void init() {
        setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public long getCents() {
        return EditMoney.toCents(getText().toString());
    }
    public void setCents(long cents) {
        setText(cents == 0 ? "" : EditMoney.toMoney(cents));
    }

    @Override public void onFocusChanged(boolean focus, int dir, Rect prev) {
        super.onFocusChanged(focus, dir, prev);
        if (!focus) {
            setCents(getCents());
        }
    }

    public static Appendable toMoneyBuilder(long cents, Appendable builder) {
        try {
            if (cents < 0) {
                builder.append("-");
                cents = -cents;
            }
            long dollarPart = cents/100;
            int centPart = (int)(cents - (dollarPart*100));
            if (dollarPart < 10) {
                builder.append("0");
            }
            builder.append(Long.toString(dollarPart));
            builder.append(centPart < 10 ? ".0" : ".");
            builder.append(Integer.toString(centPart));
            return builder;
        } catch (IOException e) {
            throw new Error(e);
        }
    }
    public static SpannableStringBuilder
                   toColoredMoneyBuilder(Context cntx, long cents,
                                         SpannableStringBuilder builder) {
        toMoneyBuilder(cents, builder);
        if (cents < 0) {
            builder.setSpan(new ForegroundColorSpan(cntx.getResources().getColor(R.color.badForeground)), 0, builder.length(), SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
    public static CharSequence toColoredMoney(Context cntx, long cents) {
        return toColoredMoneyBuilder(cntx, cents, new SpannableStringBuilder());
    }
    public static CharSequence toMoney(long cents) {
        return toMoneyBuilder(cents, new StringBuilder(5)).toString();
    }
    public static long toCents(String money) {
        String dollars = money.replaceAll("[^0-9\\.]", "");
        if (dollars.equals("")) dollars = "0";
        if (dollars.indexOf(".") == -1) {
            return Long.parseLong(dollars)*100;
        } else {
            int digitsAfterPoint = dollars.length()-(dollars.indexOf(".")+1);
            int placesFromCents = digitsAfterPoint-2;
            String cents = dollars.replaceAll("\\.","");
            if (placesFromCents < 0) {
                StringBuilder zeroes = new StringBuilder(-placesFromCents);
                for (int i = 0; i != -placesFromCents; ++i) {
                    zeroes.append("0");
                }
                cents = cents + zeroes;
            } else {
                cents = cents.substring(0, cents.length()-placesFromCents);
            }
            return Long.parseLong(cents) * (money.charAt(0) == '-' ? -1 : 1);
        }
    }
}
