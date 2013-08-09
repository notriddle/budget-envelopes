package com.notriddle.budget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PinActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getString("pin", "").equals("")) {
			start();
		}

		setContentView(R.layout.activity_pin);
		
		
		final EditText pin=(EditText) findViewById(R.id.pin);
		Button pinEnter=(Button) findViewById(R.id.pinEnter);
		pinEnter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("Blubb",preferences.getString("pin", ""));
				if(pin.getText().toString().equals(preferences.getString("pin", "")))				{
					start();
				} else {
					Toast.makeText(getApplicationContext(), R.string.pin_wrong, Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private void start() {
		Intent intent = new Intent(PinActivity.this, EnvelopesActivity.class);
		startActivity(intent);
	}

}
