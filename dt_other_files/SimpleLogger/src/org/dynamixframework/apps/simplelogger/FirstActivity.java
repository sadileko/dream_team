/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dynamixframework.apps.simplelogger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.ambientdynamix.api.application.BundleContextInfo;
import org.ambientdynamix.api.application.ContextEvent;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.ContextSupportResult;
import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.application.IDynamixFacade;
import org.ambientdynamix.api.application.IDynamixListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.contextplugins.ambientsound.IAmbientSoundContextInfo;
import org.ambientdynamix.contextplugins.barcode.IBarcodeContextInfo;
import org.ambientdynamix.contextplugins.nfc.INfcTag;
import org.ambientdynamix.contextplugins.pedometer.IPedometerStepInfo;
import org.ambientdynamix.contextplugins.sampleplugin.ISamplePluginContextInfo;
import org.ambientdynamix.contextplugins.location.ILocationContextInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * A simple Dynamix project that demonstrates how to connect a standard Android application to the Dynamix Framework,
 * register as a Dynamix listener, call various Dynamix Framework methods, and react to incoming events. The application
 * automatically registers for all supported context types and logs events using the Android logger. In addition to
 * handling simple push-based context events, this application also demonstrates how to use more advanced pull-based
 * context scanning in both programmatic and interactive modes (provided the necessary context plug-ins are installed in
 * the local Dynamix Framework). This application also demonstrates how to manage two separate Activities, each with its
 * own IDynamixListener. Note 1: This application is intentionally simplified and does not provide robust error
 * handling, state management or thread safety. Note 2: This application uses android:configChanges="orientation" in the
 * AndroidManifest.xml to prevent the app from leaking its service connection during orientation changes.
 * 
 * @author Darren Carlson
 */
public class FirstActivity extends Activity {
	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	/*
	 * In this example, the 'FirstActivity' controls the Dynamix session. A Dynamix session can handle as many Dynamix
	 * listeners as needed, each with their own context support and events. Closing a session also removes all
	 * listeners.
	 */
	private IDynamixFacade dynamix;
	private boolean requestToggle;
	TextView activityLabel;
	
	private int krok = 0;
	private double stepForce = 0;
	String location;
	
