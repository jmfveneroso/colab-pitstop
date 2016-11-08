package com.sumbioun.android.pitstop.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.refuel.RefuelGasstation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

/*MapBrowser                                                                                                    */
/*Google map that allows user to search gas stations and specify which fuel to display on the location markers. */ 
public class MapBrowser extends FragmentActivity {

	private GoogleMap mMap;
	
	private UpdateGasstationMarkers mUpdateGasstationMarkers = null;
	
	private LinearLayout mProgressBar = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_browser);
       
        try {
        	
        mProgressBar = (LinearLayout) this.findViewById(R.id.map_browser_progress_bar);
        
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        
        
        	
	        if (mMap != null) {
	        	
	        	//Enables the GPS find current position button.
	        	mMap.setMyLocationEnabled(true);
	
	            //Updates the gas stations shown when the user changes the camera position or zoom.
	            mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
	            	
	                public void onCameraChange(CameraPosition cameraPosition) {
	                	updateMapItems();
	                }
	                
	            });
	               	
	            //When the user clicks on a gas station marker open RefuelGasstation to show specific information.
	        	mMap.setOnMarkerClickListener(new OnMarkerClickListener(){
	
					public boolean onMarkerClick(Marker marker) {
						initRefuelGasstation(Long.parseLong(marker.getSnippet()));
						return false;
					}
	        		
	        	});
	        	
	        	mMap.setOnMapLongClickListener(new OnMapLongClickListener(){
	
					public void onMapLongClick(LatLng latLng) {
	
						List<Gasstation> list = MyApplication.getDatabaseHelper().getGasstations();
						for(Gasstation gas : list){
							if(Math.abs(gas.getLatitude() - latLng.latitude) < 0.0015 && Math.abs(gas.getLongitude() - latLng.longitude) < 0.0015){
								Vibrator vib = (Vibrator) MapBrowser.this.getSystemService(Context.VIBRATOR_SERVICE);
								vib.vibrate(150);
								Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+MyApplication.getGps().getLatitude()+","+MyApplication.getGps().getLongitude()+"&daddr="+gas.getLatitude()+","+gas.getLongitude()));
								startActivity(navIntent);
								break;
	        				}
						}
						
						
					}
	        		
	        	});
	        	
	        	final String[] mArrayFuel = getResources().getStringArray(R.array.array_quick_settings_fuel_types);
	        	
	        	//Fuel type controller.
	        	final CustTextView mFuelTypeButton = (CustTextView) this.findViewById(R.id.map_browser_fuel_type_button);
	        	mFuelTypeButton.setText(mArrayFuel[MyApplication.getDatabaseHelper().getFuelType()]);
	        	mFuelTypeButton.setOnClickListener(new View.OnClickListener(){
	
					public void onClick(View v) {
						int fuelType = MyApplication.getDatabaseHelper().getFuelType();
						fuelType++;
						if(fuelType > mArrayFuel.length-1){
							fuelType = 0;
						}
						
						mFuelTypeButton.setText(mArrayFuel[fuelType]);
						MyApplication.getDatabaseHelper().setFuelType(fuelType);
						updateMapItems();
					}
	        		
	        	});
	        	
	        }
	        
	        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
	
	        	double originLat = extras.getDouble("MapBrowser.EXTRA_ORIGINLAT");
	        	double originLng = extras.getDouble("MapBrowser.EXTRA_ORIGINLNG");
	        	
	        	double destinationLat = extras.getDouble("MapBrowser.EXTRA_DESTLAT");
	        	double destinationLng = extras.getDouble("MapBrowser.EXTRA_DESTLNG");
	        	
	        	if(originLat != 0.0 && originLng != 0.0 && destinationLat != 0.0 && destinationLng != 0.0){
	        		new RouteDrawer(originLat, originLng, destinationLat, destinationLng);
	        		
	        		//LatLng origin = new LatLng(originLat, originLng);
	        		//LatLng destination = new LatLng(destinationLat, destinationLng);
	        		
	        		//LatLngBounds bounds = LatLngBounds.builder().include(origin).include(destination).build();
	        		//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
	        		
	        	} else {
	        		
	        		double gasLat = extras.getDouble("MapBrowser.EXTRA_GASLAT");
	            	double gasLng = extras.getDouble("MapBrowser.EXTRA_GASLNG");
	            	
	            	if(gasLat != 0.0 && gasLng != 0.0){
	            		mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(gasLat, gasLng)));
	            		mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	            	}
	            	
	        	}
	    		
	        } else {
	        	//No route was passed to the activity.
	        	//Sets camera the initial position and zoom to the user's current position.
	            if(MyApplication.getGps().canGetLocation()){   
	            	
	            	mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude())));
	            	mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	            	
	            } else {
	            	MyApplication.getGps().showSettingsAlert();
	            }
	        }
        
        } catch(Exception e){
        	e.printStackTrace();
        } 

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
	    
	    if(mUpdateGasstationMarkers != null){
		    if(mUpdateGasstationMarkers != null){
		    	mUpdateGasstationMarkers.cancel(true);
	    	}
	    }
	}
    
    private void initRefuelGasstation(long id){
    	Intent intent = new Intent(MapBrowser.this, RefuelGasstation.class);
		intent.putExtra(getString(R.string.EXTRA_REFUELGASSTATION_ID), id);
		startActivity(intent);
    }
    
    private void updateMapItems(){
    	
    	if(mUpdateGasstationMarkers != null){
    		mUpdateGasstationMarkers.cancel(true);
    	}
    	
    	mUpdateGasstationMarkers = new UpdateGasstationMarkers();
    	mUpdateGasstationMarkers.execute();

    }
    
    public void createIcon(Gasstation gasstation){

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
        bm = bm.copy(Config.ARGB_8888 ,true);
        
	    Paint paint = new Paint();
	    paint.setColor(getResources().getColor(R.color.opaque_white));
	    paint.setTextSize(14);
	    paint.setTypeface(Typeface.DEFAULT_BOLD);
	    paint.setTextAlign(Align.CENTER);
	    
	    Canvas canvas = new Canvas(bm);

	    Rect bounds = new Rect();
	    paint.getTextBounds(String.valueOf(gasstation.getPrice(MyApplication.getDatabaseHelper().getFuelType())), 0, String.valueOf(gasstation.getPrice(MyApplication.getDatabaseHelper().getFuelType())).length(), bounds);
	    canvas.drawText(String.valueOf(gasstation.getPrice(MyApplication.getDatabaseHelper().getFuelType())), bm.getWidth()/2, bm.getHeight()/2+(bounds.bottom-bounds.top)/2 - 5, paint);
	    
	    //Paint defines the text color, stroke width, size
	   // BitmapDrawable draw = new BitmapDrawable(getResources(), bm);
	   // Bitmap drawBmp = draw.getBitmap();

	    mMap.addMarker(new MarkerOptions()
	            .position(new LatLng(gasstation.getLatitude(), gasstation.getLongitude()))
	            .icon(BitmapDescriptorFactory.fromBitmap(bm))
	            .snippet(String.valueOf(gasstation.getId()))
	            );
	    
	    
    }
    
    private static final double MAX_BOUND_DISTANCE = (((double)1/111)*10);;
    
    private class UpdateGasstationMarkers extends AsyncTask<Void, Void, List<Gasstation>>{
    	
    	LatLngBounds mCurrentScreen;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            mCurrentScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
            if(MyApplication.getDatabaseHelper().areaInsideUpdateBounds(mCurrentScreen)){
            	mProgressBar.setVisibility(View.GONE);
            } else {
            	//If data is gas station data is being retrieved from the internet we show the loading bar. 
            	mProgressBar.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        protected List<Gasstation> doInBackground(Void... params) {
        	
        	if(Math.pow(mCurrentScreen.northeast.longitude - mCurrentScreen.southwest.longitude, 2) > MAX_BOUND_DISTANCE){
        		return null;
        	}
        	
        	try{
	        	List<Gasstation> gas = MyApplication.getDatabaseHelper().getGasstationsInsideBounds(mCurrentScreen);
	            return gas;
        	} catch(Exception e){
        		return null;
        	}
            
        }
        
        @Override
        protected void onPostExecute(List<Gasstation> gas) {
            super.onPostExecute(gas); 
            
            mProgressBar.setVisibility(View.GONE);
            
            if(gas != null){
	            mMap.clear();

	            for(Gasstation gasstation : gas){
	        		createIcon(gasstation);
	        		
	        	}
            }
            
        }
    }

    private class RouteDrawer {
    	
    	public RouteDrawer(double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude){

    		String url = createURL(originLatitude, originLongitude, destinationLatitude, destinationLongitude);
    		new getDirections(url).execute();
    		
    	}
    	
    	public String createURL(double sourcelat, double sourcelog, double destlat, double destlog ){
            StringBuilder urlString = new StringBuilder();
            urlString.append("http://maps.googleapis.com/maps/api/directions/json");
            urlString.append("?origin=");// from
            urlString.append(Double.toString(sourcelat));
            urlString.append(",");
            urlString
                    .append(Double.toString( sourcelog));
            urlString.append("&destination=");// to
            urlString
                    .append(Double.toString( destlat));
            urlString.append(",");
            urlString.append(Double.toString( destlog));
            urlString.append("&sensor=false&mode=driving&alternatives=true");
            return urlString.toString();
        }
        
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng( (((double) lat / 1E5)),
                         (((double) lng / 1E5) ));
                poly.add(p);
            }

            return poly;
        }
        
        public LatLng drawPath(String result) {
        	
        	if(result == null){
        		return null;
        	}
        	
        	if(mMap == null){
        		
        	}
        	
            try {
            	   
                   //Transform the string into a JSON object
                   final JSONObject json = new JSONObject(result);
                   JSONArray routeArray = json.getJSONArray("routes");
                   JSONObject routes = routeArray.getJSONObject(0);
                   
                   JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                   String encodedString = overviewPolylines.getString("points");
                   List<LatLng> list = decodePoly(encodedString);

                   for(int z = 0; z<list.size()-1;z++){
                        LatLng src= list.get(z);
                        LatLng dest= list.get(z+1);
                        
                        LatLngBounds mCurrentScreen;
                        mCurrentScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
                        
                        double RADIUS = 4*(mCurrentScreen.northeast.longitude - mCurrentScreen.southwest.longitude); 
                        
                        if(dest.latitude <= mCurrentScreen.northeast.latitude   + RADIUS   && 
                           dest.latitude >= mCurrentScreen.southwest.latitude   - RADIUS   &&
                           dest.longitude <= mCurrentScreen.northeast.longitude + RADIUS   && 
                           dest.longitude >= mCurrentScreen.southwest.longitude - RADIUS   ){
                        	
                        } else {
                        	
                            return new LatLng(src.latitude, src.longitude);
                        }
                        
                        mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                        .width(2)
                        .color(Color.BLUE).geodesic(true));
                    }

            } 
            catch (JSONException e) {

            }
            
            return null;
            
        } 
        
        private class getDirections extends AsyncTask<Void, Void, String>{
            private ProgressDialog progressDialog;
            String url;
            
            getDirections(String url){
                this.url = url;
            }
            
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(MapBrowser.this);
                progressDialog.setMessage("Fetching route, Please wait...");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                
            }
            
            @Override
            protected String doInBackground(Void... params) {
                JSONParser jParser = new JSONParser();
                String json = jParser.getJSONFromUrl(url, 15000);
                return json;
                
            }
            
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);   
                progressDialog.hide();        
                if(result!=null){
                    drawPath(result);
                }
                
            }
        }
        
    }
    
}
