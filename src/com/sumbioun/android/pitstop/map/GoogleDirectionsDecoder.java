package com.sumbioun.android.pitstop.map;

import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.database.Gasstation;

/*GoogleDirectionsDecoder                                                                                                         */
/*Gets directions in JSON format from the GoogleDirections API at "http://maps.googleapis.com/maps/api/directions/json" and       */
/*decodes the data to get the distance and duration of the route between two coordinates.                                         */
public class GoogleDirectionsDecoder {
	
	//Stores the URL with the query to the GoogleDirections API.
	String url;
	
	//Routes distance and duration.
	int mDistance = 0; 
	int mDuration = 0;
	
	public int getDistance(){ return mDistance; }
	public int getDuration(){ return mDuration; }
	
	public GoogleDirectionsDecoder(){
		
	}
	
	//Sets the url to the route between the stated coordinates.
	public GoogleDirectionsDecoder(double sourceLat, double sourceLng, double destLat, double destLng ){
		
		url = createURL(sourceLat, sourceLng, destLat, destLng);
		
	}
	
	//Sets the given coordinates in the format specified by the GoogleDirections API. 
    public String createURL(double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json");
        //from
        urlString.append("?origins=");
        urlString.append(Double.toString(sourcelat)); //Origin Latitude
        urlString.append(",");
        urlString.append(Double.toString(sourcelog)); //Origin Longitude
        //to
        urlString.append("&destinations=");
        urlString.append(Double.toString(destlat)); //Destination Latitude
        urlString.append(",");
        urlString.append(Double.toString(destlog)); //Destination Longitude
        //extras
        urlString.append("&sensor=false&mode=driving");//&alternatives=true
        
       // http://maps.googleapis.com/maps/api/distancematrix/json?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Victoria+BC&mode=bicycling&language=fr-FR&sensor=false

        return urlString.toString();
	 }
	
	//Downloads the information from the GoogleDirections API and decodes the result.
	public void execute() throws JSONException{
		
		JSONParser jParser = new JSONParser();
        String json = jParser.getJSONFromUrl(url, 5000);
        try {
			decode(json);
		} catch (JSONException e) {
			throw e;
		}
        
	}
	
	//Decodes the result given by the server.
	public List<Integer> decode(String result) throws JSONException {

        try {
        	
           //Transforms the string into a JSON object
          /* final JSONObject json = new JSONObject(result);
           JSONArray routeArray = json.getJSONArray("routes");
           JSONObject routes = routeArray.getJSONObject(0);
           JSONObject leg = routes.getJSONArray("legs").getJSONObject(0);*/
        
    	  final JSONObject json = new JSONObject(result);
           JSONArray rowsArray = json.getJSONArray("rows");
           JSONObject row = rowsArray.getJSONObject(0);
           JSONArray elementsArray = row.getJSONArray("elements");
           JSONObject element = elementsArray.getJSONObject(0);
           
           mDistance =  element.getJSONObject("distance").getInt("value");
           mDuration =  element.getJSONObject("duration").getInt("value");

        } 
        catch (JSONException e) {
        	throw e;
        }
        
        return null;
    } 
	
	//Sets the given coordinates in the format specified by the GoogleDirections API. 
    public String createArrayURL(List<Gasstation> list){
    	
    	try {
    		
	        StringBuilder urlString = new StringBuilder();
	        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json");
	        //from
	        urlString.append("?origins=");
	        
	        double latitude = MyApplication.getGps().getLatitude();
	        double longitude = MyApplication.getGps().getLongitude();
	        
	        //for(int i = 0; i < list.size(); i++){
		        urlString.append(Double.toString(latitude)); //Origin Latitude
		        urlString.append(",");
		        urlString.append(Double.toString(longitude)); //Origin Longitude
		       // if(i !=  list.size()-1){ 
		        	//urlString.append("|");
		        //	urlString.append(URLEncoder.encode("|", "UTF-8"));
		       // }
	       // }
	        //to
	        urlString.append("&destinations=");
	        
	        for(int i = 0; i < list.size(); i++){
	        	urlString.append(Double.toString(list.get(i).getLatitude())); //Destination Latitude
	            urlString.append(",");
	            urlString.append(Double.toString(list.get(i).getLongitude())); //Destination Longitude
	            if(i !=  list.size()-1){ 
	            	//urlString.append("|");
	            	urlString.append(URLEncoder.encode("|", "UTF-8"));
	                    
	                
	            }
	    	}
	        
	        //extras
	        urlString.append("&sensor=false&mode=driving");//&alternatives=true
      
	        return urlString.toString();
	        
    	} catch (Exception e){
    		e.printStackTrace();
    	} 
    	//catch (UnsupportedEncodingException e) {
        //    e.printStackTrace();
        //}
    	
    	return null;
        
	 }
     
