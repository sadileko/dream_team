package cz.zcu.kiv.runstat.ui;

import org.ambientdynamix.api.application.IDynamixFacade;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.data.DynamixPlugin;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	// Classname for logging purposes
	//private final String TAG = this.getClass().getSimpleName();
	
	//instance of dynamix plugin
	public final DynamixPlugin dx = new DynamixPlugin();
		
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
		
		bindService(new Intent(IDynamixFacade.class.getName()), dx.sConnection, Context.BIND_AUTO_CREATE);		
		
		myHandler = new Handler();
		myHandler.post(stepsUpdate);

		
		txtLog = (EditText) findViewById(R.id.txtLog);
		txtLat = (TextView) findViewById(R.id.txtLat);
		txtLong = (TextView) findViewById(R.id.txtLong);
		txtSteps = (TextView) findViewById(R.id.txtSteps);
		
		txtLog.setEnabled(false);		

		
		//********Buttons**********		
		
		final Button btnRefreshGps = (Button) findViewById(R.id.btnRefreshGps);
			btnRefreshGps.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						txtLat.setText(Double.toString(dx.latitude));
						txtLong.setText(Double.toString(dx.longtitude));
				}
		});
			
			
		final Button btnRefreshSteps = (Button) findViewById(R.id.btnRefreshSteps);
			btnRefreshSteps.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						txtSteps.setText("" + dx.steps);
						
				}
		});			
			
	}
	
	//Refreshing textViews
	private Runnable stepsUpdate = new Runnable() {
		   public void run() {
			   
			   txtLat.setText(Double.toString(dx.latitude));
			   txtLong.setText(Double.toString(dx.longtitude));
			   
		       txtSteps.setText("" + dx.steps);
		       
		       if(dx.log != dx.cache){
		    	   txtLog.append(dx.log+"\n");
		    	   dx.cache = dx.log;
		       }
		       
		       myHandler.postDelayed(this, 250);

		    }
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		unbindService(dx.sConnection);
	}
		
}
