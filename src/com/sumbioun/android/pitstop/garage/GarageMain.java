package com.sumbioun.android.pitstop.garage;

import java.util.ArrayList;
import java.util.List;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/*WorkshopMain                                                                                           */
/*Main activity in the workshop group, that contains mostly tools that might prove useful to the user.   */
/*Provides links to access all the other workshop activities.                                            */
public class GarageMain extends Fragment {

	View rootView = null;
	
	private static CarsDataSource mDbHelper;
	
	List<Car> cars;
	
	@Override
	public void onStart() {
	    super.onStart(); 
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop(); 
	    
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.garage_main, container, false);
        
        mDbHelper = new CarsDataSource(this.getActivity());
	    mDbHelper.open();
        
        ListView list = (ListView) rootView.findViewById(R.id.garage_main_list);

        ArrayList<String> strings = new ArrayList<String>();

        cars = mDbHelper.getCars();
        
        if(cars != null){
        	
        	Log.w("size", cars.size()+"");
            
            for(int i = 0; i < cars.size(); i++){
            	strings.add(cars.get(i).getName());
            }
        	
        } else {
        	
        	cars = new ArrayList<Car>();
        	
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, strings);    
        list.setAdapter(adapter);
        
        CustTextView addCar = (CustTextView) rootView.findViewById(R.id.garage_main_add_car);
        addCar.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Animation anim = AnimationUtils.loadAnimation(GarageMain.this.getActivity(), R.anim.clickresize);
				anim.setAnimationListener(new Animation.AnimationListener(){
				    public void onAnimationStart(Animation arg0) {
				    }           
				    public void onAnimationRepeat(Animation arg0) {
				    }           
				    public void onAnimationEnd(Animation arg0) {
				    	
				    	AlertDialog.Builder builder = new AlertDialog.Builder(GarageMain.this.getActivity());
				    	
				    	final EditText input = new EditText(GarageMain.this.getActivity());
				        builder.setView(input);
						
						//The maximum amount of digits is set to 14, the amount in "(XX) XXXX-XXXX".
						InputFilter[] fArray = new InputFilter[1];
						fArray[0] = new InputFilter.LengthFilter(24);
						input.setFilters(fArray);

				    	builder.setTitle(getString(R.string.garage_main_car_name))
				    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				           
				            public void onClick(DialogInterface dialog, int id) {
				            	adapter.add(input.getText().toString());
				            	Car car = new Car(input.getText().toString());
				            	cars.add(car);
				            	
				            	mDbHelper.open();
				            	mDbHelper.updateCar(car);
				            	mDbHelper.close();
				            	
				            	dialog.dismiss();
				            }
				        })
				        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

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
				
				AlertDialog.Builder builder = new AlertDialog.Builder(GarageMain.this.getActivity());
		    	
		    	builder.setTitle(R.string.garage_main_delete_car)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           
		            public void onClick(DialogInterface dialog, int id) {
		            	adapter.remove(adapter.getItem(_position));
		            	
		        	    mDbHelper.open();
		            	mDbHelper.deleteGasstation(cars.get(_position).getId());
		            	mDbHelper.close();
		            	
		            	RefuellingsDataSource db = new RefuellingsDataSource(GarageMain.this.getActivity());
		        	    db.open();
		        	    db.deleteRefuelling(cars.get(_position).getId());
		        	    db.close();
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
        
        list.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(GarageMain.this.getActivity(), GarageRefuellings.class);
				intent.putExtra("EXTRA_GARAGEREFUELLINGS_ID", cars.get(position).getId());
				startActivity(intent);	 
			}
        	
        });
        
        mDbHelper.close();
        return rootView;
    }
	
}