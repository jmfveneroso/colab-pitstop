package com.sumbioun.android.pitstop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*MySQLiteHelper                                                */
/*SQLiteOpenHelper class for the internal gas station database  */
public class MySQLiteHelper extends SQLiteOpenHelper {

	  public static final String TABLE_GASSTATIONS = "gasstations";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_FLAG = "flag";
	  public static final String COLUMN_PRICE_GAS = "price_gasoline";
	  public static final String COLUMN_PRICE_LEA = "price_leaded_gasoline";
	  public static final String COLUMN_PRICE_ALC = "price_alcohol";
	  public static final String COLUMN_PRICE_DIE = "price_diesel";
	  public static final String COLUMN_PRICE_NAT = "price_natural_gas";
	  public static final String COLUMN_LATITUDE = "latitude";
	  public static final String COLUMN_LONGITUDE = "longitude";
	  public static final String COLUMN_EXTRA_FLAGS = "extra_flags";
	  public static final String COLUMN_LASTUPDATE_GAS = "lastupdate_gas";	  
	  public static final String COLUMN_LASTUPDATE_ALC = "lastupdate_alc";
	  public static final String COLUMN_LASTUPDATE_LEA = "lastupdate_lea";
	  public static final String COLUMN_LASTUPDATE_DIE = "lastupdate_die";
	  public static final String COLUMN_LASTUPDATE_NAT = "lastupdate_nat";
	  public static final String COLUMN_PHONE_NUMBER = "phone_number";
	  public static final String COLUMN_RATING = "rating";
	  public static final String COLUMN_CONFIRMED = "confirmed";
	  
	  private static final String DATABASE_NAME = "gasstations.db";
	  private static final int DATABASE_VERSION = 11;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_GASSTATIONS + "(" + COLUMN_ID + " integer primary key autoincrement, " 
		  + COLUMN_FLAG + " integer, " 
	      + COLUMN_PRICE_GAS + " real, " 
	      + COLUMN_PRICE_ALC + " real, " 
		  + COLUMN_PRICE_LEA + " real, "    
		  + COLUMN_PRICE_DIE + " real, "
	      + COLUMN_PRICE_NAT + " real, "
	      + COLUMN_LASTUPDATE_GAS + " text, "
	      + COLUMN_LASTUPDATE_ALC + " text, " 
		  + COLUMN_LASTUPDATE_LEA + " text, "    
		  + COLUMN_LASTUPDATE_DIE + " text, "
	      + COLUMN_LASTUPDATE_NAT + " text, "
		  + COLUMN_LATITUDE  + " real, " 
	      + COLUMN_LONGITUDE + " real, " 
		  + COLUMN_EXTRA_FLAGS + " integer, " 
	      + COLUMN_PHONE_NUMBER + " text, "
	      + COLUMN_RATING + " real, "
	      + COLUMN_CONFIRMED + " boolean);";

	  public MySQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	    
	    Log.w(MySQLiteHelper.class.getName(), "Database createss succesfully");
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
	                                                + newVersion + ", which will destroy all old data");
	   
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_GASSTATIONS);
	    onCreate(db);
	  }

} 