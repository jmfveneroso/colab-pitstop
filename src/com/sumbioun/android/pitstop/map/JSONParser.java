package com.sumbioun.android.pitstop.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

/*JSONParser                                                                 */
/*Sends HTTP request to the specified URL and parses result to JSON string.  */
public class JSONParser {
	
	public interface OnDownloadProgress{
		void onProgress(float progress);
	}
	
	private static final int TIMEOUT_MILLISEC = 5000;
	//private static final int SO_TIMEOUT_MILLISEC = 50000;
	
	static InputStream mIs = null;
    static JSONObject mJObj = null;
    static String mJson = "";
    
    OnDownloadProgress onDownloadProgress = null;
    
    public void setOnDownloadProgress(OnDownloadProgress onDownloadProgress){
    	this.onDownloadProgress = onDownloadProgress;
    }
    
    public JSONParser() {
    }
    
    public String getJSONFromUrl(String url, int timeout) {
    	
        //Making HTTP request
        try {
        	
            //DefaultHttpClient
        	HttpParams httpParams = new BasicHttpParams();
        	int connectTimeout = 10000;
        	if(((float)timeout/2) < 5000){
        		connectTimeout = (int)((float)timeout/2);
        	}
            HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, timeout);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            
            URI uri = new URI(url);
            HttpPost httpPost = new HttpPost(uri);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            mIs = httpEntity.getContent();
            long length = 99999999;
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(mIs, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            
            long totalread = 0;
            while ((line = reader.readLine()) != null) {
            	if(line.contains("lengthjm136452")){
            		String str = line.replaceAll("lengthjm136452", "");
            		length = Long.parseLong(str);

            	} else {
	                sb.append(line + "\n");
	                int count = line.length();
	                totalread += count;
	               
	                if(onDownloadProgress != null){
	                	onDownloadProgress.onProgress((float)totalread/length);
	                }
            	}
            }

            if(onDownloadProgress != null){
            	onDownloadProgress.onProgress(1.0f);
            }
            
            mJson = sb.toString();
            mIs.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
            e.printStackTrace();
        }
        
        return mJson;

    }
    
    public String sendJsonToUrl(String url, JSONObject json){
    	try {
	            HttpParams httpParams = new BasicHttpParams();
	            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
	            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
	            HttpClient client = new DefaultHttpClient(httpParams);

	            HttpPost request = new HttpPost(url);
	            
	            request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
	            //request.setHeader("json", json.toString());
	            request.setHeader("json", "Colab.Pitstop");
	            
	            HttpResponse response = client.execute(request);
	            HttpEntity entity = response.getEntity();
	            
	            //If the response does not enclose an entity, there is no need to read it.
	            if (entity != null) {
	                InputStream instream = entity.getContent();
	
	                String result = JSONParser.convertStreamToString(instream);
	                return result;
	
	            }
	            
	        } catch (Throwable t) {
	            
	         
	        }
    	
    	return null;
    }
    
    private static String convertStreamToString(final InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
                while (( line = reader.readLine() ) != null) {
                        sb.append(line); // + "\n"
                }
        } catch (IOException e) {
                e.printStackTrace();
        } finally {
                try {
                        is.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        return sb.toString();
}
}
