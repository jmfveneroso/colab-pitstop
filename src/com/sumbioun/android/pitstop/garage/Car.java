package com.sumbioun.android.pitstop.garage;

import android.os.Parcel;
import android.os.Parcelable;

/*Car                                              */
/*This class holds information about a single car. */
public class Car implements Parcelable{
	
	//Each variable in this group corresponds to a value in the database.
	public long mId;
    public String mName;
    
    public Car(){
        super();
    }
    
    public Car(String name){
        super();
        setId(-1);
        setName(name);
    }
    
    public void setId(long id){
    	mId = id;
    }
    
    public long getId(){
    	return mId;
    }
    
    public void setName(String name){
    	mName = name;
    }
    
    public String getName(){
    	return mName;
    }

    //Parcelable implementations. 
    public void writeToParcel(Parcel out, int flags) {
		out.writeLong(mId);
		out.writeString(mName);
		
	}
	
	public static final Parcelable.Creator<Car> CREATOR = new Parcelable.Creator<Car>() {
	    public Car createFromParcel(Parcel in) {
	        return new Car(in);
	    }

	    public Car[] newArray(int size) {
	        return new Car[size];
	    }
	};
	
	private Car(Parcel in) {
		this.mId = in.readLong();
        this.mName = in.readString();
        
    }

	public int describeContents() {
		return 0;
	}
    
}
