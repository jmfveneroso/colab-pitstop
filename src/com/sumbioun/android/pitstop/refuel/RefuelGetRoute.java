package com.sumbioun.android.pitstop.refuel;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.map.GoogleDirectionsDecoder;
import com.sumbioun.android.pitstop.map.MapRoute;

import deprecated.RefuelActivity;

import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.app.*;
import android.widget.*;
import android.content.*;

/*RefuelGetRoute                                                                                                                    */
/*This activity provides a mean to find the closest gas stations to a provided route.                                               */
public class RefuelGetRoute extends RefuelActivity {
	
	CustTextView destinationDisplayer;
	CustTextView currentLocationDisplayer;
	GetCheapestGasstation getCheapestGasstation;
	ProgressBar progressBar;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refuel_get_route);
 
        progressBar = (ProgressBar) findViewById(R.id.refuel_get_route_progress_bar); 

        currentLocationDisplayer = (CustTextView) findViewById(R.id.refuel_get_route_current_location); 
		currentLocationDisplayer.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v)
			{
				v.startAnimation(AnimationUtils.loadAnimation(RefuelGetRoute.this, R.anim.clickresize));
				setOrigin();
			}

		});
		currentLocationDisplayer.setVisibility(View.GONE);
		
		destinationDisplayer = (CustTextView) findViewById(R.id.refuel_get_route_destination);
		destinationDisplayer.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v)
			{
				v.startAnimation(AnimationUtils.loadAnimation(RefuelGetRoute.this, R.anim.clickresize));
				setDestination();
			}

		});
		
		CustTextView submit = (CustTextView) findViewById(R.id.refuel_get_route_submit);
		submit.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v)
			{
				v.startAnimation(AnimationUtils.loadAnimation(RefuelGetRoute.this, R.anim.clickresize));
				getCheapestGasstation = new GetCheapestGasstation();
				getCheapestGasstation.execute();
			}

		});
		
		new SetOrigin(MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude()).execute();

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
	public void onStop() {
	    super.onStop();  
	    
	    //Stops the location manager to spare the battery since the user is no longer using the application.
	    MyApplication.getGps().stopUsingGPS();

	    if(getCheapestGasstation != null){
	    	getCheapestGasstation.cancel(true);
    	}
	    
	    
	}
	
	private void setOrigin(){	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	final EditText input = new EditText(this);
    	if(!currentLocationDisplayer.getText().toString().contains(getText(R.string.refuel_get_route_origin_tip))){
    		input.setText(currentLocationDisplayer.getText().toString());
    	}
    	input.setSelection(0, input.length());
        builder.setView(input);

    	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				currentLocationDisplayer.setText(input.getText().toString());
				dialog.dismiss();
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		builder.create().show();

    }
	
	private void setDestination(){	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	final EditText input = new EditText(this);
    	if(!destinationDisplayer.getText().toString().contains(getText(R.string.refuel_get_route_destination))){
    		input.setText(destinationDisplayer.getText().toString());
    	}
    	input.setSelection(0, input.length());
        builder.setView(input);

    	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					destinationDisplayer.setText(input.getText().toString());
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});

		builder.create().show();

    }
	
	//Gets asynchronously the GPS current position and sets as origin.
	class SetOrigin extends AsyncTask<Void, Void, String>{
		  
		double mLatitude, mLongitude;
		
		SetOrigin(double latitude, double longitude){
			mLatitude = latitude;
			mLongitude = longitude;
		}
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {

        	try {
        		
        		List<Address> listAddress =  MyApplication.getGeocoder().getFromLocation(mLatitude, mLongitude, 1);
        		Address _address = listAddress.get(0);
        		return _address.getAddressLine(0);
        		
        	} catch (IOException e) {
    			e.printStackTrace();
    			
    		} catch (IndexOutOfBoundsException e){
    			e.printStackTrace();
    		}
			
			return null;
				
        }
        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string); 
            
            if(string == null){
            	
            	currentLocationDisplayer.setText(R.string.refuel_get_route_origin_tip);
            	
            } else {
            	
            	currentLocationDisplayer.setText(string);
            
            }
            
            progressBar.setVisibility(View.GONE);
            currentLocationDisplayer.setVisibility(View.VISIBLE);
        }
    }
	
	private double getDistance(double lat1, double lng1, double lat2, double lng2){
		return Math.sqrt(Math.pow(lat1-lat2, 2)+Math.pow(lng1-lng2, 2));
	}
	
	private Address getClosest(List<Address> addresses, double lat, double lng){
		
		double closestDist = 999999999;
		int pos = -1;
		for(int i = 0; i < addresses.size(); i++){
			double _distance = getDistance(addresses.get(i).getLatitude(), addresses.get(i).getLongitude(), lat, lng);
			if(_distance < closestDist){
				closestDist = _distance;
				pos = i;
			}
		}
		
		if(pos != -1){
			return addresses.get(pos);
		} else {
			return null;
		}
		
	}
	
	//Finds the cheapest gas station alongside the route.
	class GetCheapestGasstation extends AsyncTask<Void, Void, long[]>{
		private ProgressDialog progressDialog;  	
		
		Address mOrigin;
		Address mDestination; 
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RefuelGetRoute.this);
            progressDialog.setMessage(getString(R.string.refuel_get_route_finding_route));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            
        }
        @Override
        protected long[] doInBackground(Void... params) {

        	//query for the cheapest gas station
        	try {
        		
        		List<Address> origins = MyApplication.getGeocoder().getFromLocationName(currentLocationDisplayer.getText().toString(), 10);
        		List<Address> destinations = MyApplication.getGeocoder().getFromLocationName(destinationDisplayer.getText().toString(), 10);
        			
        		mOrigin = getClosest(origins, MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude());
        		mDestination = getClosest(destinations, MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude());
   
        		long gas[] = getShortestRoute(mOrigin, mDestination);
        		
        		return gas;
        	} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
				
        }
        @Override
        protected void onPostExecute(long id[]) {
            super.onPostExecute(id); 
            
            if(id == null){
            	
            	AlertDialog.Builder builder = new AlertDialog.Builder(RefuelGetRoute.this);
            	builder.setMessage(R.string.no_connection)
            	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

        			public void onClick(DialogInterface dialog, int id) {
        				dialog.dismiss();
        			}
        		});

        		builder.create().show();
        		
            }
            
            try{
            	
            	Intent intent = new Intent(RefuelGetRoute.this, MapRoute.class);

            	intent.putExtra("MapBrowser.EXTRA_GASSTATIONS", id);
            	
    	    	intent.putExtra("MapBrowser.EXTRA_ORIGINLAT", mOrigin.getLatitude());
    	    	intent.putExtra("MapBrowser.EXTRA_ORIGINLNG", mOrigin.getLongitude());
            	
    	    	intent.putExtra("MapBrowser.EXTRA_DESTLAT", mDestination.getLatitude());
    	    	intent.putExtra("MapBrowser.EXTRA_DESTLNG", mDestination.getLongitude());
            	
            	startActivity(intent);

            } catch(ActivityNotFoundException e){
            	e.printStackTrace();
            } catch(NullPointerException e){
            	e.printStackTrace();
            } catch(Exception e){
            	e.printStackTrace();
            }
            
            progressDialog.hide();
        			
        }
    }
	
	private long[] getShortestRoute(Address mOrigin, Address mDestination){
		
		try{
			mOrigin = MyApplication.getGeocoder().getFromLocationName(currentLocationDisplayer.getText().toString(), 1).get(0);
			mDestination = MyApplication.getGeocoder().getFromLocationName(destinationDisplayer.getText().toString(), 1).get(0);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
		double lat_dist = (double)0.5f*(mDestination.getLatitude() - mOrigin.getLatitude());
		double lng_dist = (double)0.5f*(mDestination.getLongitude() - mOrigin.getLongitude());
		
		//get Gas stations inside bounds
		double northeast_lat = mOrigin.getLatitude() - lat_dist;
	    double northeast_lng =  mOrigin.getLongitude() - lng_dist;
	    double southwest_lat = mDestination.getLatitude() + lat_dist;
	    double southwest_lng = mDestination.getLongitude() + lng_dist;
	    
	    if(northeast_lat < southwest_lat){
	    	double temp = southwest_lat; southwest_lat = northeast_lat; northeast_lat = temp;
	    }
	    if(northeast_lng < southwest_lng){
	    	double temp = southwest_lng; southwest_lng = northeast_lng; northeast_lng = temp;
	    }
	    
	    LatLng northeast = new LatLng(northeast_lat, northeast_lng);
	    LatLng southwest = new LatLng(southwest_lat, southwest_lng);
	    LatLngBounds latLngBounds = new LatLngBounds(southwest, northeast);
	    
		List<Gasstation> list = MyApplication.getDatabaseHelper().getGasstationsInsideBounds(latLngBounds);
		if(list == null || list.size() == 0){
			
			return null;
		}
		
		for(Gasstation gas : list){
			
			//calculate distance to origin
			double distanceToOrigin = Math.pow(gas.getLatitude() - mOrigin.getLatitude(), 2) + Math.pow(gas.getLongitude() - mOrigin.getLongitude(), 2);

			//calculate distance to destination
			double distanceToDestination = Math.pow(gas.getLatitude() - mDestination.getLatitude(), 2) + Math.pow(gas.getLongitude() - mDestination.getLongitude(), 2);
			gas.setDistance((float)distanceToOrigin + (float)distanceToDestination);
		}
		
		MyApplication.getDatabaseHelper().sortGasstationsFast(list);
		
		if(list.size() > 20){
			list = list.subList(0, 20);
			
		}
		
		GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder();
		list = gdd.executeArray(list, new LatLng(mOrigin.getLatitude(), mOrigin.getLongitude()), new LatLng(mDestination.getLatitude(), mDestination.getLongitude()));
		 
		MyApplication.getDatabaseHelper().sortGasstationsFast(list);
		
		if(list == null || list.size() == 0){
			
			return null;
		}
		
		if(list.size() > 10){
			list = list.subList(0, 10);
		}
		
		long gasId[] = new long[list.size()];
		for(int i = 0; i < list.size(); i++){
			gasId[i] = list.get(i).getId();
			
		}
		
		return gasId;
		
	}
	
}
