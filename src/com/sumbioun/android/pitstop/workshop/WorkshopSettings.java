package com.sumbioun.android.pitstop.workshop;

import java.util.Locale;

import com.sumbioun.android.pitstop.IntroSumbioun;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

/*WorkshopSettings                      */
/*Preference activity. Nothing special. */
public class WorkshopSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	public static final String KEY_PREF_LANGUAGE = "pref_language";
	public static final String KEY_PREF_UNIT_OF_MEASUREMENT = "pref_unit_of_measurement";
	public static final String KEY_PREF_FUEL_TYPE = "pref_fuel_type";
	public static final String KEY_PREF_RADIUS = "pref_radius_test";
	public static final String KEY_MAX_RESULTS = "pref_results_table";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.workshop_settings);

        setTheme(R.style.ThemeDarkText);
        
       /* Preference deleteDatabase = (Preference) findPreference("delete_database");
        deleteDatabase.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference arg0) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(WorkshopSettings.this);
		    	builder.setTitle(R.string.workshop_settings_delete_database)//getString(R.string.contribute_extra_payment_methods_dialog_title)
		    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
       	
		            	SharedPreferences settings;
						SharedPreferences.Editor editor;
						
						settings = WorkshopSettings.this.getSharedPreferences(GasstationsDataSource.FILENAME, Context.MODE_PRIVATE);
						editor = settings.edit();
						
						editor.putFloat("NORTHEASTLATITUDE", 0f);
		    		    editor.putFloat("NORTHEASTLONGITUDE", 0f);
		    		    editor.putFloat("SOUTHWESTLATITUDE", 0f);
		    		    editor.putFloat("SOUTHWESTLONGITUDE", 0f);
		    		    editor.putString("LASTUPDATE", "1980-01-01 00:00:00");
		    		    editor.commit();
						  
		    		    Toast.makeText(WorkshopSettings.this, "Deleted database with "+MyApplication.getDatabaseHelper().getDatabaseSize()+" bytes", Toast.LENGTH_SHORT).show();
		    		    MyApplication.getDatabaseHelper().deleteDatabase();
		            	dialog.dismiss();
		            }
		        })
		        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int id) {
		            	dialog.dismiss();
		            }
		        });
		    	
		    	builder.create().show();	
    		    
				return false;
			}
        	
        });*/
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
    }
    
    public void setLocale(String lang) { 
    	
    	Locale myLocale = new Locale(lang); 
    	Resources res = getResources(); 
    	DisplayMetrics dm = res.getDisplayMetrics(); 
    	Configuration conf = res.getConfiguration(); 
    	conf.locale = myLocale; 
    	res.updateConfiguration(conf, dm); 
    	onConfigurationChanged(conf);
    	
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (key.equals("pref_language")) {
			//setLocale(sharedPreferences.getString("pref_language", "pt"));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(WorkshopSettings.this);
			builder.setMessage(getString(R.string.workshop_settings_restart))
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           
	            public void onClick(DialogInterface dialog, int id) {
	            	
	        		dialog.dismiss();
	        		Intent intent = new Intent(WorkshopSettings.this, IntroSumbioun.class);
	        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	    	startActivity(intent);
	    	    	WorkshopSettings.this.finish();
	    	    	
	            }
	        });
	    	builder.create().show();
	    	
        }
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  // refresh your views here
	  super.onConfigurationChanged(newConfig);
	  Intent intent = new Intent(this, WorkshopSettings.class);
	  this.startActivity(intent);
	  this.finish();
  	
	}
    
}