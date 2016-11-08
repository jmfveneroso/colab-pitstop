package com.sumbioun.android.pitstop.garage;

import android.os.Parcel;
import android.os.Parcelable;

/*Car                                              */
/*This class holds information about a single car. */
public class Refuelling implements Parcelable{
	
	//Each variable in this group corresponds to a value in the database.
	public long mId;
	public long mCarId;
    public double mDistance;
    
    public Refuelling(){
        super();
    }
    
    public Refuelling(long carId, double distance){
        super();
        setId(-1);
        setCarId(carId);
        setDistance(distance);
    }
    
    public void setId(long id){
    	mId = id;
    }
    
    public long getId(){
    	return mId;
    }
    
    public void setCarId(long id){
    	mCarId = id;
    }
    
    public long getCarId(){
    	return mCarId;
    }
    
    public void setDistance(double distance){
    	mDistance = distance;
    }
    
    public double getDistance(){
    	return mDistance;
    }

    //Parcelable implementations. 
    public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeLong(mCarId);
		out.writeDouble(mDistance);
		
	}
	
	public static final Parcelable.Creator<Refuelling> CREATOR = new Parcelable.Creator<Refuelling>() {
	    public Refuelling createFromParcel(Parcel in) {
	        return new Refuelling(in);
	    }

	    public Refuelling[] newArray(int size) {
	        return new Refuelling[size];
	    }
	};
	
	private Refuelling(Parcel in) {
		this.mId = in.readLong();
		this.mCarId = in.readLong();
        this.mDistance = in.readDouble();
        
    }

	public int describeContents() {
		return 0;
	}
    
}
