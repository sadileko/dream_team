package cz.zcu.kiv.runstat.ui;

import java.io.IOException;
import java.util.List;

import cz.zcu.kiv.runstat.R;
import cz.zcu.kiv.runstat.data.DBHelper;
import cz.zcu.kiv.runstat.data.LocationItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Pranay Airan
 * 
 */
public class HistoryActivity extends Activity {

	
	locationAdapter locationListAdapter;
	DBHelper dbh;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);
     
        dbh = new DBHelper(getApplicationContext());
     
        try {
			locationListAdapter = new locationAdapter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ListView locationListView = (ListView)findViewById(R.id.listView);
        locationListView.setAdapter(locationListAdapter);
        
        locationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				LocationItem item = locationListAdapter.getLocationPos(arg2);
				
				//Show running on map
				Intent intent = new Intent(HistoryActivity.this, MapActivity.class); 
	        	intent.putExtra("runID", item.runID);
				startActivity(intent);
				
			}
		});
        
        
        locationListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

            		final LocationItem item = locationListAdapter.getLocationPos(arg2);

            		final int positionToRemove = arg2;	
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
        	    	builder
        	    	.setTitle("Delete item")
        	    	.setMessage("Are you sure?")
        	    	.setIcon(android.R.drawable.ic_dialog_alert)
        	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	    	    public void onClick(DialogInterface dialog, int which) {
        	    	    	
        	    	    	dbh.removeLocationByRunID(item.runID);
        	    	    	
        	    	    	locationListAdapter.remove(positionToRemove);
        	    	    	locationListAdapter.notifyDataSetChanged();  
        	    	    	
        	    	    	Toast.makeText(getApplicationContext(), "Record from " + item.locationTime + " was deleted.", Toast.LENGTH_LONG).show(); 
        	    	    }
        	    	})
        	    	.setNegativeButton("No", null);
            		builder.show();
            		
                return false;
            }

        });
        
        

    }


    public class locationAdapter extends BaseAdapter {

    	List<LocationItem> locationList;
    	
    	locationAdapter() throws IOException{
    		this.locationList = getDataForListView();
    	}
    	
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return locationList.size();
		}

		@Override
		public LocationItem getItem(int arg0) {
			// TODO Auto-generated method stub
			return locationList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		
		public void remove(int arg0){
			this.locationList.remove(arg0);
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			if(arg1==null)
			{
				LayoutInflater inflater = (LayoutInflater) HistoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				arg1 = inflater.inflate(R.layout.listitem, arg2,false);
			}
			
			TextView chapterName = (TextView)arg1.findViewById(R.id.textView1);
			TextView chapterDesc = (TextView)arg1.findViewById(R.id.textView2);
			
			LocationItem locItem = locationList.get(arg0);
			
			chapterName.setText(locItem.locationTime);
			chapterDesc.setText("run_id:"+locItem.runID);
			
			return arg1;
		}
		
		public LocationItem getLocationPos(int position)
		{
			return locationList.get(position);
		}

    }
    
    
    public List<LocationItem> getDataForListView() throws IOException
    {
    	DBHelper dbh = new DBHelper(getApplicationContext());
    	List<LocationItem> locationsList = dbh.getRunningEvents();

    	
    	return locationsList;
    	
    }
}
