package com.sumbioun.android.pitstop.contribute;

import java.io.File;
import java.util.ArrayList;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.Gasstation.ConvenienceStore;
import com.sumbioun.android.pitstop.database.Gasstation.PaymentMethods;
import com.sumbioun.android.pitstop.map.JSONPictureDecoder;

import deprecated.RefuelActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/*ContributeExtra                                                                      */
/*Edits extra information about a gas station before updating the external database    */
public class ContributeExtra extends RefuelActivity  {
	
	private static final int CAMERA_REQUEST = 1888; 
	
	//Dialogs opened when the user clicks a row on the list.
	private static final int PAYMENT_METHODS = 0;  
    private static final int CONVENIENCE_STORE = 1;  
    private static final int CAR_WASH = 2;  
    private static final int CAR_REPAIR = 3;  
    private static final int RESTAURANT = 4; 
    private static final int BUSINESS_HOURS = 5;  
    private static final int PHOTO = 6;  
    private static final int TELEPHONE = 7;  
    
    private Gasstation mGasstation;
    
    private String mAddressLine = new String();
    
    private Bitmap mPhoto = null;
    private Bitmap mThumbnail = null;
    
    private UploadExtras uploadExtras;
    
    private File pictureFile = null;
    
    private View photoCheckBox = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute_extra); 
        
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	
        	mGasstation = extras.getParcelable(getString(R.string.EXTRA_CONTRIBUTEEXTRA_GASSTATION));
            mAddressLine = extras.getString(getString(R.string.EXTRA_CONTRIBUTEEXTRA_ADDRESS));

        }
        //Storing string resources into Array
        ListView extraList = (ListView) findViewById(R.id.contribute_extra_list);  

        String str[] = getResources().getStringArray(R.array.contribute_extra_items);
        
        //Binding resources Array to ListAdapter
        CustomAdapter mAdapter = new CustomAdapter(str);
        
        //extraList.setAdapter(new ArrayAdapter<String>(this, R.layout.contribute_extra_list_item, R.id.label, extraItems));
        extraList.setAdapter(mAdapter);
        
        extraList.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				switch(position){
					case PAYMENT_METHODS:
						setPaymentMethods(view);	
						
						break;
						
					case CONVENIENCE_STORE:
						setConvenienceStore(view);
						break;
						
					case CAR_WASH:
						setCarWash(view);
						break;
					
					case CAR_REPAIR:
						setCarRepair();
						break;
					
					case RESTAURANT:
						setRestaurant();
						break;
					
					case BUSINESS_HOURS:
						setBusinessHours();
						break;
					
					case PHOTO:
						photoCheckBox = view;
						takePhoto();
						break;
					
					case TELEPHONE:
						setPhone(view);
						break;
				}
				
			}
        	
        });
        
        TextView submit = (TextView) findViewById(R.id.contribute_extra_submit);
        submit.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeExtra.this, R.anim.clickresize));
				//uploadExtras = new UploadExtras();
				//uploadExtras.execute();
				
				SubmitChanges submitChanges = new SubmitChanges();
				submitChanges.execute();
			}
        });
        
       /* TextView skip = (TextView) findViewById(R.id.contribute_extra_skip);
        skip.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ContributeExtra.this, R.anim.clickresize));
				//Do not upload changes.
				ContributeExtra.this.finish();
			}
        });*/
        
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
	    
	    if(uploadExtras != null){
	    	uploadExtras.cancel(true);
	    }

	}
    
    private void initContributeRating(){
    	
    	try {
    		
	    	Intent intent = new Intent(ContributeExtra.this, ContributeRating.class);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTERATING_GASSTATION), mGasstation);
			intent.putExtra(getString(R.string.EXTRA_CONTRIBUTERATING_ADDRESS), mAddressLine);
			startActivity(intent);	 
			ContributeExtra.this.finish();
			
    	} catch(ActivityNotFoundException e){
    		e.printStackTrace();
    	}
    	
    }
    
    //Create payment methods dialog box.
    private void setPaymentMethods(View view) {
    	String[] array = getResources().getStringArray(R.array.contribute_extra_payment_methods);
    	final ArrayList<Integer> selectedItems = new ArrayList<Integer>();
    	
    	//Get the gasstation's payment methods, so the items appear checked in the dialog box.
    	boolean checkedItems[] = {
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISA),
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MASTERCARD),
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AMERICANEXPRESS),
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISAELECTRON),
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_CHECK)
    	};
    	
    	for(int i = 0; i < checkedItems.length; i++){
    		if(checkedItems[i]){
    			selectedItems.add(i);
    		}
    	}
    	
    	final View _view = view;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_payment_methods_dialog_title))
    	
    	.setMultiChoiceItems(array, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {

    		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                	selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it 
                	selectedItems.remove(Integer.valueOf(which));
                }
         
                
    		}
    	})
    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	//cleans the payment methods flags and sets them up again with the selected items
            	for(PaymentMethods i : Gasstation.PaymentMethods.values()){
            		mGasstation.setExtraFlag(i.value, false);
            	}
            	for(int i : selectedItems){
            		mGasstation.setExtraFlag(Gasstation.PaymentMethods.values()[i].value, true);

            	}
            	
            	updatePaymentMethodsIcons(_view);
            	
            	dialog.dismiss();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            }
        });
    	
    	builder.create().show();
    }
    
    private void updatePaymentMethodsIcons(View view){
    	LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.contribute_extra_list_item_payment_methods_images);
		
		ImageView img[] = new ImageView[5];
		for(int i = 0; i < 5; i++){
			img[i] = (ImageView) linearLayout.getChildAt(i);
		}
		
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISA)){img[0].setVisibility(View.VISIBLE);}else{img[0].setVisibility(View.GONE);}
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MASTERCARD)){img[1].setVisibility(View.VISIBLE);}else{img[1].setVisibility(View.GONE);}
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AMERICANEXPRESS)){img[2].setVisibility(View.VISIBLE);}else{img[2].setVisibility(View.GONE);}
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_VISAELECTRON)){img[3].setVisibility(View.VISIBLE);}else{img[3].setVisibility(View.GONE);}
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_CHECK)){img[4].setVisibility(View.VISIBLE);}else{img[4].setVisibility(View.GONE);}
		
    }
    
    //Create convenience store dialog box.
    private void setConvenienceStore(View view) {
    	String[] array = getResources().getStringArray(R.array.contribute_extra_convenience_store);
    	final ArrayList<Integer> selectedItems = new ArrayList<Integer>();
    	
    	//Get the checked items, so they appear checked in the dialog.
    	boolean checkedItems[] = {
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_STORE),
    		mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_DRUGSTORE)
    	};

    	for(int i = 0; i < checkedItems.length; i++){
    		if(checkedItems[i]){
    			selectedItems.add(i);
    		}
    	}
    	
    	final View _view = view;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_convenience_store_dialog_title))
    	
    	.setMultiChoiceItems(array, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {

    		public void onClick(DialogInterface dialog, int which,
                    boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items.
                    selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it.
                    selectedItems.remove(Integer.valueOf(which));
                }
         
    		
    		}
    	})
    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	//cleans the payment methods flags and sets them up again with the selected items
            	for(ConvenienceStore i : Gasstation.ConvenienceStore.values()){
            		mGasstation.setExtraFlag(i.value, false);
            	}
            	for(int i : selectedItems){
            		mGasstation.setExtraFlag(Gasstation.ConvenienceStore.values()[i].value, true);

            	}
            	
            	updateConvenienceStoreIcons(_view);

            	dialog.dismiss();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            }
        });
    	
    	builder.create().show();
    }
    
    private void updateConvenienceStoreIcons(View view){
    	LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.contribute_extra_list_item_convenience_store_images);
		
		ImageView img[] = new ImageView[2];
		for(int i = 0; i < 2; i++){
			img[i] = (ImageView) linearLayout.getChildAt(i);
		}
		
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_STORE)){img[0].setVisibility(View.VISIBLE);}else{img[0].setVisibility(View.GONE);}
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_DRUGSTORE)){img[1].setVisibility(View.VISIBLE);}else{img[1].setVisibility(View.GONE);}
		
    }
    
    //Create car wash dialog box.
    private void setCarWash(View view) {
    	String[] array = getResources().getStringArray(R.array.contribute_extra_car_wash);
    	//Default option is none.
    	int selectedItem = 0;
    	
    	//Finds what is the car wash status and sets it on the view.
    	if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_PRESSURECARWASH)){
    		selectedItem = Gasstation.CarWash.PRESSURE.ordinal();
    	} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AUTOCARWASH)) {
    		selectedItem = Gasstation.CarWash.AUTO.ordinal();
    	} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MANUALCARWASH)) {
    		selectedItem = Gasstation.CarWash.MANUAL.ordinal();
    	}
    	
    	final View _view = view;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_car_wash_dialog_title))
    	
    	.setSingleChoiceItems(array, selectedItem, 
                new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				//Sets the selected option
				mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_PRESSURECARWASH, false);
				mGasstation.setExtraFlag(Gasstation.CarWash.values()[which].value, true);
				
				updateCarWashIcons(_view);
				
				dialog.dismiss();
			}
    	});
    	
    	builder.create().show();
    }
    
    private void updateCarWashIcons(View view){
    	LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.contribute_extra_list_item_car_wash_images);
		
		ImageView img[] = new ImageView[3];
		for(int i = 0; i < 3; i++){
			img[i] = (ImageView) linearLayout.getChildAt(i);
		}
		
		img[0].setVisibility(View.GONE);
		img[1].setVisibility(View.GONE);
		img[2].setVisibility(View.GONE);
		
		if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_PRESSURECARWASH)){
			img[2].setVisibility(View.VISIBLE);
    	} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_AUTOCARWASH)) {
    		img[0].setVisibility(View.VISIBLE);
    	} else if(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_MANUALCARWASH)) {
    		img[1].setVisibility(View.VISIBLE);
    	}
		
    }
    
    //Create car repair dialog box. Yes or No dialog.
    private void setCarRepair() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_car_repair_dialog_title))
    	.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_REPAIR, true);
            	dialog.dismiss();
            }
        })
        .setNegativeButton("Não", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_REPAIR, false);
            	dialog.dismiss();
            }
        });
    	
    	builder.create().show();
    }
    
    //Create car restaurant dialog box. Yes or No dialog.
    private void setRestaurant() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_restaurant_dialog_title))
    	.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT, true);
            	dialog.dismiss();
            }
        })
        .setNegativeButton("Não", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT, false);
            	dialog.dismiss();
            }
        });
    	
    	builder.create().show();
    }
    
    //Create business hours dialog box. Yes or No dialog. Sets if it is open 24 hours a day.
    private void setBusinessHours() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.contribute_extra_business_hours_dialog_title))
    	.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_24HOURS, true);
            	dialog.dismiss();
            }
        })
        .setNegativeButton("Não", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_24HOURS, false);
            	dialog.dismiss();
            }
        });
    	
    	builder.create().show();
    }
    
    //Send an intent to take a picture of the gas station. The result is stored later in mPhoto.
    private void takePhoto(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        pictureFile = new File(Environment.getExternalStorageDirectory(), "temp");
        Log.w("before sending", Uri.fromFile(pictureFile).toString());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
        startActivityForResult(cameraIntent, CAMERA_REQUEST); 

    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }
    
    //Returns from camera intent.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
        	//Bitmap mThumbnail = (Bitmap) data.getExtras().get("data"); 
  
        	BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
        	// Limit the filesize since 5MP pictures will kill you RAM  
        	bitmapOptions.inSampleSize = 8;  
        	
        	Log.w("file       ", Uri.fromFile(pictureFile).toString());
        	mPhoto = BitmapFactory.decodeFile("mnt/sdcard/temp", bitmapOptions);
        	pictureFile.delete();
        	
        	int height = (int)((float)((float)mPhoto.getHeight()/mPhoto.getWidth())*180);
        	mThumbnail = Bitmap.createScaledBitmap(mPhoto, 180, height, false);
        	
        	//mPhoto = new JSONPictureDecoder().getPicture(99)[0];
  
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	
        	final ImageView input = new ImageView(this);
        	input.setImageBitmap(mThumbnail);
            builder.setView(input);
    		
        	builder.setTitle(getString(R.string.contribute_extra_picture_dialog_title))
        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                	
                	ImageView img = (ImageView) photoCheckBox.findViewById(R.id.contribute_extra_list_item_photo_square_inner);
                	img.setVisibility(View.VISIBLE);
                }
            });
    		
    		builder.create().show();
        }  
    } 
    
    private void setPhone(View view){	
    	
    	final CustTextView _view = (CustTextView) view.findViewById(R.id.contribute_extra_list_item_telephone_text);
    	
    	//The only difference between this class and a regular EditText is that the cursor is always positioned at the end of
    	//the input text.
    	class CustEditText extends EditText{

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
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	final CustEditText input = new CustEditText(this);
        builder.setView(input);
        
        input.setText(mGasstation.getPhoneNumber());
        
        //Sets phone text watcher so the input is automatically set in the correct format: (XX) XXXX-XXXX.
		setPhoneTextWatcher(input);

		//Only numbers are allowed to be entered.
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
		
		//The maximum amount of digits is set to 14, the amount in "(XX) XXXX-XXXX".
		InputFilter[] fArray = new InputFilter[1];
		fArray[0] = new InputFilter.LengthFilter(14);
		input.setFilters(fArray);

    	builder.setTitle(getString(R.string.contribute_extra_phone_dialog_title))
    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           
            public void onClick(DialogInterface dialog, int id) {
            	mGasstation.setPhoneNumber(input.getText().toString());
            	updateTelephone(_view);
            	dialog.dismiss();
            }
        })
        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
            }
        });
		
		builder.create().show();
	
    }
    
    private void updateTelephone(View view){

    	((CustTextView) view).setText(mGasstation.getPhoneNumber());
		
    }
    
    //Sets a text watcher that only allows user to provide input in the phone format (xx) xxxx-xxxx.
    private void setPhoneTextWatcher(final EditText et) {
    	
    	et.addTextChangedListener(new TextWatcher() {
	
	    	private boolean editing = false;
			private String phone;
			private int oldSize = 0;
 
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			
				if (!editing) {
					
					editing = !editing;
					String text = et.getText().toString();
					phone = text.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");
			 
		    		if (phone.length() >= 2 && phone.length() > oldSize) {
		    			  		
		    			String parte0 = phone.substring(0, 2);
		    			String parte1 = phone.substring(2);
		    			String parte2 = "";
			     
			    			if (phone.length() < 6) {
			    				
			    				oldSize = phone.length();
			    				phone = "(" + parte0 + ") " + parte1;
			    				
			    			} else {
			    				
			    				parte1 = phone.substring(2, 6);
			    				parte2 = phone.substring(6);
			    				oldSize = phone.length();
			    				phone = "(" + parte0 + ") " + parte1 + "-" + parte2;
			    				
			    			}
			     
		    		} else {
			    			
		    			oldSize = phone.length();
		    			phone = text;
			    	}
			     
			     
		    		if (phone.length() > 2) {
		    			
		    			et.setTextKeepState(phone, BufferType.EDITABLE);
		    			et.setSelection(phone.length());
		    			
		    		}
		    		
		    		editing = !editing;
		    	}
		     
			}

			//Methods not used
			public void afterTextChanged(Editable s) {
				//nothing
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//nothing
			}
    	});
    }
    
    //Uploads extras and picture to external database.
    class UploadExtras extends AsyncTask<Void, Void, Void>{
    	private ProgressDialog progressDialog;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeExtra.this);
            progressDialog.setMessage(getString(R.string.contribute_extra_sending_data));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {

        	//Uploads extraFlags and telephone.
        	mGasstation = MyApplication.getDatabaseHelper().updateExtras(mGasstation);
			        	
			return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);   
            progressDialog.hide();  
			initContributeRating();
			
        }
    }
    
    //Adds a gas station to the external database.
    class SubmitChanges extends AsyncTask<Void, Void, Void>{
    	private ProgressDialog progressDialog;  
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ContributeExtra.this);
            progressDialog.setMessage(getString(R.string.contribute_extra_sending_data));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
	
        	mGasstation = MyApplication.getDatabaseHelper().updateGasstation(mGasstation);
        	if(mGasstation == null){
        		ContributeExtra.this.finish();
        	}
        	
        	if(mPhoto != null && mThumbnail != null){
        		long id = new JSONPictureDecoder().sendPictureToServer(mGasstation.getId(), mPhoto, mThumbnail);
        		if(id == -1){
        			
        		}
        	}
        	
        	return null;
				
        }
        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);  
            progressDialog.dismiss();
            
            initContributeRating();
			
        }
    }
    
    private class CustomAdapter extends BaseAdapter {
 
        private ArrayList<String> mData = new ArrayList<String>();
        private LayoutInflater mInflater;
        
        public CustomAdapter(String data[]) {
        	for(String str : data){
        		mData.add(str);
        	}
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
 
        @Override
        public int getItemViewType(int position) {
            return position;
        }
 
        @Override
        public int getViewTypeCount() {
            return 8;
        }
 
        public int getCount() {
            return mData.size();
        }
 
        public String getItem(int position) {
            return mData.get(position);
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
           
        	try { 	
	        	
	        	ViewHolder holder = null;
	            int type = getItemViewType(position);
	            
	            if (convertView == null) {
	            	
	                holder = new ViewHolder();
	                switch (type) {
	                
	                    case PAYMENT_METHODS:
	                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_payment_methods, null);
	                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_payment_methods_title);
	                        updatePaymentMethodsIcons(convertView);
	                        break;
	                    case CONVENIENCE_STORE:
	                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_convenience_store, null);
	                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_convenience_store_title);
	                        updateConvenienceStoreIcons(convertView);
	                        break;
	                    case CAR_WASH:
	                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_car_wash, null);
	                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_car_wash_title);
	                        updateCarWashIcons(convertView);
	                        break;
	                    case CAR_REPAIR:
		                    {
		                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_single_checkbox, null);
		                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_title);
		                        
		                        holder.checkBox = (CheckBox)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_checkbox);
		                        holder.checkBox.setChecked(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_REPAIR));
		                        
		                        holder.checkBox.setOnClickListener(new OnClickListener(){
	
									public void onClick(View v) {
										if(((CheckBox) v).isChecked()){
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_REPAIR, true);
										} else {
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_REPAIR, false);
										}
										
									}
		                        	
		                        });
		                    }
	                        break;
	                    case RESTAURANT:
		                    {
		                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_single_checkbox, null);
		                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_title);
		                        
		                        holder.checkBox = (CheckBox)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_checkbox);
		                        holder.checkBox.setChecked(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT));
		                        
		                        holder.checkBox.setOnClickListener(new OnClickListener(){
	
									public void onClick(View v) {
										
										if(((CheckBox) v).isChecked()){
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT, true);
										} else {
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_RESTAURANT, false);
										}
										
									}
		                        	
		                        });
		                    }
	                        break;
	                    case BUSINESS_HOURS:
		                    {
		                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_single_checkbox, null);
		                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_title);
		                        
		                        holder.checkBox = (CheckBox)convertView.findViewById(R.id.contribute_extra_list_item_single_checkbox_checkbox);
		                        holder.checkBox.setChecked(mGasstation.getExtraFlag(Gasstation.EXTRAFLAG_24HOURS));
		                        
		                        holder.checkBox.setOnClickListener(new OnClickListener(){
	
									public void onClick(View v) {
										if(((CheckBox) v).isChecked()){
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_24HOURS, true);
										} else {
											mGasstation.setExtraFlag(Gasstation.EXTRAFLAG_24HOURS, false);
										}
										
									}
		                        	
		                        });
		                    }
	                        break;
	                    case PHOTO:
	                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_photo, null);
	                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_photo_title);
	                        ImageView img = (ImageView) convertView.findViewById(R.id.contribute_extra_list_item_photo_square_inner);
	                        img.setVisibility(View.GONE);
	                        
	                        break;
	                    case TELEPHONE:
	                        convertView = mInflater.inflate(R.layout.contribute_extra_list_item_telephone, null);
	                        holder.textView = (TextView)convertView.findViewById(R.id.contribute_extra_list_item_telephone_title);
	                        View view = convertView.findViewById(R.id.contribute_extra_list_item_telephone_text);
	                        updateTelephone(view);
	                        break;
	                        
	                }
	                convertView.setTag(holder);
	                
	            } else {
	                holder = (ViewHolder)convertView.getTag();
	            }
	            
	            holder.textView.setText(mData.get(position));
	            
        	} catch(Exception e){
        		e.printStackTrace();
        	}
        	
            return convertView;
        }
        
        private class ViewHolder {
        	public TextView  textView;
        	public CheckBox  checkBox;
        }
 
    }

}