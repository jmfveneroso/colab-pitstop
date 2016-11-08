package com.sumbioun.android.pitstop.map;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/*JSONPictureDecoder                                                                                                         */
/*Gets directions in JSON format from the GoogleDirections API at "http://maps.googleapis.com/maps/api/directions/json" and       */
/*decodes the data to get the distance and duration of the route between two coordinates.                                         */
public class JSONPictureDecoder {
	
	//private static final String COLUMN_ID = "_id"; 
	private static final String COLUMN_GASSTATION_ID = "gasstation_id"; 
	private static final String COLUMN_PICTURE = "picture"; 
	private static final String COLUMN_THUMBNAIL = "thumbnail"; 
	private static final String COLUMN_DATE = "date"; 
	
	private static final int JPEG_QUALITY = 30; 
	
	//Stores the URL with the query to the GoogleDirections API.
	String url;
	
	//Sets the url to the route between the stated coordinates.
	public JSONPictureDecoder(){
		
		url = createURL();
		
	}
	
	//Sets the given coordinates in the format specified by the GoogleDirections API. 
    public String createURL(){
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://sumbioun.com/pitstop/addpicturetest.php");
        return urlString.toString();
	 }
	
	//Decodes the result given by the server.
	public Bitmap[] getPicture(long gasstationId) {

        try {
        	
    	   StringBuilder urlString = new StringBuilder();
           urlString.append("http://sumbioun.com/pitstop/getpicturestest.php?gasstation_id="+gasstationId);
    
           JSONParser jParser = new JSONParser();
           String result = jParser.getJSONFromUrl(urlString.toString(), 20000);
        	
           //Transforms the string into a JSON object
           final JSONObject json = new JSONObject(result);
           
           JSONArray pictureArray = json.optJSONArray("pictures");
           
           if(pictureArray == null){
        	   return null;
           }
           
           Bitmap _bitmapList[] = new Bitmap[pictureArray.length()]; 
           
           for(int i = 0; i < pictureArray.length(); i++){
        	   
	           JSONObject pictureObject = pictureArray.getJSONObject(i);
	           
	           String encodedImage = pictureObject.getString(COLUMN_PICTURE);
	           byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
	           _bitmapList[i] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	           
           }

           return _bitmapList;
           
        } 
        catch (JSONException e) {
        	e.printStackTrace();
        }
        
        return null;
    } 
	
	public Bitmap getBigPicture(long id) {

        try {
        	
    	   StringBuilder urlString = new StringBuilder();
           urlString.append("http://sumbioun.com/pitstop/getpicturetest.php?id="+id);
    
           JSONParser jParser = new JSONParser();
           String result = jParser.getJSONFromUrl(urlString.toString(), 20000);
        	
           //Transforms the string into a JSON object
           final JSONObject json = new JSONObject(result);
           
           JSONArray pictureArray = json.optJSONArray("pictures");
           
           if(pictureArray == null){
        	   return null;
           }
           
           Bitmap _bitmapList[] = new Bitmap[pictureArray.length()]; 
           
           for(int i = 0; i < pictureArray.length(); i++){
        	   
	           JSONObject pictureObject = pictureArray.getJSONObject(i);
	           
	           String encodedImage = pictureObject.getString(COLUMN_PICTURE);
	           byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
	           _bitmapList[i] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	           
           }

           return _bitmapList[0];
           
        } 
        catch (JSONException e) {
        	e.printStackTrace();
        }
        
        return null;
    } 
	
	public long sendPictureToServer(long gasstationId, Bitmap bitmap, Bitmap thumbnail){
		
		StringBuilder urlString = new StringBuilder();
        urlString.append("http://sumbioun.com/pitstop/addpicturetest.php");
		
        JSONObject json = new JSONObject();
        
        try {
        	
        	Calendar c = Calendar.getInstance();
        	
        	//json.put(COLUMN_ID, id);
			json.put(COLUMN_GASSTATION_ID, gasstationId);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream);
	
			String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
	
			json.put(COLUMN_PICTURE, encodedImage);
			
			//if(thumbnail != null){
			stream = new ByteArrayOutputStream();
			thumbnail.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream);
	
			encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
	
			json.put(COLUMN_THUMBNAIL, encodedImage);
			//}
			
			//In SQL server - YYYY-MM-DD HH:MM:SS.
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			json.put(COLUMN_DATE, sdf.format(c.getTime()));
				
			JSONParser jParser = new JSONParser();
	        String result = jParser.sendJsonToUrl(urlString.toString(), json);
	          
	        long id = (long)-1;
	        result.substring(0, result.length());
	        id = Long.parseLong(result);
	        
	        return id;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
        return -1;
        
	}

}
