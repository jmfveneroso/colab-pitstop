package com.sumbioun.android.pitstop.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.map.ExternalGasstationDecoder;
import com.sumbioun.android.pitstop.map.GoogleDirectionsDecoder;
import com.sumbioun.android.pitstop.map.JSONParser.OnDownloadProgress;
import com.sumbioun.android.pitstop.workshop.WorkshopSettings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

/*GasstationsDataSource                                                                                        */
/*This class handles access to the internal gas station database and communication with the external database. */
public class GasstationsDataSource {
	
	  private static final int DAYS_UNTIL_UPDATE = 86400000;//86400000; //In milliseconds.
	  private static final float UPDATE_INNER_RADIUS = (float)10/111;
	  private static final float UPDATE_OUTER_RADIUS = (float)30/111;
	
	  public static final String FILENAME = "updating_bounds";
	  
	  private Context mContext;
	  public void setContext(Context context){
		  mContext = context;
	  }
	  
	  //Sorting options.
	  private static float mRadius = 10;
	  private static int mMode = 0; //SHORTEST_DISTANCE or LOWEST_PRICE.
	  private static int mFuelType = 0; 
	  
	  //Sort modes.
	  public static final int SHORTEST_DISTANCE = 0;
	  public static final int LOWEST_PRICE = 1;
	  
	  private SQLiteDatabase mDatabase;
	  private MySQLiteHelper dbHelper;
	  
	  //Database fields
	  public static String[] allColumns = { 
		  MySQLiteHelper.COLUMN_ID,
	      MySQLiteHelper.COLUMN_FLAG, 
	      MySQLiteHelper.COLUMN_PRICE_GAS,
	      MySQLiteHelper.COLUMN_PRICE_ALC,
		  MySQLiteHelper.COLUMN_PRICE_LEA,	  
		  MySQLiteHelper.COLUMN_PRICE_DIE,
		  MySQLiteHelper.COLUMN_PRICE_NAT,
		  MySQLiteHelper.COLUMN_LASTUPDATE_GAS,
	      MySQLiteHelper.COLUMN_LASTUPDATE_ALC,
	      MySQLiteHelper.COLUMN_LASTUPDATE_LEA,
	      MySQLiteHelper.COLUMN_LASTUPDATE_DIE,
	      MySQLiteHelper.COLUMN_LASTUPDATE_NAT,
	      MySQLiteHelper.COLUMN_LATITUDE, 
	      MySQLiteHelper.COLUMN_LONGITUDE, 
	      MySQLiteHelper.COLUMN_EXTRA_FLAGS,   
	      MySQLiteHelper.COLUMN_PHONE_NUMBER,
	      MySQLiteHelper.COLUMN_CONFIRMED};
	  
	  public GasstationsDataSource(Context context) {
		mContext = context;
	    dbHelper = new MySQLiteHelper(mContext);
	  }
	
	  //Internal database opening.
	  public void open() throws SQLException {
		  mDatabase = dbHelper.getWritableDatabase();
	  }
	
	  //Internal database closing.
	  public void close() {
	    dbHelper.close();
	  }
	  
