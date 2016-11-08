package com.sumbioun.android.pitstop.customwidgets;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.refuel.RefuelMain;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/*QuickSettings                                                                                                      */
/*This view has three elements: fuelType, radius and sortBy. By clicking any of them, the user can change the query  */
/*options for the internal database. The current chosen option is shown below the button.                            */
public class PitstopHome extends LinearLayout {

	LinearLayout mLayout;
	Context mContext;
	
	public PitstopHome(final Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		
		if(!this.isInEditMode()){
			LayoutInflater li = (LayoutInflater) 
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			li.inflate(R.layout.pitstop_help,this,true); 
			
			//UI initialization
			mLayout = (LinearLayout) this.findViewById(R.id.pitstop_help_layout);
			setClick();

			this.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
					
		}
		
	}
	
	private void setClick(){
		mLayout.setOnClickListener(new OnClickListener(){
	
			public void onClick(View v) {
				Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.clickresize);
				anim.setAnimationListener(new Animation.AnimationListener(){  
					public void onAnimationRepeat(Animation arg0) {}
					public void onAnimationStart(Animation animation) {	}
					public void onAnimationEnd(Animation arg0) {
						Intent intent = new Intent(mContext, RefuelMain.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mContext.startActivity(intent);
				    }
				});
				v.startAnimation(anim);	
				
			
			}
	
		});
	}
	
}
