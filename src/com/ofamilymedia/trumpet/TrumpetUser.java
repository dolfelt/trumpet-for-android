package com.ofamilymedia.trumpet;


import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.Twr;
import com.ofamilymedia.trumpet.classes.NotifyData;
import com.ofamilymedia.trumpet.controls.DirectList;
import com.ofamilymedia.trumpet.controls.SearchTab;
import com.ofamilymedia.trumpet.controls.TweetList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TrumpetUser extends Activity {

	public Account account;
	public int userIndex = -1;
	
	public ViewFlipper container;
	public static Boolean isFront = false;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
		userIndex = extras.getInt("index");
		account = ((TrumpetBase)this.getApplication()).accounts.get(userIndex);//(Account) extras.getSerializable("account");    	
		
		NotifyData notifyData = ((TrumpetBase)this.getApplication()).getNotifyData(account);
		
        setContentView(R.layout.user);

        
		Button title = (Button)findViewById(R.id.title);
		title.setText("@" + account.getScreenName());
		
		title.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TrumpetBase.defaultAccount = -2;
				Intent myIntent = new Intent(TrumpetUser.this, TrumpetApp.class);
				startActivity(myIntent);
		        overridePendingTransition(R.anim.push_up_in, R.anim.hold);
			}
		});
		
        container = (ViewFlipper)findViewById(R.id.view_container);
        //TrumpetBase.container = container;
        
        final TweetList homeList = new TweetList(this, userIndex);
        homeList.RequestType = TwitterMethod.HOME_TIMELINE;
        homeList.setCurrentItem(notifyData.getCurrentTweet());
        container.addView(homeList);
        homeList.getCache(true);
        //homeList.onDisplay();

        final TweetList mentionsList = new TweetList(this, userIndex);
        mentionsList.RequestType = TwitterMethod.MENTIONS;
        mentionsList.setCurrentItem(notifyData.getCurrentMention());
        mentionsList.getCache();
        container.addView(mentionsList);
       
        final DirectList directList = new DirectList(this, userIndex);
        directList.getCache();
        container.addView(directList);
       
        final SearchTab searchTab = new SearchTab(this);
        searchTab.userIndex = userIndex;
        container.addView(searchTab);
        
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View moreTab = inflater.inflate(R.layout.user_more_panel, container);
        //container.addView(moreTab);
        
        ImageButton buttonHome = (ImageButton)findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(0);
        		moveArrow((ImageView)v);
        		homeList.onDisplay();
        	}
        });
        ImageButton buttonMentions = (ImageButton)findViewById(R.id.button_mentions);
        buttonMentions.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(1);
        		moveArrow((ImageView)v);
        		mentionsList.onDisplay();
        	}
        });
        ImageButton buttonMessages = (ImageButton)findViewById(R.id.button_messages);
        buttonMessages.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(2);
        		moveArrow((ImageView)v);
        		directList.onDisplay();
        	}
        });
        ImageButton buttonSearch = (ImageButton)findViewById(R.id.button_search);
        buttonSearch.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(3);
        		moveArrow((ImageView)v);
        		searchTab.onDisplay();
        	}
        });
        ImageButton buttonMore = (ImageButton)findViewById(R.id.button_more);
        buttonMore.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(4);
        		moveArrow((ImageView)v);
        	}
        });
      
        
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
        		ReloadLists(false);
        	}
        });
        RelativeLayout header = (RelativeLayout)findViewById(R.id.header);
        header.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Object list = container.getCurrentView();
				if(list instanceof TweetList) {
					((TweetList) list).setSelection(0);
				} else if(list instanceof DirectList) {
					((DirectList) list).setSelection(0);
				}
				return true;
			}
        });
        
        
        /**
         * MORE PAGE LISTENERS
         */
        
        RelativeLayout more_view_profile = (RelativeLayout) findViewById(R.id.item_view_profile);
        more_view_profile.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), TrumpetProfile.class);
                myIntent.putExtra("index", userIndex);
                myIntent.putExtra("screenname", account.getScreenName());
                v.getContext().startActivity(myIntent);
        	}
        });

        
	}
	
	@Override
	public void onResume() {
		super.onResume();
		isFront = true;
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel("TRUMPET_" + account.getScreenName(), 0);
		
		NotifyData data = ((TrumpetBase)this.getApplication()).getNotifyData(account);
		
		
		/*if(data.getReloadTweets()) {
			((TweetList)container.getChildAt(0)).getCache(true);
		}
		if(data.getReloadMentions()) {
			((TweetList)container.getChildAt(1)).getCache(true);
		}
		if(data.getReloadMessages()) {
			((DirectList)container.getChildAt(2)).LoadList();
		}*/
		
		
		if(data.getReloadTweets()) {
			((TweetList)container.getChildAt(0)).getCache();
		}
		if(data.getReloadMentions()) {
			((TweetList)container.getChildAt(1)).getCache();
		}
		if(data.getReloadMessages()) {
			((DirectList)container.getChildAt(2)).getCache();
		}
		
		
		if(data.getMentions() > 0) {
    		container.setDisplayedChild(1);
    		moveArrow();
    		((TweetList)container.getChildAt(1)).onDisplay();
		} else if(data.getMessages() > 0) {
    		container.setDisplayedChild(2);
    		moveArrow();
    		((DirectList)container.getChildAt(2)).onDisplay();
		} else {
			View currentList = container.getCurrentView();
			if(currentList instanceof TweetList) {
				((TweetList) currentList).onDisplay();
			} else if(currentList instanceof DirectList) {
				((DirectList) currentList).onDisplay();
			}
		}
		
		
		/** RESET THE DATA COUNTS **/
		data.setMentions(0);
		data.setMessages(0);
		data.setTweets(0);
		((TrumpetBase)this.getApplication()).setNotifyData(account, data);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		isFront = false;
		
		if(container != null) {
			NotifyData data = ((TrumpetBase)this.getApplication()).getNotifyData(account);
			
			TweetList mentionList = (TweetList)container.getChildAt(1);
			data.setCurrentMention(mentionList.getCurrentListItem());
			TweetList tweetList = (TweetList)container.getChildAt(0);
			data.setCurrentTweet(tweetList.getCurrentListItem());
			
			((TrumpetBase)this.getApplication()).setNotifyData(account, data);
		}

	}
	
	@Override
	public void onDestroy() {
		
		if(container != null) {
			int cc = container.getChildCount();
			for(int i = 0; i < cc; i++) {
				Object item = container.getChildAt(i);
				if(item == null) continue;
				
				if(item instanceof TweetList) {
					((TweetList) item).destroy();
				} else if(item instanceof DirectList) {
					((DirectList) item).destroy();
				}
			}
			container.removeAllViews();
		}
		
		
		container = null;
		//TrumpetBase.container = null;
		account = null;
		
		super.onDestroy();
	}

	public void moveArrow() {
		final int i = container.getDisplayedChild();
		final LinearLayout button_container = (LinearLayout)findViewById(R.id.bottom_container);
		button_container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				moveArrow((ImageView)button_container.getChildAt(i));
				button_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}
	public void moveArrow(ImageView view) {
		ImageView arrow = (ImageView)findViewById(R.id.arrow);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(arrow.getWidth(), arrow.getHeight());
		lp.setMargins(view.getLeft()+(view.getWidth()/2)-(arrow.getWidth()/2), 0, 0, 0);
		arrow.setLayoutParams(lp);
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		moveArrow();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
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
        	case R.id.settings:
        		Intent myIntent = new Intent(TrumpetUser.this.getApplicationContext(), TrumpetPreferences.class);
        		myIntent.putExtra("account", account);
                startActivityForResult(myIntent, 2492);
                return true;
 	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == 2492) {
			Twr.setupNotifications(this, account);
	        
			SharedPreferences settings = getSharedPreferences(account.getScreenName(), 0);
	        Boolean hl = settings.getBoolean("list_highlight", false);
	        ((TweetList)container.getChildAt(0)).highlightLinks = hl;
	        ((TweetList)container.getChildAt(1)).highlightLinks = hl;
	        ((DirectList)container.getChildAt(2)).highlightLinks = hl;
		}
	}
	
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //TrumpetBase.defaultAccount = -2;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    public void ReloadLists() {
    	ReloadLists(true);
    }
    public void ReloadLists(Boolean all) {
    	if(container != null) {
    		if(all) {
	    		int cc = container.getChildCount();
				for(int i = 0; i < cc; i++) {
					Object child = container.getChildAt(i);
					if(child == null) continue;
					
					if(child instanceof TweetList) {
						((TweetList)child).LoadList();
					}
					if(child instanceof DirectList) {
						((DirectList)child).LoadList();
					}
				}
    		} else {
    			Object item = container.getCurrentView();
    			if(item instanceof TweetList) {
    				((TweetList) item).LoadList();
    			}
    			if(item instanceof DirectList) {
    				((DirectList) item).LoadList();
    			}
    		}
    	}
    }
    
    public void NewTweet() {
        Intent myIntent = Twr.getNewTweet(this, userIndex);
        if(container.getDisplayedChild() == 2) {
        	myIntent.putExtra("isDM", true);
        }
        startActivity(myIntent);
    }

}
