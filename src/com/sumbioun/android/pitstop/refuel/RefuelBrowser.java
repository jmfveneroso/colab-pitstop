package com.sumbioun.android.pitstop.refuel;

import java.io.IOException;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.support.v4.app.*;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.PriceDisplayer;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnFuelTypeChangeListener;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnModeChangeListener;
import com.sumbioun.android.pitstop.customwidgets.QuickSettings.OnRadiusChangeListener;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
import com.sumbioun.android.pitstop.map.MapBrowser;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

/*RefuelBrowser                                                                                                       */
/*This activity is where the user can search for the cheapest and the closest gas station, being able to query for    */
/*different fuel types and define the radius of the query. With a single click over the NumberDisplayer she opens     */
/*the RefuelGasstation activity and with a long click she opens the MapBrowser, showing the route to the gas          */
/*station.                                                                                                            */
public class RefuelBrowser extends RefuelFragmentActivity {
	
	public static final String KEY_SHORTEST_DISTANCE_GAS = "RefuelBrowser_ShortestDistanceGas";
    public static final String KEY_LOWEST_PRICE_GAS = "RefuelBrowser_LowestPriceGas";
    public static final String KEY_FUEL_TYPE = "RefuelBrowser_FuelType";
    public static final String KEY_MODE = "RefuelBrowser_Mode";
    
    public static String DISTANCE_UNIT = " km";
    public static final String PRICE_SIGN = "$";
    public static final String TIME_UNIT = " min";
	
