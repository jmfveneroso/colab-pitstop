package com.sumbioun.android.pitstop.map;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.contribute.ContributeMain;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.Gasstation;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

/*MapConfirmLocation                                                                    */
/*In this activity the user can select an existing gas station in the map or create     */
/*a new one. The selected gas station is passed as an argument to ContributeMain or     */
/*ContributeNew where the user can start editing the information.                       */
public class MapConfirmLocation extends FragmentActivity {

	private GoogleMap mMap;
	private CustTextView mCreateNew;
	
	private boolean ADD_GASSTATION_ON_CLICK  = false;
	private boolean NEW_GASSTATION_MARKER_CREATED  = false;
	
	Marker mMarker = null;
	
	private UpdateGasstationMarkers mUpdateGasstationMarkers = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_confirm_location);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(MapConfirmLocation.this);
		builder.setMessage(R.string.MapConfirmLocation_Help)
    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
    	builder.create().show();
        
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
        	
        	//Enable current location finder.
        	mMap.setMyLocationEnabled(true);
            
        	//Sets the camera initial position and zoom to the user's current position.
            if(MyApplication.getGps().canGetLocation()){     	
            	mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(MyApplication.getGps().getLatitude(), MyApplication.getGps().getLongitude())));
            	mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            	
            }else{
            	MyApplication.getGps().showSettingsAlert();
            }
        	
        	//Updates the gas stations shown when the user changes the camera position or zoom.
            mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            	
                public void onCameraChange(CameraPosition cameraPosition) {
                	updateMapItems();
                }
                
            });
        	
            //Edit existing gas station.
        	mMap.setOnMarkerClickListener(new OnMarkerClickListener(){

				public boolean onMarkerClick(Marker marker) {
					
					if(marker.equals(mMarker) == false){
						final Long gasId = Long.parseLong(marker.getSnippet());
		
						AlertDialog.Builder builder = new AlertDialog.Builder(MapConfirmLocation.this);
						builder.setMessage(getString(R.string.map_confirm_location_edit_confirmation))
				    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				           
				            public void onClick(DialogInterface dialog, int id) {
				            	initContributeMain(gasId);
		                		dialog.dismiss();
				            }
				        })
				        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	
				            public void onClick(DialogInterface dialog, int id) {
				            	dialog.dismiss();
				            }
				        });
				    	builder.create().show();
					}

					return false;
				}
        		
        	});
        	
        	//Create a new gas station if "ADD_GASSTATION_ON_CLICK".
        	mMap.setOnMapClickListener(new OnMapClickListener(){

				public void onMapClick(LatLng latLng) {
					
					if(ADD_GASSTATION_ON_CLICK){
						
						if(mMarker != null){ mMarker.remove(); }
						
						mMarker = mMap.addMarker(new MarkerOptions()
				        .position(latLng)
				        .draggable(true));
						
						Toast.makeText(MapConfirmLocation.this, getString(R.string.MapConfirmLocation_HelpRepositionMarker), Toast.LENGTH_LONG).show();
						ADD_GASSTATION_ON_CLICK = false;
						NEW_GASSTATION_MARKER_CREATED = true;
					}
				}
        		
        	});
        }
        
        //Activate "create new gas station" mode. The user may click anywhere over the map to add a gas station now.
        mCreateNew = (CustTextView) this.findViewById(R.id.map_browser_create_new);
        mCreateNew.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(MapConfirmLocation.this, R.anim.clickresize));
				
				if(NEW_GASSTATION_MARKER_CREATED){
					
					AlertDialog.Builder builder = new AlertDialog.Builder(MapConfirmLocation.this);
			    	builder.setMessage(getString(R.string.map_confirm_location_create_confirmation))
			    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			           
			            public void onClick(DialogInterface dialog, int id) {
			            	initContributeMain(mMarker.getPosition());
	                		dialog.dismiss();
			            }
			        })
			        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			            public void onClick(DialogInterface dialog, int id) {
			            	dialog.dismiss();
			            }
			        });
			    	builder.create().show();
					
				} else {
				
					ADD_GASSTATION_ON_CLICK = true;
					Toast.makeText(MapConfirmLocation.this, getString(R.string.MapConfirmLocation_HelpCreateGasstation), Toast.LENGTH_LONG).show();
					mCreateNew.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.confirm, 0, 0);
					mCreateNew.setText(getString(R.string.MapConfirmLocation_Confirm));
				}
			}
        	
        });
        
        //Confirm new gas station position.
        /*mConfirm = (CustTextView) this.findViewById(R.id.map_browser_confirm);
        mConfirm.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(MapConfirmLocation.this, R.anim.clickresize));
				
				if(mMarker == null){
					AlertDialog.Builder builder = new AlertDialog.Builder(MapConfirmLocation.this);
					builder.setMessage("Adicione um posto novo ou clique sobre um existente para continuar.");
					builder.create().show();
					return;
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MapConfirmLocation.this);
		    	builder.setMessage("Tem certeza que voc� quer criar este posto aqui?")
		    	.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
		            	initContributeNew(mMarker.getPosition());
                		dialog.dismiss();
		            }
		        })
		        .setNegativeButton("N�o", new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int id) {
		            	dialog.dismiss();
		            }
		        });
		    	builder.create().show();
			}
        	
        });*/

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
    
    //Called when the user clicks over an empty space on the map and wants to add a new gas station.
    private void initContributeMain(LatLng latLng){
    	
    	try{
    		
	    	/*Intent intent = new Intent(this, ContributeNew.class);
	    	intent.putExtra(getString(R.string.EXTRA_CONTRIBUTENEW_LATITUDE), latLng.latitude);
	    	intent.putExtra(getString(R.string.EXTRA_CONTRIBUTENEW_LONGITUDE), latLng.longitude);*/
	    	
	    	Intent intent = new Intent(this, ContributeMain.class);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LATITUDE), latLng.latitude);
		    intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LONGITUDE), latLng.longitude);
		    intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_ID), ContributeMain.CREATE_NEW_GASSTATION);

	    	startActivity(intent);  
			finish();
			
    	} catch(ActivityNotFoundException e){
    		e.printStackTrace();
    	}
    	
    }
    
    //Called when the user clicks over a gas station and wants to edit it.
    private void initContributeMain(long id){
    	
    	try{
	    	Intent intent = new Intent(this, ContributeMain.class);
	    	intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_ID), id);
	    	
	    	Gasstation _gas = MyApplication.getDatabaseHelper().getGasstationById(id);
	    	intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LATITUDE), _gas.getLatitude());
	    	intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LONGITUDE), _gas.getLongitude());
    		startActivity(intent);   
			finish();
    	} catch(ActivityNotFoundException e){
    		e.printStackTrace();
    	} catch(NullPointerException e){
    		e.printStackTrace();
    	}
    	
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
	    
	    mMap.addMarker(new MarkerOptions()
	            .position(new LatLng(gasstation.getLatitude(), gasstation.getLongitude()))
	            .icon(BitmapDescriptorFactory.fromBitmap(bm))
	            .snippet(String.valueOf(gasstation.getId()))
	            
	            );
	    
	    
    }
    
    private class UpdateGasstationMarkers extends AsyncTask<Void, Void, List<Gasstation>>{
    	
    	LatLngBounds mCurrentScreen;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCurrentScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
        }
        
        @Override
        protected List<Gasstation> doInBackground(Void... params) {
        	
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
            
            if(gas != null){
            	//Save mMarker before clearing the map.
            	LatLng latLng = null;
            	if(mMarker != null){
            		latLng = mMarker.getPosition();
            	}
            	
	            mMap.clear();
	            
	            if(latLng != null){
		            mMarker = mMap.addMarker(new MarkerOptions()
			        .position(latLng)
			        .draggable(true));
	            }
	            
	            for(Gasstation gasstation : gas){
	        		createIcon(gasstation);
	        		
	        	}
            }
            
        }
    }
}
