package com.sumbioun.android.pitstop.map;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLngBounds;
import com.sumbioun.android.pitstop.MyApplication;
import com.sumbioun.android.pitstop.database.Gasstation;
import com.sumbioun.android.pitstop.database.MySQLiteHelper;
import com.sumbioun.android.pitstop.map.JSONParser.OnDownloadProgress;

/*GoogleDirectionsDecoder                                                                                                         */
/*Gets directions in JSON format from the GoogleDirections API at "http://maps.googleapis.com/maps/api/directions/json" and       */
/*decodes the data to get the distance and duration of the route between two coordinates.                                         */
public class ExternalGasstationDecoder {
	
	//Stores the URL with the query to the GoogleDirections API.
	String url;
	
	//Sets the url to the route between the stated coordinates.
	public ExternalGasstationDecoder(){
		
		url = createURL();
		
	}
	
	//Sets the given coordinates in the format specified by the GoogleDirections API. 
    public String createURL(){
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://sumbioun.com/pitstop/getgasstationtest.php?id=32");
        return urlString.toString();
	 }
	
	//Decodes the result given by the server.
	public Gasstation getGasstation(long id) {

        try {
        	
    	   StringBuilder urlString = new StringBuilder();
           urlString.append("http://sumbioun.com/pitstop/getgasstationtest.php?id="+id);
    
           JSONParser jParser = new JSONParser();
           String result = jParser.getJSONFromUrl(urlString.toString(), 5000);
        	
           //Transforms the string into a JSON object
           final JSONObject json = new JSONObject(result);
           JSONArray gasstationArray = json.getJSONArray("gasstations");
       
           JSONObject gasstationObject = gasstationArray.getJSONObject(0);

           Gasstation _gasstation = new Gasstation();
           
           double price[] = new double[5];
           String lastUpdate[] = new String[5];
           
           _gasstation.setId(gasstationObject.getInt(MySQLiteHelper.COLUMN_ID));
           _gasstation.setFlag(gasstationObject.getInt(MySQLiteHelper.COLUMN_FLAG));
           
           price[0] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_GAS);
           price[1] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_ALC);
           price[2] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_LEA);
           price[3] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_DIE);
           price[4] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_NAT);
           _gasstation.setPriceArray(price);
           
           lastUpdate[0] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_GAS);
           lastUpdate[1] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_ALC);
           lastUpdate[2] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_LEA);
           lastUpdate[3] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_DIE);
           lastUpdate[4] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_NAT);
           _gasstation.setLastUpdateArray(lastUpdate);
           
           _gasstation.setLatitude(gasstationObject.getDouble(MySQLiteHelper.COLUMN_LATITUDE));
           _gasstation.setLongitude(gasstationObject.getDouble(MySQLiteHelper.COLUMN_LONGITUDE));
           _gasstation.setExtraFlags(gasstationObject.getInt(MySQLiteHelper.COLUMN_EXTRA_FLAGS));
           _gasstation.setPhoneNumber(gasstationObject.getString(MySQLiteHelper.COLUMN_PHONE_NUMBER));
           _gasstation.setRating(gasstationObject.getDouble(MySQLiteHelper.COLUMN_RATING));

           if(gasstationObject.getString(MySQLiteHelper.COLUMN_CONFIRMED).contentEquals("CONFIRMED")){
        	   _gasstation.setConfirmed(true);
           }

           return _gasstation;
        } 
        catch (JSONException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return null;
    } 
	
	public List<Gasstation> getGasstationsInsideBounds(LatLngBounds bounds, String date, OnDownloadProgress onDownloadProgress) {

        try {
        	
           String northBoundary = String.valueOf(bounds.northeast.latitude);
	       String eastBoundary  = String.valueOf(bounds.northeast.longitude);
	       String southBoundary = String.valueOf(bounds.southwest.latitude);
	       String westBoundary  = String.valueOf(bounds.southwest.longitude);
           String priceColumn = MyApplication.getDatabaseHelper().getPriceColumn();
           
    	   StringBuilder urlString = new StringBuilder();
           urlString.append("http://sumbioun.com/pitstop/getgasstationsinsidebounds.php?");
           urlString.append("northBoundary="+northBoundary+"&");
           urlString.append("eastBoundary="+eastBoundary+"&");
           urlString.append("southBoundary="+southBoundary+"&");
           urlString.append("westBoundary="+westBoundary+"&");
           if(date != null){urlString.append("date="+URLEncoder.encode(date, "utf-8")+"&");}
           urlString.append("priceColumn="+priceColumn);
           
           JSONParser jParser = new JSONParser();
           if(onDownloadProgress != null){
        	   jParser.setOnDownloadProgress(onDownloadProgress);
           }
           
           
           String result = jParser.getJSONFromUrl(urlString.toString(), 10000);

           //Transforms the string into a JSON object
           final JSONObject json = new JSONObject(result);
           JSONArray gasstationArray = json.getJSONArray("gasstations");
          
           List<Gasstation> gasstationList = new ArrayList<Gasstation>();
           
           for(int i = 0; i < gasstationArray.length(); i++){
        	   
        	   JSONObject gasstationObject = gasstationArray.getJSONObject(i);
	           Gasstation _gasstation = new Gasstation();
	           
	           double price[] = new double[5];
	           String lastUpdate[] = new String[5];
	           
	           _gasstation.setId(gasstationObject.getInt(MySQLiteHelper.COLUMN_ID));
	           _gasstation.setFlag(gasstationObject.getInt(MySQLiteHelper.COLUMN_FLAG));
	           
	           price[0] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_GAS);
	           price[1] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_ALC);
	           price[2] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_LEA);
	           price[3] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_DIE);
	           price[4] = gasstationObject.getDouble(MySQLiteHelper.COLUMN_PRICE_NAT);
	           _gasstation.setPriceArray(price);
	           
	           lastUpdate[0] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_GAS);
	           lastUpdate[1] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_ALC);
	           lastUpdate[2] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_LEA);
	           lastUpdate[3] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_DIE);
	           lastUpdate[4] = gasstationObject.getString(MySQLiteHelper.COLUMN_LASTUPDATE_NAT);
	           _gasstation.setLastUpdateArray(lastUpdate);
	           
	           _gasstation.setLatitude(gasstationObject.getDouble(MySQLiteHelper.COLUMN_LATITUDE));
	           _gasstation.setLongitude(gasstationObject.getDouble(MySQLiteHelper.COLUMN_LONGITUDE));
	           _gasstation.setExtraFlags(gasstationObject.getInt(MySQLiteHelper.COLUMN_EXTRA_FLAGS));
	           _gasstation.setRating(gasstationObject.getDouble(MySQLiteHelper.COLUMN_RATING));
	           if(gasstationObject.getString(MySQLiteHelper.COLUMN_CONFIRMED).contentEquals("CONFIRMED")){
	        	   _gasstation.setConfirmed(true);
	           }
	           
	           gasstationList.add(_gasstation);
           }

           return gasstationList;
        } 
        catch (JSONException e) {
        	e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
			
        return null;
		
    } 
	
	public long sendGasstationToServer(Gasstation gasstation){
		
		StringBuilder urlString = new StringBuilder();
        urlString.append("http://sumbioun.com/pitstop/creategasstationtest.php");
        String url = urlString.toString();
		
        JSONObject json = new JSONObject();
        
        try {
        	
        	json.put(MySQLiteHelper.COLUMN_ID, gasstation.getId());
			json.put(MySQLiteHelper.COLUMN_FLAG, gasstation.getFlag());
			json.put(MySQLiteHelper.COLUMN_PRICE_GAS, gasstation.getPrice(Gasstation.FuelTypes.GASOLINE.value));
			json.put(MySQLiteHelper.COLUMN_PRICE_ALC, gasstation.getPrice(Gasstation.FuelTypes.ALCOHOL.value));
			json.put(MySQLiteHelper.COLUMN_PRICE_LEA, gasstation.getPrice(Gasstation.FuelTypes.LEADED_GASOLINE.value));
			json.put(MySQLiteHelper.COLUMN_PRICE_DIE, gasstation.getPrice(Gasstation.FuelTypes.DIESEL.value));
			json.put(MySQLiteHelper.COLUMN_PRICE_NAT, gasstation.getPrice(Gasstation.FuelTypes.NATURAL_GAS.value));
			json.put(MySQLiteHelper.COLUMN_LASTUPDATE_GAS, gasstation.getLastUpdate(Gasstation.FuelTypes.GASOLINE.value));
			json.put(MySQLiteHelper.COLUMN_LASTUPDATE_ALC, gasstation.getLastUpdate(Gasstation.FuelTypes.ALCOHOL.value));
			json.put(MySQLiteHelper.COLUMN_LASTUPDATE_LEA, gasstation.getLastUpdate(Gasstation.FuelTypes.LEADED_GASOLINE.value));
			json.put(MySQLiteHelper.COLUMN_LASTUPDATE_DIE, gasstation.getLastUpdate(Gasstation.FuelTypes.DIESEL.value));
			json.put(MySQLiteHelper.COLUMN_LASTUPDATE_NAT, gasstation.getLastUpdate(Gasstation.FuelTypes.NATURAL_GAS.value));
			json.put(MySQLiteHelper.COLUMN_LATITUDE, gasstation.getLatitude());
			json.put(MySQLiteHelper.COLUMN_LONGITUDE, gasstation.getLongitude());
			json.put(MySQLiteHelper.COLUMN_EXTRA_FLAGS, gasstation.getExtraFlags());
			json.put(MySQLiteHelper.COLUMN_PHONE_NUMBER, gasstation.getPhoneNumber());
				
			JSONParser jParser = new JSONParser();
	        String result = jParser.sendJsonToUrl(url, json);
	          
	        long id = (long)-1;
	        result.substring(0, result.length());
	        id = Long.parseLong(result);

	        return id;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch(NumberFormatException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
        return -1;
        
	}
	
	public boolean deleteGasstation(long id){
		
		try {
			StringBuilder urlString = new StringBuilder();
			urlString.append("http://sumbioun.com/pitstop/deletegasstationtest.php?id="+id);
			
	        JSONParser jParser = new JSONParser();
	        String result = jParser.getJSONFromUrl(urlString.toString(), 10000);
	     	
	        if(Integer.parseInt(result) != 1){
	        	return false;
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
