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
public class RefuellingsDataSource {
	  
	  private Context mContext;
	  public void setContext(Context context){
		  mContext = context;
	  }

	  private SQLiteDatabase mDatabase;
	  private GarageDatabaseRefuellings dbHelper;
	  
	  //Database fields
	  public static String[] allColumns = { 
		  GarageDatabaseRefuellings.COLUMN_ID,
		  GarageDatabaseRefuellings.COLUMN_CARID,
		  GarageDatabaseRefuellings.COLUMN_DISTANCE};
	  
	  public RefuellingsDataSource(Context context) {
		mContext = context;
	    dbHelper = new GarageDatabaseRefuellings(mContext);
	  }
	
	  //Internal database opening.
	  public void open() throws SQLException {
		  mDatabase = dbHelper.getWritableDatabase();
	  }
	
	  //Internal database closing.
	  public void close() {
	    dbHelper.close();
	  }
	  
	  /*private ContentValues RefuellingToContentValues(Refuelling refuelling){
		  	
		  	ContentValues values = new ContentValues();
		    
		  	values.put(GarageDatabaseRefuellings.COLUMN_ID, refuelling.getId());
		  	values.put(GarageDatabaseRefuellings.COLUMN_CARID, refuelling.getCarId());
		  	values.put(GarageDatabaseRefuellings.COLUMN_DISTANCE, refuelling.getDistance());
		    
		    return values;
	  }*/
	  
	  //Add or edit a gas station in the internal database.
	  public Refuelling updateRefuelling(Refuelling refuelling) {
		    
		  	ContentValues values = new ContentValues();
		    
		  	if(refuelling.getId() == -1){
		  		
		  	} else {
		  		values.put(GarageDatabaseRefuellings.COLUMN_ID, refuelling.getId());
		    }
		  	
		    values.put(GarageDatabaseRefuellings.COLUMN_CARID, refuelling.getCarId());
		    values.put(GarageDatabaseRefuellings.COLUMN_DISTANCE, refuelling.getDistance());
	    	Log.w("CARID",refuelling.getCarId() +"");
	    	Log.w("DISTANCE",refuelling.getDistance() +"");
		    
	    	long _id = mDatabase.replace(GarageDatabaseRefuellings.TABLE_REFUELLINGS, null, values);
	    	
	    	if(_id != -1){
	    		
			    Cursor cursor = mDatabase.query(GarageDatabaseRefuellings.TABLE_REFUELLINGS,
			        allColumns, GarageDatabaseRefuellings.COLUMN_ID + " = " + _id, null,
			        null, null, null);
				
				cursor.moveToFirst();
				Refuelling returningRefuelling = cursorToRefuelling(cursor);
			    cursor.close();

			    Log.w("DISTANCEreturning",returningRefuelling.getDistance() +"");
			    return returningRefuelling;
			    
	    	} else {
	    		return null;
	    	}

	  }
	  
	  public void deleteRefuelling(Refuelling refuelling) {
		  
		    long id = refuelling.getId();	    
		    mDatabase.delete(GarageDatabaseRefuellings.TABLE_REFUELLINGS, GarageDatabaseRefuellings.COLUMN_ID + " = " + id, null);
		    
		    Log.w("RefuellingDataSource", "Refuelling deleted with id: " + id);
		    
      }
	  
	  public void deleteRefuelling(long id) {
		      
		  mDatabase.delete(GarageDatabaseRefuellings.TABLE_REFUELLINGS, GarageDatabaseRefuellings.COLUMN_ID + " = " + id, null);
		    
	  }

	  //Get gas stations according to the values specified in the sorting attributes.
	  public List<Refuelling> getRefuellings(long id) {
	    
		List<Refuelling> refuellings = new ArrayList<Refuelling>();
	    
	    //Setting radius limitation in the WHERE clause. Only gas stations that are inside the 
	    //box (latitude - radius, longitude - radius, latitude + radius, longitude + radius) are returned.
	    //Later we will sort out the gas stations inside the box region whose distance is longer than the radius 	    
	    Cursor cursor;
	    cursor = mDatabase.query(GarageDatabaseRefuellings.TABLE_REFUELLINGS, allColumns, GarageDatabaseRefuellings.COLUMN_CARID + " = " + id, null, null, null, null);
        	
        if(cursor.getCount() == 0) { cursor.close(); return refuellings; }
        
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	
	    	Refuelling refuelling = cursorToRefuelling(cursor);
	    	refuellings.add(refuelling);
	        cursor.moveToNext();
	        
	    }
	    
	    //We always have to close the cursor.
	    cursor.close();
	    
	    return refuellings;
	    
	  }

	
	  public Refuelling getRefuellingById(long id) {
		  
		  Cursor cursor = mDatabase.query(GarageDatabaseRefuellings.TABLE_REFUELLINGS,
      	        allColumns, GarageDatabaseRefuellings.COLUMN_ID + "=" + id, null, null, null, null);
		  
		  //returns null if there is no entry with the specified id
		  if(cursor.getCount() == 0){ return null; }
		  
		  cursor.moveToFirst();
		  Refuelling refuelling = cursorToRefuelling(cursor);
		  cursor.close();
		  return refuelling;
		 
	  }
	  
	  //Transform data pointed by cursor in a gasstation object.
	  private Refuelling cursorToRefuelling(Cursor cursor) {
		   
		  Refuelling refuelling = new Refuelling();
	  	  refuelling.setId(cursor.getLong(0));
	  	  refuelling.setCarId(cursor.getLong(1));
	  	  refuelling.setDistance(cursor.getDouble(2));
	    
	      return refuelling;
	  }
	  
	  public boolean deleteRow(long id) 
	  {
		    return mDatabase.delete(GarageDatabaseRefuellings.TABLE_REFUELLINGS, GarageDatabaseRefuellings.COLUMN_CARID + "=" + id, null) > 0;	  
	  }
	  
	  public long getDatabaseSize(){
			return new java.io.File(mDatabase.getPath()).length();
	  }
	  
	  public void deleteDatabase(){
		  mDatabase.delete(GarageDatabaseRefuellings.TABLE_REFUELLINGS, null, null);
	  }
	  
} 	
