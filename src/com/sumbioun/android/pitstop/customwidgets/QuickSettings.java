package com.sumbioun.android.pitstop.customwidgets;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.*;
import android.widget.*;

/*QuickSettings                                                                                                      */
/*This view has three elements: fuelType, radius and sortBy. By clicking any of them, the user can change the query  */
/*options for the internal database. The current chosen option is shown below the button.                            */
public class QuickSettings extends LinearLayout {
	
	public interface OnFuelTypeChangeListener {
		void onFuelTypeChange(int fuelType);
	}
	
	public interface OnModeChangeListener {
		void onModeChange(int mode);
	}
	
	public interface OnRadiusChangeListener {
		void onRadiusChange(float radius);
	}
	
	String[] mArrayFuel = getResources().getStringArray(R.array.array_quick_settings_fuel_types);
	String[] mArraySortBy = getResources().getStringArray(R.array.array_quick_settings_sort_by);
	
	CustTextView mSettingsFuel;
	CustTextView mSettingsRadius;
	CustTextView mSettingsMode;
	
	OnFuelTypeChangeListener onFuelTypeChangeListener = null;
	OnModeChangeListener onModeChangeListener = null;
	OnRadiusChangeListener onRadiusChangeListener = null;
	
	Context mContext;
	
	public QuickSettings(final Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		
		if(!this.isInEditMode()){
			LayoutInflater li = (LayoutInflater) 
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			li.inflate(R.layout.quick_settings,this,true); 
			
			//UI initialization
			mSettingsFuel = (CustTextView) this.findViewById(R.id.quick_settings_settings_fuel);
			fuelTypeClick();
			
			mSettingsRadius = (CustTextView) this.findViewById(R.id.quick_settings_settings_radius);
			radiusClick();
		
			mSettingsMode = (CustTextView) this.findViewById(R.id.quick_settings_settings_mode);
			modeClick();
			
			this.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
					
		}
		
	}
	
	private void fuelTypeClick(){
		mSettingsFuel.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				int fuelType = MyApplication.getDatabaseHelper().getFuelType();
				fuelType++;
				if(fuelType > mArrayFuel.length-1){
					fuelType = 0;
				}
				
				mSettingsFuel.setText(mArrayFuel[fuelType]);
				
				if(onFuelTypeChangeListener != null){
					onFuelTypeChangeListener.onFuelTypeChange(fuelType);
				}
			}

	});
	}
	
	private void radiusClick(){
		
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		//This dialog is called when the user clicks on radius.
		final Dialog dialog = new Dialog(mContext);
		dialog.setCancelable(true);  
		dialog.setContentView(R.layout.dialog_setradius);
		dialog.setTitle(mContext.getString(R.string.quick_settings_radius_dialog_title));
		
		//Text view in radius dialog.
		final CustTextView dialogRadius = (CustTextView) dialog.findViewById(R.id.quick_settings_dialog_setradius_ctv_radius);
		
		//SeekBar view in radius dialog.
		final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.quick_settings_dialog_setradius_sb_radius);
		
		if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
			seekBar.setMax(190);
		} else {
			seekBar.setMax(90);
		}
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int arg1, boolean arg2) {
				
				//The seek bar's progress ranges between 20 and 200. Which correspond to 2 - 20 km.
				float radius = (float)(seekBar.getProgress()+10)/10;
				dialogRadius.setText(String.valueOf(radius) + " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
			}

			//Methods not used
			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {}
			
		});
		
		//Button view in radius dialog.
		Button button = (Button) dialog.findViewById(R.id.quick_settings_dialog_setradius_b_submit);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				float radius = (float)(seekBar.getProgress()+10)/10;
				if(onRadiusChangeListener != null){
					onRadiusChangeListener.onRadiusChange(radius);
				}
				mSettingsRadius.setText(String.valueOf(radius) + " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
				
				dialog.dismiss();
			}
		});

		if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
			seekBar.setMax(190);
		} else {
			seekBar.setMax(110);
		}
		
		if(!sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
			if(MyApplication.getDatabaseHelper().getRadius() > 12){
				MyApplication.getDatabaseHelper().setRadius(12);
			}
		}
		
		mSettingsRadius.setText(String.valueOf(MyApplication.getDatabaseHelper().getRadius()) + " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
		mSettingsRadius.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				seekBar.setProgress((int)(MyApplication.getDatabaseHelper().getRadius()*10));
				dialogRadius.setText(String.valueOf(MyApplication.getDatabaseHelper().getRadius()) + " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
				dialog.show();
			}
		});
	}
	
	private void modeClick(){
		mSettingsMode.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				int mode = MyApplication.getDatabaseHelper().getMode();
				mode++;
				if(mode > mArraySortBy.length-1){
					mode = 0;
				}
				
				mSettingsMode.setText(mArraySortBy[mode]);
				
				if(onModeChangeListener != null){
				     onModeChangeListener.onModeChange(mode);
				}
			}
			
		});
	}
	
	public void setOnFuelTypeChangeListener(OnFuelTypeChangeListener onFuelTypeChangeListener){
		this.onFuelTypeChangeListener = onFuelTypeChangeListener;
	}
	
	public void setOnModeChangeListener(OnModeChangeListener onModeChangeListener){
		this.onModeChangeListener = onModeChangeListener;
	}
	
	public void setOnRadiusChangeListener(OnRadiusChangeListener onRadiusChangeListener){
		this.onRadiusChangeListener = onRadiusChangeListener;
	}
	
	public void refresh(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		mSettingsFuel.setText(mArrayFuel[MyApplication.getDatabaseHelper().getFuelType()]);
		mSettingsMode.setText(mArraySortBy[MyApplication.getDatabaseHelper().getMode()]);
		
		if(!sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
			if(MyApplication.getDatabaseHelper().getRadius() > 12){
				MyApplication.getDatabaseHelper().setRadius(12);
			}
		}
		mSettingsRadius.setText(String.valueOf(MyApplication.getDatabaseHelper().getRadius()) + " " + sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
		
	}
}
