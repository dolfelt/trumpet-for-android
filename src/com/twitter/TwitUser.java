package com.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.User;

public class TwitUser implements java.io.Serializable {

	private static final long serialVersionUID = 1352155435377088797L;
	
	private long id;
    private String name;
    private String screenName;
    private String location;
    private String description;
    private boolean isContributorsEnabled;
    private String profileImageUrl;
    private String url;
    private boolean isProtected;
    private int followersCount;

    private Status status;

    private String profileBackgroundColor;
    private String profileTextColor;
    private String profileLinkColor;
    private String profileSidebarFillColor;
    private String profileSidebarBorderColor;
    private int friendsCount;
    private Date createdAt;
    private int favouritesCount;
    private int utcOffset;
    private String timeZone;
    private String profileBackgroundImageUrl;
    private boolean profileBackgroundTiled;
    private String lang;
    private int statusesCount;
    private boolean isGeoEnabled;
    private boolean isVerified;
    private int listedCount;
    private boolean isFollowRequestSent;
    
    
    public static TwitUser create(User user) {
    	TwitUser twit = new TwitUser();
    	
        twit.id = user.getId();
        twit.name = user.getName();
        twit.screenName = user.getScreenName();
        twit.location = user.getLocation();
        twit.description = user.getDescription();
        twit.isContributorsEnabled = user.isContributorsEnabled();
        if(user.getProfileImageURL()!=null) {
        	twit.profileImageUrl = user.getProfileImageURL().toString();
        }
        if(user.getURL()!=null) {
        	twit.url = user.getURL().toString();
        }
        twit.isProtected = user.isProtected();
        twit.followersCount = user.getFollowersCount();
        twit.favouritesCount = user.getFavouritesCount();
        twit.friendsCount = user.getFriendsCount();
        twit.statusesCount = user.getStatusesCount();
    	
    	return twit;
    }

    public static TwitUser create(Tweet user) {
    	TwitUser twit = new TwitUser();
    	
        twit.id = 0;
        twit.name = user.getFromUser();
        twit.screenName = user.getFromUser();
        twit.profileImageUrl = user.getProfileImageUrl().toString();
    	
    	return twit;
    }

    
    public long compareTo(TwitUser that) {
        return this.id - that.getId();
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }
    public void setName(String val) {
        name = val;
    }
    /**
     * {@inheritDoc}
     */
    public String getScreenName() {
        return screenName;
    }
    public void setScreenName(String val) {
        screenName = val;
    }

