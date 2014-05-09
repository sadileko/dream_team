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

package cz.zcu.kiv.runstat.logic;

import java.util.List;

import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ContextSupportResult;
import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.contextplugins.location.ILocationContextInfo;
import org.ambientdynamix.contextplugins.pedometer.IPedometerStepInfo;

import cz.zcu.kiv.runstat.db.DBHelper;
import cz.zcu.kiv.runstat.ui.BasicrunActivity.RServiceRequestReceiver;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * Service for getting position and steps
 */
public class DynamixService extends Service{
	
	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();

	private DynamixPlugin dx = null;

	private int runType = 0;
	private boolean firstCall = true;
	private boolean isFirstLocation = true;
	
	SharedPreferences sharedPref;
	private boolean settingUseWifiProvider;
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand");
    
		runType = intent.getIntExtra("runType", 0);
    
		super.onStartCommand(intent, flags, startId);       
		return START_NOT_STICKY;
	}

	
	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate()");
    
		dx = new DynamixPlugin();
    
		try {
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			
			bindService(new Intent(IDynamixFacade.class.getName()), dx.sConnection, Context.BIND_AUTO_CREATE);
			
		} catch (java.lang.SecurityException ex) {
			Log.e(TAG, "Error while binding Dynamix");
		}
		
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy()");
    
		//Clear all variables 
		try {
    	
			dx.dynamix.removeAllContextSupport();
			dx.dynamix.closeSession();		
			unbindService(dx.sConnection);
			dx.dynamix = null;
			dx = null;
		
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    
		super.onDestroy();
	} 


	
/*
 * Dynamix class used for calling framework functions
 */
public class DynamixPlugin {

	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
		
	public IDynamixFacade dynamix;
		
	//DBHelper instance used for saving data to DB
	private DBHelper db = new DBHelper(getApplicationContext());
	
	//Variables
	private int steps;
	private double stepForce;
	
	private double latitude;
	private double longtitude;
	private float speed;
	private float distance;
	
	private double prevLat;
	private double prevLng;
	
	private String provider;
	
