package com.sumbioun.android.pitstop.customwidgets;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.sumbioun.android.pitstop.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

/*PriceDisplayer                                                                                                         */
/*This view shows numeric data as in a gas station's pump price displayer. The format of the entry may be specified and  */
/*it may be editable when the user clicks over it.                                                                       */
public class PriceDisplayer extends LinearLayout {
	
	//If set to true, then the color of the view changes when the user updates its value.
	//This is only set to true in contributemain.xml.
	boolean CHANGECOLORONUPDATE = false;
	
	//The only difference between this view and a regular EditText is that the cursor is always positioned at the end
	//of the input entry.
	private class CustEditText extends EditText{

		public CustEditText(Context context) {
			super(context);
		}
		
		@Override
	    public void onSelectionChanged(int start, int end) {
	 
	        CharSequence text = getText();
	        if (text != null) {
	            if (start != text.length() || end != text.length()) {
	                setSelection(text.length(), text.length());
	                return;
	            }
	        }
	 
	        super.onSelectionChanged(start, end);
	    }
		
	}
	
	//Title of the edition dialog box.
	private String mDialogTitle = "";
	
	//The value displayed.
	private double mValue = 0f;
	
	//Number of digits displayed. "." is considered a digit.
	private int mAttributeNumDigits = 3; 
	
	//Digits before decimal point.
	private int digitsBeforePoint = 1;
	
	//Holds each digit displayer.
	private ImageView[] mDigitDisplayer;
	
	//Set to true when the user changes the value displayed to any positive number greater than 0.
	private boolean WASUPDATED = false;
	
	//Pattern to be displayed.
	private String pattern = "0.000#";
	
	private boolean mAttributeEditable = false;

	//Array that holds references to the digit drawables.
	int digits[] = { R.drawable.digit_0,
					 R.drawable.digit_1,
					 R.drawable.digit_2,
					 R.drawable.digit_3,
					 R.drawable.digit_4,
					 R.drawable.digit_5,
					 R.drawable.digit_6,
					 R.drawable.digit_7,
					 R.drawable.digit_8,
					 R.drawable.digit_9 };
	
	public PriceDisplayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if(!this.isInEditMode()){
			
			//Get attributes and set their values.
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.PriceDisplayer);
		    CharSequence s = a.getString(R.styleable.PriceDisplayer_dialog_title);
		    if (s != null) {
		        this.mDialogTitle = s.toString();
		    }
		    
		    mAttributeNumDigits = a.getInt(R.styleable.PriceDisplayer_digits, 5);
		    mAttributeEditable = a.getBoolean(R.styleable.PriceDisplayer_editable, false);
		    CHANGECOLORONUPDATE = a.getBoolean(R.styleable.PriceDisplayer_changecolor, false);
		    
		    if(a.getString(R.styleable.PriceDisplayer_pattern) != null){
		    	pattern = a.getString(R.styleable.PriceDisplayer_pattern);
		    	digitsBeforePoint = pattern.indexOf(".");
		    }
		    
		    a.recycle();

			//Initializes UI elements
		    this.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
		    
		    mDigitDisplayer = new ImageView[mAttributeNumDigits];
			initDigitDisplayers(context);
			
			for(int i = 0; i < mAttributeNumDigits; i++){
				addView(mDigitDisplayer[i]);
			}
			
