package com.twitter;

import org.json.JSONException;
import org.json.JSONObject;

public class TwitAd implements java.io.Serializable {
	
	private static final long serialVersionUID = -5823651069469842207L;
	
	//private JSONObject data;
    private long id;
    
    private String impression_url;
    private String click_url;
    private String favorite_url;
    private String friendship_url;
    private String reply_url;
    private String retweet_url;

    private String byline;

    public static TwitAd create(JSONObject ad) {
    	if(ad == null) {
    		return null;
    	}
    	
    	TwitAd twit = new TwitAd();
    	try {
			twit.id = ad.getJSONObject("status").getLong("id");
		   	
			//twit.data = ad;
	    	
	    	JSONObject urls = ad.getJSONObject("action_urls");
	    	twit.impression_url = urls.getString("impression_url");
	    	twit.click_url = urls.getString("click_url");
	    	twit.favorite_url = urls.getString("favorite_url");
	    	twit.friendship_url = urls.getString("friendship_url");
	    	twit.reply_url = urls.getString("reply_url");
	    	twit.retweet_url = urls.getString("retweet_url");
	    	
	    	twit.byline = ad.getString("byline");
	 	
    	} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
    	
    	
    	return twit;
    }


    public long getId() {
        return this.id;
    }
    public void setId(long val) {
    	this.id = val;
    }


    /**
     * {@inheritDoc}
     */
    public String getImpressionURL() {
        return this.impression_url;
    }
    public String getClickURL() {
        return this.click_url;
    }
    public String getFavoriteURL() {
        return this.favorite_url;
    }
    public String getFriendshipURL() {
        return this.friendship_url;
    }
    public String getReplyURL() {
        return this.reply_url;
    }
    public String getRetweetURL() {
        return this.retweet_url;
    }





    @Override
    public String toString() {
        return "TwitAd{" +
                "id=" + id +
                //", data='" + data + '\'' +
                ", impression_url='" + impression_url + '\'' +
                ", click_url=" + click_url +
                ", favorite_url=" + favorite_url +
                ", friendship_url=" + friendship_url +
                ", reply_url=" + reply_url +
                ", retweet_url='" + retweet_url + '\'' +
                ", byline=" + byline +
                '}';
    }
	
}
