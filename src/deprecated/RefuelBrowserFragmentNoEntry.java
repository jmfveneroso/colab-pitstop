package deprecated;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;

import android.support.v4.app.*;
import android.view.*;
import android.os.*;

public class RefuelBrowserFragmentNoEntry extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.refuel_browser_fragment_noentry, container, false);
		
		CustTextView setRadius = (CustTextView) view.findViewById(R.id.refuel_browser_lowest_price_set_radius);
		setRadius.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//goToGasstation(lowestPriceGas);	
			}
		});
        
		
		
        return view;
    }
		
}
