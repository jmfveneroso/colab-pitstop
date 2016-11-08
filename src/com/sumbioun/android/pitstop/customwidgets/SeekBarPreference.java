package com.sumbioun.android.pitstop.customwidgets;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends DialogPreference {
	
	private float mNewValue = 0;
	private float mCurrentValue = 0;
	
	private SeekBar mSeekBar;
	private CustTextView mText;
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setDialogLayoutResource(R.layout.dialog_seekbarpreference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        setDialogIcon(null);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        mNewValue = prefs.getFloat("pref_radius_test", 5.0f);
        
	}
	
	@Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // view is your layout expanded and added to the dialog
        // find and hang on to your views here, add click listeners etc
        // basically things you would do in onCreate
        mText = (CustTextView) view.findViewById(R.id.dialog_seekbarpreference_text);
        
        mSeekBar = (SeekBar) view.findViewById(R.id.dialog_seekbarpreference_radius);
        mSeekBar.setProgress((int)mNewValue*10);
        mText.setText(mNewValue+"");
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mText.setText(((float)mSeekBar.getProgress()/10)+"");
				mNewValue = ((float) mSeekBar.getProgress()/10);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
        	
        });
        	
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    // When the user selects "OK", persist the new value

	    if (positiveResult) {
	    	
	        persistFloat(mNewValue);
	        
	    }
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
	    if (restorePersistedValue) {
	        // Restore existing state
	        mCurrentValue = this.getPersistedFloat(0);
	    } else {
	        // Set default state from the XML attribute
	        mCurrentValue = (Integer) defaultValue;
	        persistFloat(mCurrentValue);
	    }
	}

}
