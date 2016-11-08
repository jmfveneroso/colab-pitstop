package deprecated;

import com.sumbioun.android.pitstop.R;

import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class BusinessHours extends ListActivity{
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.business_hours);

     // storing string resources into Array
        String[] extras = getResources().getStringArray(R.array.business_hours_items); 
        
        // Binding resources Array to ListAdapter
        setListAdapter(new ArrayAdapter<String>(this, R.layout.contribute_extra_list_item, extras));
    
			
		
	}
	
	@Override 
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
        super.onListItemClick(l, v, position, id);
          
		switch(position){
			//opening hour
			case 0:
				{
					final Dialog dialog = new Dialog(BusinessHours.this);
					dialog.setCancelable(true);  
					dialog.setContentView(R.layout.dialog_settime);
					dialog.setTitle("HORÁRIO DE ABERTURA");
					
					Button button = (Button) dialog.findViewById(R.id.submit);
	                button.setOnClickListener(new OnClickListener() {
	                    public void onClick(View v) {
	                		dialog.dismiss();
	                    }
	                });
					
					dialog.show();
				}
				break;
				
			//closing hour
			case 1:
				{
					final Dialog dialog = new Dialog(BusinessHours.this);
					dialog.setCancelable(true);  
					dialog.setContentView(R.layout.dialog_settime);
					dialog.setTitle("HORÁRIO DE FECHAMENTO");
					
					Button button = (Button) dialog.findViewById(R.id.submit);
	                button.setOnClickListener(new OnClickListener() {
	                    public void onClick(View v) {
	                		dialog.dismiss();
	                    }
	                });
					
					dialog.show();
				}
				break;
				
			//business days	
			case 2:
			{
				final Dialog dialog = new Dialog(BusinessHours.this);
				dialog.setCancelable(true);  
				dialog.setContentView(R.layout.dialog_business_days);
				dialog.setTitle("DIAS DE FUNCIONAMENTO");
				
				Button button = (Button) dialog.findViewById(R.id.submit);
	            button.setOnClickListener(new OnClickListener() {
	                public void onClick(View v) {
	            		dialog.dismiss();
	                }
	            });
				
				dialog.show();
			}
			break;
		}
    }
}
