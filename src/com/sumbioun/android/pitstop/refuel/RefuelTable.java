package com.sumbioun.android.pitstop.refuel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnFuelTypeChangeListener;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnModeChangeListener;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnRadiusChangeListener;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.Gasstation.Flag;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
import com.sumbioun.android.pitstop.map.GoogleDirectionsDecoder;
import com.sumbioun.android.pitstop.map.MapBrowser;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

/*RefuelTable                                                                                                                        */
/*Activity that shows a table with the results from a gas station query through the internal database. The criteria for the query    */
/*is provided by the Quick Settings over the table and is stored in the DatabaseHelper. By clicking on a row the RefuelGasstation    */
/*screen is opened, by long clicking it the application opens the MapBrowser, shoing a route to that gas station.                    */
public class RefuelTable extends RefuelFragmentActivity {

	RefuelTableFragment mRefuelTableFragment;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mRefuelTableFragment = new RefuelTableFragment();   
        
        //Sets the central fragment on the ViewPager
    	setFragment(mRefuelTableFragment);
	}
	
	@Override
	protected void onStart() {
	    super.onStart(); 
	    MyApplication.startGps();
	    if(!MyApplication.getGps().canGetLocation()){
	    	MyApplication.getGps().showSettingsAlert();
	    }
	    
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();    

	    mRefuelTableFragment.refresh();
	    mRefuelTableFragment.quickSettings.refresh();
		    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  
	    
	    //Stops the location manager to spare the battery since the user is no longer using the application.
	    MyApplication.getGps().stopUsingGPS();        
	    
	    if(mRefuelTableFragment != null){
		    if(mRefuelTableFragment.mQueryGasstations != null){
		    	mRefuelTableFragment.mQueryGasstations.cancel(true);
		    	
	    	}
	    }
	}
	
	//Activity's central fragment
	public static class RefuelTableFragment extends Fragment{
			
		private ListView mGasstationList;
		private GasstationAdapter mAdapter;
		
		private QuickSettings quickSettings = null;
		private CustTextView currentAddress;
		
		private RelativeLayout mProgressBar;
	        
	    private int maxResults = 10;
	    
	    private QueryGasstations mQueryGasstations;
	    
	    private View rootView;

	    @Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	
	        rootView = inflater.inflate(R.layout.refuel_table, container, false);

	        //UI initialization
	        mProgressBar = (RelativeLayout) rootView.findViewById(R.id.refuel_table_progress_bar);
	             
		    currentAddress = (CustTextView) rootView.findViewById(R.id.refuel_table_current_address);
		    setAddress();
		    
	        mGasstationList = (ListView) rootView.findViewById(R.id.gasstationList);
	        mGasstationList.setOnItemClickListener(new OnItemClickListener(){
	
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try{
						Intent intent = new Intent(RefuelTableFragment.this.getActivity(), RefuelGasstation.class);
			    		intent.putExtra(getString(R.string.EXTRA_REFUELGASSTATION_ID), mAdapter.getItem(position).getId());
			    		startActivity(intent);
					} catch (ActivityNotFoundException e){
						e.printStackTrace();
					}
					
				}
	        	
	        }); 
	        mGasstationList.setOnItemLongClickListener(new OnItemLongClickListener(){
	
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					Vibrator vib = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(200);
					initMapBrowser(mAdapter.getItem(position).getLatitude(), mAdapter.getItem(position).getLongitude());
					return false;
				}
	        	
	        });
	        
	        quickSettings = (QuickSettings) rootView.findViewById(R.id.refuel_table_quick_settings);
	        quickSettings.setOnFuelTypeChangeListener(new OnFuelTypeChangeListener(){
	
				public void onFuelTypeChange(int fuelType) {
					MyApplication.getDatabaseHelper().setFuelType(fuelType);
					refresh();
				}
		
			});
			quickSettings.setOnRadiusChangeListener(new OnRadiusChangeListener(){
	
				public void onRadiusChange(float radius) {
					MyApplication.getDatabaseHelper().setRadius(radius);
					refresh();
				}
		
			});
	        quickSettings.setOnModeChangeListener(new OnModeChangeListener(){
	
				public void onModeChange(int mode) {
					MyApplication.getDatabaseHelper().setMode(mode);
					refresh();
				}
				
			});
	        quickSettings.refresh();
	        
	        refresh();
	        
	        //Gets the amount of table rows to show from the user settings.
	        CustTextView distanceText = (CustTextView) rootView.findViewById(R.id.refuel_table_distance);   
	        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
	        distanceText.setText(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
		    maxResults = Integer.parseInt(sharedPref.getString(WorkshopSettings.KEY_MAX_RESULTS, "10"));
		    	   
	        return rootView;
	        
	    }
	    
	    //Refreshes the table, loading new results
	    private void refresh(){
	    	if(mQueryGasstations != null){
	    		mQueryGasstations.cancel(true);
	    	}
	    	
	    	mQueryGasstations = new QueryGasstations();
	    	mQueryGasstations.execute();
	    	
	    }
	    
	    //updates the address asynchronously and updates the CustTextView
	    private void setAddress(){
	    	new SetAddress().execute();
	    }
	    
	    //Opens the map browser, showing a route to the location specified.
	    private void initMapBrowser(double latitude, double longitude){
	    	
	    	try{
	    		
				Intent intent = new Intent(this.getActivity(), MapBrowser.class);
				intent.putExtra("MapBrowser.EXTRA_GASLAT", latitude);
				intent.putExtra("MapBrowser.EXTRA_GASLNG", longitude);
				startActivity(intent);
				
	    	} catch(ActivityNotFoundException e){
	    		e.printStackTrace();
	    	}
	    }
	
		//The price is displayed always in the format "X.XXX".
		private String getPriceInFormat(double Price){
	    	DecimalFormat nft = new DecimalFormat("#0.000");  
			nft.setDecimalSeparatorAlwaysShown(true);  
			
			return nft.format(Price);  
	    }
	    
		//The duration of the route is displayed in the format "XX".
	    private String getTimeInFormat(long time){
	    	DecimalFormat nft = new DecimalFormat("#00.###");  
			nft.setDecimalSeparatorAlwaysShown(false);  
			
			return nft.format((int)time/60);  
	    }
	    
	    //Queries the internal database for gas stations and sorts them according to the actual distance (the distance calculated by 
	    //the google API). The amount sorted is defined by the user setting: maxResults.
		class QueryGasstations extends AsyncTask<Void, Void, List<Gasstation>>{
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            mProgressBar.setVisibility(View.VISIBLE);
	            mGasstationList.setVisibility(View.GONE);
	        }
	        @Override
	        protected List<Gasstation> doInBackground(Void... params) {
	        	
	        	try{
	        		
		        	//if(MyApplication.getDatabaseHelper().getDatabaseSize() <= 10){
	        		//	MyApplication.getDatabaseHelper().updateInternalDatabase(null);
	        		//}
		        	
	        	} catch (Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        	List<Gasstation> values = MyApplication.getDatabaseHelper().getGasstations();
	        	if(values == null){
					MyApplication.getDatabaseHelper().updateInternalDatabase(null);
					values = MyApplication.getDatabaseHelper().getGasstations();
				}
	        	
	        	try {

					GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder();
	
					if(maxResults < values.size()){
						values = values.subList(0, maxResults);
					}
				
					List<Gasstation> _values = gdd.executeArray(values);
					if(_values != null){
						values = _values;
	
					} else {
						/*for(int i = 0; i < values.size(); i++){
							values.get(i).setTime((int)((float)values.get(i).getDistance()*2*60));
						}*/
					}
					
					if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
						
						values = MyApplication.getDatabaseHelper().sortGasstationsFast(values);
						
					} else {
						
					}

	        	} catch(Exception e){
	        		e.printStackTrace();
	        		values = null;
	        	}

				return values;
					
	        }
	        @Override
	        protected void onPostExecute(List<Gasstation> values) {
	            super.onPostExecute(values); 
	            mProgressBar.setVisibility(View.GONE);
	            mGasstationList.setVisibility(View.VISIBLE);
	            
	            if(values != null){
		            mAdapter = new GasstationAdapter(RefuelTableFragment.this.getActivity(), R.layout.refuel_table_list_item_gasstation, values);  
		            mGasstationList.setAdapter(mAdapter);
	            } else {
	            	mGasstationList.setAdapter(null);
	            }
	            
	        }
	    }
	    
	    //Gets the address through the geocoder and sets it into the view asynchronously.
	    class SetAddress extends AsyncTask<Void, Void, String>{
			  
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            currentAddress.setVisibility(View.GONE);
	        }
	        @Override
	        protected String doInBackground(Void... params) {

	        	String string = "";
				try {
					
		    		List<Address> list =  MyApplication.getGeocoder().getFromLocation(MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude(), 1);
		    		string = list.get(0).getAddressLine(0);
	    		
		    	} catch (IOException e) {
					e.printStackTrace();
					
				}
				
				return string;
					
	        }
	        @Override
	        protected void onPostExecute(String string) {
	            super.onPostExecute(string); 
	            currentAddress.setText(string);
	            currentAddress.setVisibility(View.VISIBLE);
	        }
	    }
	    
	    //The custom adapter to the main list
  		public class GasstationAdapter extends ArrayAdapter<Gasstation>{

  		    private Context mContext; 
  		    private int mLayoutResourceId;    
  		    private List<Gasstation> mData = null;
  		    
  		    public GasstationAdapter(Context context, int layoutResourceId, List<Gasstation> data) {
  		        super(context, layoutResourceId, data);
  		        this.mLayoutResourceId = layoutResourceId;
  		        this.mContext = context;
  		        this.mData = data;
  		        
  		    }

  		    @Override
  		    public View getView(int position, View convertView, ViewGroup parent) {
  		        View row = convertView;
  		        GasstationHolder holder = null;
  		        
  		        if(row == null)
  		        {
  		            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
  		            row = inflater.inflate(mLayoutResourceId, parent, false);
  		            
  		            holder = new GasstationHolder();
  		            holder.flag = (ImageView) row.findViewById(R.id.refuel_table_listitem_gasstation_flag);
  		            holder.price = (CustTextView) row.findViewById(R.id.refuel_table_listitem_gasstation_price);
  		            holder.distance = (CustTextView) row.findViewById(R.id.refuel_table_listitem_gasstation_distance);
  		            holder.time = (CustTextView) row.findViewById(R.id.refuel_table_listitem_gasstation_time);
  		            holder.lastUpdate = (CustTextView) row.findViewById(R.id.refuel_table_listitem_gasstation_last_update);
  		            holder.confirmed = (ImageView) row.findViewById(R.id.refuel_table_listitem_gasstation_confirmed);
  		            
  		            row.setTag(holder);
  		            
  		        } else {
  		            holder = (GasstationHolder)row.getTag();
  		        }
  		        
  		        Gasstation gasstation = mData.get(position);
  		        
  		        try {
  		        	
  		        	holder.flag.setImageResource(Flag.values()[gasstation.getFlag()].value);
  		        	holder.price.setText(getPriceInFormat(gasstation.getPrice(MyApplication.getDatabaseHelper().getFuelType())));
  			        holder.distance.setText(String.valueOf(((float)(Math.round(gasstation.getDistance()*10))/10)));
  			        holder.time.setText(getTimeInFormat(gasstation.getTime()));
  			        holder.lastUpdate.setText(String.valueOf(gasstation.getFormattedLastUpdate(MyApplication.getDatabaseHelper().getFuelType())));
  			        
  			        if(gasstation.getConfirmed()){
  			        	holder.confirmed.setImageResource(R.drawable.small_circle);
  			        	
  			        } else {
  			        	holder.confirmed.setImageResource(R.drawable.small_circle_red);
  			        	
  			        }
  			        
  		        } catch (NullPointerException e) {
  		        	e.printStackTrace();
  		        }
  		        
  		        
  		        
  		        return row;
  		    }
  		    
  		    //Class that holds references to the views in a row.
  		    class GasstationHolder
  		    {
  		    	ImageView flag;
  		        CustTextView price;
  		        CustTextView distance;
  		        CustTextView time;
  		        CustTextView lastUpdate;
  		        ImageView confirmed;
  		    }
  		}
		
	}

}
