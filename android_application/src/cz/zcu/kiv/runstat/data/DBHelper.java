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
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + TIME + " integer, "
				+ STEPS + " integer, "+ SPEED +" text,"+ DISTANCE+ " text," + LATITUDE + " text, " + LONGITUDE + " text);";
		
		db.execSQL(sql);
		Log.i(TAG, "Creating DB: "+sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG,"onUpgrade()-old: "+oldVersion+"new: "+newVersion);
		
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
	}
	
	/*
	 * Adds location to DB
	 */
	public void addToDatabase(String lat, String lng, int steps, String speed, String distance) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(TIME, System.currentTimeMillis());
		values.put(STEPS, steps);
		values.put(SPEED, speed);
		values.put(DISTANCE, distance);
		values.put(LATITUDE, lat);
		values.put(LONGITUDE, lng);
		
		
		db.insert(TABLE, null, values);
		
		Log.d(TAG, "add data: "+lat+":"+lng);
	}
	
	/*
	 * Returns all locations from DB as list
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
            	
            	for(int i=0;i<7;i++)
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
}
