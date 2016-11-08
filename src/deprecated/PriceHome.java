/*package deprecated;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.contribute.ContributeMain;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;
import com.sumbioun.android.pitstop.refuel.RefuelGasstation;
import com.sumbioun.android.pitstop.refuel.RefuelGetRoute;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.app.Activity;
import android.content.Intent;

//This activity is deprecated. It was substituted by RefuelBrowser
public class PriceHome extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.price_home);
        
        LinearLayout plus_price = (LinearLayout) findViewById(R.id.plus_price);
        plus_price.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(PriceHome.this, RefuelGasstation.class);
				startActivity(intent);	
			}
		});
       
        LinearLayout minus_price = (LinearLayout) findViewById(R.id.minus_price);
        minus_price.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(PriceHome.this, RefuelGasstation.class);
				startActivity(intent);	
			}
		});
        
        CustTextView contribute = (CustTextView) findViewById(R.id.contribute);
        contribute.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(PriceHome.this, ContributeMain.class);
				startActivity(intent);	
			}
		});
        
        CustTextView route = (CustTextView) findViewById(R.id.main_route);
        route.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(PriceHome.this, RefuelGetRoute.class);
				startActivity(intent);	
			}
		});
       
    }

}*/