package deprecated;

import com.sumbioun.android.pitstop.workshop.WorkshopMain;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.app.Activity;
import android.content.Intent;

/* RefuelActivity                                                                                                          */
/* This activity is a parent class for all activities inside the Refuel Package. It implements functionality to            */
/* switch between the Refuel and the Workshop group with a finger sweeping motion through a ViewPager.                     */
public class RefuelActivity extends Activity implements OnGestureListener {

	protected GestureDetector mGestureScanner;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
  //private static final int SWIPE_MAX_OFF_PATH = 250;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mGestureScanner = new GestureDetector(this);
       
    }

	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
            if(e1.getX() > e2.getX() && Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Intent intent = new Intent(RefuelActivity.this, WorkshopMain.class);
				startActivity(intent);	
            }else if (e1.getX() < e2.getX() && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	//movement to the right
            }
        } catch (Exception e) {
            // nothing
        }
        return true;
	}
	
	//Sends the touch information to the gesture scanner
	@Override
	public boolean onTouchEvent(MotionEvent me){
		return mGestureScanner.onTouchEvent(me);
	}
	
	//Non utilized methods
	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
