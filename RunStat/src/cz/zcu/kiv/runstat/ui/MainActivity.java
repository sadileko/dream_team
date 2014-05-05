/***********************************************************************************************************************
 *
 * This file is part of the ${PROJECT_NAME} project

 * ==========================================
 *
 * Copyright (C) ${YEAR} by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * ${NAME}, ${YEAR}/${MONTH}/${DAY} ${HOUR}:${MINUTE} ${USER}
 *
 **********************************************************************************************************************/

package cz.zcu.kiv.runstat.ui;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.data.DbSync;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity{

	private final String TAG = this.getClass().getSimpleName();
	
	/*
	 * Activity lifecycle
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate()");
		
		
		/*
		 * Buttons
		 */
		
		final Button btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "Start");
					Intent intent = new Intent(MainActivity.this, BasicrunActivity.class); 
					startActivityForResult(intent, 0);										
				}
		});
		
		final Button btnHistory = (Button) findViewById(R.id.btnHistory);
		btnHistory.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "History");

					Intent intent = new Intent(MainActivity.this, HistoryActivity.class); 
					startActivityForResult(intent, 0);
										
				}
		});
		
		final Button button1 = (Button) findViewById(R.id.btnSync);
		button1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "DBSync");

					Intent intent = new Intent(MainActivity.this, DbSync.class); 
					startActivityForResult(intent, 0);
										
				}
		});
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	    	Intent intent = new Intent(this, SettingsActivity.class); 
			startActivity(intent);
	        return true;
	    case R.id.action_help:
	    	Intent intent2 = new Intent(this, HelpActivity.class); 
			startActivity(intent2);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
}
