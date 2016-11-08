package deprecated;

public class ContentProviderStuff {

	/*public class GasstationCursorAdapter extends CursorAdapter {
	
		public GasstationCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}
	 
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
	
			GasstationHolder holder = null;
	
			holder = new GasstationHolder();
	        holder.flag = (ImageView) view.findViewById(R.id.refuel_table_listitem_gasstation_flag);
	        holder.flag.setImageResource(Flag.values()[cursor.getInt((cursor.getColumnIndex(MySQLiteHelper.COLUMN_FLAG)))].value);
	        
	        holder.price = (CustTextView) view.findViewById(R.id.refuel_table_listitem_gasstation_price);
	        holder.price.setText(getPriceInFormat(cursor.getDouble(cursor.getColumnIndex(datasource.getPriceColumn()))));
	        
	        holder.distance = (CustTextView) view.findViewById(R.id.refuel_table_listitem_gasstation_distance);
	        holder.time = (CustTextView) view.findViewById(R.id.refuel_table_listitem_gasstation_time);
	        
	        new asyncTimeDistance(holder, cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_LONGITUDE))).execute();
	        
	        holder.lastUpdate = (CustTextView) view.findViewById(R.id.refuel_table_listitem_gasstation_last_update);
	        holder.lastUpdate.setText(cursor.getString(cursor.getColumnIndex(datasource.getLastUpdateColumn())));
		}
	 
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.refuel_table_list_item_gasstation, parent, false);
			bindView(v, context, cursor);
			return v;
		}
		
		class GasstationHolder
	    {
	    	ImageView flag;
	        CustTextView price;
	        CustTextView distance;
	        CustTextView time;
	        CustTextView lastUpdate;
	    }
	}*/
	
	/*private void fillData() {
	
	// Fields from the database (projection)
	// Must include the _id column for the adapter to work
	String[] from = new String[] { MySQLiteHelper.COLUMN_LONGITUDE };
	// Fields on the UI to which we map
	int[] to = new int[] { android.R.id.text1 };
	
	getSupportLoaderManager().initLoader(0, null, this);
	
	adapter = new GasstationCursorAdapter(this, null, 0);
	//adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);
	
	mGasstationList.setAdapter(adapter);
	}
	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
	    CursorLoader cursorLoader = new CursorLoader(this,
	    MyContentProvider.CONTENT_URI, GasstationsDataSource.allColumns, null, null, null);
	    return cursorLoader;
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		//adapter.swapCursor(data);
	
	}
	
	public void onLoaderReset(Loader<Cursor> arg0) {
		//adapter.swapCursor(null);
	}*/
	
}
