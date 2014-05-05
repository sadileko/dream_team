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
        	Log.i(TAG, "execute");
        	
        	List<LocationItem> locations = dbh.getAllLocationsAsList();
        	
        	pBarSync.setMax( locations.size()*3 );
        	
        	for(int i =0; i<locations.size(); i++){
        		insert(
        				locations.get(i).runID,
        				locations.get(i).runType,
        				locations.get(i).locationTime,
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

        
        public void insert(long run_id, int run_type, String time, int steps, float speed, float distance, double lat, double lng)
        {
        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
     
        	String strRun_id = String.valueOf(run_id);
        	String strRun_type = String.valueOf(run_type);
        	//String strTime = String.valueOf(time);
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
        	nameValuePairs.add(new BasicNameValuePair("lng", strLat));
        	nameValuePairs.add(new BasicNameValuePair("lat", strLng));

        	try
        	{
        		
        		HttpClient httpclient = new DefaultHttpClient();
    	        HttpPost httppost = new HttpPost("http://tomasbouda.cz/zswi/dbHandler/insert.php");
    	        
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
            }
        }
        
        private void progressUp(){
        	pBarSync.setProgress(pBarSync.getProgress() + 1);
        }
        
        /*
        public void postData() {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://tomasbouda.cz/zswi/dbHandler/insert.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("lat", lat));
                nameValuePairs.add(new BasicNameValuePair("lng", lng));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                Log.e("Async", e.toString());
            }
        } */
    }
    
}