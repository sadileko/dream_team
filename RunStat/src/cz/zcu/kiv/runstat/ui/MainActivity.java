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
 * Dream team, 2014/5/11  Tomáš Bouda
 *
 **********************************************************************************************************************/

package cz.zcu.kiv.runstat.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import cz.zcu.kiv.runstat.R;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity{
	
	
	private final String TAG = this.getClass().getSimpleName();
	
	private TextView txtMessageText;
	private ProgressBar pBarLogin;
	
	InputStream is = null;
	String result = "";
	String line = "";
	int code = 0;
	
	String nick = "";
	String password = "";
	
	/*
	 * Activity lifecycle
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Log.d(TAG, "onCreate()");
			
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		//already logged in
		if( sharedPref.getBoolean("logged", false) ){
			Intent intent = new Intent(MainActivity.this, MenuActivity.class); 
    		startActivityForResult(intent, 0);
		}
		
		
		final EditText txtLogin = (EditText) findViewById(R.id.txtLogin);
		final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
		txtMessageText = (TextView) findViewById(R.id.txtMessageText);
		pBarLogin = (ProgressBar) findViewById(R.id.pBarLogin);
		
		txtLogin.setText(sharedPref.getString("nick", ""));
		
		/*
		 * Buttons
		 */
		
		//Show context menu with running types
		final Button btnLogIn = (Button) findViewById(R.id.btnLogIn);
		btnLogIn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "LogIn");
					
					nick = txtLogin.getText().toString();
					password = txtPassword.getText().toString();
					
					if(!nick.equals("") && !password.equals("")){
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("nick", nick);
						editor.commit();
						
						if(isOnline()){
							Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
							pBarLogin.setVisibility(View.VISIBLE);
							new loginOperation().execute("");
						}
						else
						{
							Toast.makeText(getApplicationContext(), "Internet connection is not available!", Toast.LENGTH_SHORT).show();
						}
							
					}
					else
					{
						txtMessageText.setText("Incorect credentials!");
						Toast.makeText(getApplicationContext(), "Incorect credentials!", Toast.LENGTH_SHORT).show(); 
					}
				}
		});
		
		
		txtPassword.addTextChangedListener(new TextWatcher() {

	          public void afterTextChanged(Editable s) {

	            txtMessageText.setText("");

	          }

	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	          public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
	
	public boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
	
	/*
	 * AsyncTask for logging in
	 */
	private class loginOperation extends AsyncTask<String, Void, String> {

	    	private final String TAG = this.getClass().getSimpleName();

	    	private final String SERVERADDRESS = "http://runstat.hostuju.cz/login_mobile.php";
	    	
	        @Override
	        protected String doInBackground(String... params) {
	        	Log.i(TAG, "doInBackground()");
	        	
	        	password = getMD5EncryptedString(password);
	        	
	        	if(isOnline())
	        		login(nick, password);
	        	else
	        		Toast.makeText(getApplicationContext(), "Internet connection is not available!", Toast.LENGTH_SHORT).show();
	        	
	        	return "Executed";
	        }

	        @Override
	        protected void onPostExecute(String result) {
	        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = settings.edit();				
	        	if(code==1){
	        		Toast.makeText(getApplicationContext(), "Succesfull", Toast.LENGTH_SHORT).show(); 
	        		
	        		editor.putBoolean("logged", true);
					editor.commit();
	        		
	        		Intent intent = new Intent(MainActivity.this, MenuActivity.class); 
	        		startActivityForResult(intent, 0);	
	        	}else{
	        		txtMessageText.setText("Incorect credentials!");
	        		Toast.makeText(getApplicationContext(), "Incorect credentials!", Toast.LENGTH_SHORT).show(); 
	        		
	        		editor.putBoolean("logged", false);
					editor.commit();
	        	}
	        	
	        	pBarLogin.setVisibility(View.INVISIBLE);
	        }

	        @Override
	        protected void onPreExecute() {}

	        @Override
	        protected void onProgressUpdate(Void... values) {}
	    
	        public void login(String nick, String passwd)
	        {
	        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        	
	        	
	        	nameValuePairs.add(new BasicNameValuePair("nick", nick));
	        	nameValuePairs.add(new BasicNameValuePair("password", passwd));

	        	try
	        	{
	        		
	        		HttpClient httpclient = new DefaultHttpClient();
	    	        HttpPost httppost = new HttpPost(SERVERADDRESS);
	    	        
	    	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    	        HttpResponse response = httpclient.execute(httppost); 
	    	        HttpEntity entity = response.getEntity();
	    	        
	    	        is = entity.getContent();
	    	        Log.d(TAG, "Connection success, parsing result...");
	    	        
	        	}
	            catch(Exception e)
	            {
	            	Log.e(TAG, e.toString());
	            }     
	            
	            try
	            {
	                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	                StringBuilder sb = new StringBuilder();
	               
	                while ((line = reader.readLine()) != null)
	                {
	                    sb.append(line + "\n");
	                }
	                
	                is.close();
	                
	                result = sb.toString();
	                
	                Log.d(TAG, "parsing ok... ");
	                
	            }
	            catch(Exception e)
	            {
	                Log.e(TAG, e.toString());
	            }     
	           
	            try
	            {
	                JSONObject json_data = new JSONObject(result);
	                code=(json_data.getInt("code"));
	    			
	                if(code==1)
	                {
	                	Log.i(TAG, "succesfull");
	                }
	                else
	                {
	                	Log.e(TAG, "code=0");
	                }
	                
	            }
	            catch(Exception e)
	            {
	                Log.e(TAG, e.toString());

	            }
	        }
	        
	        
	        public String getMD5EncryptedString(String encTarget){
	            MessageDigest mdEnc = null;
	            try {
	                mdEnc = MessageDigest.getInstance("MD5");
	            } catch (NoSuchAlgorithmException e) {
	                System.out.println("Exception while encrypting to md5");
	                e.printStackTrace();
	            } // Encryption algorithm
	            mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
	            String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
	            while ( md5.length() < 32 ) {
	                md5 = "0"+md5;
	            }
	            return md5;
	        }
	}
	
	
}


