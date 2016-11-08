package deprecated;

import java.io.IOException;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.map.MapConfirmLocation;

import deprecated.RefuelActivity;

/*ContributeNew                                                                           */
/*This activity is called when a new gas station is going to be created. It only shows    */
/*the address of the new gas station and asks for confirmation.                           */
public class ContributeNew extends RefuelActivity  {

	CustTextView mAddress;
	CustTextView addGasstation;
	
	ProgressBar mProgressBar;
	
	String mAddressLine = new String();
	
	double mLatitude = 0;
	double mLongitude = 0;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute_new);

        mProgressBar = (ProgressBar) findViewById(R.id.contribute_new_progress_bar);
        
        mAddress = (CustTextView) findViewById(R.id.contribute_new_address);
        mAddress.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeNew.this, R.anim.clickresize));
				initMapConfirmLocation();
			}
        	
        });
        
        addGasstation = (CustTextView) findViewById(R.id.contribute_new_add_gasstation);
        addGasstation.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				v.startAnimation(AnimationUtils.loadAnimation(ContributeNew.this, R.anim.clickresize));
				initContributeMain();
			
			}
		});
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            
        	mLatitude = extras.getDouble(getString(R.string.EXTRA_CONTRIBUTENEW_LATITUDE));
        	mLongitude = extras.getDouble(getString(R.string.EXTRA_CONTRIBUTENEW_LONGITUDE));
        	
        } else {
        	
        	//Since no coordinates were provided, we get the user's position as the location of the new gas station.
        	mLatitude = MyApplication.getGps().getLatitude();
    		mLongitude = MyApplication.getGps().getLongitude();
        	     	
        }
        
        new SetAddress().execute();
        
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
	
	private void initMapConfirmLocation(){
		
		try{
			
			Intent intent = new Intent(ContributeNew.this, MapConfirmLocation.class);
			startActivity(intent);
			ContributeNew.this.finish();
			
		} catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
	}
	
	private void initContributeMain(){
		
		try{
			
			//Intent intent = new Intent(ContributeNew.this, ContributeMain.class);
			//intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LATITUDE), mLatitude);
		    //intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_LONGITUDE), mLongitude);
		    //intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_ID), ContributeMain.CREATE_NEW_GASSTATION);
		    
			//startActivity(intent);	
			//ContributeNew.this.finish();
			
		} catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
	}
	
	class SetAddress extends AsyncTask<Void, Void, String>{
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mProgressBar.setVisibility(View.VISIBLE);
	        mAddress.setVisibility(View.GONE);
	    }
	    @Override
	    protected String doInBackground(Void... params) {
	
	    	try {
		
	    		List<Address> listAddress =  MyApplication.getGeocoder().getFromLocation(mLatitude, mLongitude, 1);
	    		mAddressLine = listAddress.get(0).getAddressLine(0);

	    	} catch (IOException e) {
				e.printStackTrace();
				ContributeNew.this.finish();
				
			} catch(Exception e){
				e.printStackTrace();
				ContributeNew.this.finish();
			}
			return mAddressLine;
				
	    }
	    @Override
	    protected void onPostExecute(String string) {
	        super.onPostExecute(string); 
	        mProgressBar.setVisibility(View.GONE);
	        if(string != null){
		        mAddress.setText(string);	        
		        mAddress.setVisibility(View.VISIBLE);
	        } else {
	        	ContributeNew.this.finish();
	        }
	    }
	}

}
