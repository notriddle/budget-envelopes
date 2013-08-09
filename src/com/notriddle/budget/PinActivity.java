package com.notriddle.budget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class PinActivity extends Activity {
	private EditText pin;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getString("pin", "").equals("")) {
			start();
		}

		setContentView(R.layout.activity_pin);
		setTitle(R.string.title_activity_pin);

		pin = (EditText) findViewById(R.id.pin);
		pin.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					check();
					return true;
				}
				return false;
			}
		});
		Button pinEnter = (Button) findViewById(R.id.pinEnter);
		pinEnter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				check();
			}
		});

	}

	private void check() {
		if (pin.getText().toString().equals(preferences.getString("pin", ""))) {
			start();
		} else {
			Toast.makeText(getApplicationContext(), R.string.pin_wrong,
					Toast.LENGTH_LONG).show();
		}
	}

	private void start() {
		Intent intent = new Intent(PinActivity.this, EnvelopesActivity.class);
		startActivity(intent);
	}

}
