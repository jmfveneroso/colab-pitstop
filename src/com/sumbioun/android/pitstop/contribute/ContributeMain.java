package com.sumbioun.android.pitstop.contribute;

import java.util.List;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.PriceDisplayer;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.Gasstation.Flag;
import com.sumbioun.android.pitstop.map.ExternalGasstationDecoder;

import deprecated.RefuelActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*ContributeMain
/*This activity handles price and flag edition and exits calling ContributeExtra */ 
public class ContributeMain extends RefuelActivity  {
	
	public static long CREATE_NEW_GASSTATION = -1;
	
	double mLatitude;
	double mLongitude;
	
	int selectedFlag = Flag.UNDEFINED.ordinal();
	
	private CustTextView mAddress;
	
	private LinearLayout mainLayout; 
	
	PriceDisplayer[] priceDisplayer = new PriceDisplayer[5];
	Gasstation mGasstation;
	
	private String mAddressLine = new String();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute_main);
        
        mainLayout = (LinearLayout) this.findViewById(R.id.contribute_main_main_layout);
        mainLayout.setVisibility(View.INVISIBLE);
        
        mGasstation = new Gasstation();
	    
	    priceDisplayer[0] = (PriceDisplayer) this.findViewById(R.id.contribute_main_price_displayer_gas);
        priceDisplayer[1] = (PriceDisplayer) this.findViewById(R.id.contribute_main_price_displayer_alc);
        priceDisplayer[2] = (PriceDisplayer) this.findViewById(R.id.contribute_main_price_displayer_lea);
        priceDisplayer[3] = (PriceDisplayer) this.findViewById(R.id.contribute_main_price_displayer_die);
        priceDisplayer[4] = (PriceDisplayer) this.findViewById(R.id.contribute_main_price_displayer_nat);    
	    
        mAddress = (CustTextView) this.findViewById(R.id.contribute_main_address);

        TextView delete = (TextView) findViewById(R.id.contribute_main_delete);
        delete.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeMain.this, R.anim.clickresize));		
				deleteGasstation();
			}
        });
        
        try{
        	
	        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
	        	
	        	mGasstation.setId(extras.getLong(getString(R.string.EXTRA_CONTRIBUTEMAIN_ID)));
	        	
	        	if(mGasstation.getId() == CREATE_NEW_GASSTATION){
					 
	            	//create new gas station
	            	mLatitude = extras.getDouble(getString(R.string.EXTRA_CONTRIBUTEMAIN_LATITUDE));
	                mLongitude = extras.getDouble(getString(R.string.EXTRA_CONTRIBUTEMAIN_LONGITUDE)); 
	                delete.setVisibility(View.GONE);
	                
	            }

	        	new SetGasstation().execute();	
	        	
	        	Log.w("ContributeMain gas id", mGasstation.getId()+"");
	            
	        }
	        
        } catch(NullPointerException e){
        	e.printStackTrace();
        }
        
        LinearLayout flagGroup = (LinearLayout) this.findViewById(R.id.contribute_main_flag_group);
        for(int i = 0; i < flagGroup.getChildCount(); i++){
        	ImageView imageView = (ImageView) flagGroup.getChildAt(i);
        	final int index = i;
        	
        	imageView.setOnClickListener(new OnClickListener(){
        		
				public void onClick(View view) {
					selectFlag(index);
				}
        		
        	});
        }

        TextView submit = (TextView) findViewById(R.id.contribute_main_submit);
        submit.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeMain.this, R.anim.clickresize));
				
				boolean NO_PRICE_UPDATED = true;
            	for(int i = 0; i < 5; i++){
            		if(priceDisplayer[i].getWasUpdated()){
            			NO_PRICE_UPDATED = false;
            			mGasstation.setLastUpdate(i);
            		}
            	}
            	
            	if(NO_PRICE_UPDATED){
            		AlertDialog.Builder builder = new AlertDialog.Builder(ContributeMain.this);
                	builder.setMessage(getString(R.string.contribute_main_confirm_price))
                	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
           
			            public void onClick(DialogInterface dialog, int id) {
			            	dialog.dismiss();
			           
			            }
			        });
                	builder.create().show();
            	} else {
            		submitChanges();
            	}
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

	}
    
    //Highlights the selected flag.
    private void selectFlag(int value){
    	
    	selectedFlag = value;
    	
    	LinearLayout flag_group = (LinearLayout) this.findViewById(R.id.contribute_main_flag_group);
        for(int i = 0; i < flag_group.getChildCount(); i++){
        	ImageView imageView = (ImageView) flag_group.getChildAt(i);
        	
        	int tint = 0;
    		
    		if(i == value){
    			tint = getResources().getColor(R.color.opaque_yellow);
    		} else {
    			tint = getResources().getColor(R.color.opaque_white);
    		}

    		imageView.setColorFilter(tint, Mode.SRC_ATOP);
				
        }
    }
    
    private void deleteGasstation(){
		
		try{

			AlertDialog.Builder builder = new AlertDialog.Builder(ContributeMain.this);
			builder.setMessage(getString(R.string.refuel_gasstation_delete_confirmation))
	    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	           
	            public void onClick(DialogInterface dialog, int id) {
	            	MyApplication.getDatabaseHelper().deleteGasstation(mGasstation.getId());
					DeleteGasstation deleteGasstation = new DeleteGasstation();
					deleteGasstation.execute();
            		dialog.dismiss();
	            }
	        })
	        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int id) {
	            	dialog.dismiss();
	            }
	        });
	    	builder.create().show();
			
		} catch(ActivityNotFoundException e){
			e.printStackTrace();
		}
		
	}
    
    class DeleteGasstation extends AsyncTask<Void, Void, Void>{
    	private ProgressDialog progressDialog;
    		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeMain.this);
            progressDialog.setMessage(getString(R.string.refuel_gasstation_deleting_gasstation));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            
        }
        @Override
        protected Void doInBackground(Void... params) {

        	ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
			edd.deleteGasstation(mGasstation.getId());
			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);         
            progressDialog.dismiss();  
            ContributeMain.this.finish();

        }
    }
    
    //Find current address asynchronously.
    class SetGasstation extends AsyncTask<Void, Void, Void>{
		  
    	private ProgressDialog progressDialog;
    	private boolean noConnectivity = false;
    	private long id;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeMain.this);
            progressDialog.setMessage(getString(R.string.contribute_main_loading_data));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            
        }
        @Override
        protected Void doInBackground(Void... params) {

			try {
				
				if(mGasstation.getId() == CREATE_NEW_GASSTATION){	            	
	            	
	            } else {
	            	
	            	//Edit existing gas station
	            	//Gets gas station from external database
	            	//mGasstation = MyApplication.getDatabaseHelper().getGasstationById(mGasstation.getId());
	            	
	            	ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
	            	id = mGasstation.getId();
	            	mGasstation = edd.getGasstation(id);
	            	if(mGasstation == null){
	            		//If the gas station doesn't exist anymore in the external database
	            		//we delete it and return to the main screen.	
	            		
	            	} else {
	            	
		            	selectedFlag = mGasstation.getFlag();
		            	
		            	mLatitude = mGasstation.getLatitude();
		            	mLongitude = mGasstation.getLongitude();
		            	
	            	}

	            }
				
	    		List<Address> list =  MyApplication.getGeocoder().getFromLocation(mLatitude, mLongitude, 1);
	    		mAddressLine = list.get(0).getAddressLine(0);
    		
	    	} catch (Exception e) {
				e.printStackTrace();
				noConnectivity = true;
			}
			
			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param); 
            progressDialog.dismiss(); 
            
            if(mGasstation != null){
            	
	            mAddress.setText(mAddressLine);
	            
	            //If we are editing an existing gas station, we update the price displayers so they show the fuel prices as
	            //they were last updated.
	            if(mGasstation.getId() != CREATE_NEW_GASSTATION){
		            for(int i = 0; i < 5; i++) {
		        		priceDisplayer[i].updateDigitDisplayers(mGasstation.getPrice(i));
		        	}
	            }
	            
	            selectFlag(selectedFlag);
	            
	            mainLayout.setVisibility(View.VISIBLE);
	            
            } else {
            	
            	if(noConnectivity){
            		
	            	AlertDialog.Builder builder = new AlertDialog.Builder(ContributeMain.this);
					builder.setMessage(getString(R.string.no_connection))
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
	
						public void onClick(DialogInterface dialog, int which) {
							ContributeMain.this.finish();
							dialog.dismiss();
							
						}
						
					})
			    	.create().show();	
					
            	} else {
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(ContributeMain.this);
    				builder.setMessage(getString(R.string.contribute_main_gas_deleted))
    		    	.create().show();
            		
            		MyApplication.getDatabaseHelper().deleteRow(id);
            		ContributeMain.this.finish();
            		
            	}
            }
 
        }
    }
    
    //Adds a gas station to the external database.
    class SubmitChanges extends AsyncTask<Void, Void, Void>{
		  	 	
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
            
            submitChanges();
			
        }
    }
    
    void submitChanges(){
    	
    	try{
        	
        	double price[] = { (double)priceDisplayer[0].getValue(), (double)priceDisplayer[1].getValue(), (double)priceDisplayer[2].getValue(), (double)priceDisplayer[3].getValue(), (double)priceDisplayer[4].getValue() };		
			
        	mGasstation.setFlag(selectedFlag);
        	mGasstation.setPriceArray(price);
        	mGasstation.setLatitude(mLatitude);
        	mGasstation.setLongitude(mLongitude);
        	
            Intent intent = new Intent(ContributeMain.this, ContributeExtra.class);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEEXTRA_GASSTATION), mGasstation);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEEXTRA_ADDRESS), mAddressLine);
			startActivity(intent);	 
			ContributeMain.this.finish();

        } catch(ActivityNotFoundException e){
        	e.printStackTrace();
        }
    	
    }

}