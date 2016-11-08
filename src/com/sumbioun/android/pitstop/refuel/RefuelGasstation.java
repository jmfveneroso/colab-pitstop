package com.sumbioun.android.pitstop.refuel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.contribute.ContributeMain;
import com.sumbioun.android.pitstop.contribute.ContributeRating;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.StarGroup;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.map.ExternalGasstationDecoder;
import com.sumbioun.android.pitstop.map.JSONCommentDecoder;
import com.sumbioun.android.pitstop.map.JSONCommentDecoder.Comment;
import com.sumbioun.android.pitstop.map.JSONPictureDecoder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/*RefuelGasstation                                                                                                     */
/*This activity shows particular information about a gas station like location, prices, rating, services, forms of     */
/*payment, comments and pictures. The services are represented through a group of icons. This activity has a lot of    */
/*views but the functioning is very simple.                                                                            */                                                                                                  
public class RefuelGasstation extends RefuelFragmentActivity  {

	RefuelGasstationFragment mRefuelGasstationFragment;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mRefuelGasstationFragment = new RefuelGasstationFragment();
		
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	mRefuelGasstationFragment.setArguments(extras);
        }
		
    	setFragment(mRefuelGasstationFragment);
    	
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
	 
	//Activity's main fragment
	public static class RefuelGasstationFragment extends Fragment {
	
		Gasstation mGasstation;
		
		ImageView mDashedLine[] = new ImageView[5];
			
		ImageView mFlag;
		CustTextView mName;
		CustTextView mAddress;
		CustTextView mLocale;
		
		StarGroup mRating;
		
		CustTextView mFuelLabel[] = new CustTextView[5];
		CustTextView mPrice[] = new CustTextView[5];
		CustTextView mLastUpdateDisplayer[] = new CustTextView[5];
		
		LinearLayout flexFrame;
		CustTextView mFlexPercentageDisplayer;
		CustTextView mBestFuelOptionDisplayer;
		
		CustTextView mBusinessHours;
		CustTextView mPhoneNumber;
		
		ImageView mExtras[] = new ImageView[7];
		
		LinearLayout mPaymentMethodsContainer;
		ImageView mPaymentMethods[] = new ImageView[5];
		
		private RelativeLayout mainLayout;

		private LinearLayout mCommentLabelContainer;
		
		private LinearLayout mCommentList;
		
		ViewPictures viewPictures = null;
		
		View rootView;
		
		//-1 stands means an error (the gas station data could not be retrieved).
		long mGasId = -1;
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
	        super.onCreate(savedInstanceState);
	        rootView = inflater.inflate(R.layout.refuel_gasstation, container, false);
	        
	        //UI initialization
	        mainLayout = (RelativeLayout) rootView.findViewById(R.id.refuel_gasstation_main_layout);
	        mainLayout.setVisibility(View.INVISIBLE);
	        
	        mFlag = (ImageView) rootView.findViewById(R.id.refuel_gasstation_flag);
	        mName = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_name);
	        mAddress = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_address);
	        mLocale = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_locale);
	       
	    	mFuelLabel[0] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_gas_label);
	    	mFuelLabel[1] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_alc_label);
	    	mFuelLabel[2] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_lea_label);
	    	mFuelLabel[3] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_die_label);
	    	mFuelLabel[4] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_nat_label);
	    	
	    	mDashedLine[0] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_gas_dashed_line);
	    	mDashedLine[1] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_alc_dashed_line);
	    	mDashedLine[2] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_lea_dashed_line);
	    	mDashedLine[3] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_die_dashed_line);
	    	mDashedLine[4] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_nat_dashed_line);
	    	
	    	mPrice[0] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_gas_price);
	    	mPrice[1] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_alc_price);
	    	mPrice[2] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_lea_price);
	    	mPrice[3] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_die_price);
	    	mPrice[4] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_nat_price);
	        
	        mLastUpdateDisplayer[0] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_gas_last_update);
	        mLastUpdateDisplayer[1] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_alc_last_update);
	        mLastUpdateDisplayer[2] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_lea_last_update);
	        mLastUpdateDisplayer[3] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_die_last_update);
	        mLastUpdateDisplayer[4] = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_nat_last_update);
	        
	        flexFrame = (LinearLayout) rootView.findViewById(R.id.refuel_gasstation_flex_frame);
	        mFlexPercentageDisplayer = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_flex_percentage); 
	        mBestFuelOptionDisplayer = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_best_fuel_option);
	        
	        mBusinessHours = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_business_hours);
	        mPhoneNumber = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_phone_number);
	        
	        mExtras[0] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_store);
	        mExtras[1] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_drugstore);
	        mExtras[2] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_carwash);
	        mExtras[3] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_manual);
	        mExtras[4] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_pressure);
	        mExtras[5] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_restaurant);
	        mExtras[6] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_repair);
	        
	        mPaymentMethodsContainer = (LinearLayout) rootView.findViewById(R.id.refuel_gasstation_payment_methods);
	        
	        mPaymentMethods[0] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_visa);
	        mPaymentMethods[1] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_mastercard);
	        mPaymentMethods[2] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_american_express);
	        mPaymentMethods[3] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_visaelectron);
	        mPaymentMethods[4] = (ImageView) rootView.findViewById(R.id.refuel_gasstation_check);
	        
	        for(ImageView img : mPaymentMethods){
	        	img.setVisibility(View.GONE);
	        }
	        
	        mCommentLabelContainer = (LinearLayout) rootView.findViewById(R.id.refuel_gasstation_comment_label_container);
	        mCommentLabelContainer.setVisibility(View.GONE);
	        
	        mCommentList = (LinearLayout) rootView.findViewById(R.id.refuel_gasstation_comment_list);
	        
	        initLayout();
	        
	        mRating = (StarGroup) rootView.findViewById(R.id.refuel_gasstation_rating);
	        mRating.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					v.startAnimation(AnimationUtils.loadAnimation(RefuelGasstationFragment.this.getActivity(), R.anim.clickresize));
					initContributeRating();
				}
	        });
	        
	        CustTextView route = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_route);
	        route.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					v.startAnimation(AnimationUtils.loadAnimation(RefuelGasstationFragment.this.getActivity(), R.anim.clickresize));
					Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+MyApplication.getGps().getLatitude()+","+MyApplication.getGps().getLongitude()+"&daddr="+mGasstation.getLatitude()+","+mGasstation.getLongitude()));
					startActivity(navIntent);				
	
				}
			});
	        
	        CustTextView edit = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_edit);
	        edit.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					v.startAnimation(AnimationUtils.loadAnimation(RefuelGasstationFragment.this.getActivity(), R.anim.clickresize));
					initEditGasstation();				
	
				}
			});
	        
	        CustTextView pictures = (CustTextView) rootView.findViewById(R.id.refuel_gasstation_pictures);
	        pictures.setOnClickListener(new View.OnClickListener() {
	
				public void onClick(View v) {
					v.startAnimation(AnimationUtils.loadAnimation(RefuelGasstationFragment.this.getActivity(), R.anim.clickresize));
					initPicturesViewer();				
	
				}
			});
	        
	        return rootView;
	    }
		
		synchronized private void initLayout(){
			Bundle extras = this.getArguments();
	        if (extras != null) {
	        	mGasId = extras.getLong(getString(R.string.EXTRA_REFUELGASSTATION_ID));	
	        	new GetGasstation(mGasId).execute();
	        	
	        }
			
		}
	    
		private void initContributeRating(){
			
			try{
				
				Intent intent = new Intent(RefuelGasstationFragment.this.getActivity(), ContributeRating.class);
				intent.putExtra(getString(R.string.EXTRA_CONTRIBUTERATING_GASSTATION), mGasstation);
				startActivity(intent);
				
			} catch(ActivityNotFoundException e){
				e.printStackTrace();
			}
			
		}
		
		/*private void deleteGasstation(){
			
			try{
	
				AlertDialog.Builder builder = new AlertDialog.Builder(RefuelGasstationFragment.this.getActivity());
				builder.setMessage(getString(R.string.refuel_gasstation_delete_confirmation))
		    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
		            	MyApplication.getDatabaseHelper().deleteGasstation(mGasId);
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
			
		}*/
		
		private void initEditGasstation(){
			
			try{
				
				Intent intent = new Intent(RefuelGasstationFragment.this.getActivity(), ContributeMain.class);
				intent.putExtra(getString(R.string.EXTRA_CONTRIBUTEMAIN_ID), mGasId);
				startActivity(intent);
				
			} catch(ActivityNotFoundException e){
				e.printStackTrace();
			}
			
		}
		
		private void initPicturesViewer(){
			
			if(viewPictures != null){
				viewPictures.cancel(true);
			}
			
			viewPictures = new ViewPictures();
			viewPictures.execute();
		}
		
		private void setLocation(Address address){
			
			if(address == null){ return; }
			
    		mAddress.setText(address.getAddressLine(0).split("-")[0].trim());
    		
    		String str = address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryCode();
    		mLocale.setText(str);
	    }
		
		private void setRating(){
			//get rating and set it to the star group view
			try{
				if(mGasstation.getRating() != 0){
					mRating.setRating(mGasstation.getRating());
				} else {
					mRating.setVisibility(View.GONE);
				}
			} catch (NullPointerException e){
			
			}
			 
		}

		private void setPrices(double prices[]){
	    	for(int i = 0; i < Gasstation.FuelTypes.values().length; i++){
	    		if(prices[i] > 0){
	    			//Sets the price format to: "X.XXX".
		    		String str = String.format(Locale.getDefault(), "%.3f", prices[i]);
		    		mPrice[i].setText(str);
	    		} else {
	    			mPrice[i].setText("");
	    		}
	    	}
	    }
	    
	    private void setLastUpdate(Gasstation gas){
	    	for(int i = 0; i < Gasstation.FuelTypes.values().length; i++){
	    		mLastUpdateDisplayer[i].setText(gas.getFormattedLastUpdate(i));
	    	}
	    }
	    
	    //This function finds the most efficient fuel according to the prices provided for alcohol and gasoline.
	    private void setFlex(){
	    	double gasPrice = mGasstation.getPrice(Gasstation.FuelTypes.GASOLINE.value);
	    	double alcPrice = mGasstation.getPrice(Gasstation.FuelTypes.ALCOHOL.value);
	    	
	    	//If there is no information about the price of gasoline or alcohol, then we can't find the best fuel.
	    	if(gasPrice == 0 || alcPrice == 0) {
	    		flexFrame.setVisibility(View.GONE);
	    		return;
	    	}
	    	
	    	//Alcohol's average performance is usually only 70% of gasoline's, so its price has to be less than 
	    	//70% the price of gasoline. Otherwise it's not worth it.
	    	
	    	double _priceDifference = Math.abs(((double)1-((double)alcPrice/gasPrice))*100);
	    	String priceDifference = Double.toString(_priceDifference);
	    	priceDifference = (priceDifference.length() > 4 ? priceDifference.substring(0, 4) : priceDifference)  + "%";
	    	mFlexPercentageDisplayer.setText(priceDifference);
	    	
	    	if(alcPrice < gasPrice*0.7f){ 
	    		//alcohol is the better option
	    		mBestFuelOptionDisplayer.setText(R.string.refuel_gasstation_alcohol);
	    	} else { 
	    		//gasoline is the better option
	    		mBestFuelOptionDisplayer.setText(R.string.refuel_gasstation_gasoline);
	    	}
	
	    }
  
	    //Sets info about the opening hours and the gas station's phone. If any of those is missing we will not set a "|" between them.
	    private void setInfo(){
	    	
	    	boolean ALWAYSOPEN = false;
	    	if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_24HOURS)){
	    		
	    		mBusinessHours.setText(getString(R.string.refuel_gasstation_business_hours));
	    		ALWAYSOPEN = true;
	    		
	    	} else {
	    		mBusinessHours.setText("");
	    	}
	    	
	    	if(mGasstation.getPhoneNumber().contentEquals("") == false){
	    		if(ALWAYSOPEN){
	    			mPhoneNumber.setText(" | " + mGasstation.getPhoneNumber());
    			} else {
    				mPhoneNumber.setText(mGasstation.getPhoneNumber());
    			}
	    	} else {
	    		mPhoneNumber.setText("");
	    	}
	    }
	    
	    private void setExtras(){
	    	
	    	boolean NO_PAYMENT_METHODS = true;
	    	if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISA)){
	    		mPaymentMethods[0].setVisibility(View.VISIBLE);
	    		NO_PAYMENT_METHODS = false;
	    	}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MASTERCARD)){
				mPaymentMethods[1].setVisibility(View.VISIBLE);  
				NO_PAYMENT_METHODS = false;
			}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AMERICANEXPRESS)){
				mPaymentMethods[2].setVisibility(View.VISIBLE);
				NO_PAYMENT_METHODS = false;
			}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISAELECTRON)){
				mPaymentMethods[3].setVisibility(View.VISIBLE);
				NO_PAYMENT_METHODS = false;
			}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_CHECK)){
				mPaymentMethods[4].setVisibility(View.VISIBLE);
				NO_PAYMENT_METHODS = false;
			}
			
			if(NO_PAYMENT_METHODS){
				mPaymentMethodsContainer.setVisibility(View.GONE);
			} else {
				mPaymentMethodsContainer.setVisibility(View.VISIBLE);
			}
			
			for(ImageView view : mExtras){
				view.setVisibility(View.GONE);
			}
			
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_STORE)){
				mExtras[0].setVisibility(View.VISIBLE);
			}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_DRUGSTORE)){
				mExtras[1].setVisibility(View.VISIBLE);
			}
			
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_PRESSURECARWASH)){
				mExtras[4].setVisibility(View.VISIBLE);
			} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MANUALCARWASH)){
				mExtras[3].setVisibility(View.VISIBLE);
			} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AUTOCARWASH)){
				mExtras[2].setVisibility(View.VISIBLE);
			}
	
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_REPAIR)){
				mExtras[5].setVisibility(View.VISIBLE);
			}
			if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT)){
				mExtras[6].setVisibility(View.VISIBLE);
			}
			
	    }
	    
	    //This class retrieves gas station data and initializes the layout.
	    class GetGasstation extends AsyncTask<Void, Void, Address>{
	    	private ProgressDialog progressDialog;
	    	private long mGasId;
	    	
	    	public GetGasstation(long id){
	    		mGasId = id;

	    	}
	    	
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            progressDialog = new ProgressDialog(RefuelGasstationFragment.this.getActivity());
	            progressDialog.setMessage(getString(R.string.refuel_gasstation_loading_gas_data));
	            progressDialog.setIndeterminate(true);
	            progressDialog.show();
	        }
	        
	        @Override
	        protected Address doInBackground(Void... params) {

	        	try{
	        		//Gets the gas station data from the external database.
		        	ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
	            	mGasstation = edd.getGasstation(mGasId);  
	            	
	            	//If the external database in unaccessible we get the data from the internal database.
	            	if(mGasstation == null){
	            		
	            		mGasstation = MyApplication.getDatabaseHelper().getGasstationById(mGasId);
	            		if(mGasstation == null){
		            		//If the gas station doesn't exist anymore in the internal database we finish the activity. 		
		            		RefuelGasstationFragment.this.getActivity().finish();
		            	
		            	}
	            	}  	
	            
	        	} catch( Exception e){
	        		e.printStackTrace();
	        	}
	        	
	        	Address address = null;
	        	try {
		    		
		    		List<Address> listAddress =  MyApplication.getGeocoder().getFromLocation(mGasstation.getLatitude(), mGasstation.getLongitude(), 10);
		    		address = listAddress.get(0);
 		
		    	} catch (IOException e) {
					e.printStackTrace();
					
				} catch (NullPointerException e){
					e.printStackTrace();
				}
	        	
	        	return address;
					
	        }
	        
	        @Override
	        protected void onPostExecute(Address address) {
	            super.onPostExecute(address); 
	            progressDialog.dismiss();  
	            
	            if(mGasstation != null){
	            	
		        	mFlag.setImageResource(Gasstation.Flag.values()[mGasstation.getFlag()].big);
		        	mName.setText(Gasstation.Flag.values()[mGasstation.getFlag()].toString());
		        	
		        	//Initializes various groups of UI elements.
		        	setLocation(address);     	
		        	setRating();
		        	setPrices(mGasstation.getPriceArray());
		        	setLastUpdate(mGasstation);
		        	setFlex();
		        	setInfo();
		        	setExtras();
		        	
		        	new QueryComments(mGasId).execute();
		        	
	        	} else {
	        		RefuelGasstationFragment.this.getActivity().finish();
	        	}
	            
	            mainLayout.setVisibility(View.VISIBLE);
   
	        }
	        
	    }
	    
	    class DeleteGasstation extends AsyncTask<Void, Void, Void>{
	    	private ProgressDialog progressDialog;
	    		
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            progressDialog = new ProgressDialog(RefuelGasstationFragment.this.getActivity());
	            progressDialog.setMessage(getString(R.string.refuel_gasstation_deleting_gasstation));
	            progressDialog.setIndeterminate(true);
	            progressDialog.show();
	            
	        }
	        @Override
	        protected Void doInBackground(Void... params) {

	        	ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
				edd.deleteGasstation(mGasId);
				return null;
					
	        }
	        @Override
	        protected void onPostExecute(Void param) {
	            super.onPostExecute(param);         
	            progressDialog.dismiss();  
	            RefuelGasstationFragment.this.getActivity().finish();
   
	        }
	    }
	    
	    class QueryComments extends AsyncTask<Void, Void, List<Comment>>{
	        
	    	long mGasId;
	    	
	    	QueryComments(long id) {
	    		mGasId = id;
	    	}
	    	
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            
	            mCommentList.setVisibility(View.GONE);
	        }
	        @Override
	        protected List<Comment> doInBackground(Void... params) {
	        	try{
	        		
		        	Comment[] comments = new JSONCommentDecoder().getComments(mGasId); 	
					return Arrays.asList(comments);
					
	        	} catch(Exception e){
	        		e.printStackTrace();
	        		return null;
	        		
	        	}
					
	        }
	        @Override
	        protected void onPostExecute(List<Comment> values) {
	            super.onPostExecute(values); 

	            try {
	            	        
		            if(values != null){
		            	
		            	if(values.size() > 0){
			            	mCommentLabelContainer.setVisibility(View.VISIBLE);
			            	mCommentList.setVisibility(View.VISIBLE);
		            	}
		            	
			            LayoutInflater inflater = (LayoutInflater) RefuelGasstationFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
			            for (int i = 0; i < values.size(); i++) {
		            	   View view = inflater.inflate(R.layout.refuel_gasstation_list_item_comment, mCommentList, false);
		            	   
		            	   CommentHolder holder = new CommentHolder();
	  		               holder.rating = (StarGroup) view.findViewById(R.id.refuel_gasstation_list_item_comment_rating);
	  		               holder.username = (CustTextView) view.findViewById(R.id.refuel_gasstation_list_item_comment_username);
	  		               holder.comment = (CustTextView) view.findViewById(R.id.refuel_gasstation_list_item_comment_comment);
		  		            
	  		               holder.rating.setRating(values.get(i).rating);
	   			           holder.username.setText(values.get(i).username);
	   			           holder.comment.setText(values.get(i).comment);
	  		               
		            	   mCommentList.addView(view);
		            	   
		            	   if (i < values.size() - 1) {
		            		   ImageView imgView = new ImageView(RefuelGasstationFragment.this.getActivity());
		            		   imgView.setImageResource(R.drawable.line_horizontal_cropped);
		            		   mCommentList.addView(imgView);
		            	   }
		            	   
		            	}
			            
		            }
		            
	            } catch(Exception e){
	            	e.printStackTrace();
	            }
	            
	        }
	    }
 
	    class CommentHolder
	    {
	    	StarGroup rating;
	        CustTextView username;
	        CustTextView comment;
	    }
	    
	    class ViewPictures extends AsyncTask<Void, Void, Bitmap[]>{
	    	private ProgressDialog progressDialog;
	    		
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            progressDialog = new ProgressDialog(RefuelGasstationFragment.this.getActivity());
	            progressDialog.setMessage(getString(R.string.refuel_gasstation_loading_pictures));
	            progressDialog.setIndeterminate(true);
	            progressDialog.show();
	            
	        }
	        @Override
	        protected Bitmap[] doInBackground(Void... params) {
	        	
		    	
		        Bitmap pictures[] = new JSONPictureDecoder().getPicture(mGasstation.getId());
				return pictures;
					
	        }
	        @Override
	        protected void onPostExecute(Bitmap pictures[]) {
	            super.onPostExecute(pictures); 
	            
	            progressDialog.dismiss();  
	            
	            AlertDialog.Builder builder = new AlertDialog.Builder(RefuelGasstationFragment.this.getActivity());
	            if(pictures == null){
		        	
		        	builder.setMessage(R.string.refuel_gasstation_no_pictures);
		        	
		        } else if(pictures.length == 0){
		        	
		        	builder.setMessage(R.string.refuel_gasstation_no_pictures);
		        	
		        } else {
		        	
		        	ScrollView scrollView = new ScrollView(RefuelGasstationFragment.this.getActivity());
		        	
		        	LinearLayout linearLayout = new LinearLayout(RefuelGasstationFragment.this.getActivity());
		        	linearLayout.setOrientation(LinearLayout.VERTICAL);
		        	
		        	for(Bitmap bmp : pictures){
			        	
			        	ImageView pictureView = new ImageView(RefuelGasstationFragment.this.getActivity());
			        	pictureView.setMaxWidth(300);
			        	pictureView.setImageBitmap(bmp);
			        	pictureView.setPadding(5, 5, 5, 5);
			        	
			        	final Bitmap _bmp = bmp;
			        	pictureView.setOnClickListener(new OnClickListener(){

							public void onClick(View v) {

								try {
									
								    FileOutputStream fos = RefuelGasstationFragment.this.getActivity().openFileOutput("tempBMP", Context.MODE_PRIVATE);
								    _bmp.compress(CompressFormat.JPEG, 30, fos);
								    fos.close();
								    
								} catch (Exception e) {
								    e.printStackTrace();
								}
								
								Intent intent = new Intent(v.getContext(), RefuelFullscreenPicture.class); 
								startActivity(intent);
							}
			        		
			        	});
			        	
			            linearLayout.addView(pictureView);
			            
			        }
		        	
		        	scrollView.addView(linearLayout);
		        	builder.setView(scrollView);
		        	
		        }
				builder.create().show();
   
	        }
	    }
		
	}

}