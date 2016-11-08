package com.sumbioun.android.pitstop.refuel;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.garage.GarageMain;
import com.sumbioun.android.pitstop.workshop.WorkshopMain;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/* RefuelActivity                                                                                                              */
/* This fragment activity is a parent class for all fragment activities inside the Refuel Package. It implements functionality */
/* to switch between the Refuel and the Workshop group with a finger sweeping motion through a ViewPager.                      */
public class RefuelFragmentActivity extends FragmentActivity{

	ViewPagerAdapter mViewPagerAdapter;
    ViewPager mViewPager;
	
    protected String currentLanguage = null;
    
    //This is the main fragment. The center fragment. To its left there is the GarageFragment and to its right there is the 
    //WorkshopFragment.
    Fragment mFragment;
    public void setFragment(Fragment fragment){ this.mFragment = fragment; }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refuel_fragment_activity);
        
        mViewPagerAdapter = new ViewPagerAdapter(this.getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.refuel_main_pager);
        mViewPager.setAdapter(mViewPagerAdapter);
             
        mViewPager.setCurrentItem(1);
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);  
        currentLanguage = sharedPref.getString(WorkshopSettings.KEY_PREF_LANGUAGE, "en");
   
    }
	
	//We use a FragmentStatePagerAdapter, and not a FragmentPagerAdapter, for the ViewPager to hold less memory.
	public class ViewPagerAdapter extends FragmentStatePagerAdapter {
	    
		public ViewPagerAdapter(FragmentManager fragmentManager) {
	        super(fragmentManager);
	    }
	
	    @Override
	    public Fragment getItem(int i) {
	    	
	    	switch(i){
	    	
	    		//garage fragment
	    		case 0:
	    			return new GarageMain();
	    			
	    		//central fragment
		    	case 1:
		    		return mFragment;
		    	
		        //workshop fragment
		    	case 2:
		    		return new WorkshopMain();
		    		
		    	
		    	default:
		    		return null;
	    	}
	    	
	    }
	
	    //Amount of fragments in the ViewPager
	    @Override
	    public int getCount() {
	        return 3;
	    }
	
	    //This is not used
	    @Override
	    public CharSequence getPageTitle(int position) {
	        return "";
	    }
	}

}
