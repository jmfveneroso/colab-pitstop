package com.sumbioun.android.pitstop.map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*JSONPictureDecoder                                                                                                         */
/*Gets directions in JSON format from the GoogleDirections API at "http://maps.googleapis.com/maps/api/directions/json" and       */
/*decodes the data to get the distance and duration of the route between two coordinates.                                         */
public class JSONCommentDecoder {
	
	//private static final String COLUMN_ID = "_id"; 
	private static final String COLUMN_GASSTATION_ID = "gasstation_id"; 
	private static final String COLUMN_RATING = "rating"; 
	private static final String COLUMN_COMMENT = "comment"; 
	private static final String COLUMN_USERNAME = "username"; 
	//private static final String COLUMN_USER_IP= "user_ip"; 
	private static final String COLUMN_DATE = "date"; 
	
	//Sets the url to the route between the stated coordinates.
	public JSONCommentDecoder(){}
	
	public class Comment{
		public String comment;
		public String username;
		public double rating;
	}
	
	//Decodes the result given by the server.
	public Comment[] getComments(long gasstationId) {

        try {
        	
    	   StringBuilder urlString = new StringBuilder();
           urlString.append("http://sumbioun.com/pitstop/getcommentstest.php?gasstation_id="+gasstationId);
    
           JSONParser jParser = new JSONParser();
           String result = jParser.getJSONFromUrl(urlString.toString(), 5000);
        	
           //Transforms the string into a JSON object
           final JSONObject json = new JSONObject(result);
           
           JSONArray commentArray = json.optJSONArray("comments");
           
           if(commentArray == null){
        	   return null;
           }
           
           Comment _commentList[] = new Comment[commentArray.length()]; 
           
           for(int i = 0; i < commentArray.length(); i++){
        	   
        	   _commentList[i] = new Comment();
	           JSONObject commentObject = commentArray.getJSONObject(i);
	           _commentList[i].comment = commentObject.getString(COLUMN_COMMENT);
	           _commentList[i].username = commentObject.getString(COLUMN_USERNAME);
	           _commentList[i].rating = commentObject.getDouble(COLUMN_RATING);
	           
           }

           return _commentList;
           
        } 
        catch (JSONException e) {
        	e.printStackTrace();
        }
        
        return null;
    } 
	
	public long sendCommentToServer(long gasstationId, String username, String comment, double rating){
		
		StringBuilder urlString = new StringBuilder();
        urlString.append("http://sumbioun.com/pitstop/addcommenttest.php");
		
        JSONObject json = new JSONObject();
        
        try {
        	
			json.put(COLUMN_GASSTATION_ID, gasstationId);
			json.put(COLUMN_RATING, rating);
			json.put(COLUMN_USERNAME, username);
	        json.put(COLUMN_COMMENT, comment);
	        
			//In SQL server dateTime object pattern is -> "YYYY-MM-DD HH:MM:SS".
			Calendar c = Calendar.getInstance();
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
		} catch(NullPointerException e){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
        return -1;
        
	}

}
