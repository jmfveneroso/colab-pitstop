package com.sumbioun.android.pitstop.refuel;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;
import com.google.ads.AdRequest.ErrorCode;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
import com.sumbioun.android.pitstop.map.MapBrowser;
import com.sumbioun.android.pitstop.map.MapConfirmLocation;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/*REFUEL - MAIN                                                                                                         */
/*This is the main activity in the application. It provides four buttons to navigate through four different tasks:      */
/*-find the closest gas station in RefuelBrowser                                                                        */
/*-find the cheapest gas station in RefuelBrowser                                                                       */
/*-open the map in MapBrowser                                                                                           */
/*-contribute, adding a gas station or editing an existing one in ContributeMain                                        */
public class RefuelMain extends RefuelFragmentActivity {
	
	//This value defines the minimum distance to the closest gas station that is interpreted as meaning the user is
	//currently positioned at this actual gas station. The value is expressed in degrees (latitude and longitude).
	public static final float MINIMUM_DISTANCE_THRESHOLD = (float) 0.1;
	
	RefuelMainFragment refuelMainFragment;
	
	SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);

        refuelMainFragment = new RefuelMainFragment();        
    	setFragment(refuelMainFragment);
    	
    	settings = RefuelMain.this.getSharedPreferences(GasstationsDataSource.FILENAME, Context.MODE_PRIVATE);
    	
	    if(settings.getBoolean("SHOW_DISCLAIMER", true)){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(getString(R.string.refuelMain_help))
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           
	            public void onClick(DialogInterface dialog, int id) {
	
	            	AlertDialog.Builder builder = new AlertDialog.Builder(RefuelMain.this);
	            	builder.setMessage(getString(R.string.refuelMain_showTooltips))
	            	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	                   
	                    public void onClick(DialogInterface dialog, int id) {
	                    	dialog.dismiss();
	                    }
	                })
	            	.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	                    
	                    public void onClick(DialogInterface dialog, int id) {

	                	    editor = settings.edit();
	                	    editor.putBoolean("SHOW_DISCLAIMER", false);
	                	    editor.commit();
	                    	dialog.dismiss();
	                    }
	                });
	            	builder.create().show();
	            	
	            	dialog.dismiss();
	            }
	        });
	    	builder.create().show();
	    	
	    }

    } 
    
    @Override
	protected void onResume() {
	    super.onResume(); 
    
	}
    
    @Override
    protected void onRestart() {
    	super.onRestart(); 

    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);  
    	
    	try{
    		
        	TextView browserDistance = (TextView) refuelMainFragment.rootView.findViewById(R.id.refuel_main_browser_distance);
        	if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
				browserDistance.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.refuel_km);
			} else {
				browserDistance.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.refuel_mi);
			}
        	
    	} catch(NullPointerException e){
    		
    	}

    }
    
    //This fragment constitutes the leftmost tab in the activity's ViewPager
    public static class RefuelMainFragment extends Fragment implements AdListener {
		
    	private InterstitialAd interstitial;
    	View rootView;
    	
	    @Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	    	
			rootView = inflater.inflate(R.layout.refuel_main, container, false);

			//UI initialization
	        TextView browserDistance = (TextView) rootView.findViewById(R.id.refuel_main_browser_distance);
	        browserDistance.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){
					    public void onAnimationStart(Animation arg0) {
					    }           
					    public void onAnimationRepeat(Animation arg0) {
					    }           
					    public void onAnimationEnd(Animation arg0) {
					    	initRefuelBrowserDistance();
					    }
					});
					v.startAnimation(anim);	
				}
			});
	        
	        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RefuelMainFragment.this.getActivity());
			if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
				browserDistance.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.refuel_km);
			} else {
				browserDistance.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.refuel_mi);
			}
	        
	        TextView browserPrice = (TextView) rootView.findViewById(R.id.refuel_main_browser_price);
	        browserPrice.setOnClickListener(new View.OnClickListener() {
	        	
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){
					    public void onAnimationStart(Animation arg0) {
					    }           
					    public void onAnimationRepeat(Animation arg0) {
					    }           
					    public void onAnimationEnd(Animation arg0) {
					    	initRefuelBrowserPrice();
					    }
					});
					v.startAnimation(anim);	
				}
			});
	             
	        LinearLayout pitstopHelp = (LinearLayout) rootView.findViewById(R.id.refuel_main_pitstop_help);
	        pitstopHelp.setOnClickListener(new View.OnClickListener(){

				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){
					    public void onAnimationStart(Animation arg0) {
					    }           
					    public void onAnimationRepeat(Animation arg0) {
					    }           
					    public void onAnimationEnd(Animation arg0) {
					    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sumbioun.com/pitstop"));
					        startActivity(browserIntent);
					    }
					});
					v.startAnimation(anim);	
					
				}
	        	
	        });

	        TextView map = (TextView) rootView.findViewById(R.id.refuel_main_map);
	        map.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){
					    public void onAnimationStart(Animation arg0) {
					    }           
					    public void onAnimationRepeat(Animation arg0) {
					    }           
					    public void onAnimationEnd(Animation arg0) {
					    	initMapBrowser();
					    }
					});
					v.startAnimation(anim);	
			    	
				}
			});
	        
	        TextView contribute = (TextView) rootView.findViewById(R.id.refuel_main_contribute);
	        contribute.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){
					    public void onAnimationStart(Animation arg0) {
					    }           
					    public void onAnimationRepeat(Animation arg0) {
					    }           
					    public void onAnimationEnd(Animation arg0) {
					    	initContribute();
					    }
					});
					v.startAnimation(anim);	
					
					
				}
			});
	        
	        CustTextView route = (CustTextView) rootView.findViewById(R.id.refuel_main_route);
	        route.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					Animation anim = AnimationUtils.loadAnimation(RefuelMainFragment.this.getActivity(), R.anim.clickresize);
					anim.setAnimationListener(new Animation.AnimationListener(){  
						public void onAnimationRepeat(Animation arg0) {}
						public void onAnimationStart(Animation animation) {	}
						public void onAnimationEnd(Animation arg0) {
							try{
					    		Intent intent = new Intent(RefuelMainFragment.this.getActivity(), RefuelGetRoute.class);
				    			startActivity(intent);
					    	} catch(ActivityNotFoundException e){
					    		e.printStackTrace();
					    	}
					    }
					});
					v.startAnimation(anim);	
					
				}
				
			});
	        
	        interstitial = new InterstitialAd(this.getActivity(), "ca-app-pub-1978179266754065/5796971233");
	        
	        AdRequest adRequest = new AdRequest();
	        //adRequest.addTestDevice("D41628AB291D6462B7331439BDF3B4C4");

	        interstitial.loadAd(adRequest);
	        interstitial.setAdListener(this);
			
	        return rootView;
	    }
	    
	    private void initRefuelBrowserDistance(){
	    	
	    	try {
	    		
				Intent intent = new Intent(getActivity(), RefuelBrowser.class);
				intent.putExtra(getString(R.string.EXTRA_SORTER), GasstationsDataSource.SHORTEST_DISTANCE);
				startActivity(intent);	
			
		    } catch(ActivityNotFoundException e){	    		 
		   		e.printStackTrace();	 
		   	}
			
	    }
	    
	    private void initRefuelBrowserPrice(){
	    	
	    	try {
	    		
		    	Intent intent = new Intent(getActivity(), RefuelBrowser.class);
				intent.putExtra(getString(R.string.EXTRA_SORTER), GasstationsDataSource.LOWEST_PRICE);
				startActivity(intent);
				
	    	} catch(ActivityNotFoundException e){	    		 
		   		e.printStackTrace();	 
		   	}
			
	    }
	    
	    private void initMapBrowser(){
	    	
	    	try {

		    	Intent intent = new Intent(getActivity(), MapBrowser.class);
				startActivity(intent);
				
	    	} catch(ActivityNotFoundException e){	    		 
		   		e.printStackTrace();	 
		   	}
			
	    }
	    
	    private void initContribute(){
    		
	    	try {
	    	
	    		Intent intent  = new Intent(getActivity(), MapConfirmLocation.class);
	    		startActivity(intent);
	    	
		    } catch(ActivityNotFoundException e){	    		 
	   		 e.printStackTrace();	 
	   	 	}
	    	
	    }

	    public void onReceiveAd(Ad ad) {
	  	  
		    if (ad == interstitial) {
		      interstitial.show();
		    }
	    }

		public void onDismissScreen(Ad ad) {}
		public void onFailedToReceiveAd(Ad ad, ErrorCode error) {}
		public void onLeaveApplication(Ad ad) {}
		public void onPresentScreen(Ad ad) {}
	    
	}
    
    
    
}
