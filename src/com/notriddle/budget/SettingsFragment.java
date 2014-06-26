/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Anatolij Zelenin <az@azapps.de>
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

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment
                                     implements Preference
                                                .OnPreferenceClickListener,
                                                TitleFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        EditTextPreference pref
         = (EditTextPreference) findPreference("com.notriddle.budget.pin");
        pref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        Preference p = findPreference("com.notriddle.budget.export");
        p.setOnPreferenceClickListener(this);
        p = findPreference("com.notriddle.budget.import");
        p.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        DialogFragment f = (p == findPreference("com.notriddle.budget.import")) ? ImportFragment.newInstance() : ExportFragment.newInstance();
        f.show(getFragmentManager(), "dialog");
        return true;
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.settings_name);
    }
}