	/*
	 * Constructor
	 */
	public DynamixPlugin(){
		
		this.steps = 0;
		this.stepForce = 0.0;
		this.latitude = 0.0;
		this.longtitude = 0.0;
		this.speed = 0;
		this.distance = 0;
		this.prevLat = 0.0;
		this.prevLng = 0.0;
		this.provider = "";
		
		if (dynamix == null) {
			Log.i(TAG, "Connecting to Dynamix...");
			
		} else {
			try {
				if (!dynamix.isSessionOpen()) {
					Log.i(TAG, "Dynamix connected... trying to open session");
					
					dynamix.openSession();
				} else
				{
					Log.i(TAG, "Session is already open");
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}
		
	}
    
	/*
	 * Called on location event
	 */
	private void locationEvent(ILocationContextInfo data){
		
		//First location is usually bad, so we wont save it in DB. This is caused by framework and can´t fix it.
		if(!isFirstLocation){
			settingUseWifiProvider = sharedPref.getBoolean("pref_key_usewifi", false);
		
			//Save location for calculating distance
			prevLat = latitude;
			prevLng = longtitude;
	     
			//get location
			latitude = data.getLatitude();
			longtitude = data.getLongitude();
	     
			//get current speed
			speed = data.getSpeed();

			//Calculate distance between two locations
			if(prevLat!=0.0 && prevLng!=0.0){
				Location prevLocation = new Location("");
				prevLocation.setLatitude(prevLat);
				prevLocation.setLongitude(prevLng);
				Location myLocation = new Location("");
				myLocation.setLatitude(latitude);
				myLocation.setLongitude(longtitude);
				distance += prevLocation.distanceTo(myLocation);
	    	 
			}else{
				distance = 0;
			}
	     
			provider = data.getProvider();	  
			
			//if wifi provider is enabled in settings or gps is available
			if((settingUseWifiProvider && provider.equals("network")) || provider.equals("gps")){
	    	 
	    	 
				//Save location to DB	    	 
				db.addToDatabase(Double.toString(latitude), Double.toString(longtitude), runType, steps, Float.toString(speed), Float.toString(distance), firstCall);
	    		 
				//Send data to UI
				broadcast();
	     
				//Lock run_id
				firstCall = false;
	    	 
				isFirstLocation = false;
			}
		}
		
		isFirstLocation = false;
	}
	
	/*
	 * Called on pedometer event
	 */
	private void pedometerEvent(IPedometerStepInfo data){
		
		settingUseWifiProvider = sharedPref.getBoolean("pref_key_usewifi", false);
		
		stepForce = data.getRmsStepForce();
		
		if(stepForce>=1.0)
			steps++;		
		
		Log.d(TAG, "Kroky:" + steps);
		
		if((settingUseWifiProvider && provider.equals("network")) || provider.equals("gps")){
			broadcast();
		}
	}
	
	
	/*
	 * Sends broadcast to Activity
	 */
	private void broadcast(){
		
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RServiceRequestReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        broadcastIntent.putExtra("DxSteps", this.steps);
        broadcastIntent.putExtra("DxLat", this.latitude);
        broadcastIntent.putExtra("DxLng", this.longtitude);
        broadcastIntent.putExtra("DxSpeed", this.speed);
        broadcastIntent.putExtra("DxDistance", this.distance);
        broadcastIntent.putExtra("DxProvider", this.provider);

        sendBroadcast(broadcastIntent);
	}
	
	
	/*
	 * Methods of dynamix framework, don't change!
	 */
	
	public ServiceConnection sConnection = new ServiceConnection() {
		/*
		 * Indicates that we've successfully connected to Dynamix. During this call, we transform the incoming IBinder
		 * into an instance of the IDynamixFacade, which is used to call Dynamix methods.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Add ourselves as a Dynamix listener
			try {
				Log.i(TAG, "Dynamix is connected!");
				
				// Create a Dynamix Facade using the incoming IBinder
				dynamix = IDynamixFacade.Stub.asInterface(service);
				// Create a Dynamix listener using the callback
				dynamix.addDynamixListener(dynamixCallback);
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}

		/*
		 * Indicates that a previously connected IDynamixFacade has been disconnected from Dynamix. This typically means
		 * that Dynamix has crashed or been shut down by Android to conserve resources. In this case,
		 * 'onServiceConnected' will be called again automatically once Dynamix boots again.
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Dynamix is disconnected!");
		
			dynamix = null;
		}
	};
	
	
	private IDynamixListener dynamixCallback = new IDynamixListener.Stub() {
		@Override
		public void onDynamixListenerAdded(String listenerId) throws RemoteException {
			Log.i(TAG, "onDynamixListenerAdded for listenerId: " + listenerId);
			// Open a Dynamix Session if it's not already opened
			if (dynamix != null) {
				if (!dynamix.isSessionOpen())
					dynamix.openSession();
				else
					registerForContextTypes();
			} else
			{
				Log.i(TAG, "dynamix already connected");
			}
		}

		@Override
		public void onDynamixListenerRemoved() throws RemoteException {
			Log.i(TAG, "onDynamixListenerRemoved");
		}

		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "onSessionOpened");
			registerForContextTypes();
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "onSessionClosed");

		}

		@Override
		public void onAwaitingSecurityAuthorization() throws RemoteException {
			Log.i(TAG, "onAwaitingSecurityAuthorization");
		}

		@Override
		public void onSecurityAuthorizationGranted() throws RemoteException {
			Log.i(TAG, "onSecurityAuthorizationGranted");
			registerForContextTypes();
		}

		@Override
		public void onSecurityAuthorizationRevoked() throws RemoteException {
			Log.w(TAG, "onSecurityAuthorizationRevoked");
		}

		@Override
		public void onContextSupportAdded(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "onContextSupportAdded for " + supportInfo.getContextType() + " using plugin "
					+ supportInfo.getPlugin() + " | id was: " + supportInfo.getSupportId());
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "onContextSupportRemoved for " + supportInfo.getSupportId());
		}

		@Override
		public void onContextTypeNotSupported(String contextType) throws RemoteException {
			Log.i(TAG, "onContextTypeNotSupported for " + contextType);
		}

		@Override
		public void onInstallingContextSupport(ContextPluginInformation plug, String contextType)
				throws RemoteException {
			Log.i(TAG, "onInstallingContextSupport: plugin = " + plug + " | Context Type = " + contextType);
		}

		@Override
		public void onContextPluginInstallProgress(ContextPluginInformation plug, int percentComplete)
				throws RemoteException {
			Log.i(TAG, "onContextPluginInstallProgress for " + plug + " with % " + percentComplete);
		}

		@Override
		public void onInstallingContextPlugin(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "onInstallingContextPlugin: plugin = " + plug);
		}

		@Override
		public void onContextPluginInstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "onContextPluginInstalled for " + plug);
			/*
			 * Automatically create context support for specific context types
			 */
			for (String type : plug.getSupportedContextTypes()) {
				dynamix.addContextSupport(dynamixCallback, type);
			}
		}

		@Override
		public void onContextPluginUninstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "onContextPluginUninstalled for " + plug);
		}