	TextView forceLabel;
	TextView pedoLog;
	Button btnRefresh;		
	Activity mActivity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "ON CREATE for: Dynamix Simple Logger (A1)");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		activityLabel = (TextView) findViewById(R.id.activity_label);
		
        forceLabel = (TextView) findViewById(R.id.txtForce);
        
		activityLabel.setText("Dynamix Simple Logger (A1)");
		
		pedoLog = (TextView) findViewById(R.id.logText);
		mActivity = this;
		// Setup the connect button
		Button btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix == null) {
					/*
					 * Bind to the Dynamix service using the Activity's 'bindService' method, which completes
					 * asynchronously. As such, you must wait until the 'onServiceConnected' method of the
					 * ServiceConnection 'sConnection' implementation is called (see below) before calling Dynamix
					 * methods.
					 */
					bindService(new Intent(IDynamixFacade.class.getName()), sConnection, Context.BIND_AUTO_CREATE);
					Log.i(TAG, "A1 - Connecting to Dynamix...");
					
					activityLabel.setText("A1 - Connecting to Dynamix...");
				} else {
					try {
						if (!dynamix.isSessionOpen()) {
							Log.i(TAG, "Dynamix connected... trying to open session");
							dynamix.openSession();
						} else
							Log.i(TAG, "Session is already open");
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});

			
		// Setup the refresh button
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
				btnRefresh.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

					 pedoLog.setText("" + krok);
					 
					}
				});
				
		
				
		// Setup the disconnect button
		Button btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		btnDisconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						/*
						 * In this example, this Activity controls the session, so we call closeSession here. This will
						 * close the session for ALL of the application's IDynamixListeners.
						 */
						logResult(dynamix.closeSession());
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});
		// Setup the send cached context button
		Button btnGetCachedContext = (Button) findViewById(R.id.btnGetCachedContext);
		btnGetCachedContext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						logResult(dynamix.resendAllCachedContextEvents(dynamixCallback));
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		Button btnProgrammaticConfiguration = (Button) findViewById(R.id.btnProgrammaticConfiguration);
		btnProgrammaticConfiguration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "A1 - Requesting Programmatic Configuration");
				if (dynamix != null) {
					try {
						logResult(dynamix.openContextPluginConfigurationView(dynamixCallback,
								"org.ambientdynamix.contextplugins.sampleplugin"));
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		/*
		 * Setup the interactive context acquisition button. Note that this method only works if the
		 * 'org.ambientdynamix.contextplugins.barcode' plug-in is installed.
		 */
		Button btnInteractiveAcquisition = (Button) findViewById(R.id.btnInteractiveAcquisition);
		btnInteractiveAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						Log.i(TAG, "A1 - Requesting Interactive Context Acquisition");
						/*
						 * Launch interactive context acquisition. Note that you no longer need to use Intents; simply
						 * use the normal 'contextRequest' method.
						 */
						logResult(dynamix.contextRequest(dynamixCallback, "org.ambientdynamix.contextplugins.barcode",
								"org.ambientdynamix.contextplugins.barcode"));
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		/*
		 * Setup the interactive context acquisition button. Note that this method only works if the
		 * 'org.ambientdynamix.contextplugins.sampleplugin' plug-in is installed.
		 */
		Button btnProgrammaticAcquisition = (Button) findViewById(R.id.btnProgrammaticAcquisition);
		btnProgrammaticAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						Log.i(TAG, "A1 - Requesting Programmatic Context Acquisitions");
						// First, request the sample data using the org.ambientdynamix.contextplugins.sampleplugin
						if (requestToggle) {
							Log.i(TAG, "Requesting sample data with multiple context risk levels");
							logResult(dynamix.contextRequest(dynamixCallback,
									"org.ambientdynamix.contextplugins.sampleplugin",
									"org.ambientdynamix.contextplugins.sample.multi"));
						} else {
							Log.i(TAG, "Requesting sample data with a persistent request channel");
							logResult(dynamix.contextRequest(dynamixCallback,
									"org.ambientdynamix.contextplugins.sampleplugin",
									"org.ambientdynamix.contextplugins.sample.persistent"));
						}
						requestToggle = !requestToggle;
						// Next, request the user's sound level using org.ambientdynamix.contextplugins.ambientsound
						logResult(dynamix.contextRequest(dynamixCallback,
								"org.ambientdynamix.contextplugins.ambientsound",
								"org.ambientdynamix.contextplugins.ambientsound"));
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		Button btnToggleActivities = (Button) findViewById(R.id.btn_toggle_activities);
		btnToggleActivities.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
				// Use FLAG_ACTIVITY_CLEAR_TOP to ensure that we only have two Activities
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "ON DESTROY for: Dynamix Simple Logger (A1)");
		/*
		 * Always remove our listener and unbind so we don't leak our service connection
		 */
		if (dynamix != null) {
			try {
				dynamix.removeDynamixListener(dynamixCallback);
				unbindService(sConnection);
			} catch (RemoteException e) {
			}
		}
		super.onDestroy();
	}

	/*
	 * The ServiceConnection is used to receive callbacks from Android telling our application that it's been connected
	 * to Dynamix, or that it's been disconnected from Dynamix. These events come from Android, not Dynamix. Dynamix
	 * events are always sent to our IDynamixListener object (defined farther below), which is registered (in this case)
	 * in during the 'addDynamixListener' call in the 'onServiceConnected' method of the ServiceConnection.
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		/*
		 * Indicates that we've successfully connected to Dynamix. During this call, we transform the incoming IBinder
		 * into an instance of the IDynamixFacade, which is used to call Dynamix methods.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Add ourselves as a Dynamix listener
			try {
				Log.i(TAG, "A1 - Dynamix is connected!");
				activityLabel.setText("Dynamix is connected!");
				
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
			Log.i(TAG, "A1 - Dynamix is disconnected!");
			dynamix = null;
		}
	};
	/*
	 * Implementation of the IDynamixListener interface. For details on the IDynamixListener interface, see the Dynamix
	 * developer website.
	 */
	private IDynamixListener dynamixCallback = new IDynamixListener.Stub() {
		@Override
		public void onDynamixListenerAdded(String listenerId) throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerAdded for listenerId: " + listenerId);
			// Open a Dynamix Session if it's not already opened
			if (dynamix != null) {
				if (!dynamix.isSessionOpen())
					dynamix.openSession();
				else
					registerForContextTypes();
			} else
				Log.i(TAG, "dynamix already connected");
		}

		@Override
		public void onDynamixListenerRemoved() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixListenerRemoved");
		}

		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "A1 - onSessionOpened");
			registerForContextTypes();
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "A1 - onSessionClosed");
		}

		@Override
		public void onAwaitingSecurityAuthorization() throws RemoteException {
			Log.i(TAG, "A1 - onAwaitingSecurityAuthorization");
		}

		@Override
		public void onSecurityAuthorizationGranted() throws RemoteException {
			Log.i(TAG, "A1 - onSecurityAuthorizationGranted");
			registerForContextTypes();
		}

		@Override
		public void onSecurityAuthorizationRevoked() throws RemoteException {
			Log.w(TAG, "A1 - onSecurityAuthorizationRevoked");
		}

		@Override
		public void onContextSupportAdded(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "A1 - onContextSupportAdded for " + supportInfo.getContextType() + " using plugin "
					+ supportInfo.getPlugin() + " | id was: " + supportInfo.getSupportId());
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo supportInfo) throws RemoteException {
			Log.i(TAG, "A1 - onContextSupportRemoved for " + supportInfo.getSupportId());
		}

		@Override
		public void onContextTypeNotSupported(String contextType) throws RemoteException {
			Log.i(TAG, "A1 - onContextTypeNotSupported for " + contextType);
		}

		@Override
		public void onInstallingContextSupport(ContextPluginInformation plug, String contextType)
				throws RemoteException {
			Log.i(TAG, "A1 - onInstallingContextSupport: plugin = " + plug + " | Context Type = " + contextType);
		}

		@Override
		public void onContextPluginInstallProgress(ContextPluginInformation plug, int percentComplete)
				throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginInstallProgress for " + plug + " with % " + percentComplete);
		}

		@Override
		public void onInstallingContextPlugin(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onInstallingContextPlugin: plugin = " + plug);
		}

		@Override
		public void onContextPluginInstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginInstalled for " + plug);
			/*
			 * Automatically create context support for specific context types
			 */
			for (String type : plug.getSupportedContextTypes()) {
				dynamix.addContextSupport(dynamixCallback, type);
			}
		}

		@Override
		public void onContextPluginUninstalled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginUninstalled for " + plug);
		}

		@Override
		public void onContextPluginInstallFailed(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginInstallFailed for " + plug + " with message: " + message);
		}

		@Override
		public void onContextEvent(ContextEvent event) throws RemoteException {
			/*
			 * Log some information about the incoming event
			 */			
			Log.i(TAG, "A1 - onContextEvent received from plugin: " + event.getEventSource());
			Log.i(TAG, "A1 - -------------------");
			Log.i(TAG, "A1 - Event context type: " + event.getContextType());
			Log.i(TAG, "A1 - Event timestamp " + event.getTimeStamp().toLocaleString());
			if (event.expires())
				Log.i(TAG, "A1 - Event expires at " + event.getExpireTime().toLocaleString());
			else
				Log.i(TAG, "A1 - Event does not expire");
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
				Log.i(TAG, "A1 - Event contains native IContextInfo: " + event.getIContextInfo());
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
				Log.i(TAG, "A1 - IContextInfo implimentation class: " + nativeInfo.getImplementingClassname());
				// Example of using IPedometerStepInfo
				if (nativeInfo instanceof IPedometerStepInfo) {
					IPedometerStepInfo stepInfo = (IPedometerStepInfo) nativeInfo;
					Log.i(TAG, "A1 - Received IPedometerStepInfo with RmsStepForce: " + stepInfo.getRmsStepForce());
					
					stepForce = stepInfo.getRmsStepForce();
					
					krok += 1;					
					
				}
				
				// Example of using ISamplePluginContextInfo
				if (nativeInfo instanceof ISamplePluginContextInfo) {
					ISamplePluginContextInfo info = (ISamplePluginContextInfo) nativeInfo;
					Log.i(TAG, "A1 - Received ISamplePluginContextInfo with sample data: " + info.getSampleData());
				}
				// Example of using IAmbientSoundContextInfo
				if (nativeInfo instanceof IAmbientSoundContextInfo) {
					IAmbientSoundContextInfo info = (IAmbientSoundContextInfo) nativeInfo;
					Log.i(TAG, "A1 - Received IAmbientSoundContextInfo with dB value: " + info.getDbValue());
				}
				// Example of using INfcTag
				if (nativeInfo instanceof INfcTag) {
					INfcTag tagInfo = (INfcTag) nativeInfo;
					Log.i(TAG, "A1 - Received INfcTag with tag value: " + tagInfo.getTagIdAsString());
				}
				// Example of using IBarcodeContextInfo
				if (nativeInfo instanceof IBarcodeContextInfo) {
					IBarcodeContextInfo info = (IBarcodeContextInfo) nativeInfo;
					Log.i(TAG, "A1 - Received IBarcodeContextInfo with format  " + info.getBarcodeFormat()
							+ " and value " + info.getBarcodeValue());
				}
				// Example of using BundleContextInfo within the SampleContextPlugin
				if (event.getEventSource().getPluginId()
						.equalsIgnoreCase("org.ambientdynamix.contextplugins.sampleplugin")) {
					// Ensure the event contains a BundleContextInfo
					if (event.getIContextInfo() instanceof BundleContextInfo) {
						BundleContextInfo bundleInfo = (BundleContextInfo) event.getIContextInfo();
						for (String key : bundleInfo.getData().keySet()) {
							Log.i(TAG,
									"A1 - BundleContextInfo " + key + " contained data "
											+ bundleInfo.getData().get(key));
						}
					}
				}
				
				// Example of using Location
				if (nativeInfo instanceof ILocationContextInfo) {
			          ILocationContextInfo data = (ILocationContextInfo) nativeInfo;
			          Log.i(TAG, "Received ILocationContextInfo with location: " + data.getLatitude() + ":" + data.getLongitude());
			     location = "location: " + data.getLatitude() + ":" + data.getLongitude();
				}
				
				// Check for other interesting types, if needed...
			} else
				Log.i(TAG,
						"Event does NOT contain native IContextInfo... we need to rely on the string representation!");

		}

		@Override
		public void onContextRequestFailed(String requestId, String errorMessage, int errorCode) throws RemoteException {
			Log.w(TAG, "A1 - onContextRequestFailed for requestId " + requestId + " with error message: "
					+ errorMessage);
		}

		@Override
		public void onContextPluginDiscoveryStarted() throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginDiscoveryStarted");
		}

		@Override
		public void onContextPluginDiscoveryFinished(List<ContextPluginInformation> discoveredPlugins)
				throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginDiscoveryFinished");
		}

		@Override
		public void onDynamixFrameworkActive() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixFrameworkActive");
		}

		@Override
		public void onDynamixFrameworkInactive() throws RemoteException {
			Log.i(TAG, "A1 - onDynamixFrameworkInactive");
		}

		@Override
		public void onContextPluginError(ContextPluginInformation plug, String message) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginError for " + plug + " with message " + message);
		}

		@Override
		public void onContextPluginEnabled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginEnabled for " + plug);
		}

		@Override
		public void onContextPluginDisabled(ContextPluginInformation plug) throws RemoteException {
			Log.i(TAG, "A1 - onContextPluginDisabled for " + plug);
		}

		@Override
		public void onContextSupportResult(ContextSupportResult result) throws RemoteException {
			Log.i(TAG,
					"A1 - onContextSupportResult for id " + result.getResponseId() + " with success "
							+ result.wasSuccessful());
		}
	};

	/*
	 * Utility method that registers for the context types needed by this class. This method demonstrates the various
	 * ways of adding context support.
	 */
	private void registerForContextTypes() throws RemoteException {
		/*
		 * Method 1: Specifying a specific context type (without specifying a plug-in). In this case, the plug-in (or
		 * plug-ins) assigned to handle context type will be automatically selected by Dynamix.
		 */
		logResult(dynamix.addContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.ambientsound"));
		logResult(dynamix.addContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.barcode"));
		
		/*
		 * Method 2: Specifying a target plug-in and context type. In this case, Dynamix will only assign the specified
		 * plug-in to handle the context support.
		 */
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.nfc",
				"org.ambientdynamix.contextplugins.nfc.tag"));
		/*
		 * Method 3: Specifying a target plug-in and a wildcard ("*") context type. In this case, Dynamix will
		 * automatically assign all of the plug-in's supported context types.
		 */
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.sampleplugin",
				"*"));
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.pedometer", "*"));
		logResult(dynamix.addPluginContextSupport(dynamixCallback, "org.ambientdynamix.contextplugins.location", "*"));
	}
	
	/*
	 * Utility method that outputs the result of Dynamix method calls.
	 */
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

