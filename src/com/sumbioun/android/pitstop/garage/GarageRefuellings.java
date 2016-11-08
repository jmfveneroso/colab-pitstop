package com.sumbioun.android.pitstop.garage;

import java.util.ArrayList;
import java.util.List;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/*WorkshopMain                                                                                           */
/*Main activity in the workshop group, that contains mostly tools that might prove useful to the user.   */
/*Provides links to access all the other workshop activities.                                            */
public class GarageRefuellings extends FragmentActivity {
	
	private static RefuellingsDataSource mDbHelper;
	
	List<Refuelling> refuellings;
	private long carId = -1;
	
	@Override
	public void onStart() {
	    super.onStart(); 
	    
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.garage_refuellings);
        
        mDbHelper = new RefuellingsDataSource(this);
	    mDbHelper.open();
        
        ListView list = (ListView) this.findViewById(R.id.garage_refuellings_list);

        ArrayList<String> strings = new ArrayList<String>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	carId = extras.getLong("EXTRA_GARAGEREFUELLINGS_ID");
        }
        
        refuellings = mDbHelper.getRefuellings(carId);
        Log.w("IDIDIDIDI", carId+"");
        
        if(refuellings != null){
        	
	        for(int i = 0; i < refuellings.size(); i++){
	        	strings.add(String.valueOf(refuellings.get(i).getDistance()));
	        }
	        
        } else {
        	refuellings = new ArrayList<Refuelling>();
        }
        
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_list_item_1, strings);    
        list.setAdapter(adapter);
        
        CustTextView addCar = (CustTextView) this.findViewById(R.id.garage_refuellings_add_refuelling);
        addCar.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Animation anim = AnimationUtils.loadAnimation(GarageRefuellings.this, R.anim.clickresize);
				anim.setAnimationListener(new Animation.AnimationListener(){
				    public void onAnimationStart(Animation arg0) {
				    }           
				    public void onAnimationRepeat(Animation arg0) {
				    }           
				    public void onAnimationEnd(Animation arg0) {
				    	
				    	AlertDialog.Builder builder = new AlertDialog.Builder(GarageRefuellings.this);
				    	
				    	final EditText input = new EditText(GarageRefuellings.this);
				        builder.setView(input);
						
				        input.setInputType(InputType.TYPE_CLASS_PHONE);
						input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

						//The maximum amount of digits is set to 14, the amount in "(XX) XXXX-XXXX".
						InputFilter[] fArray = new InputFilter[1];
						fArray[0] = new InputFilter.LengthFilter(6);
						input.setFilters(fArray);

				    	builder.setTitle(R.string.garage_refuellings_distance_driven)
				    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           
				            public void onClick(DialogInterface dialog, int id) {
				            	adapter.add(input.getText().toString());
				            	Refuelling ref = mDbHelper.updateRefuelling(new Refuelling(carId, Double.parseDouble(input.getText().toString())));
				            	Log.w("new ref", ref.getDistance()+"");
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
				});
				v.startAnimation(anim);	
			}
        	
        });
        
        list.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				final int _position = position;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(GarageRefuellings.this);
		    	
		    	builder.setTitle(R.string.garage_refuellings_delete_refuelling)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
		            	adapter.remove(adapter.getItem(_position));
		            	mDbHelper.deleteRefuelling(refuellings.get(_position).getId());
		            	dialog.dismiss();
		            }
		        })
		        .setNegativeButton("No", new DialogInterface.OnClickListener() {

		            public void onClick(DialogInterface dialog, int id) {
		            	dialog.dismiss();
		            }
		        });
				
				builder.create().show();
				return false;
				
			}
        	
        });
        
    }
	
}