		@Override
		public void onContextPluginInstallFailed(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "onContextPluginInstallFailed for " + plug + " with message: " + message);
		}

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException {
			/*
			 * Log some information about the incoming event
			 */			
			
			// Check for native IContextInfo
			if (event.hasIContextInfo()) {
				Log.i(TAG, "Event contains native IContextInfo: " + event.getIContextInfo());
				IContextInfo nativeInfo = event.getIContextInfo();
				/*
				 * Note: At this point you can cast the IContextInfo into its native type and then call its methods. In
				 * order for this to work, you'll need to have the proper Java classes for the IContextInfo data types
				 * on your app's classpath. If you don't, event.hasIContextInfo() will return false and
				 * event.getIContextInfo() would return null, meaning that you'll need to rely on the string
				 * representation of the context info. To use native context data-types, simply download the data-types
				 * JAR for the Context Plug-in you're interested in, include the JAR(s) on your build path, and you'll
				 * have access to native context type objects instead of strings.
				 */
				Log.i(TAG, "IContextInfo implimentation class: " + nativeInfo.getImplementingClassname());
				// Example of using IPedometerStepInfo
				if (nativeInfo instanceof IPedometerStepInfo) {
					IPedometerStepInfo stepInfo = (IPedometerStepInfo) nativeInfo;
					Log.i(TAG, "Received IPedometerStepInfo with RmsStepForce: " + stepInfo.getRmsStepForce());

					pedometerEvent(stepInfo);
				}
				
				// Example of using Location
				if (nativeInfo instanceof ILocationContextInfo) {
			          ILocationContextInfo data = (ILocationContextInfo) nativeInfo;
			          Log.i(TAG, "Received ILocationContextInfo with location: " + data.getLatitude() + ":" + data.getLongitude());
			     
			    
			          locationEvent(data);
				}

			} else
				Log.i(TAG,"Event does NOT contain native IContextInfo... we need to rely on the string representation!");

		}

		@Override
		public void onContextRequestFailed(String requestId, String errorMessage, int errorCode) throws RemoteException {
			Log.w(TAG, "onContextRequestFailed for requestId " + requestId + " with error message: "
					+ errorMessage);
		}

		@Override
		public void onContextPluginDiscoveryStarted() throws RemoteException {
			Log.i(TAG, "onContextPluginDiscoveryStarted");
		}

		@Override
		public void onContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins)
				throws RemoteException {
			Log.i(TAG, "onContextPluginDiscoveryFinished");
		}

		@Override
		public void onDynamixFrameworkActive() throws RemoteException {
			Log.i(TAG, "onDynamixFrameworkActive");
		}

		@Override
		public void onDynamixFrameworkInactive() throws RemoteException {
			Log.i(TAG, "onDynamixFrameworkInactive");
		}

		@Override
		public void onContextPluginError(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "onContextPluginError for " + plug + " with message " + message);
		}

		@Override
		public void onContextPluginEnabled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "onContextPluginEnabled for " + plug);
		}

		@Override
		public void onContextPluginDisabled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "onContextPluginDisabled for " + plug);
		}

		@Override
		public void onContextSupportResult(ContextSupportResult result) throws RemoteException {
			Log.i(TAG,
					"onContextSupportResult for id " + result.getResponseId() + " with success "
							+ result.wasSuccessful());
		}
	};
	
	private void registerForContextTypes() throws RemoteException {
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.pedometer", "*"));
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.location", "*"));
	}
	
	private void logResult(Result result) {
		if (result.wasSuccessful()) {
			if (result instanceof IdResult) {
				Log.i(TAG, "Request was accepted by Dynamix: " + ((IdResult) result).getId());
			} else {
				Log.i(TAG, "Request was accepted by Dynamix");
			}
		} else {
			Log.w(TAG, "Request failed! Message: " + result.getMessage() + " | Error code: " + result.getErrorCode());
		}
	}
	
}

}