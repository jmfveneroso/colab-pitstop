package com.sumbioun.android.pitstop.contribute;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.StarGroup;
import com.sumbioun.android.pitstop.database.Gasstation;

import deprecated.RefuelActivity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/*ContributeRating
/*This activity provides three StarGroups in which the user can rate the quality, speed and treatment of a particular gas  */
/*station. The result is sent to the external database. If the user has already rated this gas station, her last rating is */
/*retrieved from the online database so she can change it.                                                                 */
public class ContributeRating extends RefuelActivity  {

	private StarGroup mQuality;
	private StarGroup mSpeed;
	private StarGroup mTreatment;
	
	private Gasstation mGasstation;
	private CustTextView mAddress;

	private float mRating = 0;
	
	private String mAddressLine = new String();
	
	private SubmitRating submitRating;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute_rating);

        mAddress = (CustTextView) this.findViewById(R.id.contribute_rating_address);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	
        	mGasstation = extras.getParcelable(getString(R.string.EXTRA_CONTRIBUTERATING_GASSTATION));
        	mAddressLine = extras.getString(getString(R.string.EXTRA_CONTRIBUTERATING_ADDRESS));
            mAddress.setText(mAddressLine);
            
        } else {
        	throw new RuntimeException();
        }
        
        mQuality = (StarGroup) findViewById(R.id.contribute_rating_quality);
        mSpeed = (StarGroup) findViewById(R.id.contribute_rating_speed);
        mTreatment = (StarGroup) findViewById(R.id.contribute_rating_treatment);
        
        TextView submit = (TextView) findViewById(R.id.contribute_rating_submit);
        submit.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeRating.this, R.anim.clickresize));
				//submitRating = new SubmitRating();
				//submitRating.execute();	
				initContributeComment();
			}
        });
        
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
	    
	    if(submitRating != null){
	    	submitRating.cancel(true);
	    }

	}
    
    private void initContributeComment(){
    	try{
    		mRating = (float) (mQuality.getRating() + mSpeed.getRating() + mTreatment.getRating())/3;
    		
	    	Intent intent = new Intent(ContributeRating.this, ContributeComment.class);
	        intent.putExtra(getString(R.string.EXTRA_CONTRIBUTECOMMENT_RATING), mRating);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTECOMMENT_ADDRESS), mAddressLine);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTECOMMENT_GASSTATION), mGasstation);
	    	startActivity(intent);
	    	ContributeRating.this.finish();
	    	
    	} catch(ActivityNotFoundException e){
    		e.printStackTrace();
    	}
    	
    }
    
    //Tries to retrieve the rating info for the user if she has already rated this gas station before.
    class RetrievePreviousRating extends AsyncTask<Void, Void, Void>{
    	private ProgressDialog progressDialog;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeRating.this);
            progressDialog.setMessage("Retrieving previous rating...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {

        	//Gets the user previous ratings from the external database.
			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param); 
            progressDialog.hide();  
   
        }
    }
    
    //Submits the rating to the external database.
    class SubmitRating extends AsyncTask<Void, Void, Void>{
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {

			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param); 
            
            initContributeComment();
        	
        }
    }

}