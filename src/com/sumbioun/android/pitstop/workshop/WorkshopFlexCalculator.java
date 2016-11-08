package com.sumbioun.android.pitstop.workshop;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.customwidgets.PriceDisplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/*WorkshopFlexCalculator                                                                          */
/*This activity calculates which fuel is more efficient, alcohol or gasoline, given the prices.   */
public class WorkshopFlexCalculator extends Activity {

	private static float GASOLINE_ALCOHOL_PERFORMANCE_DIFFERENCE = 30.0f;
	
	CustTextView mCalculate;	
	CustTextView mBestFuelDisplayer;
	
	PriceDisplayer mAlcPrice;
	PriceDisplayer mGasPrice;
	PriceDisplayer mPerformanceDifference;
	PriceDisplayer mPriceDifference;
	
	LinearLayout mResultContainer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workshop_flex_calculator);
        
        mAlcPrice = (PriceDisplayer) this.findViewById(R.id.workshop_flex_calculator_alc_price);
    	mGasPrice = (PriceDisplayer) this.findViewById(R.id.workshop_flex_calculator_gas_price);
    	mPerformanceDifference = (PriceDisplayer) this.findViewById(R.id.workshop_flex_calculator_performance_difference);
    	mPriceDifference = (PriceDisplayer) this.findViewById(R.id.workshop_flex_calculator_price_difference);
    	mBestFuelDisplayer = (CustTextView) this.findViewById(R.id.workshop_flex_calculator_best_fuel);
    	
        mCalculate = (CustTextView) this.findViewById(R.id.workshop_flex_calculator_calculate);
        mCalculate.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(WorkshopFlexCalculator.this, R.anim.clickresize));
				calculateBestFuel();
			}
		});
        
        mPerformanceDifference.updateDigitDisplayers(GASOLINE_ALCOHOL_PERFORMANCE_DIFFERENCE);
        
        mResultContainer = (LinearLayout) this.findViewById(R.id.workshop_flex_calculator_result_container);
        mResultContainer.setVisibility(View.INVISIBLE);
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
    
    private void calculateBestFuel(){
    	double alcPrice = mAlcPrice.getValue();
    	double gasPrice = mGasPrice.getValue();
    	
    	if(alcPrice == 0 || gasPrice == 0){
    		return;
    	}
    	
    	mResultContainer.setVisibility(View.VISIBLE);
    	
    	double priceDifference = ((double)1 - (double)alcPrice/gasPrice)*100;
    	double performanceDifference = (((double)mPerformanceDifference.getValue()));
    	
    	mPriceDifference.updateDigitDisplayers(Math.abs(priceDifference));
 	
    	if(priceDifference > performanceDifference){ 
    		//alcohol is more efficient
    		mBestFuelDisplayer.setText(R.string.workshop_flex_calculator_alcohol);
    	} else { 
    		//gas is more efficient
    		mBestFuelDisplayer.setText(R.string.workshop_flex_calculator_gasoline);
    	}
    }
}
