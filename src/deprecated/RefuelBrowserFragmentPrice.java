package deprecated;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
//import com.sumbioun.android.pitstop.customwidgets.PriceDisplayer;
//import com.sumbioun.android.pitstop.database.GPSTracker;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
//import com.sumbioun.android.pitstop.map.GoogleDirectionsDecoder;
import com.sumbioun.android.pitstop.refuel.RefuelBrowser;
import com.sumbioun.android.pitstop.refuel.RefuelGasstation;

import deprecated.RefuelBrowserFragmentDistance.OnOpenMapListener;

import android.view.*;
import android.widget.LinearLayout;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.*;

public class RefuelBrowserFragmentPrice  extends Fragment {
	
	private LinearLayout ll_shortestDistance;
	private LinearLayout ll_lowestPrice;
	
	//private CustTextView lowestPriceTime = null;
	//private PriceDisplayer lowestPriceDisplayer = null;
	//private CustTextView lowestPriceDistance = null;
	private CustTextView lowestPriceLastupdate = null;
    
	//private CustTextView shortestDistanceTime = null;
	//private PriceDisplayer shortestDistanceDisplayer = null;
	//private CustTextView shortestDistanceDistance = null;
	private CustTextView shortestDistanceLastupdate = null;
	
	Gasstation shortestDistanceGas = null;
	Gasstation lowestPriceGas = null;
	
	private int mFuelType = 0;
	
	OnOpenMapListener onOpenMapListener;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		
		View view = inflater.inflate(R.layout.refuel_browser_fragment_price, container, false);
        
        //lowestPriceTime = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_lowest_price_time);
	//	lowestPriceDisplayer = (PriceDisplayer) view.findViewById(R.id.refuel_browser_fragment_price_lowest_price_displayer);   
		//lowestPriceDistance = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_lowest_price_distance);
		lowestPriceLastupdate = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_lowest_price_last_update);
        
        //shortestDistanceTime = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_shortest_distance_time);
		//shortestDistanceDisplayer = (PriceDisplayer) view.findViewById(R.id.refuel_browser_fragment_price_shortest_distance_displayer);
	//	shortestDistanceDistance = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_shortest_distance_distance);
		shortestDistanceLastupdate = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_price_shortest_distance_last_update);
		
		//Gets two gas stations (the one with the lowest price and the one with the shortest distance from the user)
		//that were passed by RefuelBrowser and updates the views to show the relevant information 
        Bundle bundle = getArguments();
		if(bundle != null){
			
			shortestDistanceGas = bundle.getParcelable(RefuelBrowser.KEY_SHORTEST_DISTANCE_GAS);
			lowestPriceGas = bundle.getParcelable(RefuelBrowser.KEY_LOWEST_PRICE_GAS);
			mFuelType = bundle.getInt(RefuelBrowser.KEY_FUEL_TYPE);
			
			updateLowestPrice();
			updateShortestDistance();
			
		}
        
		//Initializing the views	
		ll_lowestPrice = (LinearLayout) view.findViewById(R.id.refuel_browser_fragment_price_lowest_price);
		ll_lowestPrice.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				goToGasstation(shortestDistanceGas);
			}
		});
		ll_lowestPrice.setOnLongClickListener(new View.OnLongClickListener() {

			public boolean onLongClick(View v) {
				onOpenMapListener.onOpenMap(GasstationsDataSource.LOWEST_PRICE);
				return false;
			}
		});
		
		
		ll_shortestDistance = (LinearLayout) view.findViewById(R.id.refuel_browser_fragment_price_shortest_distance);
		ll_shortestDistance.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {	
					goToGasstation(shortestDistanceGas);
				}
		});
		ll_shortestDistance.setOnLongClickListener(new View.OnLongClickListener() {

			public boolean onLongClick(View v) {
				onOpenMapListener.onOpenMap(GasstationsDataSource.SHORTEST_DISTANCE);
				return false;
			}
		});
        
        return view;
    }
	
	void updateLowestPrice(){
		if(lowestPriceGas != null){
			//lowestPriceTime.setText(String.valueOf(lowestPriceGas.getRoundedDistance()*1.75));
		//	lowestPriceDisplayer.updateImages(lowestPriceGas.getPrice(mFuelType));  
			//lowestPriceDistance.setText(String.valueOf(lowestPriceGas.getRoundedDistance()));
			lowestPriceLastupdate.setText(String.valueOf(lowestPriceGas.getLastUpdate(mFuelType)));
			
			//GPSTracker gps = new GPSTracker(this.getActivity());
			/*new GoogleDirectionsDecoder(gps.getLatitude(), gps.getLongitude(), lowestPriceGas.getLatitude(), lowestPriceGas.getLongitude()).setOnInfoRetrievedListener(new OnInfoRetrievedListener(){

    			public void onInfoRetrieved(int distance, int duration) {
    				//Toast.makeText(RefuelBrowser.this, String.valueOf(distance), Toast.LENGTH_SHORT).show();
    				lowestPriceTime.setText(String.valueOf((float)distance/1000));
    			}
    			
    		});*/
		}
	}
	
	void updateShortestDistance(){
		if(shortestDistanceGas!= null){
			//shortestDistanceTime.setText(String.valueOf(shortestDistanceGas.getRoundedDistance()*1.75));
		//	shortestDistanceDisplayer.updateImages(shortestDistanceGas.getPrice(mFuelType));  
			//shortestDistanceDistance.setText(String.valueOf(shortestDistanceGas.getRoundedDistance()));
			shortestDistanceLastupdate.setText(String.valueOf(shortestDistanceGas.getLastUpdate(mFuelType)));
		
			//GPSTracker gps = new GPSTracker(this.getActivity());
			//GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder(gps.getLatitude(), gps.getLongitude(), lowestPriceGas.getLatitude(), lowestPriceGas.getLongitude());
			//gdd.execute();
			//lowestPriceTime.setText(String.valueOf((float)gdd.getDistance()/1000));

		}
	}
	
	void goToGasstation(Gasstation gasstation){
		Intent intent = new Intent(getActivity(), RefuelGasstation.class);
		startActivity(intent);
	}
	
	void setOnOpenMapListener(OnOpenMapListener onOpenMapListener){
		this.onOpenMapListener = onOpenMapListener;
	}
}
