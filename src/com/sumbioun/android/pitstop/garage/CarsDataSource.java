package com.sumbioun.android.pitstop.garage;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*GasstationsDataSource                                                                                        */
/*This class handles access to the internal gas station database and communication with the external database. */
public class CarsDataSource {
	  
	  private Context mContext;
	  public void setContext(Context context){
		  mContext = context;
	  }

	  private SQLiteDatabase mDatabase;
	  private GarageDatabaseCars dbHelper;
	  
	  //Database fields
	  public static String[] allColumns = { 
		  GarageDatabaseCars.COLUMN_ID,
		  GarageDatabaseCars.COLUMN_NAME};
	  
	  public CarsDataSource(Context context) {
		mContext = context;
	    dbHelper = new GarageDatabaseCars(mContext);
	  }
	
	  //Internal database opening.
	  public void open() throws SQLException {
		  mDatabase = dbHelper.getWritableDatabase();
	  }
	
	  //Internal database closing.
	  public void close() {
	    dbHelper.close();
	    
	  }
	  
	  /*private ContentValues CarToContentValues(Car car){
		  	
		  	ContentValues values = new ContentValues();
		    
		  	values.put(GarageDatabaseCars.COLUMN_ID, car.getId());
		    values.put(GarageDatabaseCars.COLUMN_NAME, car.getName());
		    
		    return values;
	  }*/
	  
	  //Add or edit a gas station in the internal database.
	  public Car updateCar(Car car) {
		    
		  	ContentValues values = new ContentValues();
		    
		  	if(car.getId() == -1){
		  		
		  	} else {
		  		values.put(GarageDatabaseCars.COLUMN_ID, car.getId());
		    }
		  	
		    values.put(GarageDatabaseCars.COLUMN_NAME, car.getName());
	    	
	    	long _id = mDatabase.replace(GarageDatabaseCars.TABLE_CARS, null, values);
	    	
	    	if(_id != -1){
	    		
			    Cursor cursor = mDatabase.query(GarageDatabaseCars.TABLE_CARS,
			        allColumns, GarageDatabaseCars.COLUMN_ID + " = " + _id, null,
			        null, null, null);
				
				cursor.moveToFirst();
			    Car returningCar = cursorToCar(cursor);
			    cursor.close();

			    return returningCar;
			    
	    	} else {
	    		return null;
	    	}

	  }
	  
	  public void deleteGasstation(Car car) {
		  
		    long id = car.getId();	    
		    mDatabase.delete(GarageDatabaseCars.TABLE_CARS, GarageDatabaseCars.COLUMN_ID + " = " + id, null);
		    
		    Log.w("CarsDataSource", "Car deleted with id: " + id);
		    
      }
	  
	  public void deleteGasstation(long id) {
		      
		  mDatabase.delete(GarageDatabaseCars.TABLE_CARS, GarageDatabaseCars.COLUMN_ID + " = " + id, null);
		    
	  }

	  //Get gas stations according to the values specified in the sorting attributes.
	  public List<Car> getCars() {
	    
		List<Car> cars = new ArrayList<Car>();
	    
	    //Setting radius limitation in the WHERE clause. Only gas stations that are inside the 
	    //box (latitude - radius, longitude - radius, latitude + radius, longitude + radius) are returned.
	    //Later we will sort out the gas stations inside the box region whose distance is longer than the radius 	    
	    Cursor cursor;
	    cursor = mDatabase.query(GarageDatabaseCars.TABLE_CARS, allColumns, null, null, null, null, null);
        	
        if(cursor.getCount() == 0) { cursor.close(); return null; }
        
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	
	        Car car = cursorToCar(cursor);
	        cars.add(car);
	        cursor.moveToNext();
	        
	    }
	    
	    //We always have to close the cursor.
	    cursor.close();
	    
	    return cars;
	    
	  }

	
	  public Car getCarById(long id) {
		  
		  Cursor cursor = mDatabase.query(GarageDatabaseCars.TABLE_CARS,
      	        allColumns, GarageDatabaseCars.COLUMN_ID + "=" + id, null, null, null, null);
		  
		  //returns null if there is no entry with the specified id
		  if(cursor.getCount() == 0){ return null; }
		  
		  cursor.moveToFirst();
		  Car car = cursorToCar(cursor);
		  cursor.close();
		  return car;
		 
	  }
	  
	  //Transform data pointed by cursor in a gasstation object.
	  private Car cursorToCar(Cursor cursor) {
		   
		  Car car = new Car();
		    car.setId(cursor.getLong(0));
		    car.setName(cursor.getString(1));
		    
		    return car;
	  }
	  
	  public boolean deleteRow(long id) 
	  {
		    return mDatabase.delete(GarageDatabaseCars.TABLE_CARS, GarageDatabaseCars.COLUMN_ID + "=" + id, null) > 0;	  
	  }
	  
	  public long getDatabaseSize(){
			return new java.io.File(mDatabase.getPath()).length();
	  }
	  
	  public void deleteDatabase(){
		  mDatabase.delete(GarageDatabaseCars.TABLE_CARS, null, null);
	  }
	  
} 	
