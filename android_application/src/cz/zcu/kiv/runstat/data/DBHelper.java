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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {
	
	private final String TAG = this.getClass().getSimpleName();
	
	private static final String DATABASE_NAME = "locationDB";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "location";

	// Columns
	public static final String TIME = "time";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	public static final String STEPS = "steps";
	public static final String SPEED = "speed";
	public static final String DISTANCE = "distance";
	public static final String TYPE = "type";			//Typ bìhu, 0 - zakladni, 1 - distancni, 2 - casovy
	public static final String RUN_ID = "run_id";	//Id bìhu
	
	private long lastRowID = 1;
	private Context ctx;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.ctx = context;
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + RUN_ID + " integer," + TYPE + " integer," + TIME + " integer, "
				+ STEPS + " integer, "+ SPEED +" text,"+ DISTANCE+ " text," + LATITUDE + " text, " + LONGITUDE + " text);";
		
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
	public void addToDatabase(String lat, String lng, int type, int steps, String speed, String distance, boolean firstCall) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
				
		if(firstCall){
			lastRowID = getLastRowId() + 1; 
		}
		
		values.put(RUN_ID, lastRowID);
		values.put(TYPE, type);
		values.put(TIME, System.currentTimeMillis());
		values.put(STEPS, steps);
		values.put(SPEED, speed);
		values.put(DISTANCE, distance);
		values.put(LATITUDE, lat);
		values.put(LONGITUDE, lng);
		
		
		db.insert(TABLE, null, values);
		
		Log.v(TAG, "add data: "+lat+":"+lng);
	}
	
	
	/*
	 * Get last row runID
	 */
	public long getLastRowId(){
		
		
		String selectQuery = "SELECT  * FROM " + TABLE;
		 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        long lastRow = 0;
        if(cursor.getCount()>0){
        	cursor.moveToLast();
        	lastRow = cursor.getInt(1);
        }
        
        return lastRow;
	}
	
	
	/*
	 * Get locations by given runId
	 */
	public Cursor getLocationsByRunId(long runID){
		String selectQuery = "SELECT  * FROM " + TABLE + " WHERE run_id="+runID;
		 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        return cursor;
	}
	
	
	/*
	 * Get all running events
	 */
	public List<LocationItem> getRunningEvents() throws IOException{
			//removeSingleMarkerLocations();
		
			List<LocationItem> locationsList = new ArrayList<LocationItem>();
	        String selectQuery = "SELECT  * FROM " + TABLE;
	 
	        SQLiteDatabase db = this.getWritableDatabase();
	        Cursor cursor = db.rawQuery(selectQuery, null);
	        
	        long runID = 0;
	        long temp = 0;
	                
	        if (cursor.moveToFirst()) {
	            do {
	            	runID = cursor.getLong(1);
	            	
	            	if(runID != temp || cursor.getPosition() == 0){
	            		locationsList.add(new LocationItem(cursor.getLong(3), "", runID, cursor.getDouble(7), cursor.getDouble(8), ctx));
	            	}
	            	
	            	temp = runID;
	            	
	            } while (cursor.moveToNext());
	        }
	        
	        return locationsList;
	}
	
	
	/*
	 * Returns all locations from DB as list of strings
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
            	
            	for(int i=0;i<9;i++)
            		location += cursor.getString(i)+"|";

            	locationList.add(location);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return locationList;
    }
	
	
	
	/*
	 * Removes all locations in DB
	 */
	public void removeAllLocations(){
		SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
	    db.delete(TABLE, null, null);
	}
	
	
	/*
	 * Removes locations by given RunId
	 */
	public void removeLocationByRunID(long runID){
		String deleteQuery = "DELETE FROM "+ TABLE + " WHERE run_id=" + runID;
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL(deleteQuery);
		Log.i(TAG, "RunId: " + runID + " deleted succesfully.");
	}
	
	
	/*
	 * Removes locations where is only one/two markers
	 */
	public void removeSingleMarkerLocations(){
		String selectQuery = "SELECT  * FROM " + TABLE;
		 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        List<location> locList = new ArrayList<location>();
        long runIDprev = 0;
        long runID = 0;
        long count = 0;
        
        if (cursor.moveToFirst()) {
            do {
            	runID = cursor.getLong(1);
            	
            	if(runID!=runIDprev){
            		location loc = new location(runID,count);
            		locList.add(loc);
            		count = 1;
            	}
            	else{
            		count++;
            	}
            	
            	runIDprev = runID;
            	
            } while (cursor.moveToNext());
        }
        
        String deleteQuery = "DELETE FROM "+ TABLE + " WHERE ";
        boolean isFirst = true;
        int countDel = 0;
        
        for(int i=0; i<locList.size();i++){
        	if(locList.get(i).count <=1){
        		if(isFirst){
        			deleteQuery += "run_id="+locList.get(i).runID;
        			isFirst = false;
        			countDel++;
        		}
        		else{
        			deleteQuery += " OR run_id="+ locList.get(i).runID;
        			countDel++;
        		}
        		
        	}
        }
         
        if(countDel > 0){
        	db.execSQL(deleteQuery, null);
        	Log.i(TAG, countDel + " records was deleted");
        }
	}
	
	
	private class location{
		
		public long runID;
		public long count;
		
		public location(long runID, long count){
			this.runID = runID;
			this.count = count;
		}
	}
}
