package com.example.runstat;

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

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	
	private IDynamixFacade dynamix;
	
	//Variables
	private int steps = 0;
	private double stepForce = 0.0;
	private double latitude = 0.0;
	private double longtitude = 0.0;
	private String log;
	private String cache;
	
	//Widgets
	private Handler myHandler;
	
	private EditText txtLog;
	private TextView txtLat;
	private TextView txtLong;
	private TextView txtSteps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		myHandler = new Handler();
		myHandler.post(stepsUpdate);

		
		txtLog = (EditText) findViewById(R.id.txtLog);
		txtLat = (TextView) findViewById(R.id.txtLat);
		txtLong = (TextView) findViewById(R.id.txtLong);
		txtSteps = (TextView) findViewById(R.id.txtSteps);
		
		txtLog.setEnabled(false);
		
		if (dynamix == null) {
			/*
			 * Bind to the Dynamix service using the Activity's 'bindService' method, which completes
			 * asynchronously. As such, you must wait until the 'onServiceConnected' method of the
			 * ServiceConnection 'sConnection' implementation is called (see below) before calling Dynamix
			 * methods.
			 */
			bindService(new Intent(IDynamixFacade.class.getName()), sConnection, Context.BIND_AUTO_CREATE);
			Log.i(TAG, "Connecting to Dynamix...");
			txtLog.append("Connecting to Dynamix...\n");
			
		} else {
			try {
				if (!dynamix.isSessionOpen()) {
					Log.i(TAG, "Dynamix connected... trying to open session");
					txtLog.append("Dynamix connected... trying to open session\n");
					
					dynamix.openSession();
				} else
				{
					Log.i(TAG, "Session is already open");
					txtLog.append("Session is already open\n");
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
				txtLog.append(e.toString() +"\n");
			}
		}
		
		
		//********Buttons**********
		
		final Button btnRefreshGps = (Button) findViewById(R.id.btnRefreshGps);
			btnRefreshGps.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						txtLat.setText(Double.toString(latitude));
						txtLong.setText(Double.toString(longtitude));
				}
		});
			
			
		final Button btnRefreshSteps = (Button) findViewById(R.id.btnRefreshSteps);
			btnRefreshSteps.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						txtSteps.setText("" + steps);
						
				}
		});
		
			
			
	}
	
	private Runnable stepsUpdate = new Runnable() {
		   public void run() {
			   
			   txtLat.setText(Double.toString(latitude));
			   txtLong.setText(Double.toString(longtitude));
			   
		       txtSteps.setText("" + steps);
		       
		       if(log != cache){
		    	   txtLog.append(log+"\n");
		    	   cache = log;
		       }
		       
		       myHandler.postDelayed(this, 500);

		    }
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	private ServiceConnection sConnection = new ServiceConnection() {
		/*
		 * Indicates that we've successfully connected to Dynamix. During this call, we transform the incoming IBinder
		 * into an instance of the IDynamixFacade, which is used to call Dynamix methods.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Add ourselves as a Dynamix listener
			try {
				Log.i(TAG, "Dynamix is connected!");
				log = "Dynamix is connected!";
				
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
			log = "Dynamix is disconnected!";
			
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
				txtLog.append("dynamix already connected\n");
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
			txtLog.append("onSessionClosed\n");
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
			Log.i(TAG, "onContextEvent received from plugin: " + event.getEventSource());
			log = "onContextEvent received from plugin: " + event.getEventSource();
			
			Log.i(TAG, "-------------------");
			Log.i(TAG, "Event context type: " + event.getContextType());
			Log.i(TAG, "Event timestamp " + event.getTimeStamp().toLocaleString());
			if (event.expires())
				Log.i(TAG, "Event expires at " + event.getExpireTime().toLocaleString());
			else
				Log.i(TAG, "Event does not expire");
			/*
			 * To illustrate how string-based context representations are accessed, we log each contained in the event.
			 */
			for (String format : event.getStringRepresentationFormats()) {
				Log.i(TAG,
						"Event string-based format: " + format + " contained data: "
								+ event.getStringRepresentation(format));
			}
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
					log = "Received IPedometerStepInfo with RmsStepForce: " + stepInfo.getRmsStepForce();
					
					stepForce = stepInfo.getRmsStepForce();
					
					if(stepForce>=1.0)
						steps++;					
					
				}
				
				// Example of using Location
				if (nativeInfo instanceof ILocationContextInfo) {
			          ILocationContextInfo data = (ILocationContextInfo) nativeInfo;
			          Log.i(TAG, "Received ILocationContextInfo with location: " + data.getLatitude() + ":" + data.getLongitude());
			         log = "Received ILocationContextInfo with location: " + data.getLatitude() + ":" + data.getLongitude();
			          
			     latitude = data.getLatitude();
			     longtitude = data.getLongitude();
				}
				
				// Check for other interesting types, if needed...
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
