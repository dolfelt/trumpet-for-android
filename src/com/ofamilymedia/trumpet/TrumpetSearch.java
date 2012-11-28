package com.ofamilymedia.trumpet;


import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.classes.Twr;
import com.ofamilymedia.trumpet.controls.TweetList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TrumpetSearch extends Activity {

	public int userIndex = -1;
	public String search;
	public String searchUser;
	
	public String screenname;
	
	ViewFlipper container;
	TweetList convoList;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
        userIndex = extras.getInt("index"); 
        search = extras.getString("search"); 
        searchUser = extras.getString("searchUser");
        
        setContentView(R.layout.search);
        
        TextView title = (TextView)findViewById(R.id.title);
        title.setText("Search " + search);

        container = (ViewFlipper)findViewById(R.id.view_container);
        
        convoList = new TweetList(this, userIndex);
        
        if(searchUser != null) {
        	convoList.RequestType = TwitterMethod.SEARCH_USERS;
            convoList.search = searchUser;
        } else {
        	convoList.RequestType = TwitterMethod.SEARCH;
            convoList.search = search;
        }
        container.addView(convoList);
        convoList.LoadList();

        
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

	public void moveArrow(ImageView view) {
		ImageView arrow = (ImageView)findViewById(R.id.arrow);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(view.getLeft()+(view.getWidth()/2)-(arrow.getWidth()/2), 0, 0, 0);
		arrow.setLayoutParams(lp);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user, menu);
        return true;
    }
    

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
		convoList.setAdapter(null);
		super.onDestroy();
    }
}
