package com.sumbioun.android.pitstop.refuel;

import java.io.FileNotFoundException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/*RefuelFullscreenPicture                                                                     */
/*This activity shows a single picture in full screen and allows the user to zoom in and out. */
public class RefuelFullscreenPicture extends RefuelFragmentActivity  {
	
	ImageView img;
	
	//Touch event related variables
	private int touchState;
    private final static int IDLE = 0;
    private final static int TOUCH = 1;
    private final static int PINCH = 2;
    private float dist0, distCurrent;
    
    private final static int IMAGE_MAX_SCALE = 4;
	
	//This horizontal scrollview holds a vertical scrollview, so the user can drag the screen to
	//scroll in any direction. It is going to be our activity content view.
	public class WScrollView extends HorizontalScrollView
	{
	    public ScrollView sv;
	    public WScrollView(Context context)
	    {
	        super(context);
	    }

	    public WScrollView(Context context, AttributeSet attrs)
	    {
	        super(context, attrs);
	    }

	    public WScrollView(Context context, AttributeSet attrs, int defStyle)
	    {
	        super(context, attrs, defStyle);
	    }

	    @Override public boolean onTouchEvent(MotionEvent event)
	    {
	        boolean ret = super.onTouchEvent(event);
	        ret = ret | sv.onTouchEvent(event);
	        
	        float distx, disty;
			   
			   switch(event.getAction() & MotionEvent.ACTION_MASK){
			   
				   case MotionEvent.ACTION_DOWN:
				    //A pressed gesture has started, the motion contains the initial starting location.
				   
				    touchState = TOUCH;
				    break;
				    
				   case MotionEvent.ACTION_POINTER_DOWN:
				    //A non-primary pointer has gone down.
				  
				    touchState = PINCH;
				    
				    //Get the distance when the second pointer touch
				    distx = event.getX(0) - event.getX(1);
				    disty = event.getY(0) - event.getY(1);
				    dist0 = (float) Math.sqrt(distx * distx + disty * disty);
				    _width = img.getWidth();
				    _height = img.getHeight();
				
				    break;
				    
				   case MotionEvent.ACTION_MOVE:
				    //A change has happened during a press gesture (between ACTION_DOWN and ACTION_UP).
	
				    if(touchState == PINCH){      
				     //Get the current distance
				     distx = event.getX(0) - event.getX(1);
				     disty = event.getY(0) - event.getY(1);
				     distCurrent = (float) Math.sqrt(distx * distx + disty * disty);
				
				     drawMatrix();
				    }
				    
				    break;
				    
				   case MotionEvent.ACTION_UP:
				    //A pressed gesture has finished.
				    touchState = IDLE;
				    break;
				    
				   case MotionEvent.ACTION_POINTER_UP:
				    //A non-primary pointer has gone up.
				    touchState = TOUCH;
				    break;
			   }
	        
	        return ret;
	    }

	    @Override public boolean onInterceptTouchEvent(MotionEvent event)
	    {
	        super.onInterceptTouchEvent(event);
	        sv.onInterceptTouchEvent(event);
	        
	        return true;
	        
	    }
	    
	}
	
	LinearLayout ll;
	ScrollView sv;
	WScrollView hsv;
	
	Bitmap bmp;
    int bmpWidth, bmpHeight;
    int _width, _height;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        img = new ImageView(this);
        img.setLayoutParams(layoutParams);
        
        
        ll = new LinearLayout(this);   
        ll.setBackgroundColor(0xFF0000FF);
        LinearLayout.LayoutParams _lp= new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        _lp.gravity=Gravity.CENTER;
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(img, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        sv = new ScrollView(this);
        sv.addView(ll, _lp);
        
        hsv = new WScrollView(this);
        hsv.sv = sv;
        hsv.addView(sv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        setContentView(hsv);

		try {
			
			//This file is written before we call this activity.
			bmp = BitmapFactory.decodeStream(openFileInput("tempBMP"));
			img.setImageBitmap(bmp);
	    	img.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					RefuelFullscreenPicture.this.finish();
				}
	    		
	    	});
	    	deleteFile("tempBMP");
	    	
	    	bmpWidth = bmp.getWidth();
	        bmpHeight = bmp.getHeight();
	        _width = bmpWidth;
	        _height = bmpHeight;
	    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
		distCurrent = 1; //Dummy default distance
        dist0 = 1;   //Dummy default distance
        
        drawMatrix();
        
        img.setOnTouchListener(new OnTouchListener(){

    		public boolean onTouch(View view, MotionEvent event) {
    			   return true;
    		}
         
        });
        
        touchState = IDLE;

 	}
 
 	@Override
	protected void onStart() {
	    super.onStart(); 
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();  

	}
    
	//Draws the bitmap in the required scale and sets it as the image shown. 
    private void drawMatrix(){
    	
        float curScale = distCurrent/dist0;
        
        Bitmap resizedBitmap;    
        int height = (int) (_height * curScale);
        int width = (int) (_width * curScale);
        
        if(width < hsv.getWidth()){
        	
        	height = (int) (((float)height/width)*hsv.getWidth());
        	width = hsv.getWidth();
        	
        } else if(width > (hsv.getWidth()*IMAGE_MAX_SCALE) && hsv.getWidth() > 0){
        	height = (int) (((float)height/width)*(hsv.getWidth()*IMAGE_MAX_SCALE));
        	width = hsv.getWidth()*IMAGE_MAX_SCALE;
        }
        
        ll.getLayoutParams().height = height;
        ll.getLayoutParams().width = width;
        
        if(height < hsv.getHeight()){

        	ll.getLayoutParams().height = hsv.getHeight();
        	
        }
    
        resizedBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
        img.setImageBitmap(resizedBitmap); 
   }

}