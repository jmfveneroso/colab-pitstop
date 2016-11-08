package com.sumbioun.android.pitstop.workshop;

import com.sumbioun.android.pitstop.R;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/*WorkshopMain                                                                                           */
/*Main activity in the workshop group, that contains mostly tools that might prove useful to the user.   */
/*Provides links to access all the other workshop activities.                                            */
public class WorkshopMain extends Fragment {

	View rootView = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.workshop_main, container, false);
        
        TextView flexCalculator = (TextView) rootView.findViewById(R.id.workshop_main_flex_calculator);
        flexCalculator.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.clickresize));
				initWorkshopFlexCalculator();
			}
		});
        
        LinearLayout pitstopHelp = (LinearLayout) rootView.findViewById(R.id.workshop_main_pitstop_help);
        pitstopHelp.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Animation anim = AnimationUtils.loadAnimation(WorkshopMain.this.getActivity(), R.anim.clickresize);
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
        
        TextView settings = (TextView) rootView.findViewById(R.id.workshop_main_settings);
        settings.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.clickresize));
				initWorkshopSettings();
			}
		});

        return rootView;
    }
	
	private void initWorkshopFlexCalculator(){
		try{
			
			Intent intent = new Intent(getActivity(), WorkshopFlexCalculator.class);
			startActivity(intent);
			
		} catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
	}
	
	private void initWorkshopSettings(){
		try{
			
			Intent intent = new Intent(getActivity(), WorkshopSettings.class);
			startActivity(intent);
			
		} catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
	}
	
}