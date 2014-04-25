package cz.zcu.kiv.runstat.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
    
    @Override
	public void onDestroy(){
		super.onDestroy();
		
		finish();
	}
    
    @Override
	public void onPause(){
		super.onPause();

		finish();
	}
    
    private Cursor getLocations() {
    	SQLiteDatabase db = dbh.getReadableDatabase();
    	Cursor cursor = db.query(DBHelper.TABLE, null, null, null, null, null, null);

    	startManagingCursor(cursor);
    	return cursor;
    	
    }
    
	protected void setMarkers(Cursor cursor, long idPoint){
		PolylineOptions po = new PolylineOptions().color(Color.argb(150,148,0,211)).geodesic(true);
		while (cursor.moveToNext()) {

			long id = cursor.getLong(0);
			long timeMils = cursor.getLong(1);
			int steps = cursor.getInt(2);
			float speed = cursor.getFloat(3);
			float distance = cursor.getFloat(4);
			double latitude = cursor.getDouble(5);
			double longitude = cursor.getDouble(6);	
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd.MMM.yyyy");
			
			Date resultdate = new Date(timeMils);
			String time = ((sdf.format(resultdate)).toString());


			addMarker(new LatLng(latitude, longitude), time, speed);	
			po.add(new LatLng(latitude, longitude));
			
			if(idPoint==0||idPoint==id){
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(latitude, longitude), 16));
			}
			
		}
		map.addPolyline(po);
	}
	
	protected void addMarker(LatLng position, String time, float speed){
		map.addMarker(new MarkerOptions()
		.icon(BitmapDescriptorFactory.defaultMarker(270))
		.title(time)
		.snippet("Speed: " + Float.toString(speed))
		//.anchor(0.0f, 1.0f)
		.position(position));
	}
}

