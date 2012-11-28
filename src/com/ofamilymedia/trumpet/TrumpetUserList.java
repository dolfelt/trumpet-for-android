package com.ofamilymedia.trumpet;


import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.classes.Twr;
import com.ofamilymedia.trumpet.controls.TweetList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TrumpetUserList extends Activity {

	//public static JSONObject account;
	public int userIndex = -1;
	
	//public String screenname;
	public String userScreenname;
	
	public TwitterMethod method;
	
	ViewFlipper container;
	TweetList userList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
        userIndex = extras.getInt("index"); 
        userScreenname = extras.getString("screenname"); 
        
        method = (TwitterMethod)extras.getSerializable("method");
		
        
 
        setContentView(R.layout.user_list);

        TextView title = (TextView)findViewById(R.id.title);
        
        if(method == TwitterMethod.FRIENDS_STATUSES) {
        	title.setText(userScreenname + "'s Friends");
        } else if(method == TwitterMethod.FOLLOWERS_STATUSES) {
        	title.setText(userScreenname + "'s Followers");
        }

        container = (ViewFlipper)findViewById(R.id.view_container);
        
        userList = new TweetList(this, userIndex);
        userList.RequestType = method; // Conversation Block
        userList.userScreenname = userScreenname;
        container.addView(userList);
        userList.LoadList();

        
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

	
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user, menu);
        return true;
    }*/
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.refresh:
        		ReloadLists();
        		return true;
        	case R.id.new_tweet:
        		NewTweet();
    	        return true;
 	        default:
	            return super.onOptionsItemSelected(item);
        }
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
		userList.setAdapter(null);

    	super.onDestroy();
    }
}
