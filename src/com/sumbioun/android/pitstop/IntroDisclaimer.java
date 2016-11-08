package com.sumbioun.android.pitstop;

import com.sumbioun.android.pitstop.refuel.RefuelMain;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.app.Activity;
import android.content.Intent;

/*Intro Disclaimer                                                                            */
/*This activity is merely a screen that provides the user with useful information concerning  */
/*the app's intention and it's collaborative proposal. When the user touches anywhere on the  */
/*screen she is forwarded to the main activity (RefuelMain).                                    */
public class IntroDisclaimer extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_disclaimer);

        //Layout Elements Initialization
        LinearLayout body = (LinearLayout) findViewById(R.id.intro_disclaimer_body);
        body.setOnClickListener(new View.OnClickListener() {
			  	
			public void onClick(View v) {
				//Calls the RefuelMain activity    
				
				Intent intent = new Intent(IntroDisclaimer.this, RefuelMain.class);
				IntroDisclaimer.this.startActivity(intent);
				IntroDisclaimer.this.finish();
				
			}
		});
            
    }
    
    @Override
	protected void onResume() {
	    super.onResume(); 
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  
	    finish();
	}
}
