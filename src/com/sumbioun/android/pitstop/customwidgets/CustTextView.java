package com.sumbioun.android.pitstop.customwidgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
 
/*CustTextView                                                               */
/*Only difference from a regular TextView are the custom fonts incorporated. */
public class CustTextView extends TextView {
 
    public CustTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if(!this.isInEditMode()){
        	FontManager.setFontFromAttributeSet(context, attrs, this);
        }
    }
}