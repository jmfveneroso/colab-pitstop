package deprecated;


import com.sumbioun.android.pitstop.customwidgets.FontManager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
 
public class CustEditText extends TextView {
 
    public CustEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if(!this.isInEditMode()){
        	FontManager.setFontFromAttributeSet(context, attrs, this);
        }
    }
}