	  private ContentValues gasstationToContentValues(Gasstation gasstation){
		  	
		  	ContentValues values = new ContentValues();
		    
		  	values.put(MySQLiteHelper.COLUMN_ID, gasstation.getId());
		    values.put(MySQLiteHelper.COLUMN_FLAG, gasstation.getFlag());
		    values.put(MySQLiteHelper.COLUMN_PRICE_GAS, gasstation.getPrice(Gasstation.FuelTypes.GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_ALC, gasstation.getPrice(Gasstation.FuelTypes.ALCOHOL.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_LEA, gasstation.getPrice(Gasstation.FuelTypes.LEADED_GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_DIE, gasstation.getPrice(Gasstation.FuelTypes.DIESEL.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_NAT, gasstation.getPrice(Gasstation.FuelTypes.NATURAL_GAS.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_GAS, gasstation.getLastUpdate(Gasstation.FuelTypes.GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_ALC, gasstation.getLastUpdate(Gasstation.FuelTypes.ALCOHOL.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_LEA, gasstation.getLastUpdate(Gasstation.FuelTypes.LEADED_GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_DIE, gasstation.getLastUpdate(Gasstation.FuelTypes.DIESEL.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_NAT, gasstation.getLastUpdate(Gasstation.FuelTypes.NATURAL_GAS.value));
		    values.put(MySQLiteHelper.COLUMN_LATITUDE, gasstation.getLatitude());
		    values.put(MySQLiteHelper.COLUMN_LONGITUDE, gasstation.getLongitude());
		    values.put(MySQLiteHelper.COLUMN_EXTRA_FLAGS, gasstation.getExtraFlags()); 
		    values.put(MySQLiteHelper.COLUMN_PHONE_NUMBER, gasstation.getPhoneNumber());
		    values.put(MySQLiteHelper.COLUMN_CONFIRMED, gasstation.getConfirmed());
	
		    
		    return values;
	  }
	  
	  //Add or edit a gas station in the internal database.
	  public Gasstation updateGasstation(Gasstation gasstation) {
		    
		  	ContentValues values = new ContentValues();
		    
		    values.put(MySQLiteHelper.COLUMN_FLAG, gasstation.getFlag());
		    values.put(MySQLiteHelper.COLUMN_PRICE_GAS, gasstation.getPrice(Gasstation.FuelTypes.GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_ALC, gasstation.getPrice(Gasstation.FuelTypes.ALCOHOL.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_LEA, gasstation.getPrice(Gasstation.FuelTypes.LEADED_GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_DIE, gasstation.getPrice(Gasstation.FuelTypes.DIESEL.value));
		    values.put(MySQLiteHelper.COLUMN_PRICE_NAT, gasstation.getPrice(Gasstation.FuelTypes.NATURAL_GAS.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_GAS, gasstation.getLastUpdate(Gasstation.FuelTypes.GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_ALC, gasstation.getLastUpdate(Gasstation.FuelTypes.ALCOHOL.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_LEA, gasstation.getLastUpdate(Gasstation.FuelTypes.LEADED_GASOLINE.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_DIE, gasstation.getLastUpdate(Gasstation.FuelTypes.DIESEL.value));
		    values.put(MySQLiteHelper.COLUMN_LASTUPDATE_NAT, gasstation.getLastUpdate(Gasstation.FuelTypes.NATURAL_GAS.value));
		    values.put(MySQLiteHelper.COLUMN_LATITUDE, gasstation.getLatitude());
		    values.put(MySQLiteHelper.COLUMN_LONGITUDE, gasstation.getLongitude());
		    values.put(MySQLiteHelper.COLUMN_EXTRA_FLAGS, gasstation.getExtraFlags()); 
		    values.put(MySQLiteHelper.COLUMN_PHONE_NUMBER, gasstation.getPhoneNumber());
		    values.put(MySQLiteHelper.COLUMN_CONFIRMED, gasstation.getConfirmed());

		   /* long id = gasstation.getId();
		    if(getGasstationById(id) == null){
		    	
		    	//There is no gas station with the id provided so we create a new one.
		    	id = mDatabase.insert(MySQLiteHelper.TABLE_GASSTATIONS, null,
				        values);

		    } else {
		    	
		    	//We found a gas station with a matching id, so we update it.
		    	mDatabase.update(MySQLiteHelper.TABLE_GASSTATIONS, 
				                values, MySQLiteHelper.COLUMN_ID + " = " + id, null);
		    }*/
		    
		    ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
	    	long id = edd.sendGasstationToServer(gasstation);
	    	
	    	if(id != -1){
	    		
		    	values.put(MySQLiteHelper.COLUMN_ID, id);
		    	long _id = mDatabase.replace(MySQLiteHelper.TABLE_GASSTATIONS, null, 
		    			values);
		    	
			    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_GASSTATIONS,
			        allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
			        null, null, null);
				
				cursor.moveToFirst();
			    Gasstation returningGasstation = cursorToGasstation(cursor);
			    cursor.close();
			    Log.w("id da database", ""+_id);
			    Log.w("latitude da database", ""+returningGasstation.getLatitude());
			    Log.w("longitude da database", ""+returningGasstation.getLongitude());
			    return returningGasstation;
			    
	    	} else {
	    		return null;
	    	}
	  }
	  
	  //Update only the extras in a gas station. Method called in ContributeExtra.
	  public Gasstation updateExtras(Gasstation gasstation) {
		    
		  	ContentValues values = new ContentValues();
		    
		    values.put(MySQLiteHelper.COLUMN_EXTRA_FLAGS, gasstation.getExtraFlags()); 
		    values.put(MySQLiteHelper.COLUMN_PHONE_NUMBER, gasstation.getPhoneNumber());

		    long id = gasstation.getId();
		    if(getGasstationById(id) == null){
		    	//error
		    	Log.e("updateExtras", "Gas station not found.");
		    	return null;
		    } else {
		    	//We found a gas station with a matching id, so we update it.
		    	mDatabase.update(MySQLiteHelper.TABLE_GASSTATIONS, 
				                values, MySQLiteHelper.COLUMN_ID + " = " + id, null);
		    }

		    Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_GASSTATIONS,
		        allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
		        null, null, null);
			
			cursor.moveToFirst();
		    Gasstation returningGasstation = cursorToGasstation(cursor);
		    cursor.close();

		    return returningGasstation;
	  }
	  
	  public void deleteGasstation(Gasstation gasstation) {
		  
		    long id = gasstation.getId();	    
		    mDatabase.delete(MySQLiteHelper.TABLE_GASSTATIONS, MySQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		    
		    Log.w("GasstationsDataSource", "Gasstation deleted with id: " + id);
		    
      }
	  
	  public void deleteGasstation(long id) {
		      
		    mDatabase.delete(MySQLiteHelper.TABLE_GASSTATIONS, MySQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		    
		    Log.w("GasstationsDataSource", "Gasstation deleted with id: " + id);
		    
	  }

	  //Get gas stations according to the values specified in the sorting attributes.
	  public List<Gasstation> getGasstations() {
	    List<Gasstation> gasstations = new ArrayList<Gasstation>();
	
	    String orderBy = null;
	    switch(mMode){
	    	case SHORTEST_DISTANCE:
	    		//the order makes no difference since we're getting  to sort out the list later
	    		orderBy = null;
	    		break;
	    	case LOWEST_PRICE:
	    		orderBy = getPriceColumn() + " ASC";
	    		break;
	    }
	    
	    //Setting radius limitation in the WHERE clause. Only gas stations that are inside the 
	    //box (latitude - radius, longitude - radius, latitude + radius, longitude + radius) are returned.
	    //Later we will sort out the gas stations inside the box region whose distance is longer than the radius 	    
	    Cursor cursor;

	    if(MyApplication.getGps().canGetLocation()){
	    	
        	//Radius is in Km or Mi so we have to convert it to coordinates
        	String northBoundary = String.valueOf((float) MyApplication.getGps().getLongitude() + toCoordinate((float)mRadius/1.5f));
        	String eastBoundary  = String.valueOf((float) MyApplication.getGps().getLatitude() + toCoordinate((float)mRadius/1.5f));
        	String southBoundary = String.valueOf((float) MyApplication.getGps().getLongitude() - toCoordinate((float)mRadius/1.5f));
        	String westBoundary  = String.valueOf((float) MyApplication.getGps().getLatitude() - toCoordinate((float)mRadius/1.5f));
        	
        	String selection = MySQLiteHelper.COLUMN_LATITUDE + " <=? AND " + 
        	                   MySQLiteHelper.COLUMN_LATITUDE + " >=? AND " +
        	                   MySQLiteHelper.COLUMN_LONGITUDE + " <=? AND " + 
        	                   MySQLiteHelper.COLUMN_LONGITUDE + " >=? AND " +
        			           getPriceColumn() + " >0";
        	
        	String[] args = { eastBoundary, westBoundary, northBoundary, southBoundary};
        	
        	cursor = mDatabase.query(MySQLiteHelper.TABLE_GASSTATIONS,
        	        allColumns, selection, args, null, null, orderBy);
        	
        } else {
        	
        	return null;
		    
        }
	
        if(cursor.getCount() == 0) { cursor.close(); return null; }
        
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	
	        Gasstation gasstation = cursorToGasstation(cursor);
	        gasstations.add(gasstation);
	        cursor.moveToNext();
	        
	    }
	    
	    //We always have to close the cursor.
	    cursor.close();
	    
	    if(mMode == SHORTEST_DISTANCE){
	    	//Sorts gas stations from closest to farthest distance estimation.
	    	Collections.sort(gasstations, new Gasstation());
		
	    }
	    
	    for(int i = 0; i < gasstations.size(); i++){
	    	gasstations.get(i).setTime((int)((float)gasstations.get(i).getDistanceEstimation()*2*60));
		}
	    
	    return gasstations;
	    
	  }
	  
	  //Sorts gas station by road distance from 0 to count.
	  public List<Gasstation> sortGasstations(List<Gasstation> list, int count){
		  
		  if(count > list.size()){
			  throw new ArrayIndexOutOfBoundsException();
		  }
		  
		  for(int i = 0; i < count; i++){
			  
	    		float lhs = list.get(i).getRoadDistance();
	    		for(int j = i+1; j < count; j++){
	    			
	    			float rhs = list.get(j).getRoadDistance();
	    			if(lhs > rhs){
	    				Collections.swap(list, i, j);
	    			}
	    			
	    		}
	    		
	      }
		  return list;
	  }
	  
	  public List<Gasstation> sortGasstationsFast(List<Gasstation> list){
		  
		  for(int i = 0; i < list.size(); i++){
  			  
	    		float lhs = list.get(i).getDistance();
	    		for(int j = i+1; j < list.size(); j++){
	    			
	    			float rhs = list.get(j).getDistance();
	    			if(lhs > rhs){
	    				Collections.swap(list, i, j);
	    				lhs = rhs;
	    			}
	    			
	    		}
	    }  
		return list;
		
	  }
	
	  public Gasstation getGasstationById(long id) {
		  
		  Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_GASSTATIONS,
      	        allColumns, MySQLiteHelper.COLUMN_ID + "=" + id, null, null, null, null);
		  
		  //returns null if there is no entry with the specified id
		  if(cursor.getCount() == 0){ return null; }
		  
		  cursor.moveToFirst();
		  Gasstation gasstation = cursorToGasstation(cursor);
		  cursor.close();
		  return gasstation;
		 
	  }
	  
	  //Transform data pointed by cursor in a gasstation object.
	  private Gasstation cursorToGasstation(Cursor cursor) {
		    Gasstation gasstation = new Gasstation();
		    gasstation.setId(cursor.getLong(0));
		    gasstation.setFlag(cursor.getInt(1));
		    
		    double[] prices = {cursor.getDouble(2), cursor.getDouble(3), 
		    cursor.getDouble(4), cursor.getDouble(5), cursor.getDouble(6)};
		    
		    gasstation.setPriceArray(prices);
		    
		    String[] updates = {cursor.getString(7), cursor.getString(8), 
		    	    cursor.getString(9), cursor.getString(10), cursor.getString(11)};
		    	    
		    gasstation.setLastUpdateArray(updates);
		    
		    gasstation.setLatitude(cursor.getFloat(12));
		    gasstation.setLongitude(cursor.getFloat(13));
		    gasstation.setExtraFlags(cursor.getInt(14));
		    
		    gasstation.setPhoneNumber(cursor.getString(15));
		    boolean value = cursor.getInt(16)>0;
		    gasstation.setConfirmed(value);
		    return gasstation;
	  }
	  
	  //Get closest gas station according to road distance.
	  public Gasstation getClosestGasstation(boolean SORT_BY_ROAD_DISTANCE){
		 
		 int mode = mMode;
		 setMode(SHORTEST_DISTANCE);
		 
		 //Get gas stations sorted by distance estimate.
		 List<Gasstation> list = getGasstations();

		 if(list == null){ setMode(mode); return null; }
		 
		 //Calculates road distance for the "DISTANCE_SORTING_COUNT" first gas stations and sorte them from closest to farthest.
		 if(SORT_BY_ROAD_DISTANCE){
			 //list = sortGasstations(list, DISTANCE_SORTING_COUNT);
			 
			 if(list.size() > 3){
				 list = list.subList(0, 3);
			 }
				 
			 GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder();
			 List<Gasstation> _list = gdd.executeArray(list);
			 
			 if(_list != null){
				 list = sortGasstationsFast(_list);
			 } else {
				/* for(int i = 0; i < list.size(); i++){
					 list.get(i).setTime((int)((float)list.get(i).getDistance()*2*60));
				 }*/
			 }
		 }
		 
		 setMode(mode);
		 
		 //return first gas station in the list (the closest).
		 if(list.size()  > 0){
			 return list.get(0);
		 } else {
			 return null;
		 }
	  }
	  
	  public Gasstation getCheapestGasstation(){
		  
		int mode = mMode;
		setMode(LOWEST_PRICE);
		
		//Get gas station in the internal database and sort by price.
		List<Gasstation> list = getGasstations();
		GoogleDirectionsDecoder gdd = new GoogleDirectionsDecoder();

		if(list == null || list.size() == 0){ setMode(mode); return null;}
		
		list = list.subList(0, 1);
		List<Gasstation> _list = gdd.executeArray(list);
		
		if(_list != null){
			list = _list;
		} else {
			/*if(list.get(0) != null){
				list.get(0).setTime((int)((float)list.get(0).getDistance()*2*60));
			}*/
		}

		setMode(mode);
		
		//Return first gas station in the list (the cheapest);
		if(list.size()  > 0){
			return list.get(0);
		} else {
			return null;
		}
		
	  }
	  
	  public boolean deleteRow(long id) 
	  {
		    return mDatabase.delete(MySQLiteHelper.TABLE_GASSTATIONS, MySQLiteHelper.COLUMN_ID + "=" + id, null) > 0;	  
	  }
 
	  //Get all gas stations inside the area specified. If the area is not entirely covered by the internal database,
	  //the list is downloaded from the external database.
	  public List<Gasstation> getGasstationsInsideBounds(LatLngBounds bounds){
		  
		  	if(areaInsideUpdateBounds(bounds)){
		  		Log.w("querying", "internal database");
		  		
			  	String northBoundary = String.valueOf(bounds.northeast.latitude);
		      	String eastBoundary  = String.valueOf(bounds.northeast.longitude);
		      	String southBoundary = String.valueOf(bounds.southwest.latitude);
		      	String westBoundary  = String.valueOf(bounds.southwest.longitude);
		      	
		      	String selection = MySQLiteHelper.COLUMN_LATITUDE + " <=? AND " + 
		      	                   MySQLiteHelper.COLUMN_LATITUDE + " >=? AND " +
		      	                   MySQLiteHelper.COLUMN_LONGITUDE + " <=? AND " + 
		      	                   MySQLiteHelper.COLUMN_LONGITUDE + " >=? AND " +
		      			           getPriceColumn() + " >0";
		      	
		      	String[] args = { northBoundary, southBoundary, eastBoundary, westBoundary};
		      	
		      	Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_GASSTATIONS,
		      	        allColumns, selection, args, null, null, null);
		      	
		      	List<Gasstation> gasstations = new ArrayList<Gasstation>();
		      	
		      	if(cursor.getCount() == 0){ return null; }
				  
			  	cursor.moveToFirst();
			    while (!cursor.isAfterLast()) {
			        Gasstation gasstation = cursorToGasstation(cursor);
			        gasstations.add(gasstation);
			        cursor.moveToNext();
			    }
			    
			    cursor.close();
		  		
		  		return gasstations;
		  		
		  	} else {
		  		
		  		
		  		//if(Math.pow(bounds.northeast.latitude-bounds.southwest.latitude,2) > MAXAREASIZE*MAXAREASIZE){
		  			
		  			//return null;
		  			
		  		//} else {
			  		Log.w("querying", "external database");
				    ExternalGasstationDecoder edd = new ExternalGasstationDecoder();
				    List<Gasstation> gasstations = edd.getGasstationsInsideBounds(bounds, null, null);
				  
				    return gasstations;
		  		//}
		  	}
			  
	  }
		  	
      public boolean areaInsideUpdateBounds(LatLngBounds bounds){
    	  
    	  SharedPreferences settings;
		  settings = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);

          boolean AREA_INSIDE_BOUNDS = false;
          float northBound = settings.getFloat("NORTHEASTLATITUDE", 0);
          float eastBound = settings.getFloat("NORTHEASTLONGITUDE", 0);
          float southBound = settings.getFloat("SOUTHWESTLATITUDE", 0);
          float westBound = settings.getFloat("SOUTHWESTLONGITUDE", 0);
          LatLng northeast = new LatLng(northBound, eastBound);
		  LatLng southwest = new LatLng(southBound, westBound);  
		  LatLngBounds latLngBounds = new LatLngBounds(southwest, northeast);
		  
		  //If area is inside updated area returns false;
          if(latLngBounds.contains(bounds.northeast) && latLngBounds.contains(bounds.southwest)){
        	  AREA_INSIDE_BOUNDS = true;
        	  
          }
          
          return AREA_INSIDE_BOUNDS;
    	  
      }
	  
	  private boolean userIsOutsideUpdateBounds(){
		  
		  SharedPreferences settings;
		  settings = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);

          boolean USER_OUTSIDE_BOUNDS = true;
          float northBound = settings.getFloat("NORTHEASTLATITUDE", 0) - (UPDATE_OUTER_RADIUS - UPDATE_INNER_RADIUS);
          float eastBound = settings.getFloat("NORTHEASTLONGITUDE", 0) + (UPDATE_OUTER_RADIUS - UPDATE_INNER_RADIUS);
          float southBound = settings.getFloat("SOUTHWESTLATITUDE", 0) - (UPDATE_OUTER_RADIUS - UPDATE_INNER_RADIUS);
          float westBound = settings.getFloat("SOUTHWESTLONGITUDE", 0) + (UPDATE_OUTER_RADIUS - UPDATE_INNER_RADIUS);
          if( MyApplication.getGps().getLatitude() > southBound &&
              MyApplication.getGps().getLatitude() < northBound &&
              MyApplication.getGps().getLongitude() > westBound &&
              MyApplication.getGps().getLongitude() < eastBound ){
        	  
        	  USER_OUTSIDE_BOUNDS = false;
        	  
          }
          
          return USER_OUTSIDE_BOUNDS;
          
	  }
	  
	  private LatLngBounds getNewUpdateBounds(){
		  
		  if(!MyApplication.getGps().canGetLocation()){
		    	return null;
		  }
		  
		  double northeast_lat = MyApplication.getGps().getLatitude() + UPDATE_OUTER_RADIUS;
		  double northeast_lng = MyApplication.getGps().getLongitude() + UPDATE_OUTER_RADIUS;
		  double southwest_lat = MyApplication.getGps().getLatitude() - UPDATE_OUTER_RADIUS;
		  double southwest_lng = MyApplication.getGps().getLongitude() - UPDATE_OUTER_RADIUS;
		  LatLng northeast = new LatLng(northeast_lat, northeast_lng);
		  LatLng southwest = new LatLng(southwest_lat, southwest_lng);
		  LatLngBounds latLngBounds = new LatLngBounds(southwest, northeast);
		  
		  return latLngBounds;
		  
	  }
	  
	  private boolean databaseIsOld(){
		  
		  try {
			  
			  SharedPreferences settings;
			  settings = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
			  
			  Calendar c = Calendar.getInstance(); 
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			  Date previousDate = new Date();
			  String val = settings.getString("LASTUPDATE", "1980-01-01 00:00:00");
			
			  previousDate = sdf.parse(val);  
			  
			  
			  if(previousDate != null){
	
		          Date currentDate = c.getTime();
	
		          //If at least "DAYS_UNTIL_UPDATE" have passed since the last update, we update the internal database.
		          if(currentDate.getTime() > (previousDate.getTime() + DAYS_UNTIL_UPDATE)){
		        	  return true;
		          }
		          
			  } else {
				  return false;
			  }
		  
		  } catch (ParseException e) {
		  	  e.printStackTrace();
		  } catch (Exception e){
			  e.printStackTrace();
		  }
		  
		  return false;
	          
	  }
	  
	  //Called at the beginning or when user moves more than 5km from his initial position. Downloads info about all gas 
	  //stations inside a 60km square area with the user's position in the center.  
	  public int updateInternalDatabase(OnDownloadProgress onDownloadProgress){

		  SharedPreferences settings;
		  SharedPreferences.Editor editor;
		  
		  settings = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
		  editor = settings.edit();
		  
		  if(databaseIsOld()){
			  Log.w("Gasstation", "old");
		  } 
		  if(userIsOutsideUpdateBounds()){
			  Log.w("Gasstation", "not inside bounds");
		  }
		  
          if(databaseIsOld() || userIsOutsideUpdateBounds()){
        	  //update database
        	  
    		  LatLngBounds latLngBounds = getNewUpdateBounds();
    		  if(latLngBounds == null){
    			  return 1;
    		  }
    		   
    		  Calendar c = Calendar.getInstance(); 
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			  
			  String lastUpdate = settings.getString("LASTUPDATE", "1980-01-01 00:00:00");
			  
			  if(databaseIsOld()){
				  lastUpdate = "1980-01-01 00:00:00";
			  }
    		  
    		  Log.w("Database", "Updating database from " + lastUpdate + " to " 
    		  + sdf.format(c.getTime()) + " with bounds: (northeast)" + latLngBounds.northeast.latitude + ", " + latLngBounds.northeast.longitude + " (southwest)"
    		  + latLngBounds.southwest.latitude + ", " + latLngBounds.southwest.longitude);   
    		  
    		  List<Gasstation> gasstations = new ExternalGasstationDecoder().getGasstationsInsideBounds(latLngBounds, lastUpdate, onDownloadProgress);
    		  if(gasstations == null){
    			 return 2; 
    		  }
    		  
    		  if(databaseIsOld()){
    			  deleteDatabase();
			  }
    		  
    		  for(Gasstation gas : gasstations){
    			  mDatabase.replace(MySQLiteHelper.TABLE_GASSTATIONS, null, gasstationToContentValues(gas));	  
    		  }

    		  editor.putFloat("NORTHEASTLATITUDE", (float)latLngBounds.northeast.latitude);
    		  editor.putFloat("NORTHEASTLONGITUDE", (float)latLngBounds.northeast.longitude);
    		  editor.putFloat("SOUTHWESTLATITUDE", (float)latLngBounds.southwest.latitude);
    		  editor.putFloat("SOUTHWESTLONGITUDE", (float)latLngBounds.southwest.longitude);
    		  editor.putString("LASTUPDATE", sdf.format(c.getTime()));
    		  editor.commit();
    		   
          } 
	
		  return 0; 
  
	  }
	  
	  //Get internal database price column associated with the fuelType sorting attribute.
	  public String getPriceColumn(){
		  
		  Gasstation.FuelTypes fuel = Gasstation.FuelTypes.values()[mFuelType];
		  switch(fuel){
		  	case GASOLINE:
		  		return MySQLiteHelper.COLUMN_PRICE_GAS;
		  	case ALCOHOL:
		  		return MySQLiteHelper.COLUMN_PRICE_ALC;
		  	case LEADED_GASOLINE:
		  		return MySQLiteHelper.COLUMN_PRICE_LEA;
		  	case DIESEL:
		  		return MySQLiteHelper.COLUMN_PRICE_DIE;
		  	case NATURAL_GAS:
		  		return MySQLiteHelper.COLUMN_PRICE_NAT;
		  	default:
		  		return null;
		  }
	  }
	  
	  //Get internal database lastUpdate column associated with the fuelType sorting attribute.
	  public String getLastUpdateColumn(){
		  
		  Gasstation.FuelTypes fuel = Gasstation.FuelTypes.values()[mFuelType];
		  switch(fuel){
		  	case GASOLINE:
		  		return MySQLiteHelper.COLUMN_LASTUPDATE_GAS;
		  	case ALCOHOL:
		  		return MySQLiteHelper.COLUMN_LASTUPDATE_ALC;
		  	case LEADED_GASOLINE:
		  		return MySQLiteHelper.COLUMN_LASTUPDATE_LEA;
		  	case DIESEL:
		  		return MySQLiteHelper.COLUMN_LASTUPDATE_DIE;
		  	case NATURAL_GAS:
		  		return MySQLiteHelper.COLUMN_LASTUPDATE_NAT;
		  	default:
		  		return null;
		  }
	  }
	  
	  public long getDatabaseSize(){
			return new java.io.File(mDatabase.getPath()).length();
	  }
	  
	  public void deleteDatabase(){
		  mDatabase.delete(MySQLiteHelper.TABLE_GASSTATIONS, null, null);
	  }
	  
	  //Converts from distance units of measure to coordinates.
	  public float toCoordinate(float measure){
		  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		  
	      return measure/((sharedPref.getString(WorkshopSettings.KEY_PREF_UNIT_OF_MEASUREMENT, "km").contentEquals("km")) ? 111 : 69);
	  }
	  
	  //Setters and getters.
	  public int getMode(){ return mMode; }
	  public void setMode(int mode){ mMode = mode; }
	  
	  public float getRadius(){ return mRadius; }
	  public void setRadius(float radius){ mRadius = radius; }
	  
	  public int getFuelType(){ return mFuelType; }
	  public void setFuelType(int fuelType){ mFuelType = fuelType; }
	  
} 	
