package com.ofamilymedia.trumpet;


import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.classes.Twr;
import com.ofamilymedia.trumpet.controls.TweetList;
import com.twitter.Twit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TrumpetList extends Activity {

	//public static JSONObject account;
	public int userIndex = -1;
	public Twit tweet;
	public Type type;
	
	public static enum Type {
		CONVO, RETWEETS
	}
	
	//public String screenname;
	
	ViewFlipper container;
	TweetList tweetList;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
        userIndex = extras.getInt("index"); 
        tweet = (Twit)extras.getSerializable("status"); 


		type = (Type)extras.getSerializable("type");
        
 
        setContentView(R.layout.convo);

        TextView title = (TextView)findViewById(R.id.title);
        container = (ViewFlipper)findViewById(R.id.view_container);
        
        if(type == Type.CONVO) {
            title.setText("Conversation");
            tweetList = new TweetList(this, userIndex);
            tweetList.RequestType = TwitterMethod.SHOW_STATUS; // Conversation Block
            tweetList.statusID = tweet.getInReplyToStatusId();
            container.addView(tweetList);
            tweetList.eAdapter.addStatusItem(tweet);
            tweetList.LoadList();
        } else if(type == Type.RETWEETS) {
	        title.setText("Retweets");
	        tweetList = new TweetList(this, userIndex);
	        tweetList.RequestType = TwitterMethod.RETWEETS; // Conversation Block
	        tweetList.statusID = tweet.getId();
	        container.addView(tweetList);
	        //convoList.eAdapter.addStatusItem(tweet);
	        tweetList.LoadList();
        }

        
        ImageButton btnTweet = (ImageButton)findViewById(R.id.title_tweet);
        btnTweet.setVisibility(ImageButton.VISIBLE);
        btnTweet.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		NewTweet();
        	}
        });
        ImageButton btnRefresh = (ImageButton)findViewById(R.id.title_refresh);
        btnRefresh.setVisibility(ImageButton.VISIBLE);
        btnRefresh.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		ReloadLists();
        	}
        });
        
        
	}
    
    
    public void ReloadLists() {
		for(int i = 0; i < container.getChildCount(); i++) {
			Object child = container.getChildAt(i);
			if(child instanceof TweetList) {
				((TweetList)child).LoadList();
			}
		}
    }
    
    public void NewTweet() {
        Intent myIntent = Twr.getNewTweet(this, userIndex);
        startActivity(myIntent);
    }
    
    @Override
    public void onDestroy() {
    	if(tweetList != null) {
    		tweetList.setAdapter(null);
    	}
    	super.onDestroy();
    }

}
