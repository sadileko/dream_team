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

import java.util.Locale;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.logic.DynamixService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


public class BasicrunActivity extends Activity {
	
		// Classname for logging purposes
		private final String TAG = this.getClass().getSimpleName();
			
		//BroadcastReciever instance
		private RServiceRequestReceiver receiver;
		
		WifiManager wifi;
		SharedPreferences sharedPref;
		
		//Custom variables
		public int steps = 0;
		public double stepForce = 0.0;
		public double latitude = 0.0;
		public double longtitude = 0.0;
		public float speed = 0;
		public float distance = 0;
		public String provider = "";
		boolean settingsUseWifi;
		
		//View
		private Handler myHandler;
		private AlertDialog.Builder builder;
		private TextView txtSteps;	
		private TextView txtCurrentSpeed;
		private TextView txtDistance;
		private CheckBox chckLocated;
		private TextView  txtProvider;
		private ProgressBar pBar;
		
		/*
		 * Activity lifecycle
		 */
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_basicrun);
			Log.d(TAG,"onCreate()");			
				
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			
			/*
			 * 	View
			 */
			txtSteps = (TextView) findViewById(R.id.txtSteps);			
			txtCurrentSpeed = (TextView) findViewById(R.id.txtCurrentSpeed);			
			txtDistance = (TextView) findViewById(R.id.txtDistance);
			txtProvider = (TextView) findViewById(R.id.txtProvider);			
			chckLocated = (CheckBox) findViewById(R.id.chckLocated);							
			pBar = (ProgressBar) findViewById(R.id.pBar);
			
			//Handler for refreshing UI
			myHandler = new Handler();
			myHandler.post(stepsUpdate);
						
			//Intent filter for broadcast receiver
			IntentFilter filter = new IntentFilter(RServiceRequestReceiver.PROCESS_RESPONSE);
	        filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new RServiceRequestReceiver();
	        registerReceiver(receiver, filter);
	        
	        //Start dynamix service
	        startRService();
	        
	        //Message box 
	        builder = new AlertDialog.Builder(this);
	    	builder
	    	.setTitle("Exit running")
	    	.setMessage("Are you sure?")
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	
	    	    	//Stop service and finish activity
	    	    	stopRService();	    	    	
	    	    	finish();    	    	
	    	    }
	    	})
	    	.setNegativeButton("No", null);	
	    	
	    	
			/*
			 * 	Buttons			
			 */
	    	
	    	//Close application
			final Button btnEnd = (Button) findViewById(R.id.btnEnd);
			btnEnd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							builder.show();									
					}
			});
						
			//Show help
			final ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
			btnHelp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(BasicrunActivity.this, HelpActivity.class); 
						startActivityForResult(intent, 0);									
					}
			});
	
		}
		
		/*
		 * Hardware button back
		 */
		@Override
		public void onBackPressed() {
			builder.show();
		}
		
		@Override
		public void onDestroy(){
			super.onDestroy();
			
			stopRService();
			unregisterReceiver(receiver);
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
		 * Start service
		 */
		public void startRService(){
			Log.d(TAG, "startRService()");
			Intent intent = new Intent(BasicrunActivity.this, DynamixService.class); 
			intent.putExtra("runType", 0);
			startService(intent); 		
		}

		
		/*
		 * Stops running service
		 */
		public void stopRService()
		{
			Log.d(TAG, "stopRService()");
			Intent intent = new Intent(BasicrunActivity.this, DynamixService.class);		
			stopService(intent);
		}
		
				
		/*
		 * Refreshing UI
		 */
		private Runnable stepsUpdate = new Runnable() {
			   public void run() {

				   wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				   settingsUseWifi = sharedPref.getBoolean("pref_key_usewifi", false);
				   String settingsProvider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				   
				   if(settingsProvider.contains("gps") || (wifi.isWifiEnabled() && settingsUseWifi)){
					   
					   if(settingsProvider.contains("gps"))
						   settingsProvider = "gps";
					   
					   txtSteps.setText("(Aprox. " + steps + " steps)");
				       
				       //convert meters to km per hours and round the value
				       float roundedSpeed= (float)Math.round(speed * 36) / 10;
				       txtCurrentSpeed.setText("" + roundedSpeed);
				       
				       txtDistance.setText(""+ Math.round(distance) +" m");
				       
				       txtProvider.setText("Provider: " + settingsProvider); 
				        
				   }else
				   {
					   txtProvider.setText("Please turn GPS on, or turn wifi on and allow it in settings."); 
				   }
   				          			     
			       myHandler.postDelayed(this, 250);
			    }
		};

		
		/*
		 * Broadcast receiver for receiving values from service
		 */
		public class RServiceRequestReceiver extends BroadcastReceiver{
			 
	        public static final String PROCESS_RESPONSE = "cz.zcu.kiv.runstat.intent.action.PROCESS_RESPONSE";
	 
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	
	            steps = intent.getIntExtra("DxSteps", 0);
	            latitude = intent.getDoubleExtra("DxLat", 0.0);
	            longtitude = intent.getDoubleExtra("DxLng", 0.0);
	            speed = intent.getFloatExtra("DxSpeed", 0);
	            distance = intent.getFloatExtra("DxDistance", 0);
	            provider = intent.getStringExtra("DxProvider").toUpperCase(new Locale("cs", "CZ"));
	            
	            if(latitude!=0.0 || longtitude!=0.0){
	            	pBar.setVisibility(View.INVISIBLE);
	            	chckLocated.setVisibility(View.VISIBLE);
	            	chckLocated.setChecked(true);
	            }	            	
	            else{
	            	chckLocated.setVisibility(View.INVISIBLE);
	            	pBar.setVisibility(View.VISIBLE);
	            }
	            	
	        }
		}
	
}
