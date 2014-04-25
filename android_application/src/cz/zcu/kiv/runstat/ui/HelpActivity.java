package cz.zcu.kiv.runstat.ui;

import cz.zcu.kiv.runstat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelpActivity extends Activity {
	
	private final String TAG = this.getClass().getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		Log.i(TAG, "onCreate()");
		
		final Button btnBackHelp = (Button) findViewById(R.id.btnBackHelp);
		btnBackHelp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "Start");
					finish();										
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
	        // showHelp();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
