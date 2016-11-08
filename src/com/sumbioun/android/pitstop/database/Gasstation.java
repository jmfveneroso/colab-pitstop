package com.sumbioun.android.pitstop.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.map.GoogleDirectionsDecoder;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

/*Gasstation                                                                                                                  */
/*This class holds information about a single gas station, ranging from fuel prices to information about shops and open hours */
public class Gasstation implements Parcelable, Comparator<Gasstation>, Comparable<Gasstation>{
	
	//The resources linked to each gas station flag.
	public enum Flag {
        ESSO(R.drawable.color_flag_esso, R.drawable.colored_flag_esso_big), BR(R.drawable.color_flag_br, R.drawable.colored_flag_br_big), IPIRANGA(R.drawable.color_flag_ipiranga, R.drawable.colored_flag_ipiranga_big), ALE(R.drawable.color_flag_ale, R.drawable.colored_flag_ale_big), SHELL(R.drawable.color_flag_shell, R.drawable.colored_flag_shell_big), TEXACO(R.drawable.flag_texaco, R.drawable.colored_flag_esso_big), UNDEFINED(R.drawable.flag_undefined, R.drawable.flag_undefined_big);
        
        public int value;
        public int big;
        private Flag(int value, int big) {
        	this.value = value;
        	this.big = big;
        }
    }
	
	//The types of fuel the gas station offers.
	public enum FuelTypes {
		GASOLINE(0), ALCOHOL(1), LEADED_GASOLINE(2), DIESEL(3), NATURAL_GAS(4);
	
		public int value;
        private FuelTypes(int value) {
        	this.value = value;
        }
	}
	
	//The possible payment methods in the gas station.
	public enum PaymentMethods {
		VISA(EXTRAFLAG_VISA), MASTERCARD(EXTRAFLAG_MASTERCARD), AMERICANEXPRESS(EXTRAFLAG_AMERICANEXPRESS), VISA_ELECTRON(EXTRAFLAG_VISAELECTRON), CHECK(EXTRAFLAG_CHECK);
	
		public int value;
        private PaymentMethods(int value) {
        	this.value = value;
        }
	}
	
	//The type of convenience store in the gas station.
	public enum ConvenienceStore {
		STORE(EXTRAFLAG_STORE), DRUGSTORE(EXTRAFLAG_DRUGSTORE);
	
		public int value;
        private ConvenienceStore(int value) {
        	this.value = value;
        }
	}
	
	//The type of car-wash in the gas station.
	public enum CarWash {
		NONE(0), AUTO(EXTRAFLAG_AUTOCARWASH), MANUAL(EXTRAFLAG_MANUALCARWASH), PRESSURE(EXTRAFLAG_PRESSURECARWASH);
	
		public int value;
        private CarWash(int value) {
        	this.value = value;
        }
	}
	
	//Extra flag is a long value that holds single bit information about the gas station.
	public static final int EXTRAFLAG_VISA = 1;
	public static final int EXTRAFLAG_MASTERCARD = 2;
	public static final int EXTRAFLAG_AMERICANEXPRESS = 4;
	public static final int EXTRAFLAG_VISAELECTRON = 8;
	public static final int EXTRAFLAG_CHECK = 16;
	public static final int EXTRAFLAG_STORE = 32;
	public static final int EXTRAFLAG_DRUGSTORE = 64;
	public static final int EXTRAFLAG_AUTOCARWASH = 128;
	public static final int EXTRAFLAG_MANUALCARWASH = 256;
	public static final int EXTRAFLAG_PRESSURECARWASH = 384;
	public static final int EXTRAFLAG_REPAIR = 512;
	public static final int EXTRAFLAG_RESTAURANT = 1024;
	public static final int EXTRAFLAG_24HOURS = 2048;
	
	private static final float UPDATE_RANGE = 0.1f;
	
	//If set to false then we will use the imperial system for the conversions in getDistance().
	private boolean FLAG_KILOMETER = true; 
	
	//The user's coordinates in the most recent distance measurement. So we know that, if the user's position hasn't changed
	//we may safely assume the distance to this gas station is unchanged.
	public float mLastLatitude = 0;
	public float mLastLongitude = 0;
	
	//Each variable in this group corresponds to a value in the database.
	public long mId;
    public int mFlag;
    public double[] mPrice = new double[5];
    
