/***********************************************************************************************************************
 *
 * This file is part of the RunStat project

 * ==========================================
 *
 * Copyright (C) 2014 by University of West Bohemia (http://www.zcu.cz/en/)
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
 * Dream team, 2014/5/11  Tom� Bouda
 *
 **********************************************************************************************************************/

package cz.zcu.kiv.runstat.ui;

import java.util.List;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.db.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MenuActivity extends Activity{
	
	DBHelper db;
	SharedPreferences sharedPref;
	private TextView txtLogedUser;
	
	
	private final String TAG = this.getClass().getSimpleName();
	
	/*
	 * Activity lifecycle
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate()");
		
		db = new DBHelper(getApplicationContext());
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		txtLogedUser = (TextView) findViewById(R.id.txtLogedUser);
		txtLogedUser.setText("Username: " + sharedPref.getString("nick", "") );
		
		/*
		 * Buttons
		 */
		
		//Show context menu with running types
		final Button btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "Start");

					String settingsProvider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
					
					if(settingsProvider.contains("gps")){
						registerForContextMenu(v); 
						openContextMenu(v);
						unregisterForContextMenu(v);
					}else
					{
						Toast.makeText(getApplicationContext(), "Please turn GPS on.", Toast.LENGTH_LONG).show(); 
					}
				}
		});
		
		//Show history
		final Button btnHistory = (Button) findViewById(R.id.btnHistory);
		btnHistory.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "History");

					Intent intent = new Intent(MenuActivity.this, HistoryActivity.class); 
					startActivityForResult(intent, 0);				
				}
		});
		
		//Show basic UI for sync locaitons with server
		final Button button1 = (Button) findViewById(R.id.btnSync);
		button1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "DBSync");
					
					Intent intent = new Intent(MenuActivity.this, DbSync.class); 
					startActivityForResult(intent, 0);
					
					/*
					List<String> locations = db.getAllLocations();
					for(int i=0;i<locations.size();i++){
						
						Log.v("DB", locations.get(i));
					}*/
				}
		});
		
		//Log out
		final Button btnLogOut = (Button) findViewById(R.id.btnLogOut);
		btnLogOut.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "LogOut");

					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("logged", false);
					editor.commit();
					
					Intent intent = new Intent(MenuActivity.this, MainActivity.class); 
					startActivity(intent);				
				}
		});
		
	}
	
	
	/*
	 * Hardware button back
	 */
	@Override
	public void onBackPressed() {
		//
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
	
	
	/*
	 * Context menu
	 */
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.basic:
	        	Intent intent = new Intent(MenuActivity.this, BasicrunActivity.class); 
				startActivityForResult(intent, 0);
	            return true;
	        case R.id.distance:
	        	Intent intent2 = new Intent(MenuActivity.this, DistancerunActivity.class); 
				startActivityForResult(intent2, 0);
	            return true;
	        case R.id.time:
	        	Intent intent3 = new Intent(MenuActivity.this, TimerunActivity.class); 
				startActivityForResult(intent3, 0);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
