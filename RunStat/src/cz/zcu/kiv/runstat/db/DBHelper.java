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

package cz.zcu.kiv.runstat.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.runstat.logic.LocationItem;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {
	
	private final String TAG = this.getClass().getSimpleName();
	
	private static final String DATABASE_NAME = "locationDB";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "location";

	// Columns names
	public static final String DATE = "date";
	public static final String LONGITUDE = "longtitude";
	public static final String LATITUDE = "latitude";
	public static final String STEPS = "steps";
	public static final String SPEED = "speed";
	public static final String DISTANCE = "distance";
	public static final String TYPE = "type";			//Runtype, 0 - basic, 1 - distance, 2 - time
	public static final String RUN_ID = "run_id";	//Id of running
	public static final String SYNC = "sync";		//marks if the record is on server
	public static final String TIME = "time";
	public static final String NICK = "nick";
	
	/*
	 * Columns position
	 */
	private final int ID = 			0;
	private final int _RUNID = 		1;
	private final int _RUNTYPE = 	2;
	private final int _SYNC = 		3;
	private final int _DATE = 		4;
	private final int _STEPS = 		5;
	private final int _SPEED = 		6;
	private final int _DISTANCE =	7;
	private final int _LAT = 		8;
	private final int _LNG = 		9;
	private final int _TIME=		10;
	private final int _NICK =		11;
	
	private long lastRowID = 1;
	private Context ctx;
	SharedPreferences sharedPref;
	
	private long startTime = 0;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.ctx = context;
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + RUN_ID + " integer," + TYPE + " integer,"+ SYNC + " integer," + DATE + " integer, "
				+ STEPS + " integer, "+ SPEED +" real,"+ DISTANCE+ " real," + LATITUDE + " real, " + LONGITUDE + " real,"+ TIME + " integer," +NICK+ " text);";
		
		db.execSQL(sql);
		Log.i(TAG, "Created DB: "+sql);
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG,"onUpgrade()-old: "+oldVersion+",new: "+newVersion);
		
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
	}
	
	
	/*
	 * Add location to DB
	 */
	public void addToDatabase(double lat, double lng, int type, int steps, float speed, float distance, boolean firstCall) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
			
		sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		if(firstCall){
			lastRowID = sharedPref.getLong("run_id", 1) + 1;
			startTime = System.currentTimeMillis();
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("run_id", lastRowID);
			editor.commit();
		}
		
		values.put(RUN_ID, lastRowID);
		values.put(TYPE, type);
		values.put(SYNC, 0);
		values.put(DATE, System.currentTimeMillis());
		values.put(STEPS, steps);
		values.put(SPEED, speed);
		values.put(DISTANCE, distance);
		values.put(LATITUDE, lat);
		values.put(LONGITUDE, lng);
	
		if(firstCall)
			values.put(TIME, 0);
		else
			values.put(TIME, System.currentTimeMillis()-startTime);
		
		
		
		values.put(NICK, sharedPref.getString("nick", ""));
		
		db.insert(TABLE, null, values);
		
		Log.v(TAG, "add data: "+lat+":"+lng);
	}
	
	
	/*
	 * Get last row runID
	 */
	public long getLastRowId(){	
		
		String selectQuery = "SELECT MAX(run_id) FROM " + TABLE;
		 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        long lastRow = 1;
        
        if( cursor.getCount() > 0 ){
        	cursor.moveToLast();
        	lastRow = cursor.getLong(0);
        }
        
        Log.d(TAG,"lastrow: "+lastRow);
        
        return lastRow;
	}
	
	
	/*
	 * Get locations by given runId
	 */
	public List<LocationItem> getLocationsByRunId(long runID){
		String selectQuery = "SELECT  * FROM " + TABLE + " WHERE run_id="+runID;
		
		List<LocationItem> locations = new ArrayList<LocationItem>();
		
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        while (cursor.moveToNext()) {	

			locations.add(new LocationItem(
					cursor.getLong(ID),
					cursor.getLong(_DATE), 
					cursor.getFloat(_SPEED),
					cursor.getFloat(_DISTANCE),
					cursor.getDouble(_LAT),
					cursor.getDouble(_LNG)
					));
						
		}
        
        db.close();
        
        return locations;
	}
	
	
	/*
	 * Get all running events for history
	 */
	public List<LocationItem> getRunningEvents() throws IOException{
			removeSingleMarkerLocations();
		
			List<LocationItem> locationsList = new ArrayList<LocationItem>();
	        String selectQuery = "SELECT MIN(sync), run_id, type, MIN(date), MAX(date), MAX(steps), AVG(speed), MAX(speed), MAX(distance), latitude, longtitude, MAX(time) FROM "+TABLE+" GROUP BY run_id;";
	 
	        SQLiteDatabase db = this.getWritableDatabase();
	        Cursor cursor = db.rawQuery(selectQuery, null);
	        	                
	        if (cursor.moveToFirst()) {
	            do {
	            	locationsList.add( new LocationItem(
	            			ctx,
	            			cursor.getInt(0),
	            			cursor.getLong(1),
	            			cursor.getInt(2),
	            			cursor.getLong(3),
	            			cursor.getLong(4),
	            			cursor.getInt(5),
	            			cursor.getFloat(6),
	            			cursor.getFloat(7),
	            			cursor.getFloat(8),
	            			cursor.getDouble(9),
	            			cursor.getDouble(10),
	            			cursor.getLong(11)
	            			));	        
	            } while (cursor.moveToNext());
	        }
	        
	        db.close();
	        
	        return locationsList;
	}
	
	
	/*
	 * Get locations as list of LocationItems for DbSync
	 */
	public List<LocationItem> getAllLocationsAsList(){
		removeSingleMarkerLocations();	//clean up DB before synchronize
		
		List<LocationItem> locationsList = new ArrayList<LocationItem>();
        String selectQuery = "SELECT  * FROM " + TABLE + " WHERE sync=0";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
	
        if (cursor.moveToFirst()) {
            do {
            	
            	locationsList.add(new LocationItem(
            			cursor.getLong(ID),
            			cursor.getLong(_RUNID),
            			cursor.getInt(_RUNTYPE),
            			cursor.getLong(_DATE),
            			cursor.getInt(_STEPS),
            			cursor.getFloat(_SPEED),
            			cursor.getFloat(_DISTANCE),
            			cursor.getDouble(_LAT),
            			cursor.getDouble(_LNG),
            			cursor.getLong(_TIME),
            			cursor.getString(_NICK)
            			));
            	
            } while (cursor.moveToNext());
        }
        
        db.close();
        
        return locationsList;
	}
	
	/*
	 * Mark location in DB as synchronized
	 */
	public void markAsSynchronized(long id){
		SQLiteDatabase db = this.getWritableDatabase();
		
		String updateQuery = "UPDATE "+TABLE+" SET sync='1' WHERE sync='0' AND _id='"+id+"'";
        db.execSQL(updateQuery);
        
        db.close();
	}
	
	
	/*
	 * Returns all locations from DB as list of strings - for logging
	 */
	public List<String> getAllLocations() {
        List<String> locationList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	String location = "";
            	
            	for(int i=0;i<12;i++)
            		location += cursor.getString(i)+"|";

            	locationList.add(location);
            } while (cursor.moveToNext());
        }
 
        db.close();

        return locationList;
    }
	
	
	
	/*
	 * Removes all locations in DB
	 */
	public void removeAllLocations(){
		SQLiteDatabase db = this.getWritableDatabase();
	    db.delete(TABLE, null, null);
	    
	    db.close();
	}
	
	
	/*
	 * Removes locations by given RunId
	 */
	public void removeLocationByRunID(long runID){
		String deleteQuery = "DELETE FROM "+ TABLE + " WHERE run_id=" + runID;
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL(deleteQuery);
		Log.i(TAG, "RunId: " + runID + " deleted succesfully.");
		
		db.close();
	}
	
	
	/*
	 * Removes locations where is only one marker
	 */
	public void removeSingleMarkerLocations(){
		String selectQuery = "SELECT run_id FROM "+TABLE+" GROUP BY run_id HAVING COUNT(*)=1";
		String deleteQuery = "DELETE FROM "+TABLE+" WHERE "; 

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if(cursor.getCount()>0){
        	
        	if (cursor.moveToFirst()) {
        	   do {
        		   if(cursor.isLast()){
        			   deleteQuery += "run_id='" + cursor.getLong(0) + "'";
        		   }else{
        			   deleteQuery += "run_id='" + cursor.getLong(0) + "' OR ";
        		   }
        	   } while (cursor.moveToNext());
        	}
        	
        	db.execSQL(deleteQuery);
        }
  
        db.close();
	}
	
	
	/*
	 * Removes locations where distance is less than 1m
	 */
	public void removeZeroDistanceLocations(){
		String selectQuery = "SELECT run_id, MAX(distance) FROM "+TABLE+" GROUP BY run_id";
		String deleteQuery = "DELETE FROM "+TABLE+" WHERE "; 
		
		SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        boolean executable = false;
        
		if(cursor.getCount()>0){
        	
        	if (cursor.moveToFirst()) {
        	   do {
        		   
        		   if(cursor.getFloat(1) < 1){
        			   
        			   if(cursor.isLast()){
        				   deleteQuery += "run_id='" + cursor.getLong(0) + "'";
        			   }else{
        				   deleteQuery += "run_id='" + cursor.getLong(0) + "' OR ";
        			   }
        			   
        			   executable = true;
        		   }
        		   
        	   } while (cursor.moveToNext());
        	}
        	
        	if(executable)
        		db.execSQL(deleteQuery);
        }
		
		db.close();
	}

}