			//If the view is set to be editable, we create a dialog box when the user clicks over it, so she can provide a
			//new value to substitute the old one.
			if(mAttributeEditable){
				this.setOnClickListener(new View.OnClickListener(){
						
					public void onClick(View v) {
		
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				    	
						//CustEditText to make the value displayed editable.
						final CustEditText editablePrice = new CustEditText(v.getContext());

						InputFilter[] fArray = new InputFilter[1];
						
						//Set the maximum length of the input.
						fArray[0] = new InputFilter.LengthFilter(mAttributeNumDigits);
						editablePrice.setFilters(fArray);
						
						//Only digits can be entered as input.
						editablePrice.setInputType(InputType.TYPE_CLASS_PHONE);
						editablePrice.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
						
						//Set standard decimal and grouping separators.
						DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
						otherSymbols.setDecimalSeparator('.');
						otherSymbols.setGroupingSeparator(','); 
						
						//Set our pattern to the value displayed.
						DecimalFormat nft = new DecimalFormat(pattern, otherSymbols);  
		        		nft.setDecimalSeparatorAlwaysShown(true); 
		        		
		        		if(mValue == 0){
		        			editablePrice.setText("");
		        		} else {
		        			editablePrice.setText(nft.format(mValue));
		        		} 
		        		//Position the cursor at the end of the entry in the CustEditText.
		        		editablePrice.setSelection(0, editablePrice.length());
		        		
		        		setInputMask(editablePrice);
		        		
		        		//Add the CustEditText to the dialog
		        		builder.setView(editablePrice);
				    	builder.setTitle(mDialogTitle)
				    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           
				            public void onClick(DialogInterface dialog, int id) {
				            	try {
				            		if(editablePrice.getText().toString() != "" && Double.parseDouble(editablePrice.getText().toString()) != 0.0f ){
				            			//If the input is valid we set that the view was updated.
				            			WASUPDATED = true;
				            			changeColor(getResources().getColor(R.color.opaque_yellow));
				            		}
				            		
				            		updateDigitDisplayers(Double.parseDouble(editablePrice.getText().toString()));	
		                			
		                		} catch (NumberFormatException e) {
		                			updateDigitDisplayers(0f);
		                		}
		                		dialog.dismiss();
				            }
				        })
				        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

				            public void onClick(DialogInterface dialog, int id) {
				            	//Don't set the view as updated.
				            	dialog.dismiss();
				            }
				        });
		        		
	        			builder.create().show();
					}
					
				});
			}
		}
			
	}
	
	private void initDigitDisplayers(Context context) {
		
		for(int i = 0; i < mAttributeNumDigits; i++){
			mDigitDisplayer[i] = new ImageView(context);
			
		}
		
		updateDigitDisplayers(mValue);
		
	}
	
	//Set the digit drawables corresponding to the provided value.
	public void updateDigitDisplayers(double newValue){
		
		mValue = newValue;
		
		String string = new String();
		string = String.valueOf(mValue);
			
		for(int i = 0; i < mAttributeNumDigits; i++){
			if(string.length() >= i+1){
				if(string.charAt(i) == '.'){
					mDigitDisplayer[i].setImageResource(R.drawable.digit_dot);
				} else {
					mDigitDisplayer[i].setImageResource(digits[Character.getNumericValue(string.charAt(i))]);
				}
			} else {
				mDigitDisplayer[i].setImageResource(digits[0]);
			}
			
		}
	}
	
	//Set input mask to transform the user entry into the format specified.
	private void setInputMask(final CustEditText editablePrice){

		editablePrice.addTextChangedListener(new TextWatcher(){
			
			//Set "." automatically when the digits before point assume the value specified.
			public void afterTextChanged(Editable text) {
				
				if(text.length() > digitsBeforePoint){
					if(text.charAt(digitsBeforePoint) != '.'){
						
						editablePrice.removeTextChangedListener(this);
						text.insert(digitsBeforePoint, ".");
						editablePrice.addTextChangedListener(this);
						
					}
				} else if(text.length() == digitsBeforePoint) {
					
					editablePrice.removeTextChangedListener(this);
					text.append(".");
					editablePrice.addTextChangedListener(this);
					
				}
			}
			
			public void beforeTextChanged(CharSequence s,
					int start, int count, int after) {

				//Cursor is just after "." and user hits backspace.
				if((s.length() == digitsBeforePoint+1) && (after == 0)){
					editablePrice.removeTextChangedListener(this);
					editablePrice.setText(editablePrice.getText().toString().substring(0, digitsBeforePoint-1));
					editablePrice.addTextChangedListener(this);

				}
			}

			//Not used
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
			}
			
		});
	}
	
	public double getValue(){ return mValue; }

	public boolean getWasUpdated(){
		return WASUPDATED;
	}
	
	//Change the digits colors.
	private void changeColor(int color){
		
		if(CHANGECOLORONUPDATE){
			for(int i = 0; i < mAttributeNumDigits; i++){
				mDigitDisplayer[i].setColorFilter(color);
				mDigitDisplayer[i].invalidate();
				
			}
		}
	}
	
}
