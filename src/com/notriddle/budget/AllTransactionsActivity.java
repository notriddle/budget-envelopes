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
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AllTransactionsActivity extends Activity
                                     implements LoaderCallbacks<Cursor> {
    LogAdapter mAdapter;
    ListView mListView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.alltransactionsactivity);
        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new LogAdapter(this, null);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String time = Long.toString(System.currentTimeMillis()-5184000000l);
        SQLiteLoader retVal = new SQLiteLoader(
            this,
            new EnvelopesOpenHelper(this),
            "SELECT e.name AS envelope, e.color AS color, l.description AS description, l.cents AS cents, l.time AS time, l._id AS _id FROM log AS l LEFT JOIN envelopes AS e ON (e._id = l.envelope) ORDER BY l.time * -1"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        // Do nothing.
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, EnvelopesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

};

