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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public class FragmentActivity extends Activity
                              implements OkFragment.OnDismissListener {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        final Intent i = getIntent();
        final Bundle args = i.getExtras();
        final Fragment frag = Fragment.instantiate(this, i.getData().getHost(),
                                                   args);
        if (frag instanceof DialogFragment) {
            DialogFragment dFrag = (DialogFragment)frag;
            dFrag.setShowsDialog(false);
        }
        getFragmentManager()
        .beginTransaction()
         .replace(android.R.id.content, frag)
         .commit();

        findViewById(android.R.id.content).postDelayed(new Runnable() {
            public void run() {
                if (frag instanceof OkFragment) {
                    OkFragment tFrag = (OkFragment)frag;
                    setTitle(tFrag.getTitle());
                }
            }
        }, 5);
    }

    @Override public void onDismiss() {
        finish();
    }
}