    //The date of that a price was last updated.
    public String[] mLastUpdate = new String[5];
    
    public double mLatitude = 0;
    public double mLongitude = 0;
    
    private long mExtraFlags = 0;
    private String mPhoneNumber = "";
    
    private double mRating = 3;
    private boolean mConfirmed = false;
    
    //Duration of the route from the user's current position to the gas station.
    private int mTime;
    
    //Last distance measured.
    private float mDistance;
    
    public Gasstation(){
        super();
    }
    
	//Setters and getters
    public void setId(long id){ this.mId = id; }
    public long getId(){ return this.mId; }
    
    public void setFlag(int flag){ this.mFlag = flag; }
    public int getFlag(){ return this.mFlag; }
    
    public void setPrice(int fuelType, double price){ this.mPrice[fuelType] = price; }
    public double getPrice(int fuelType){ return this.mPrice[fuelType]; } 
    
    public void setPriceArray(double[] price){ this.mPrice = price; }
    public double[] getPriceArray(){ return this.mPrice; }
    
    //Set specified lastUpdate to current time.
    public void setLastUpdate(int fuelType){
    	Calendar c = Calendar.getInstance(); 
		Date date = c.getTime();

		String string = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
		Log.w("updated", string); 
    	this.mLastUpdate[fuelType] = string; 
    	
    }
    public String getLastUpdate(int fuelType){ 
    	return this.mLastUpdate[fuelType]; 
    }
    public String getFormattedLastUpdate(int fuelType){ 
    	try {
    		if(this.mLastUpdate[fuelType].contentEquals("0000-00-00 00:00:00")){return "";}
    		
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this.mLastUpdate[fuelType]);
			String str = new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(date);
			return str;
    	} catch (ParseException e) {
			e.printStackTrace();
		}
    	return ""; 
    }
    
    public void setLastUpdateArray(String lastUpdate[]){
    	this.mLastUpdate = lastUpdate;
    }
    public String[] getLastUpdateArray(){ return this.mLastUpdate; }

    public void setExtraFlags(long extraFlags){ this.mExtraFlags = extraFlags; }
    public long getExtraFlags(){ return this.mExtraFlags; }
    
    public void setExtraFlag(int extraFlag, boolean state){ 
    	//We first make the flag's state to be "false" in "(extraFlag & mExtraFlags)*(-1)" and then we set the new 
    	//value in "(state ? extraFlag : 0)".
    	this.mExtraFlags += (extraFlag & mExtraFlags)*(-1) + (state ? extraFlag : 0); 
    	
    }
    public boolean getExtraFlag(int extraFlag){ 
    	if((extraFlag & mExtraFlags) == extraFlag){
    		return true;
    	} else {
    		return false;
    	}
    }
 
    public void setLatitude(double latitude){ this.mLatitude = latitude; }
    public double getLatitude(){ return this.mLatitude; }
    
    public void setLongitude(double longitude){ this.mLongitude = longitude; }
    public double getLongitude(){ return this.mLongitude; }
    
    public void setPhoneNumber(String phoneNumber){ this.mPhoneNumber = phoneNumber; }
    public String getPhoneNumber(){ return this.mPhoneNumber; }
    
    public void setRating(double rating){ this.mRating = rating; }
    public double getRating(){ return this.mRating; }
    
    public void setConfirmed(boolean confirmed){ this.mConfirmed = confirmed; }
    public boolean getConfirmed(){ return this.mConfirmed; }
    

	public void setTime(int time) {	this.mTime = time; }
    public int getTime(){return mTime;}
    
    public float getDistance(){ 
    	
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
    	FLAG_KILOMETER = false;
        if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
        	FLAG_KILOMETER = true;
        }
        
    	return ((float) mDistance*((FLAG_KILOMETER) ? 1.0f : 0.625f)); 
    }
    
    public void setDistance(float distance){ this.mDistance = distance; }
    
    //Estimate road distance by calculating the linear distance between the user's position and the gas station.
	public float getDistanceEstimation(){ 

        if(MyApplication.getGps().canGetLocation()){

        	float latitudeDiference = (float) (MyApplication.getGps().getLatitude() - getLatitude());
        	float longitudeDiference = (float) (MyApplication.getGps().getLongitude() - getLongitude());
        	double quadraticSum = (Math.pow(latitudeDiference, 2) + Math.pow(longitudeDiference, 2));
        	
        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        	FLAG_KILOMETER = false;
            if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
            	FLAG_KILOMETER = true;
            }
        	
        	mDistance = (float) Math.sqrt(quadraticSum)*111;
        	return ((float) mDistance*((FLAG_KILOMETER) ? 1.0f : 0.625f));
        }
        
    	return 0; 
    }
 
	//Get road distance by calling the Google Directions API with user and the gas station position.
    public float getRoadDistance(){
    	
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
    	FLAG_KILOMETER = false;
        if(sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")){
        	FLAG_KILOMETER = true;
        }
    	
        if(MyApplication.getGps().canGetLocation()){
        	
        	//Get linear distance between the current user location and the previous location.
        	float latitudeDiference = (float) (MyApplication.getGps().getLatitude() - mLastLatitude);
        	float longitudeDiference = (float) (MyApplication.getGps().getLongitude() - mLastLongitude);
        	mLastLatitude = (float) MyApplication.getGps().getLatitude();
        	mLastLongitude = (float) MyApplication.getGps().getLongitude();
        	double quadraticSum = (Math.pow(latitudeDiference, 2) + Math.pow(longitudeDiference, 2));

        	//If the distance is less than a minimum update range we return the last distance measured.
        	if(Math.sqrt((float) quadraticSum)*(FLAG_KILOMETER ? 111 : 69) <= UPDATE_RANGE){
        		return ((float) mDistance*((FLAG_KILOMETER) ? 1.0f : 0.625f));
        	}
        	
        	//Call the Google Directions API to get the road distance and route duration.
	    	try{
	    		
	        	GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder(MyApplication.getGps().getLatitude(), 
		    			MyApplication.getGps().getLongitude(), getLatitude(), getLongitude());
		    	
				gdd.execute();
				
				//Converts to a value with one decimal place.
				mDistance = (float)Math.round(gdd.getDistance()/100)/10;
				
				//Updates route duration.
				mTime = gdd.getDuration();
				
	    	} catch(JSONException e){
	    		e.printStackTrace();
	    		return -1;
	    	}
			
	    	Log.w("disatnce", ""+((float) mDistance*((FLAG_KILOMETER) ? 1.0f : 0.625f)));
	    	if(FLAG_KILOMETER){
	    		Log.w("oh yeah", sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km"));
	    	}
	    	
			return ((float) mDistance*((FLAG_KILOMETER) ? 1.0f : 0.625f));

        } else {
        	Log.e("getRoadDistance", "GPS is off.");
        	return 0;
        }
        
    }
    
    //Parcelable implementations. 
    public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeInt(mFlag);
		out.writeDoubleArray(mPrice);
		out.writeStringArray(mLastUpdate);
		out.writeDouble(mLatitude);
		out.writeDouble(mLongitude);
		out.writeLong(mExtraFlags);		
		out.writeString(mPhoneNumber);
		out.writeByte((byte) (mConfirmed? 1 : 0));
		
	}
	
	public static final Parcelable.Creator<Gasstation> CREATOR = new Parcelable.Creator<Gasstation>() {
	    public Gasstation createFromParcel(Parcel in) {
	        return new Gasstation(in);
	    }

	    public Gasstation[] newArray(int size) {
	        return new Gasstation[size];
	    }
	};
	
	private Gasstation(Parcel in) {
		this.mId = in.readLong();
        this.mFlag = in.readInt();
        in.readDoubleArray(this.mPrice);
        in.readStringArray(this.mLastUpdate);
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mExtraFlags = in.readLong();
        this.mPhoneNumber = in.readString();
        if(in.readByte() == 1) { this.mConfirmed = true; } else { this.mConfirmed = false; }
        
    }
	
	public int describeContents() {
		return 0;
	}
	
	//Comparator implementations. Used to sort gas stations by distance.
	public int compareTo(Gasstation another) {
		final int NOT_EQUAL = -1;
	    final int EQUAL = 0;
	    
	    if ( this == another ) return EQUAL;
	    return NOT_EQUAL;
	    
	}
	
	public int compare(Gasstation lhs, Gasstation rhs) {
		if (lhs.getDistanceEstimation() < rhs.getDistanceEstimation()) return -1;
        if (lhs.getDistanceEstimation() > rhs.getDistanceEstimation()) return 1;
		return 0;
	}
	
}
