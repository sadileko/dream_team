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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
		
		
		/*
		 * Constructor for HistoryActivity
		 */
		public LocationItem(long runID, int run_type, long startTime, long endTime, int steps, float avgSpeed, float maxSpeed, float distance, double lat, double lng){		
			
			this.runID = runID;
			this.runType = run_type;
			this.locationDescription = ""; //unimplemented
			this.timeDate = convertToDateFormat(startTime);
			this.steps = steps;
			this.speed = Math.round(maxSpeed * 3.6);
			this.avgSpeed = Math.round(avgSpeed * 3.6);
			this.distance = Math.round(distance);			
			this.lat = lat;
			this.lng = lng;					
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
		
}
