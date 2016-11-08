package com.sumbioun.android.pitstop.garage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*MySQLiteHelper                                                */
/*SQLiteOpenHelper class for the internal gas station database  */
public class GarageDatabaseCars extends SQLiteOpenHelper {

	  public static final String TABLE_CARS = "cars";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_NAME = "name";
	  
	  private static final String DATABASE_NAME = "cars.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_CARS + "(" + COLUMN_ID + " integer primary key autoincrement, " 
		  + COLUMN_NAME + " text);";

	  public GarageDatabaseCars(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(GarageDatabaseCars.class.getName(), "Upgrading database from version " + oldVersion + " to "
	                                                + newVersion + ", which will destroy all old data");
	   
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
	    onCreate(db);
	  }

} 