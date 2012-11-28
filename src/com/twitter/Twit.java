package com.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ofamilymedia.trumpet.classes.Ads;
import com.ofamilymedia.trumpet.classes.ObjectSerializer;

import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.User;

public class Twit implements Comparable<Twit>, java.io.Serializable {
	
	private static final long serialVersionUID = -5823651069469842207L;
	
	private Date createdAt;
    private long id;
    private String text;
    private String source;
    private boolean isTruncated;
    private long inReplyToStatusId;
    private long inReplyToUserId;
    private boolean isFavorited;
    private String inReplyToScreenName;
    private GeoLocation geoLocation = null;
    private Place place = null;
    private long retweetCount;
    private boolean wasRetweetedByMe;
    
    private TwitUser user = null;

    private long[] contributors;
    //private Annotations annotations = null;

    private Twit retweetedStatus;
    private User[] userMentions;
    private URL[] urls;
    private String[] hashtags;

    private TwitAd ad;
    
    public static Twit create(Status status) {
    	if(status == null) {
    		return null;
    	}
    	
    	Twit twit = new Twit();
    	twit.createdAt = status.getCreatedAt();
    	twit.id = status.getId();
    	twit.text = status.getText();
    	twit.source = status.getSource();
    	twit.isTruncated = status.isTruncated();
    	twit.inReplyToStatusId = status.getInReplyToStatusId();
    	twit.inReplyToUserId = status.getInReplyToUserId();
    	twit.isFavorited = status.isFavorited();
    	twit.inReplyToScreenName = status.getInReplyToScreenName();
    	twit.geoLocation = status.getGeoLocation();
    	twit.place = status.getPlace();
    	twit.retweetCount = status.getRetweetCount();
    	twit.wasRetweetedByMe = status.isRetweetedByMe();
    	twit.contributors = status.getContributors();
    	twit.retweetedStatus = Twit.create(status.getRetweetedStatus());
    	//twit.userMentions = status.getUserMentions();
    	//twit.urls = status.getURLs();
    	//twit.hashtags = status.getHashtags();
    	
    	if(status.getUser() != null) {
    		twit.user = TwitUser.create(status.getUser());
    	}
    	
    	return twit;
    }
    
    public static Twit create(Tweet status) {
    	
    	Twit twit = new Twit();
    	twit.createdAt = status.getCreatedAt();
    	twit.id = status.getId();
    	twit.text = status.getText();
    	twit.source = status.getSource();
    	twit.geoLocation = status.getGeoLocation();
    	//twit.userMentions = status.getUserMentions();
    	//twit.urls = status.getURLs();
    	//twit.hashtags = status.getHashtags();
    	
    	twit.user = TwitUser.create(status);
    	
    	return twit;
    }
    
