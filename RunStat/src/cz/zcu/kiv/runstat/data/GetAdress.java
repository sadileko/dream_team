package cz.zcu.kiv.runstat.data;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class GetAdress extends IntentService {


	public GetAdress(String name) {
		super(name);
		
	}


	@Override
	public void onCreate() {
    super.onCreate();
   
    }


	@Override
	protected void onHandleIntent(Intent intent) {
		
		
	}
	
	
	private String findAdress(Location location, Context ctx){
		Geocoder geocoder =
                new Geocoder(ctx, Locale.getDefault());
        // Get the current location from the input parameter list
        Location loc = location;
        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            /*
             * Return 1 address.
             */
            addresses = geocoder.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
        } catch (IOException e1) {
        Log.e("LocationSampleActivity",
                "IO Exception in getFromLocation()");
        e1.printStackTrace();
        return ("IO Exception trying to get address");
        } catch (IllegalArgumentException e2) {
        // Error message to post in the log
        String errorString = "Illegal arguments " +
                Double.toString(loc.getLatitude()) +
                " , " +
                Double.toString(loc.getLongitude()) +
                " passed to address service";
        Log.e("LocationSampleActivity", errorString);
        e2.printStackTrace();
        return errorString;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
            /*
             * Format the first line of address (if available),
             * city, and country name.
             */
            String addressText = String.format(
                    "%s, %s, %s",
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getLocality(),
                    // The country of the address
                    address.getCountryName());
            // Return the text
            return addressText;
        } else {
            return "No address found";
        }

	}
   

}