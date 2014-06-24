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
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class EnvelopesActivity extends LockedActivity
                               implements OkFragment.OnDismissListener,
                                          FragmentManager.OnBackStackChangedListener,
                                          ListView.OnItemClickListener {

    ListView mNavDrawer;
    NavAdapter mNavAdapter;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity);
        setupDrawer();

        if (state == null) {
            final Intent i = getIntent();
            final Bundle args = i.getExtras();
            final Uri data = i.getData();
            final String fragmentName = data != null
                ? data.getHost()
                : EnvelopesFragment.class.getName();
            final Fragment frag = Fragment.instantiate(this, fragmentName, args);
            if (frag instanceof DialogFragment) {
                DialogFragment dFrag = (DialogFragment)frag;
                dFrag.setShowsDialog(false);
            }
            getFragmentManager()
            .beginTransaction()
             .replace(R.id.content_frame, frag)
             .commit();
        }

        getFragmentManager().addOnBackStackChangedListener(this);

        onReplacedFragment();
    }

    private void setupDrawer() {
        mNavDrawer = (ListView) findViewById(R.id.left_drawer);
        mNavAdapter = new NavAdapter(this);
        mNavDrawer.setAdapter(mNavAdapter);
        mNavDrawer.setOnItemClickListener(this);
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment frag = Fragment.instantiate(
            this,
            mNavAdapter.getItem(pos).getName()
        );
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
         .replace(R.id.content_frame, frag)
         .commit();
        onReplacedFragment();
    }

    @Override public void onResume() {
        super.onResume();
        (new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... o) {
                EnvelopesOpenHelper.playLog(EnvelopesActivity.this);
                return null;
            }
            protected void onPostExecute(Object o) {
                // do nothing.
            }
        }).execute();
    }

    private void onReplacedFragment() {
        findViewById(android.R.id.content).postDelayed(new Runnable() {
            public void run() {
                onBackStackChanged();
            }
        }, 5);
    }

    @Override public void onBackStackChanged() {
        Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
        if (frag instanceof TitleFragment) {
            TitleFragment tFrag = (TitleFragment)frag;
            setTitle(tFrag.getTitle());
            getActionBar().setDisplayHomeAsUpEnabled(!(frag instanceof EnvelopesFragment));
            for (int i = 0; i != mNavAdapter.getCount(); ++i) {
                if (mNavAdapter.getItem(i) == frag.getClass()) {
                    mNavDrawer.setItemChecked(i, true);
                    break;
                } else {
                    mNavDrawer.setItemChecked(i, false);
                }
            }
        }
    }

    @Override public void onDismiss() {
        onBackPressed();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getFragmentManager();
                Fragment frag = Fragment.instantiate(
                    this,
                    EnvelopesFragment.class.getName()
                );
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, frag)
                 .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
