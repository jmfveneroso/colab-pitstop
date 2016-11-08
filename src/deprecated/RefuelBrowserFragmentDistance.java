package deprecated;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.GasstationsDataSource;
import com.sumbioun.android.pitstop.refuel.RefuelBrowser;
import com.sumbioun.android.pitstop.refuel.RefuelGasstation;

import android.support.v4.app.*;
import android.view.*;
import android.widget.LinearLayout;
import android.content.Context;
import android.content.Intent;
import android.os.*;

public class RefuelBrowserFragmentDistance extends Fragment {

	public interface OnOpenMapListener{
		void onOpenMap(int mode);
	}
	
	private LinearLayout shortestDistance;
	private LinearLayout lowestPrice;
	
	private CustTextView shortestDistanceTime = null;
	//private PriceDisplayer shortestDistanceDisplayer = null;
	private CustTextView shortestDistancePrice = null;
	private CustTextView shortestDistanceLastupdate = null;
	
	private CustTextView lowestPriceTime = null;
	//private PriceDisplayer lowestPriceDisplayer = null;
	private CustTextView lowestPricePrice = null;
	private CustTextView lowestPriceLastupdate = null;
	
	private CustTextView labelLess = null;
	private CustTextView labelMore = null;
	
	Gasstation shortestDistanceGas = null;
	Gasstation lowestPriceGas = null;
	
	private int mFuelType = 0;
	private int mMode = 0;
	
	OnOpenMapListener onOpenMapListener;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		
		View view = inflater.inflate(R.layout.refuel_browser_fragment_distance, container, false);
		
		shortestDistanceTime = (CustTextView) view.findViewById(R.id.refuel_browser_shortest_distance_time);
		//shortestDistanceDisplayer = (PriceDisplayer) view.findViewById(R.id.refuel_browser_shortest_distance_displayer);
		shortestDistancePrice = (CustTextView) view.findViewById(R.id.refuel_browser_shortest_distance_price);
		shortestDistanceLastupdate = (CustTextView) view.findViewById(R.id.refuel_browser_shortest_distance_last_update);
		
		lowestPriceTime = (CustTextView) view.findViewById(R.id.refuel_browser_lowest_price_time);
		//lowestPriceDisplayer = (PriceDisplayer) view.findViewById(R.id.refuel_browser_lowest_price_displayer);   
		lowestPricePrice = (CustTextView) view.findViewById(R.id.refuel_browser_lowest_price_price);
		lowestPriceLastupdate = (CustTextView) view.findViewById(R.id.refuel_browser_lowest_price_last_update);
		
		labelLess = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_label_less);
		labelMore = (CustTextView) view.findViewById(R.id.refuel_browser_fragment_label_more);
		
		//Gets two gas stations (the one with the lowest price and the one with the shortest distance from the user)
		//that were passed by RefuelBrowser and updates the views to show the relevant information 
		Bundle bundle = getArguments();
		if(bundle != null){
			
			shortestDistanceGas = bundle.getParcelable(RefuelBrowser.KEY_SHORTEST_DISTANCE_GAS);
			lowestPriceGas = bundle.getParcelable(RefuelBrowser.KEY_LOWEST_PRICE_GAS);
			mFuelType = bundle.getInt(RefuelBrowser.KEY_FUEL_TYPE);
			mMode = bundle.getInt(RefuelBrowser.KEY_MODE);
			
			if(mMode == GasstationsDataSource.SHORTEST_DISTANCE){
				labelLess.setText("-km");
				labelMore.setText("+km");
				updateShortestDistance(shortestDistanceGas);
				updateLowestPrice(lowestPriceGas);
			} else {
				labelLess.setText("-$");
				labelMore.setText("+$");
				updateShortestDistance(lowestPriceGas);
				updateLowestPrice(shortestDistanceGas);
			}
			
		}
		
		shortestDistance = (LinearLayout) view.findViewById(R.id.refuel_browser_fragment_distance_shortest_distance);
		shortestDistance.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					goToGasstation(shortestDistanceGas);
				}
		});
		shortestDistance.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				Vibrator vib = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
				vib.vibrate(200);
				onOpenMapListener.onOpenMap(GasstationsDataSource.SHORTEST_DISTANCE);
				return false;
			}
		});
			
        lowestPrice = (LinearLayout) view.findViewById(R.id.refuel_browser_fragment_distance_lowest_price);
        lowestPrice.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				goToGasstation(lowestPriceGas);	
			}
		});
        lowestPrice.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				Vibrator vib = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
				vib.vibrate(200);
				onOpenMapListener.onOpenMap(GasstationsDataSource.LOWEST_PRICE);
				return false;
			}
		});
        
        return view;
    }
	
	private void updateShortestDistance(Gasstation gasstation){
		if(shortestDistanceGas!= null){
			shortestDistanceTime.setText(String.valueOf(gasstation.getTime()/60) + " min");
			if(mMode == GasstationsDataSource.SHORTEST_DISTANCE){
				//shortestDistanceDisplayer.updateImages(gasstation.getActualDistance());
				shortestDistancePrice.setText("$" + String.valueOf(gasstation.getPrice(mFuelType)));
			} else {
				//shortestDistanceDisplayer.updateImages(gasstation.getPrice(mFuelType));
				shortestDistancePrice.setText(String.valueOf(gasstation.getRoadDistance() + " km"));
			}
			shortestDistanceLastupdate.setText(String.valueOf(gasstation.getLastUpdate(mFuelType)));
		}
	}
	
	private void updateLowestPrice(Gasstation gasstation){
		if(lowestPriceGas != null){
			lowestPriceTime.setText(String.valueOf(gasstation.getTime()/60 + " min"));
			if(mMode == GasstationsDataSource.SHORTEST_DISTANCE){
				//lowestPriceDisplayer.updateImages(gasstation.getActualDistance());
				lowestPricePrice.setText("$" + String.valueOf(gasstation.getPrice(mFuelType)));
			} else {
				//lowestPriceDisplayer.updateImages(gasstation.getPrice(mFuelType));
				lowestPricePrice.setText(String.valueOf(gasstation.getRoadDistance() + " km"));
			}
			lowestPriceLastupdate.setText(String.valueOf(gasstation.getLastUpdate(mFuelType)));
		}
	}
	
	void goToGasstation(Gasstation gasstation){

		Intent intent = new Intent(getActivity(), RefuelGasstation.class);
		intent.putExtra("RefuelBrowserFragmentDistance.EXTRA_GASSTATION", gasstation);
		startActivity(intent);
	}
	
	void setOnOpenMapListener(OnOpenMapListener onOpenMapListener){
		this.onOpenMapListener = onOpenMapListener;
	}
		
}
