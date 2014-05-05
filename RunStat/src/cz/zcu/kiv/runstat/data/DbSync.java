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

package cz.zcu.kiv.runstat.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
 
public class DbSync extends Activity {

	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	
	DBHelper dbh;
	
	ProgressBar pBarSync;
	
	InputStream is = null;
	String result = null;
	String line = null;
	int code;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbsync);
        Log.d(TAG, "onCreate()");
        
        dbh = new DBHelper(getApplicationContext());
        
        Button insert=(Button) findViewById(R.id.btnSync);
        
        pBarSync = (ProgressBar) findViewById(R.id.pBarSync);
        
        insert.setOnClickListener(new View.OnClickListener() {
			
		@Override
		public void onClick(View v) {
			
			new postOperation().execute("");

		}
	});
        
}
    
    
private class postOperation extends AsyncTask<String, Void, String> {

    	private final String TAG = this.getClass().getSimpleName();
    	
        @Override
        protected String doInBackground(String... params) {
        	Log.i(TAG, "doInBackground()");
        	
        	List<LocationItem> locations = dbh.getAllLocationsAsList();
        	
        	pBarSync.setMax( locations.size()*3 );
        	
        	for(int i =0; i<locations.size(); i++){
        		insert(
        				locations.get(i).runID,
        				locations.get(i).runType,
        				locations.get(i).timeDate,
        				locations.get(i).steps,
        				locations.get(i).speed,
        				locations.get(i).distance,
        				locations.get(i).lat,
        				locations.get(i).lng
        				);
        	}
        	
        	return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        	Toast.makeText(getApplicationContext(), String.valueOf(pBarSync.getMax()/3) + " rows was succesfully inserted into MySql DB", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        /*
         * Insert location to server over POST request
         */
        public void insert(long run_id, int run_type, String time, int steps, float speed, float distance, double lat, double lng)
        {
        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     
        	String strRun_id = String.valueOf(run_id);
        	String strRun_type = String.valueOf(run_type);
        	String strSteps = String.valueOf(steps);
        	String strSpeed = String.valueOf(speed);
        	String strDistance = String.valueOf(distance);
        	String strLat = String.valueOf(lat);
        	String strLng = String.valueOf(lng);
        	
        	nameValuePairs.add(new BasicNameValuePair("run_id", strRun_id));
        	nameValuePairs.add(new BasicNameValuePair("run_type", strRun_type));
        	nameValuePairs.add(new BasicNameValuePair("time", time));
        	nameValuePairs.add(new BasicNameValuePair("steps", strSteps));
        	nameValuePairs.add(new BasicNameValuePair("speed", strSpeed));
        	nameValuePairs.add(new BasicNameValuePair("distance", strDistance));
        	nameValuePairs.add(new BasicNameValuePair("lat", strLat));
        	nameValuePairs.add(new BasicNameValuePair("lng", strLng));

        	try
        	{
        		
        		HttpClient httpclient = new DefaultHttpClient();
    	        HttpPost httppost = new HttpPost("http://runstat.hostuju.cz/insert.php");
    	        
    	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    	        HttpResponse response = httpclient.execute(httppost); 
    	        HttpEntity entity = response.getEntity();
    	        
    	        is = entity.getContent();
    	        Log.d(TAG, "Connection success, parsing result...");
    	        
    	        progressUp();
    	        
        	}
            catch(Exception e)
            {
            	Log.e(TAG, e.toString());
            	progressUp();
            	pBarSync.setBackgroundColor(Color.RED);
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
                
                progressUp();
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString());
                progressUp();
            	pBarSync.setBackgroundColor(Color.RED);
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
                
                progressUp();
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString());
                progressUp();
            	pBarSync.setBackgroundColor(Color.RED);
            }
        }
        
        /*
         * Increment progressbar
         */
        private void progressUp(){
        	pBarSync.setProgress(pBarSync.getProgress() + 1);
        }
        
    }
    
}