    private RefuelBrowserFragment mRefuelBrowserFragment = null;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		mRefuelBrowserFragment = new RefuelBrowserFragment();
		
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	
        	mRefuelBrowserFragment.setArguments(extras);
            
        } else {
        	
        	Bundle bundle = new Bundle();
        	bundle.putInt(getString(R.string.EXTRA_SORTER), GasstationsDataSource.SHORTEST_DISTANCE);
        	mRefuelBrowserFragment.setArguments(bundle);
        	
        }
		
        //sets refuelBrowserFragment as the central fragment in the activity's ViewPager.
    	setFragment(mRefuelBrowserFragment);
		
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
	    super.onStart(); 
	    mRefuelBrowserFragment.refresh();
	    mRefuelBrowserFragment.quickSettings.refresh();
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  
	    
	    //Stops the location manager to spare the battery since the user is no longer using the application.
	    MyApplication.getGps().stopUsingGPS();        
	    
	    //Cancel the async task.
	    if(mRefuelBrowserFragment != null){
		    if(mRefuelBrowserFragment.queryBestGasstations != null){
		    	mRefuelBrowserFragment.queryBestGasstations.cancel(true);
	    	}
	    }
	}

	//Activity's main fragment
	public static class RefuelBrowserFragment extends Fragment{
	    
	    private RelativeLayout mProgressBar;
	    private RelativeLayout mContainer;
	    
	    private QuickSettings quickSettings = null;	        
	    
	    //Central Container
	    private LinearLayout leftDisplayer;
		private LinearLayout rightDisplayer;
		
		private CustTextView labelLess = null;
		private CustTextView labelMore = null;
		
		private CustTextView leftDisplayerTop = null;
		private PriceDisplayer leftDisplayerCenter = null;
		private CustTextView leftDisplayerSub = null;
		private CustTextView leftDisplayerLastupdate = null;
		
		private CustTextView rightDisplayerTop = null;
		private PriceDisplayer rightDisplayerCenter = null;
		private CustTextView rightDisplayerSub = null;
		private CustTextView rightDisplayerLastupdate = null;

		
		Gasstation closestGasstation = null;
		Gasstation cheapestGasstation = null;
		
		private CustTextView currentAddress;
	    
	    //asyncTask to query the best gas stations on the inner database
	    private QueryBestGasstations queryBestGasstations;
	    
	    private View rootView;
	
	    @Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        rootView = inflater.inflate(R.layout.refuel_browser, container, false);
	        
            Bundle bundle = getArguments();
    		if(bundle != null){
    			
    			int sorter = bundle.getInt(getString(R.string.EXTRA_SORTER));
 	            MyApplication.getDatabaseHelper().setMode(sorter);

    		} else {
    			throw new RuntimeException();
    		}
    		
    		//UI initialization
    		mProgressBar = (RelativeLayout) rootView.findViewById(R.id.refuel_browser_progress_bar);
	    	mProgressBar.setVisibility(View.GONE);

	    	mContainer = (RelativeLayout) rootView.findViewById(R.id.refuel_browser_container);
	    	mContainer.setVisibility(View.GONE);
	    	
    		quickSettings = (QuickSettings) rootView.findViewById(R.id.refuel_browser_quick_settings);		
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
					updateContainer();
				}
				
			});
	        quickSettings.refresh();
	       
	        CustTextView route = (CustTextView) rootView.findViewById(R.id.refuel_browser_route);
	        route.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelBrowserFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){  
						public void onAnimationRepeat(Animation arg0) {}
						public void onAnimationStart(Animation animation) {	}
						public void onAnimationEnd(Animation arg0) {
							initGetRoute();
					    }
					});
					v.startAnimation(anim);	
					
				}
				
			});
	        
	        CustTextView map = (CustTextView) rootView.findViewById(R.id.refuel_browser_map);
	        map.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelBrowserFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){  
						public void onAnimationRepeat(Animation arg0) {}
						public void onAnimationStart(Animation animation) {	}
						public void onAnimationEnd(Animation arg0) {
							initMapBrowser(MyApplication.getDatabaseHelper().getMode());
					    }
					});
					v.startAnimation(anim);	
					
				}
			});
	        
	        CustTextView showTable = (CustTextView) rootView.findViewById(R.id.refuel_browser_contribute);
	        showTable.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View v) {
	        		Animation anim = AnimationUtils.loadAnimation(RefuelBrowserFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){  
						public void onAnimationRepeat(Animation arg0) {}
						public void onAnimationStart(Animation animation) {	}
						public void onAnimationEnd(Animation arg0) {
							Intent intent = new Intent(RefuelBrowserFragment.this.getActivity(), RefuelTable.class);
							intent.putExtra(getString(R.string.EXTRA_SORTER), MyApplication.getDatabaseHelper().getMode());
							startActivity(intent);	
					    }
					});
					v.startAnimation(anim);	
	        		
	        	}
	        });
	        
	        currentAddress = (CustTextView) rootView.findViewById(R.id.refuel_browser_current_address);  
	        setAddress();
	        
	    	initContainer();
	        
	    	refresh();
	        
	        return rootView;
	       
	    }
	    
	    private void initGetRoute(){    
			
	    	Gasstation gasstation;
			if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
				gasstation = closestGasstation;
				if(gasstation == null){
					gasstation = MyApplication.getDatabaseHelper().getClosestGasstation(false);
				}
			} else {
				gasstation = cheapestGasstation;
				if(gasstation == null){
					gasstation = MyApplication.getDatabaseHelper().getCheapestGasstation();
				}
			}
	    	
	    	try{
	    		Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+MyApplication.getGps().getLatitude()+","+MyApplication.getGps().getLongitude()+"&daddr="+gasstation.getLatitude()+","+gasstation.getLongitude()));
				startActivity(navIntent);	
	    	} catch(ActivityNotFoundException e){
	    		e.printStackTrace();
	    	} catch(NullPointerException e){
	    		
	    	}
        
	    }
	    
	    //Initializes the views inside the central container. This function is separated just to make the code clearer. 
  		private void initContainer(){
  			
  			leftDisplayerTop = (CustTextView) rootView.findViewById(R.id.refuel_browser_shortest_distance_top);
  			leftDisplayerCenter = (PriceDisplayer) rootView.findViewById(R.id.refuel_browser_shortest_distance_displayer);
  			leftDisplayerSub = (CustTextView) rootView.findViewById(R.id.refuel_browser_shortest_distance_sub);
  			leftDisplayerLastupdate = (CustTextView) rootView.findViewById(R.id.refuel_browser_shortest_distance_last_update);
  			
  			rightDisplayerTop = (CustTextView) rootView.findViewById(R.id.refuel_browser_lowest_price_top);
  			rightDisplayerCenter = (PriceDisplayer) rootView.findViewById(R.id.refuel_browser_lowest_price_displayer);   
  			rightDisplayerSub = (CustTextView) rootView.findViewById(R.id.refuel_browser_lowest_price_sub);
  			rightDisplayerLastupdate = (CustTextView) rootView.findViewById(R.id.refuel_browser_lowest_price_last_update);
  			
  			labelLess = (CustTextView) rootView.findViewById(R.id.refuel_browser_fragment_label_less);
  			labelMore = (CustTextView) rootView.findViewById(R.id.refuel_browser_fragment_label_more);
  			
  			leftDisplayer = (LinearLayout) rootView.findViewById(R.id.refuel_browser_fragment_distance_shortest_distance);
  			leftDisplayer.setOnClickListener(new View.OnClickListener() {
	
					public void onClick(View v) {
						Animation anim = AnimationUtils.loadAnimation(RefuelBrowserFragment.this.getActivity(), R.anim.clickresize);
						anim.setAnimationListener(new Animation.AnimationListener(){  
							public void onAnimationRepeat(Animation arg0) {}
							public void onAnimationStart(Animation animation) {	}
							public void onAnimationEnd(Animation arg0) {
								if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
									initRefuelGasstation(closestGasstation);
								} else {
									initRefuelGasstation(cheapestGasstation);
								}
						    }
						});
						v.startAnimation(anim);	
						
					}
			});
  			leftDisplayer.setOnLongClickListener(new View.OnLongClickListener() {
				
				public boolean onLongClick(View v) {
					//Vibrate the cell phone so the user knows she is long clicking
					Vibrator vib = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(200);
					
					if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
						initMapBrowser(GasstationsDataSource.SHORTEST_DISTANCE);
					} else {
						initMapBrowser(GasstationsDataSource.LOWEST_PRICE);
					}
					
					return false;
				}
			});
				
  			rightDisplayer = (LinearLayout) rootView.findViewById(R.id.refuel_browser_fragment_distance_lowest_price);
  			rightDisplayer.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelBrowserFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){  
						public void onAnimationRepeat(Animation arg0) {}
						public void onAnimationStart(Animation animation) {	}
						public void onAnimationEnd(Animation arg0) {
					    	if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
								initRefuelGasstation(cheapestGasstation);
							} else {
								initRefuelGasstation(closestGasstation);
							}
					    }
					});
					v.startAnimation(anim);	
				
				}
			});
  			rightDisplayer.setOnLongClickListener(new View.OnLongClickListener() {
				
				public boolean onLongClick(View v) {
					//Vibrate the cell phone so the user knows she is long clicking
					Vibrator vib = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(200);
					
					if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
						initMapBrowser(GasstationsDataSource.LOWEST_PRICE);
					} else {
						initMapBrowser(GasstationsDataSource.SHORTEST_DISTANCE);
					}
					return false;
				}
			});
  			
  		}
    
	    //Gets the data on the closest and cheapest gas stations and populates the central ViewGroup according to the sorting mode.
	    private void refresh(){
		
	    	if(queryBestGasstations != null){
	    		queryBestGasstations.cancel(true);
	    	}
	    	
	    	queryBestGasstations = new QueryBestGasstations();
	    	queryBestGasstations.execute();
	    	
	    }
	    
	    //updates the address asynchronously and updates the CustTextView
	    private void setAddress(){
	    	new SetAddress().execute();
	    }
	    
	    private void initMapBrowser(int mode) throws NullPointerException {
	    	
	    	Gasstation gasstation;
			if(mode == GasstationsDataSource.SHORTEST_DISTANCE){
				gasstation = closestGasstation;
				if(gasstation == null){
					gasstation = MyApplication.getDatabaseHelper().getClosestGasstation(false);
				}
			} else {
				gasstation = cheapestGasstation;
				if(gasstation == null){
					gasstation = MyApplication.getDatabaseHelper().getCheapestGasstation();
				}
			}
			if(gasstation == null){
				throw new NullPointerException();
			}
			
			try {
				
				Intent intent = new Intent(this.getActivity(), MapBrowser.class);
				intent.putExtra("MapBrowser.EXTRA_GASLAT", gasstation.getLatitude());
				intent.putExtra("MapBrowser.EXTRA_GASLNG", gasstation.getLongitude());
				startActivity(intent);
				
			} catch(ActivityNotFoundException e){	    		 
				e.printStackTrace();
	    	}
	    	
	    }
	    
	    //Queries the internal database for the closest gas station and the cheapest gas station 
	    class QueryBestGasstations extends AsyncTask<Void, Void, Boolean>{
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            mProgressBar.setVisibility(View.VISIBLE);
	            mContainer.setVisibility(View.GONE);
	        }
	        
	        @Override
	        protected Boolean doInBackground(Void... params) {
	
	        	try{
	        		
	        		//if(MyApplication.getDatabaseHelper().getDatabaseSize() == 0){
	        		//	MyApplication.getDatabaseHelper().updateInternalDatabase(null);
	        		//}
	        		
	        		closestGasstation = MyApplication.getDatabaseHelper().getClosestGasstation(true);
	        		cheapestGasstation = MyApplication.getDatabaseHelper().getCheapestGasstation();
		        	
	        		if(closestGasstation == null || cheapestGasstation == null){
	        			MyApplication.getDatabaseHelper().updateInternalDatabase(null);
	        			closestGasstation = MyApplication.getDatabaseHelper().getClosestGasstation(true);
		        		cheapestGasstation = MyApplication.getDatabaseHelper().getCheapestGasstation();
		        		
	        		}
	        		
	        	} catch(NullPointerException e) {
	        		e.printStackTrace();
	        		return false;
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        		return false;
	        	}
	        	
				return true;
					
	        }
	        
	        @Override
	        protected void onPostExecute(Boolean v) {
	            super.onPostExecute(v); 

	            updateContainer();
	            
	        }
	    }
	    
		//Updates the container with the information provided and according to the sorting mode.
		private void updateContainer(){
	    	
			mProgressBar.setVisibility(View.GONE);
	        mContainer.setVisibility(View.VISIBLE);
			
			if(closestGasstation == null || cheapestGasstation == null){
				
				//If there were no gas stations nearby that met the query requirements, we ask the user if he wants to find the
				//closest gas station without limiting the search.
				mContainer.setVisibility(View.INVISIBLE);
				
				Toast.makeText(RefuelBrowserFragment.this.getActivity(), getString(R.string.refuel_browser_no_results), Toast.LENGTH_SHORT).show();
				/*AlertDialog.Builder builder = new AlertDialog.Builder(RefuelBrowserFragment.this.getActivity());
		    	builder.setMessage(getString(R.string.refuel_browser_no_results))
		    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
		            	dialog.dismiss();
		            }
		        });
		    	
		    	builder.create().show();*/
				
				return;
			} 
	    	
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			
	    	if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
	    		//Updates the displayers highlighting the distance to the gas stations 
				labelLess.setText("-" + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
				labelMore.setText("+" + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
				updateLeftDisplayer(closestGasstation);
				updateRightDisplayer(cheapestGasstation);
			} else {
				//Updates the displayers highlighting the price of the gas stations 
				labelLess.setText(getResources().getString(R.string.refuel_browser_label_less_price));
				labelMore.setText(getResources().getString(R.string.refuel_browser_label_more_price));
				updateLeftDisplayer(cheapestGasstation);
				updateRightDisplayer(closestGasstation);
			}
	   
	    }	
		
		private void updateLeftDisplayer(Gasstation gasstation){
			int fuelType = MyApplication.getDatabaseHelper().getFuelType();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			DISTANCE_UNIT = " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km");
	
			leftDisplayerTop.setText(String.valueOf(gasstation.getTime()/60) + TIME_UNIT);
			if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
				leftDisplayerCenter.updateDigitDisplayers(gasstation.getDistance());
				leftDisplayerSub.setText(PRICE_SIGN + String.valueOf(gasstation.getPrice(fuelType)));
			} else {
				leftDisplayerCenter.updateDigitDisplayers(gasstation.getPrice(fuelType));
				leftDisplayerSub.setText(String.valueOf(((float)((int)(gasstation.getDistance()*100))/100) + DISTANCE_UNIT));
			}
			leftDisplayerLastupdate.setText(String.valueOf(gasstation.getFormattedLastUpdate(fuelType)));
			
		}
		
		private void updateRightDisplayer(Gasstation gasstation){
			int fuelType = MyApplication.getDatabaseHelper().getFuelType();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			DISTANCE_UNIT = " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km");
			
			if(cheapestGasstation != null){
				rightDisplayerTop.setText(String.valueOf(gasstation.getTime()/60 + TIME_UNIT));
				if(MyApplication.getDatabaseHelper().getMode() == GasstationsDataSource.SHORTEST_DISTANCE){
					rightDisplayerCenter.updateDigitDisplayers(gasstation.getDistance());
					rightDisplayerSub.setText(PRICE_SIGN + String.valueOf(gasstation.getPrice(fuelType)));
				} else {
					rightDisplayerCenter.updateDigitDisplayers(gasstation.getPrice(fuelType));
					rightDisplayerSub.setText(String.valueOf(((float)((int)(gasstation.getDistance()*100))/100) + DISTANCE_UNIT));
				}
				rightDisplayerLastupdate.setText(String.valueOf(gasstation.getFormattedLastUpdate(fuelType)));
			}
		}
		
		void initRefuelGasstation(Gasstation gasstation){
	
			try{
				
				Intent intent = new Intent(this.getActivity(), RefuelGasstation.class);
				intent.putExtra(getString(R.string.EXTRA_REFUELGASSTATION_ID), gasstation.getId());
				startActivity(intent);
				
			} catch(ActivityNotFoundException e){
				e.printStackTrace();
			}
		}
		
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
	
	}
}
