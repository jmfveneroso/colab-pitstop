package com.sumbioun.android.pitstop;

import java.util.Locale;

import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
import com.sumbioun.android.pitstop.map.JSONParser.OnDownloadProgress;
import com.sumbioun.android.pitstop.refuel.RefuelMain;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;

/*IntroLoadingScreen                                                                         */ 
/*This activity shows the application logo while doing the required initializations:         */
/*-retrieve the user settings and update the internal database                               */
/*-download recent data from the external database                                           */
public class IntroLoadingScreen extends Activity {

	private ProgressBar mProgressBar;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_loading_screen);
	      
	    mProgressBar = (ProgressBar) this.findViewById(R.id.intro_loading_screen_progress_bar);
  
	    if(!MyApplication.getGps().canGetLocation()){
	    	showSettingsAlert(this);
		}
	    //Calls the async load function
	    new load().execute();
	         
    }
    
    @Override
	protected void onResume() {
	    super.onResume(); 
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();        
	    
	}
	
    //Loads data from the external database, initializes the application's resources and checks the functionalities 
    //required by the application, so the user can check the progress through a progressBar.
    class load extends AsyncTask<Void, Void, Integer>{
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setMax(100); 
            

        }
        
        @Override
        protected Integer doInBackground(Void... params) {
	
        	//Sets the user preferences
        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(IntroLoadingScreen.this);
    	    String fuelType = sharedPref.getString(WorkshopSettings.KEY_PREF_FUEL_TYPE, "GASOLINE");
    	    float radius = sharedPref.getFloat(WorkshopSettings.KEY_PREF_RADIUS, 10);
    	    
    	    MyApplication.getDatabaseHelper().setRadius(radius);
    	    MyApplication.getDatabaseHelper().setFuelType(Gasstation.FuelTypes.valueOf(fuelType).value);
    	    
    	    //Sets the application language.
    	    Locale myLocale = new Locale(sharedPref.getString("pref_language", Locale.getDefault().getLanguage())); 
        	Resources res = getResources(); 
        	DisplayMetrics dm = res.getDisplayMetrics(); 
        	Configuration conf = res.getConfiguration(); 
        	conf.locale = myLocale; 
        	res.updateConfiguration(conf, dm); 
    	    
    	    //Downloads data from the external database if needed
    	    //updateDatabase();
    	    int connectivity = MyApplication.getDatabaseHelper().updateInternalDatabase(new OnDownloadProgress(){

    			public void onProgress(float progress) {
    				mProgressBar.setProgress((int)((float)progress*100));
    				
    			}
        		
        	});
    	    
    	    MyApplication.setUpdating(false);
    
			return connectivity;
				
        }
        
        @Override
        protected void onPostExecute(Integer connectivity) {
            super.onPostExecute(connectivity); 
            
	  		if(connectivity == 2){
    	    	
	  			/*if(MyApplication.getDatabaseHelper().getDatabaseSize() <= 10){
	  				*/
	  				AlertDialog.Builder builder = new AlertDialog.Builder(IntroLoadingScreen.this);
	    	    	
	    	 		builder.setMessage(getString(R.string.intro_loading_screen_no_conectivity))
	    	 		.setCancelable(false)
	    	     	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    	            
	    	             public void onClick(DialogInterface dialog, int id) {
	    	            	
	    	                //startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	    	            	startNextActivity();
	    	         		dialog.dismiss();
	    	             }
	    	        });
	    	 		builder.create().show();
	    	 		/*
	  			} else {*/
	  				
		  			//Toast.makeText(IntroLoadingScreen.this, getString(R.string.intro_loading_screen_no_conectivity), Toast.LENGTH_LONG).show();
		  			//startNextActivity();
		  			
		  		//}
		  			/*builder = new AlertDialog.Builder(IntroLoadingScreen.this);
    	    	
    	 		builder.setMessage(getString(R.string.intro_loading_screen_no_conectivity))
    	     	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    	            
    	             public void onClick(DialogInterface dialog, int id) {
    	            	
    	                //startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
    	            	startNextActivity();
    	         		dialog.dismiss();
    	             }
    	        })	
    	 		.setOnCancelListener(new OnCancelListener(){

					public void onCancel(DialogInterface dialog) {
						startNextActivity();
					}
    	 			
    	 		});
    	 		builder.create().show();*/
    	     	
    	    } else {
    	    	//Toast.makeText(IntroLoadingScreen.this, "Yo mama", Toast.LENGTH_LONG).show();
    	    	startNextActivity();
    	    }
	  		
        }
    }
    
    void startNextActivity(){
    	
    	SharedPreferences settings;
  		settings = IntroLoadingScreen.this.getSharedPreferences(GasstationsDataSource.FILENAME, Context.MODE_PRIVATE);

  		//If SHOW_DISCLAIMER is true, then we show the Disclaimer screen. If the user disables the tooltips it won't be shown again.
    	Intent mainIntent = null;
  		if(settings.getBoolean("SHOW_DISCLAIMER", true)){
            mainIntent = new Intent(IntroLoadingScreen.this, IntroDisclaimer.class);   
  		} else {
  			mainIntent = new Intent(IntroLoadingScreen.this, RefuelMain.class);
  		}
        IntroLoadingScreen.this.startActivity(mainIntent);    
       
        //Finish splash activity so user can't go back to it.
        IntroLoadingScreen.this.finish();
        
        //Apply our splash exit (fade out) and main entry (fade in) animation transitions. 
        //Apparently it does not work in Samsung devices, even with the "all animation" setting selected.
        //overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
        
    }
    
    public void showSettingsAlert(Context context){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);
 
        final Context _context = context;
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                _context.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
  
}
