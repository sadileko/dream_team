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
import cz.zcu.kiv.runstat.data.DynamixService;
import cz.zcu.kiv.runstat.data.Helper;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class BasicrunActivity extends Activity {
	
	// Classname for logging purposes
		private final String TAG = this.getClass().getSimpleName();
			
		//Helper instance
		public final Helper hlp = new Helper(); 
		
		//BroadcastReciever instance
		private RServiceRequestReceiver receiver;
			
		private SharedPreferences mPrefs;

		 
		public int steps = 0;
		public double stepForce = 0.0;
		public double latitude = 0.0;
		public double longtitude = 0.0;
		
		//Widgets
		private Handler myHandler;
		private TextView txtLat;
		private TextView txtLng;
		private TextView txtSteps;	
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_basicrun);

			Log.d(TAG,"onCreate()");
			
			//Refreshing UI
			myHandler = new Handler();
			myHandler.post(stepsUpdate);
			
			txtLat = (TextView) findViewById(R.id.txtLat);
			txtLng = (TextView) findViewById(R.id.txtLng);
			txtSteps = (TextView) findViewById(R.id.txtSteps);
			
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 		
							
			IntentFilter filter = new IntentFilter(RServiceRequestReceiver.PROCESS_RESPONSE);
	        filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new RServiceRequestReceiver();
	        registerReceiver(receiver, filter);
	        
	        startRService();
	        
			//********Buttons**********				
			final Button btnEnd = (Button) findViewById(R.id.btnEnd);
			btnEnd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(BasicrunActivity.this, MainActivity.class); 
						startActivity(intent);										
					}
			});
		}
		
		public void startRService(){
			Log.d(TAG, "startRService()");
			Intent intent = new Intent(BasicrunActivity.this, DynamixService.class); 
			startService(intent); 		
		}

		public void stopRService()
		{
			Log.d(TAG, "stopRService()");
			Intent intent = new Intent(BasicrunActivity.this, DynamixService.class);		
			stopService(intent);
		}
		
		
		public boolean serviceRunning()
		{
			Log.d(TAG, "serviceRunning()");
			ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (DynamixService.class.getName().equals(service.service.getClassName())) {
					Log.i(TAG, "serviceRunning() -> true");
					return true;
				}
			}
			Log.i(TAG, "serviceRunning() -> false");
			return false;
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
		        // showHelp();
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
		    }
		}
		
		
		//Refreshing View
		private Runnable stepsUpdate = new Runnable() {
			   public void run() {
				   
				   txtLat.setText(Double.toString(latitude));
				   txtLng.setText(Double.toString(longtitude));
				   
			       txtSteps.setText("" + steps);
			       
			       myHandler.postDelayed(this, 250);
			    }
		};

		
		public class RServiceRequestReceiver extends BroadcastReceiver{
			 
	        public static final String PROCESS_RESPONSE = "cz.zcu.kiv.runstat.intent.action.PROCESS_RESPONSE";
	 
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            steps = intent.getIntExtra("DxSteps", 0);
	            latitude = intent.getDoubleExtra("DxLat", 0.0);
	            longtitude = intent.getDoubleExtra("DxLng", 0.0);

	        }
		}
	
}