    /**
     * {@inheritDoc}
     */
    public String getLocation() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isContributorsEnabled() {
        return isContributorsEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public URL getProfileImageURL() {
        try {
            return new URL(profileImageUrl);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    public void setProfileImageURL(String val) {
    	profileImageUrl = val;
    }

    /**
     * {@inheritDoc}
     */
    public URL getURL() {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProtected() {
        return isProtected;
    }

    /**
     * {@inheritDoc}
     */
    public int getFollowersCount() {
        return followersCount;
    }

    /**
     * {@inheritDoc}
     */
    public Date getStatusCreatedAt() {
        return status.getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    public long getStatusId() {
        return status.getId();
    }

    /**
     * {@inheritDoc}
     */
    public String getStatusText() {
        return status.getText();
    }

    /**
     * {@inheritDoc}
     */
    public String getStatusSource() {
        return status.getSource();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStatusTruncated() {
        return status.isTruncated();
    }

    /**
     * {@inheritDoc}
     */
    public long getStatusInReplyToStatusId() {
        return status.getInReplyToStatusId();
    }

    /**
     * {@inheritDoc}
     */
    public long getStatusInReplyToUserId() {
        return status.getInReplyToUserId();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStatusFavorited() {
        return status.isFavorited();
    }

    /**
     * {@inheritDoc}
     */
    public String getStatusInReplyToScreenName() {
        return status.getInReplyToScreenName();
    }

    /**
     * {@inheritDoc}
     */
    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    public String getProfileTextColor() {
        return profileTextColor;
    }

    /**
     * {@inheritDoc}
     */
    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    /**
     * {@inheritDoc}
     */
    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }

    /**
     * {@inheritDoc}
     */
    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }

    /**
     * {@inheritDoc}
     */
    public int getFriendsCount() {
        return friendsCount;
    }

    /**
     * {@inheritDoc}
     */
    public Status getStatus() {
        return status;
    }


    /**
     * {@inheritDoc}
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * {@inheritDoc}
     */
    public int getFavouritesCount() {
        return favouritesCount;
    }

    /**
     * {@inheritDoc}
     */
    public int getUtcOffset() {
        return utcOffset;
    }

    /**
     * {@inheritDoc}
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * {@inheritDoc}
     */
    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTiled;
    }

    /**
     * {@inheritDoc}
     */
    public String getLang() {
        return lang;
    }

    /**
     * {@inheritDoc}
     */
    public int getStatusesCount() {
        return statusesCount;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGeoEnabled() {
        return isGeoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVerified() {
        return isVerified;
    }

    /**
     * {@inheritDoc}
     */
    public int getListedCount() {
        return listedCount;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFollowRequestSent() {
        return isFollowRequestSent;
    }

    public String toString() {
        return "TwitUser{" +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", url='" + url + '\'' +
                ", isProtected=" + isProtected +
                ", followersCount=" + followersCount +
                ", status=" + status +
                ", profileBackgroundColor='" + profileBackgroundColor + '\'' +
                ", profileTextColor='" + profileTextColor + '\'' +
                ", profileLinkColor='" + profileLinkColor + '\'' +
                ", profileSidebarFillColor='" + profileSidebarFillColor + '\'' +
                ", profileSidebarBorderColor='" + profileSidebarBorderColor + '\'' +
                ", friendsCount=" + friendsCount +
                ", createdAt=" + createdAt +
                ", favouritesCount=" + favouritesCount +
                ", utcOffset=" + utcOffset +
                ", timeZone='" + timeZone + '\'' +
                ", profileBackgroundImageUrl='" + profileBackgroundImageUrl + '\'' +
                ", profileBackgroundTiled='" + profileBackgroundTiled + '\'' +
                ", statusesCount=" + statusesCount +
                ", geoEnabled=" + isGeoEnabled +
                ", verified=" + isVerified +
                '}';
    }

    public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("id", id);
			json.put("name", name);
			json.put("screenName", screenName);
			json.put("location", location);
			json.put("description", description);
			json.put("profileImageUrl", profileImageUrl);
			json.put("url", url);
			json.put("isProtected", isProtected);
			json.put("followersCount", followersCount);
			json.put("status", status);
			json.put("profileBackgroundColor", profileBackgroundColor);
			json.put("profileTextColor", profileTextColor);
			json.put("profileLinkColor", profileLinkColor);
			json.put("profileSidebarFillColor", profileSidebarFillColor);
			json.put("profileSidebarBorderColor", profileSidebarBorderColor);
			json.put("friendsCount", friendsCount);
			json.put("createdAt", createdAt);
			json.put("favouritesCount", favouritesCount);
			json.put("utcOffset", utcOffset);
			json.put("timeZone", timeZone);
			json.put("profileBackgroundImageUrl", profileBackgroundImageUrl);
			json.put("profileBackgroundTiled", profileBackgroundTiled);
			json.put("statusesCount", statusesCount);
			json.put("isGeoEnabled", isGeoEnabled);
			json.put("isVerified", isVerified);
			json.put("isContributorsEnabled", isContributorsEnabled);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
    }

    public void fromJSON(JSONObject json) {
    	
		try {
	        id = json.getInt("id");
	        name = json.getString("name");
	        screenName = json.getString("screenName");
	        if(!json.isNull("location")) {
	        	location = json.getString("location");
	        }
	        if(!json.isNull("description")) {
	        	description = json.getString("description");
	        }
	        isContributorsEnabled = json.getBoolean("isContributorsEnabled");
	        if(!json.isNull("profileImageUrl")) {
	        	profileImageUrl = json.getString("profileImageUrl");
	        }
	        if(!json.isNull("url")) {
	        	url = json.getString("url");
	        }
	        isProtected = json.getBoolean("isProtected");
	        followersCount = json.getInt("followersCount");
	        favouritesCount = json.getInt("favouritesCount");
	        friendsCount = json.getInt("friendsCount");
	        statusesCount = json.getInt("statusesCount");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}
