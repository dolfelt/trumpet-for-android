package com.ofamilymedia.trumpet.classes;

import java.util.HashMap;

import com.twitter.Twit;

public class Account implements java.io.Serializable {

	private static final long serialVersionUID = 4815819079178827048L;

	private long id;
	private String token;
	private String secret;
	private String screenname;
	
	//private Twit currentTweet;
	//private Twit currentMention;
	
	private HashMap<String, String> userColors;
	
	public void setId(long value) {
		id = value;
	}
	public long getId() {
		return id;
	}
	
	public void setToken(String value) {
		token = value;
	}
	public String getToken() {
		return token;
	}
	
	public void setSecret(String value) {
		secret = value;
	}
	public String getSecret() {
		return secret;
	}
	
	public void setScreenName(String value) {
		screenname = value;
	}
	public String getScreenName() {
		return screenname;
	}

	/*public void setCurrentTweet(Twit value) {
		currentTweet = value;
	}
	public Twit getCurrentTweet() {
		return currentTweet;
	}
	public void setCurrentMention(Twit value) {
		currentMention = value;
	}
	public Twit getCurrentMention() {
		return currentMention;
	}*/
	
	public Colors.Gradient getUserColor(String screenname) {
		if(userColors == null) return null;
		screenname = screenname.toLowerCase();
		if(userColors.containsKey(screenname)) {
			return Colors.getGradient(userColors.get(screenname));
		}
		
		return null;
	}
	public void setUserColor(String username, String color) {
		if(userColors == null) userColors = new HashMap<String, String>();
		
		userColors.put(username.toLowerCase(), color);
	}
	
	
    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof Account && ((Account) obj).getId() == this.id;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", token=" + token +
                ", secret=" + secret +
                ", screenname=" + screenname +
                ", userColors=" + userColors +
                //", currentTweet=" + currentTweet +
                //", currentMention=" + currentMention +
               '}';
    }
	
}
