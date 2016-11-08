package deprecated;

import android.content.*;
import android.database.*;
import android.net.*;
import android.database.sqlite.*;
import android.text.*;

import java.util.*;

import com.sumbioun.android.pitstop.database.MySQLiteHelper;

public class MyContentProvider extends ContentProvider
{

  // database
  private MySQLiteHelper database;

  // Used for the UriMacher
  private static final int GAS_ALL = 10;
  private static final int GAS_ID = 20;

  private static final String AUTHORITY = "com.sumbioun.android.pitstop.database.mycontentprovider";

  private static final String BASE_PATH = "gasstations";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
      + "/" + BASE_PATH);

  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/gass";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/gas";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, GAS_ALL);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", GAS_ID);
  }

  @Override
  public boolean onCreate() {
    database = new MySQLiteHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
    String[] selectionArgs, String sortOrder) {

    // Uisng SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // Check if the caller has requested a column which does not exists
    checkColumns(projection);

    // Set the table
    queryBuilder.setTables(MySQLiteHelper.TABLE_GASSTATIONS);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case GAS_ALL:
      break;
    case GAS_ID:
      // Adding the ID to the original query
			queryBuilder.appendWhere(MySQLiteHelper.COLUMN_ID + "="
          + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    // Make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();

    long id = 0;
    switch (uriType) {
    case GAS_ALL:
			id = sqlDB.insert(MySQLiteHelper.TABLE_GASSTATIONS, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    switch (uriType) {
    case GAS_ALL:
      rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_GASSTATIONS, selection,
          selectionArgs);
      break;
    case GAS_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
		  rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_GASSTATIONS,
            MySQLiteHelper.COLUMN_ID + "=" + id, 
            null);
      } else {
		  rowsDeleted = sqlDB.delete(MySQLiteHelper.TABLE_GASSTATIONS,
            MySQLiteHelper.COLUMN_ID + "=" + id 
            + " and " + selection,
            selectionArgs);

      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsUpdated = 0;
    switch (uriType) {
    case GAS_ALL:
			rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_GASSTATIONS, 
          values, 
          selection,
          selectionArgs);
      break;
    case GAS_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
		  rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_GASSTATIONS, 
            values,
            MySQLiteHelper.COLUMN_ID + "=" + id, 
            null);
      } else {
		  rowsUpdated = sqlDB.update(MySQLiteHelper.TABLE_GASSTATIONS, 
            values,
			MySQLiteHelper.COLUMN_ID + "=" + id 
            + " and " 
            + selection,
            selectionArgs);
      }
      break;
    default:
    	
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  private void checkColumns(String[] projection) {
    String[] available = { MySQLiteHelper.COLUMN_ID,
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
		MySQLiteHelper.COLUMN_PHONE_NUMBER};
    
    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
      // Check if all columns which are requested are available
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException("Unknown columns in projection");
      }
    }
  }

}
