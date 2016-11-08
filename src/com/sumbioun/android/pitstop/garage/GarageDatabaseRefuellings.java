package com.sumbioun.android.pitstop.garage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*MySQLiteHelper                                                */
/*SQLiteOpenHelper class for the internal gas station database  */
public class GarageDatabaseRefuellings extends SQLiteOpenHelper {

	  public static final String TABLE_REFUELLINGS = "refuellings";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_CARID= "carid";
	  public static final String COLUMN_DISTANCE= "distance";
	  
	  private static final String DATABASE_NAME = "refuellings.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_REFUELLINGS + "(" + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_CARID + " integer, " 
		  + COLUMN_DISTANCE + " real);";

	  public GarageDatabaseRefuellings(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(GarageDatabaseRefuellings.class.getName(), "Upgrading database from version " + oldVersion + " to "
	                                                + newVersion + ", which will destroy all old data");
	   
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_REFUELLINGS);
	    onCreate(db);
	  }

} 