    public static Twit createAd(JSONObject json) {
    	try {
        	Twit twit = new Twit();
        	twit.createdAt = new Date();
        	twit.id = Ads.adsId;

        	twit.text = json.getString("text");
	    	twit.source = json.getString("byline");
	    	//twit.userMentions = status.getUserMentions();
	    	//twit.urls = status.getURLs();
	    	//twit.hashtags = status.getHashtags();
	    	twit.ad = TwitAd.create(json);
	    	twit.user = new TwitUser();
	    	
	    	JSONObject usr = json.getJSONObject("user");
	    	twit.user.setName(usr.getString("name"));
	    	twit.user.setScreenName(usr.getString("screen_name"));
	    	twit.user.setProfileImageURL(usr.getString("profile_image_url"));
	    	
	    	return twit;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
    	
    	
    }
    
    public int compareTo(Twit that) {
        long delta = this.id - that.getId();
        if (delta < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else if (delta > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) delta;
    }
    
    public Date getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(Date val) {
    	this.createdAt = val;
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return this.id;
    }
    public void setId(long val) {
    	this.id = val;
    }

    public String getText() {
        return this.text;
    }
    public void setText(String val) {
    	this.text = val;
    }

    /**
     * {@inheritDoc}
     */
    public String getSource() {
        return this.source;
    }
    public void setSource(String val) {
    	this.source = val;
    }

    /**
     * {@inheritDoc}
     */
    public TwitAd getAd() {
        return this.ad;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTruncated() {
        return isTruncated;
    }
    public void isTruncated(boolean val) {
    	this.isTruncated = val;
    }


    /**
     * {@inheritDoc}
     */
    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }
    public void setInReplyToStatusId(long val) {
    	this.inReplyToStatusId = val;
    }

    
    public long getInReplyToUserId() {
        return inReplyToUserId;
    }
    public void setInReplyToUserId(int val) {
    	this.inReplyToUserId = val;
    }


    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    
    public GeoLocation getGeoLocation(){
        return geoLocation;
    }

    
    public Place getPlace(){
        return place;
    }

    
    public long[] getContributors() {
        return contributors;
    }

	/**
     * {@inheritDoc}
     */
    public boolean isFavorited() {
        return isFavorited;
    }



    /**
     * {@inheritDoc}
     */
    public TwitUser getUser() {
        return user;
    }
    
    public void setUser(TwitUser val) {
    	user = val;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRetweet(){
        return null != retweetedStatus;
    }

    /**
     * {@inheritDoc}
     */
    public Twit getRetweetedStatus() {
        return retweetedStatus;
    }

    public void setRetweetedStatus(Twit val) {
        retweetedStatus = val;
    }

    /**
     * {@inheritDoc}
     */
    public long getRetweetCount() {
        return retweetCount;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRetweetedByMe() {
        return wasRetweetedByMe;
    }

    /**
     * {@inheritDoc}
     */
    public User[] getUserMentions() {
        return userMentions;
    }

    /**
     * {@inheritDoc}
     */
    public URL[] getURLs() {
        return urls;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getHashtags() {
        return hashtags;
    }
    
    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof Twit && ((Twit) obj).getId() == this.id;
    }

    @Override
    public String toString() {
        return "Twit{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", ad=" + ad +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", isTruncated=" + isTruncated +
                ", inReplyToStatusId=" + inReplyToStatusId +
                ", inReplyToUserId=" + inReplyToUserId +
                ", isFavorited=" + isFavorited +
                ", inReplyToScreenName='" + inReplyToScreenName + '\'' +
                ", geoLocation=" + geoLocation +
                ", place=" + place +
                ", contributors=" + (contributors == null ? null : Arrays.asList(contributors)) +
                ", retweetedStatus=" + retweetedStatus +
                ", user=" + user +
                '}';
    }
    
    
    public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("createdAt", createdAt.getTime());
			json.put("id", id);
			json.put("text", text);
			json.put("source", source);
			json.put("isTruncated", isTruncated);
			json.put("inReplyToStatusId", inReplyToStatusId);
			json.put("inReplyToUserId", inReplyToUserId);
			json.put("isFavorited", isFavorited);
			json.put("inReplyToScreenName", inReplyToScreenName);
			json.put("geoLocation", (geoLocation == null) ? null : ObjectSerializer.serialize(geoLocation));
			json.put("place", (place == null) ? null : ObjectSerializer.serialize(place));
			json.put("contributors", (contributors == null) ? null : ObjectSerializer.serialize(contributors));
			json.put("retweetedStatus", (retweetedStatus==null) ? null : retweetedStatus.toJSON());
			json.put("user", (user==null) ? null : user.toJSON());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return json;
    }
	
	public void fromJSON(JSONObject json) throws TwitException {

		try {
			createdAt = new Date(json.getLong("createdAt"));
			id = json.getLong("id");
			text = json.getString("text");
			source = json.getString("source");
			isTruncated = json.getBoolean("isTruncated");
			inReplyToStatusId = json.getLong("inReplyToStatusId");
			inReplyToUserId = json.getInt("inReplyToUserId");
			isFavorited = json.getBoolean("isFavorited");
			
			if(json.isNull("inReplyToScreenName")==false)
				inReplyToScreenName = json.getString("inReplyToScreenName");
			if(json.isNull("geoLocation")==false)
				geoLocation = (GeoLocation)ObjectSerializer.deserialize(json.getString("geoLocation"));
			if(json.isNull("place")==false)
				place = (Place)ObjectSerializer.deserialize(json.getString("place"));
			if(json.isNull("contributors")==false)
				contributors = (long[])ObjectSerializer.deserialize(json.getString("contributors"));
			if(json.isNull("retweetedStatus")==false) {
				retweetedStatus = new Twit();
				retweetedStatus.fromJSON(json.getJSONObject("retweetedStatus"));
			}
			
			user = new TwitUser();
			user.fromJSON(json.getJSONObject("user"));
		} catch (JSONException e) {
			throw new TwitException(e);
		} catch (IOException e) {
			throw new TwitException(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new TwitException(e);
		} catch (ClassNotFoundException e) {
			throw new TwitException(e);
		}
	}
	
	
	/** SERIALIZATION CLASSES **/
	
	public static void toJSONFile(File file, List<Twit> data) {
        try {
			ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(file));
			JSONArray arr = new JSONArray();
			for(Twit t : data) {
				if(t.getAd() == null) {
					arr.put(t.toJSON());
				}
			}
			o.writeObject(arr.toString());
			o.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static List<Twit> fromJSONFile(File file) {
        final ArrayList<Twit> data = new ArrayList<Twit>();
        if(!file.exists()) {
        	return data;
        }
        try {
        	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			JSONArray arr = new JSONArray((String)in.readObject());
        	for(int i=0; i<arr.length(); i++) {
			    Twit twit = new Twit();
			    try {
					twit.fromJSON(arr.getJSONObject(i));
				    data.add(twit);
				} catch (TwitException e) {
					// CANNOT PARSE ENTRY
					e.printStackTrace();
				}
			}
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	
	public class TwitException extends Exception {
		private static final long serialVersionUID = -2516516639873574357L;

		public TwitException(Exception cause) {
	        super(cause);
	        
	    }
	}
	
}
