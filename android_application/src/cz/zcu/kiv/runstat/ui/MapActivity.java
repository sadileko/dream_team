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

package cz.zcu.kiv.runstat.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.data.DBHelper;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class MapActivity extends Activity {

	GoogleMap map;
	DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        dbh = new DBHelper(this);
        
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    	Cursor cursor = getLocations();

    	setMarkers(cursor, 0);
    	
    	cursor.close();
    	dbh.close();
    }
    
    private Cursor getLocations() {
    	SQLiteDatabase db = dbh.getReadableDatabase();
    	Cursor cursor = db.query(DBHelper.TABLE, null, null, null, null, null, null);

    	startManagingCursor(cursor);
    	return cursor;
    	
    }
    
	protected void setMarkers(Cursor cursor, long idPoint){
		PolylineOptions po = new PolylineOptions().color(Color.argb(125,14,4,161)).geodesic(true);
		
		long rows = cursor.getCount();
		
		while (cursor.moveToNext()) {

			long id = cursor.getLong(0);
			long timeMils = cursor.getLong(1);
			int steps = cursor.getInt(2);
			
			float speed = cursor.getFloat(3);
			float roundedSpeed = (float)Math.round(speed * 36) / 10;
			
			float distance = cursor.getFloat(4);
			double latitude = cursor.getDouble(5);
			double longitude = cursor.getDouble(6);	
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd.MMM.yyyy");
			
			Date resultdate = new Date(timeMils);
			String time = ((sdf.format(resultdate)).toString());

			long position = cursor.getPosition();
			
			if(position == 0 || position == (rows-1)){
				distance = Math.round(distance);
				addMarker(new LatLng(latitude, longitude), time, roundedSpeed, 270, distance);				
			}
			
			po.add(new LatLng(latitude, longitude));
			
			if(idPoint==0||idPoint==id){
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(latitude, longitude), 16));
			}
			
		}
		map.addPolyline(po);
	}
	
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

