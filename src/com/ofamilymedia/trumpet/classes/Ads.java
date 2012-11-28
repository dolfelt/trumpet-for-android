package com.ofamilymedia.trumpet.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ofamilymedia.trumpet.controls.TweetList;
import com.twitter.Twit;

import android.os.AsyncTask;
import android.util.Log;

public class Ads {

	public static long adsId = 9223372036854775807l;
	
	private static String ad_url = "http://api.140proof.com/ads/user.json?app_id=trumpet_android&user_id=";
	
	
	public void InsertAd(String username, TweetList list) {
		new InsertAdAsync(list).execute(ad_url+username);
	}
	private class InsertAdAsync extends DownloadURL {
		private TweetList list;
		public InsertAdAsync(TweetList list) {
			this.list = list;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			if(result == null) return;
			try {
				JSONArray ads = result.getJSONArray("ads");
				Twit twit = Twit.createAd(ads.getJSONObject(0));
		        if(twit != null) {
					list.eAdapter.addStatusItem(twit);
					if(list.eAdapter.getCount() > 8) {
						list.setCurrentItem();
					}
					list.eAdapter.refreshList();
					if(list.eAdapter.getCount() > 8) {
						list.setSelection();
					}
		        }
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
	}
	
	
	public void ImpressionMade(String url) {
		new DownloadURL().execute(url);
	}
	
	
	
	
	public class DownloadURL extends AsyncTask<String, Void, JSONObject> {
		protected JSONObject doInBackground(String... urls) {
	    	HttpClient httpclient = new DefaultHttpClient();
	    	HttpGet get = new HttpGet(urls[0]);
	    	 
	    	HttpResponse response;
	    	try {
				response = httpclient.execute(get);
		    	
				Log.i("RETURN", response.getEntity().toString());
		        String result = "";
		        try{
		            InputStream in = response.getEntity().getContent();
		            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		            StringBuilder str = new StringBuilder();
		            String line = null;
		            while((line = reader.readLine()) != null){
		                str.append(line + "\n");
		            }
		            in.close();
		            result = str.toString();
		            
					try {
						JSONObject json = new JSONObject(result);
				        return json;
					} catch (JSONException e) {
						e.printStackTrace();
					}
		        }catch(Exception ex){
		            ex.printStackTrace();
		        }
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}  
			
	    	return null;
		}

	    protected void onPostExecute(JSONObject result) {
	         
	    }
	}
	
}
