package com.sumbioun.android.pitstop.customwidgets;

import com.sumbioun.android.pitstop.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/*StarGroup                                                                                                          */
/*View that holds 5 star drawables to rate a gas station. By clicking over a star, the user can change the rating,   */
/*from 1 to 5 stars. The view supports half stars, but by clicking the user can only set entire stars.               */ 
public class StarGroup extends LinearLayout{
	
	private static float AVERAGE_RATING = 3.0f; 
	
	//ImageViews to hold a star drawable.
	ImageView[] imageView = new ImageView[5];
	
	//The view's size attribute in density independent pixels.
	String mSize = null;
	
	boolean editable = false;
	
	private double mHalfStars;
	
    public StarGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.StarGroup);
	    editable = a.getBoolean(R.styleable.StarGroup_star_editable, false);
	    mSize = a.getString(R.styleable.StarGroup_star_size);
	    a.recycle();
		
		if(!this.isInEditMode()){
			this.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
			
			initImages(context);
			
			addView(imageView[0]);
			addView(imageView[1]);
			addView(imageView[2]);
			addView(imageView[3]);
			addView(imageView[4]);
		}
	}
		
    //Convert the value in half stars and updates the view.
	public void setRating(double rating){
		
		mHalfStars = (int) ((rating+0.25)/0.5);
		
		for(int i = 0; i < 5; i++){
			if(mHalfStars >= 2){
				mHalfStars -= 2;
				imageView[i].setImageResource(R.drawable.star_full);
			} else if(mHalfStars == 1) {
				mHalfStars -= 1;
				imageView[i].setImageResource(R.drawable.star_half);
			} else {
				imageView[i].setImageResource(R.drawable.star_empty);
			}
		}
		
		mHalfStars = rating;
		
	}
		
	//Initializes the view, changing the size of the star drawables.
	public void initImages(Context context) {
		
		for(int i = 0; i < 5; i++){
			imageView[i] = new ImageView(context);
			
			LinearLayout.LayoutParams layoutParams;
			
			if(mSize == null){
				layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			} else {
				//Transforms the density independent pixels into pixels.
				final float scale = getContext().getResources().getDisplayMetrics().density;
				int widthPixels = (int) (Integer.parseInt(mSize) * scale + 0.5f);
				
				layoutParams = new LinearLayout.LayoutParams(widthPixels, LayoutParams.WRAP_CONTENT);
			}
			
			imageView[i].setLayoutParams(layoutParams);
			
			final int iterator = i;			
			if(editable){
				imageView[i].setOnClickListener(new OnClickListener(){
	
					public void onClick(View view) {
						setRating(iterator+1);
					}
					
				});
			}
		}
		
		//Initializes the view by setting the average rating.
		setRating(AVERAGE_RATING);
		
	}
	
	public double getRating(){ return mHalfStars; }
}
