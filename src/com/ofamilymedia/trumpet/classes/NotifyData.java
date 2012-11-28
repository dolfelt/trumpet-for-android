package com.ofamilymedia.trumpet.classes;

import com.twitter.Twit;


public class NotifyData implements java.io.Serializable {

	private static final long serialVersionUID = -5509459958501931798L;
	
	/** COUNT PARAMS **/
	private int tweetsCount = 0;
	private int mentionsCount = 0;
	private int messagesCount = 0;
	
	/** RELOAD PARAMS **/
	private Boolean reloadTweets = false;
	private Boolean reloadMentions = false;
	private Boolean reloadMessages = false;
	
	/** CACHE PARAMS **/
	private long recentTweetID = 0;
	private long recentMentionID = 0;
	private long recentMessageID = 0;
	
	private Twit currentTweet = null;
	private Twit currentMention = null;
	
	public int getTweets() {
		return tweetsCount;
	}
	public void setTweets(int value) {
		tweetsCount = value;
	}
	public void addTweets(int value) {
		tweetsCount += value;
	}

	public int getMentions() {
		return mentionsCount;
	}
	public void setMentions(int value) {
		mentionsCount = value;
	}
	public void addMentions(int value) {
		mentionsCount += value;
	}
	
	public int getMessages() {
		return messagesCount;
	}
	public void setMessages(int value) {
		messagesCount = value;
	}
	public void addMessages(int value) {
		messagesCount += value;
	}
	
	
	
	public void setReloadTweets(Boolean val) {
		reloadTweets = val;
	}
	public Boolean getReloadTweets() {
		return reloadTweets;
	}	
	public void setReloadMentions(Boolean val) {
		reloadMentions = val;
	}
	public Boolean getReloadMentions() {
		return reloadMentions;
	}
	public void setReloadMessages(Boolean val) {
		reloadMessages = val;
	}
	public Boolean getReloadMessages() {
		return reloadMessages;
	}

	
	
	public void setCurrentTweet(Twit val) {
		currentTweet = val;
	}
	public Twit getCurrentTweet() {
		return currentTweet;
	}
	public void setCurrentMention(Twit val) {
		currentMention = val;
	}
	public Twit getCurrentMention() {
		return currentMention;
	}

	
    @Override
    public String toString() {
        return "NotifyData{" +
                "tweetsCount=" + tweetsCount +
                ", mentionsCount=" + mentionsCount +
                ", messagesCount=" + messagesCount +
                ", reloadTweets=" + reloadTweets +
                ", reloadMentions=" + reloadMentions +
                ", reloadMessages=" + reloadMessages +
                ", recentTweetID=" + recentTweetID +
                ", recentMentionID=" + recentMentionID +
                ", recentMessageID=" + recentMessageID +
                ", currentTweet=" + currentTweet +
                ", currentMention=" + currentMention +
                '}';
    }

}