    public List<Gasstation> executeArray(List<Gasstation> list){
    	String _url = createArrayURL(list);
    	
    	try {
    		
	    	JSONParser jParser = new JSONParser();
	        String result = jParser.getJSONFromUrl(_url, 1000);
	        
        	final JSONObject json = new JSONObject(result);
            JSONArray rowsArray = json.getJSONArray("rows");
            JSONObject row = rowsArray.getJSONObject(0);
            JSONArray elementsArray = row.getJSONArray("elements");

            for(int i = 0; i < elementsArray.length(); i++){
            	
                JSONObject element = elementsArray.getJSONObject(i);

                float dist = (float)Math.round(element.getJSONObject("distance").getInt("value")/100)/10;
                
                list.get(i).setDistance( dist );
                list.get(i).setTime( element.getJSONObject("duration").getInt("value") );
                       
            }
            
            return list;

		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return null;
    	
    }
    
    public String createRouteURL(List<Gasstation> list, LatLng origin, LatLng destination){
    	
    	try {
    		
	        StringBuilder urlString = new StringBuilder();
	        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json");
	        //from
	        urlString.append("?origins=");
	        
	        urlString.append(Double.toString(origin.latitude)); //Origin Latitude
	        urlString.append(",");
	        urlString.append(Double.toString(origin.longitude)); //Origin Longitude
	        urlString.append(URLEncoder.encode("|", "UTF-8"));
	        urlString.append(Double.toString(destination.latitude)); //Destination Latitude
	        urlString.append(",");
	        urlString.append(Double.toString(destination.longitude)); //Destination Longitude

	        urlString.append("&destinations=");
	        
	        for(int i = 0; i < list.size(); i++){
	        	urlString.append(Double.toString(list.get(i).getLatitude())); //Destination Latitude
	            urlString.append(",");
	            urlString.append(Double.toString(list.get(i).getLongitude())); //Destination Longitude
	            if(i !=  list.size()-1){ 
	            	urlString.append(URLEncoder.encode("|", "UTF-8"));
	                    
	                
	            }
	    	}
	        
	        //extras
	        urlString.append("&sensor=false&mode=driving");//&alternatives=true
      
	        return urlString.toString();
	        
    	} catch (Exception e){
    		e.printStackTrace();
    	} 
    	
    	return null;
        
	 }
    
    public List<Gasstation> executeArray(List<Gasstation> list, LatLng origin, LatLng destination){
    	String _url = createRouteURL(list, origin, destination);
    	
    	try {
    		
	    	JSONParser jParser = new JSONParser();
	        String result = jParser.getJSONFromUrl(_url, 10000);
	        
        	final JSONObject json = new JSONObject(result);
            JSONArray rowsArray = json.getJSONArray("rows");
            JSONObject row = rowsArray.getJSONObject(0);
            JSONArray elementsArray = row.getJSONArray("elements");
            for(int i = 0; i < elementsArray.length(); i++){
            	
                JSONObject element = elementsArray.getJSONObject(i);

                float dist = element.getJSONObject("distance").getInt("value");
                
                list.get(i).setDistance(dist);
                list.get(i).setTime( element.getJSONObject("duration").getInt("value") );
                
            }
            
            row = rowsArray.getJSONObject(1);
            elementsArray = row.getJSONArray("elements");
            for(int i = 0; i < elementsArray.length(); i++){
            	
                JSONObject element = elementsArray.getJSONObject(i);

                float dist = element.getJSONObject("distance").getInt("value");
                
                list.get(i).setDistance(list.get(i).getDistance() + dist);
                list.get(i).setTime( list.get(i).getTime() + element.getJSONObject("duration").getInt("value") );
                
            }
            
            return list;

		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return null;
    	
    }

}
