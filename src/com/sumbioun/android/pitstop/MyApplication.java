package com.sumbioun.android.pitstop;

import java.util.Locale;

import com.sumbioun.android.pitstop.database.GPSTracker;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;

import android.app.Application;
import android.content.Context;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*MyApplication
/*This class extends the application class and stores references to the internal database and the LocationManager
/*to be used globally throughout the application. */
public class MyApplication extends Application {
	
	private static boolean isUpdating = true;
	
	public static boolean canUpdate(){
		return isUpdating;			
	}
	public static void setUpdating(boolean bool){
		isUpdating = bool;			
	}
	
	//To make sure there is only one instance of this class running at any time.
	private static MyApplication singleton;
	
	//The database helper.
	private static GasstationsDataSource mDbHelper;
	
	//The LocationManager.
	private static GPSTracker gps;
	
	//Translates coordinates into address and vice versa
	private static Geocoder geocoder;

	public static MyApplication getInstance(){
		return singleton;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		mDbHelper = new GasstationsDataSource(this);
        mDbHelper.open();
        startGps();
        
        try{
        	geocoder = new Geocoder(this, Locale.getDefault());
        } catch (NullPointerException e) {
        	e.printStackTrace();
        }
    }

	//On normal circumstances android should close the database automatically when it no longer needs it. This function
	//is only for developmental purposes.
    @Override
    public void onTerminate() {
        mDbHelper.close();
        mDbHelper = null;
    }
    
    //Called whenever the application returns from a stopped state.
    public static void startGps(){
    	gps = new GPSTracker(singleton);
    	
    }
    
    public static GasstationsDataSource getDatabaseHelper() {
        return mDbHelper;
    }
    
    public static GPSTracker getGps() {
        return gps;
    }
    
    public static Geocoder getGeocoder() {
        return geocoder;
    }
    
    public boolean checkOnlineState() {
    	
        ConnectivityManager CManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo NInfo = CManager.getActiveNetworkInfo();
        if (NInfo != null && NInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
        
    }
}
