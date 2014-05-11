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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.db.DBHelper;
import cz.zcu.kiv.runstat.logic.LocationItem;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;


public class MapActivity extends Activity {

	// Classname for logging purposes
	private final String TAG = this.getClass().getSimpleName();
	
	private Locale locale = new Locale("cs", "CZ");
	
	GoogleMap map;
	DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG,"onCreate()");
        
        //Get runID form given intent
        Intent i = this.getIntent();
        long runID = i.getLongExtra("runID", 1);
        
        dbh = new DBHelper(this);
        
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        
        //Load locations with specified run_id from DB       
        List<LocationItem> locations = dbh.getLocationsByRunId(runID);
              
    	setMarkers(locations, 0);
    	
    	dbh.close();
    }
    
    
    /*
     * Pin given locations to map
     */
	protected void setMarkers(List<LocationItem> locations, long idPoint){
		PolylineOptions po = new PolylineOptions().color(Color.argb(125,14,4,161)).geodesic(true);
		
		for(int i=0; i<locations.size();i++){
			long id = locations.get(i).id;
			long timeMills = locations.get(i).time;
			float speed =locations.get(i).speed;
			float distance = locations.get(i).distance;
			double latitude = locations.get(i).lat;
			double longitude = locations.get(i).lng;

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd.MMM.yyyy", locale);
			
			Date resultdate = new Date(timeMills);
			String timeDate = ((sdf.format(resultdate)).toString());
			
			if(i == 0 || i == (locations.size()-1)){
				distance = Math.round(distance);
				addMarker(new LatLng(latitude, longitude), timeDate, speed, 270, distance);				
			}			
			
			if(idPoint==0||idPoint==id){
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(latitude, longitude), 16));
			}
			
			po.add(new LatLng(latitude, longitude));
		}

		map.addPolyline(po);
	}
	
	
	/*
	 * Add marker to map
	 */
	protected void addMarker(LatLng position, String time, float speed, int color, float distance){
		if(distance!=0){
			map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
			.title(time)
			.snippet("Distance: "+ distance + "m")
			.position(position));
		}
		else{
			map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
			.title(time)
			.snippet("Speed: " + Float.toString(speed)+"km/h")
			.position(position));
		}
	}
}

