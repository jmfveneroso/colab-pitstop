package com.sumbioun.android.pitstop;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.InterstitialAd;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

/*IntroSumbioun                                                                                                   */
/*This is the main activity. It only shows the company logo for a while and then fades away to the next activity. */
public class IntroSumbioun extends Activity {
	private static final int SPLASH_DISPLAY_TIME = 2000;  /* 2 seconds */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_sumbioun);
        
        
        
        new Handler().postDelayed(new Runnable() {
            public void run() {
                   
                //Create an intent that will call the loading screen.
                Intent intent = new Intent(IntroSumbioun.this, IntroLoadingScreen.class);
                IntroSumbioun.this.startActivity(intent);
               
                //Finish splash activity so user cant go back to it.
                IntroSumbioun.this.finish();
               
                //Apply our splash exit (fade out) and main entry (fade in) animation transitions.
                //Doesn't work in Samsung devices.
                overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
                    
            }
        }, SPLASH_DISPLAY_TIME);
        
        
    }
    
    @Override
	protected void onResume() {
	    super.onResume(); 
	 	    
	    MyApplication.startGps();
	    if(!MyApplication.getGps().canGetLocation()){
	    	MyApplication.getGps().showSettingsAlert();
	    }
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  
	    
	    //Stops the location manager to spare the battery since the user is no longer using the application
	    MyApplication.getGps().stopUsingGPS();        
	    
	}

}
