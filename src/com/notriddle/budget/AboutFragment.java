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

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AboutFragment extends Fragment
                           implements View.OnClickListener,
                                      TitleFragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.aboutactivity, cont, false);

        TextView txt = (TextView) retVal.findViewById(R.id.aboutText);
        try {
            txt.setText(String.format(txt.getText().toString(), getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
        } catch (Throwable e) {
            throw new Error(e);
        }
        txt = (TextView) retVal.findViewById(R.id.donateButton);
        txt.setOnClickListener(this);
        final TextView gplView = (TextView) retVal.findViewById(R.id.gplText);
        (new AsyncTask<Object, Object, CharSequence>() {
            protected CharSequence doInBackground(Object... o) {
                try {
                    InputStream gplStream
                     = getActivity().getResources().getAssets().open("gpl.html");
                    ByteArrayOutputStream gplBytes = new ByteArrayOutputStream(
                        gplStream.available()
                    );
                    Util.pump(gplStream, gplBytes);
                    return Html.fromHtml(
                        new String(gplBytes.toByteArray(), "ASCII")
                    );
                } catch (Throwable e) {
                    return e.toString();
                }
            }
            protected void onPostExecute(CharSequence result) {
                gplView.setText(result);
            }
        }).execute();

        return retVal;
    }

    @Override public void onClick(View v) {
        startActivity(new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.notriddle.com/donate/")
        ));
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.about_name);
    }
}
