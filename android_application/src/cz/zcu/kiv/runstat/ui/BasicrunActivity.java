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

import java.util.List;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.data.DBHelper;
import cz.zcu.kiv.runstat.data.DynamixService;
import cz.zcu.kiv.runstat.data.Helper;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class BasicrunActivity extends Activity {
	
	// Classname for logging purposes
		private final String TAG = this.getClass().getSimpleName();
			
		//Helper instance
		public final Helper hlp = new Helper(); 
		
		//BroadcastReciever instance
		private RServiceRequestReceiver receiver;
		 
		public int steps = 0;
		public double stepForce = 0.0;
		public double latitude = 0.0;
		public double longtitude = 0.0;
		public float speed = 0;
		public float distance = 0;
		public String provider = "";
		
		//View
		private Handler myHandler;
		private TextView txtSteps;	
		private TextView txtCurrentSpeed;
		private TextView txtDistance;
		private CheckBox chckLocated;
		private TextView  txtProvider;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_basicrun);

			Log.d(TAG,"onCreate()");
			
			final DBHelper db = new DBHelper(getApplicationContext());

			//Handler for refreshing UI
			myHandler = new Handler();
			myHandler.post(stepsUpdate);
			
			//Inicialize widgets
			txtSteps = (TextView) findViewById(R.id.txtSteps);
			
			txtCurrentSpeed = (TextView) findViewById(R.id.txtCurrentSpeed);
			
			txtDistance = (TextView) findViewById(R.id.txtDistance);
			txtProvider = (TextView) findViewById(R.id.txtProvider);
			
			chckLocated = (CheckBox) findViewById(R.id.chckLocated);		
					
			//Intent filter for broadcast receiver
			IntentFilter filter = new IntentFilter(RServiceRequestReceiver.PROCESS_RESPONSE);
	        filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new RServiceRequestReceiver();
	        registerReceiver(receiver, filter);
	        
	        startRService();
	        
	        //Message box
	        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder
	    	.setTitle("Exit running")
	    	.setMessage("Are you sure?")
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {			      	
	    	    	stopRService();
	    	    	
	    	    	finish();    	    	
	    	    }
	    	})
	    	.setNegativeButton("No", null);	
	    	
	    	
			//********Buttons**********				
			final Button btnEnd = (Button) findViewById(R.id.btnEnd);
			btnEnd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							builder.show();									
					}
			});
			
			final ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
			btnHelp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						stopRService();
						Intent intent = new Intent(BasicrunActivity.this, HelpActivity.class); 
						startActivityForResult(intent, 0);									
					}
			});
			
			final Button button1 = (Button) findViewById(R.id.button1);
			button1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						List<String> locations = db.getAllLocations();      
				         
				        for (int i =0; i<locations.size();i++) {
				            String log = "From DB:" + locations.get(i);
				            Log.d("TAG ", log);										
				        }
					}
			});
			
			final Button button2 = (Button) findViewById(R.id.button2);
			button2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						db.removeAllLocations();
					}
			});
			
			txtCurrentSpeed.setOnLongClickListener(new OnLongClickListener() { 
		        @Override
		        public boolean onLongClick(View v){    
		        	//Show MAP
		        	Toast.makeText(getApplicationContext(), "Starting Google maps", Toast.LENGTH_SHORT).show();
		        	
		        	Intent intent = new Intent(BasicrunActivity.this, MapActivity.class); 
					startActivity(intent);	

		            return true;
		        }
		    });
								
	    	
		}
		
		/*
		 * Start service
		 */
		public void startRService(){
			Log.d(TAG, "startRService()");
			Intent intent = new Intent(BasicrunActivity.this, DynamixService.class); 
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
		 * Checks whether the service is running
		 */
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
			
			stopRService();
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}	
		
		/*
		 * (non-Javadoc)
		 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
		 */
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
		 * Refreshing UI
		 */
		private Runnable stepsUpdate = new Runnable() {
			   public void run() {

			       txtSteps.setText("(Aprox. " + steps + " steps)");
			       
			       //convert meters to km per hours and round the value
			       float roundedSpeed= (float)Math.round(speed * 36) / 10;
			       txtCurrentSpeed.setText("" + roundedSpeed);
			       
			       txtDistance.setText(""+ distance +" m");
			       
			       txtProvider.setText("Provider: " + provider);
			       
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
	            provider = intent.getStringExtra("DxProvider").toUpperCase();
	            
	            if(latitude!=0.0)
	            	chckLocated.setChecked(true);
	        }
		}
	
}