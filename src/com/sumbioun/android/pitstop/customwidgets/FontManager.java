package com.sumbioun.android.pitstop.customwidgets;

import com.sumbioun.android.pitstop.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
 
/*FontManager
/*Gets custom font from attributeSet and sets it to the CustTextView.*/
public class FontManager {
	
  //This enum is tightly coupled with the enum in res/values/attrs.xml!
  //Make sure their orders stay the same.
  public enum Font {
	SourceSansPro_Black ("fonts/SourceSansPro-Black.ttf"),
	SourceSansPro_BlackItalic ("fonts/SourceSansPro-BlackItalic.ttf"),
	SourceSansPro_Bold ("fonts/SourceSansPro-Bold.ttf"),
	SourceSansPro_BoldItalic ("fonts/SourceSansPro-BoldItalic.ttf"),
	SourceSansPro_ExtraLight ("fonts/SourceSansPro-ExtraLight.ttf"),
	SourceSansPro_ExtraLightItalic ("fonts/SourceSansPro-ExtraLightItalic.ttf"),
	SourceSansPro_Italic ("fonts/SourceSansPro-Italic.ttf"),
	SourceSansPro_Light ("fonts/SourceSansPro-Light.ttf"),
	SourceSansPro_LightItalic ("fonts/SourceSansPro-LightItalic.ttf"),
	SourceSansPro_Regular ("fonts/SourceSansPro-Regular.ttf"),
	SourceSansPro_Semibold ("fonts/SourceSansPro-Semibold.ttf"),
	SourceSansPro_SemiboldItalic ("fonts/SourceSansPro-SemiboldItalic.ttf");
 
    public final String fileName;
 
    private Font(String name) {
      fileName = name;
    }
  }
 
  public static void setFont(TextView tv, int fontId) {
    Font font = getFontFromId(fontId);
    Typeface typeface = Typeface.createFromAsset(tv.getContext().getAssets(), font.fileName);
    tv.setTypeface(typeface);
  }
 
  public static Font getFontFromId(int fontId) {
    switch(fontId) {
      case 0:
        return Font.SourceSansPro_Black;
      case 1:
        return Font.SourceSansPro_BlackItalic;
      case 2:
        return Font.SourceSansPro_Bold;
      case 3:
          return Font.SourceSansPro_BoldItalic;
      case 4:
          return Font.SourceSansPro_ExtraLight;
      case 5:
          return Font.SourceSansPro_ExtraLightItalic;
      case 6:
          return Font.SourceSansPro_Italic;
      case 7:
          return Font.SourceSansPro_Light;
      case 8:
          return Font.SourceSansPro_LightItalic;
      case 9:
          return Font.SourceSansPro_Regular;
      case 10:
          return Font.SourceSansPro_Semibold;
      case 11:
          return Font.SourceSansPro_SemiboldItalic;
    }
 
    return null;
  }
 
  //Gets CustTextView attribute "CustTextView_cust_font" and sets font to the CustTexView.
  public static void setFontFromAttributeSet(Context context, AttributeSet attrs, TextView tv) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustTextView);
    if (a.length() > 0) {
      int fontId = -1;
      for (int i = 0; i < a.length(); i++) {
        int attr = a.getIndex(i);
        if (attr == R.styleable.CustTextView_cust_font) {
          fontId = a.getInt(attr, -1);
        }
      }
 
      if (fontId != -1) {
        FontManager.setFont(tv, fontId);
      }
    }
    
    a.recycle();
    
  }
}