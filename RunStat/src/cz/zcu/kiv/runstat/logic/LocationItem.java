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

package cz.zcu.kiv.runstat.logic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class LocationItem {
	
		public String timeDate;
		public String locationDescription;
		
		public long id;
		public long runID;
		public int runType;
		public long time;
		public int steps;
		public float speed;
		public float avgSpeed;
		public float distance;
		public double lat;
		public double lng;
		public boolean synchronyzed;
		public String date;
				
		/*
		 * Constructor for HistoryActivity
		 */
		public LocationItem(Context ctx, int synced, long runID, int run_type, long startTime, long endTime, int steps, float avgSpeed, float maxSpeed, float distance, double lat, double lng){		
			
			this.runID = runID;
			this.runType = run_type;
			this.timeDate = convertToDateFormat(startTime);
			this.date = convertToDate(startTime);
			this.steps = steps;
			this.speed = Math.round(maxSpeed * 3.6);
			this.avgSpeed = Math.round(avgSpeed * 3.6);
			this.distance = Math.round(distance);			
			this.lat = lat;
			this.lng = lng;	

			this.locationDescription = getAddress(lat, lng, ctx);
			
			if(synced == 1)
				this.synchronyzed = true; 
			else
				this.synchronyzed = false;			
		}
		
		
		/*
		 * Constructor for DbSync
		 */
		public LocationItem(long id, long run_id, int run_type, long time, int steps, float speed, float distance, double lat, double lng){
			this.id = id;
			this.runID = run_id;
			this.runType = run_type;
			this.time = time;
			this.steps = steps;
			this.speed = speed;
			this.distance = distance;
			this.lat = lat;
			this.lng = lng;	
			this.timeDate = convertToDateFormatDB(time);
		}
		
		/*
		 * Constructor for Map
		 */
		public LocationItem(long id, long time, float speed, float distance, double lat, double lng){
			this.time = time;
			this.speed = (float)Math.round(speed * 36) / 10;
			this.distance = distance;
			this.lat = lat;
			this.lng = lng;	
			this.timeDate = convertToDateFormat(time);
		}
		
		/*
		 * Converts milliseconds to date time format (5:40, 09.kvì.2014)
		 */
		private String convertToDateFormat(long timeInMills){
			Locale locale = new Locale("cs", "CZ");
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd.MMM.yyyy", locale);
			
			Date resultdate = new Date(timeInMills);
			String time = ((sdf.format(resultdate)).toString());
			
			return time;
		}
		
		private String convertToDateFormatDB(long timeInMills){
			Locale locale = new Locale("cs", "CZ");
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss, dd.MM.yy", locale);
			
			Date resultdate = new Date(timeInMills);
			String time = ((sdf.format(resultdate)).toString());
			
			return time;
		}
		
		private String convertToDate(long timeInMills){
			Locale locale = new Locale("cs", "CZ");
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", locale);
			
			Date resultdate = new Date(timeInMills);
			String time = ((sdf.format(resultdate)).toString());
			
			return time;
		}

		
		public String getAddress(double lat, double lng, Context ctx){
			
			if(isOnline(ctx)){
    	    	Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
    	        // Get the current location from the input parameter list
    	        Location loc = new Location("");
    	        loc.setLatitude(lat);
    	        loc.setLongitude(lng);
    	        // Create a list to contain the result address
    	        List<Address> addresses = null;
    	        try {
    	            /*
    	             * Return 1 address.
    	             */
    	            addresses = geocoder.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
    	        } catch (IOException e1) {
    	        	
    	        Log.e("LocationSampleActivity","IO Exception in getFromLocation()");
    	        e1.printStackTrace();
    	        } catch (IllegalArgumentException e2) {
    	        // Error message to post in the log
    	        	
    	        String errorString = "Illegal arguments " + Double.toString(loc.getLatitude()) +" , " +
    	                Double.toString(loc.getLongitude()) + " passed to address service";
    	        
    	        Log.e("LocationSampleActivity", errorString);
    	        e2.printStackTrace();
    	        }
    	        // If the reverse geocode returned an address
    	        if (addresses != null && addresses.size() > 0) {
    	            // Get the first address
    	            Address address = addresses.get(0);
    
    	            return address.getSubAdminArea();
    	        } else {
    	        	return "...";
    	        }
			}
			else
			{
				return "...";
			}
		}
	
		/*
	     * Check if Internet connection is available
	     */
		public boolean isOnline(Context ctx) {
		    ConnectivityManager cm =(ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}
}


