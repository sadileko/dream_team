package cz.zcu.kiv.runstat.data;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;



public class LocationItem {
	
		public String locationTime;
		public String locationDescription;
		public long runID;
		public double lat;
		public double lng;
		
		
		public LocationItem(long time, String description, long runID, double lat, double lng, Context ctx) throws IOException{
			this.locationTime = convertToDateFormat(time);
			this.locationDescription = description;
			this.runID = runID;
			this.lat = lat;
			this.lng = lng;
					
		}
		
		
		private String convertToDateFormat(long timeInMills){
			Locale locale = new Locale("cs", "CZ");
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd.MMM.yyyy", locale);
			
			Date resultdate = new Date(timeInMills);
			String time = ((sdf.format(resultdate)).toString());
			
			return time;
		}
}
