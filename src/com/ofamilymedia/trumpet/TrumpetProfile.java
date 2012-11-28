package com.ofamilymedia.trumpet;

import twitter4j.AsyncTwitter;
import twitter4j.Relationship;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.User;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.controls.TweetList;
import com.twitter.TwitUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TrumpetProfile extends Activity {

	public Account account;
	public int userIndex = -1;
	
	ViewFlipper container;
	
	TwitUser profile;
	int userId = 0;
	String screenname = "";
	
	ProgressDialog progress;
	
	AsyncTwitter asyncTwitter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
		userIndex = extras.getInt("index"); 
		
		account = ((TrumpetBase)this.getApplication()).accounts.get(userIndex); //(Account)extras.getSerializable("account"); //TrumpetApp.accounts.get(userIndex);
		
		profile = (TwitUser)extras.getSerializable("profile");

        setContentView(R.layout.profile);
		
        container = (ViewFlipper)findViewById(R.id.view_container);

        
        /** SETUP ASYNC TWITTER CLIENT **/
		asyncTwitter = ((TrumpetBase)this.getApplication()).getAsyncTwitter(userIndex, twitAdapter);

        
        
        if(profile == null || profile.getId() <= 0) {
			
			userId = extras.getInt("userId", 0);
			screenname = extras.getString("screenname");
			
			if(screenname == null && profile != null) {
				screenname = profile.getScreenName();
			}
			
			if(userId == 0 && (screenname==null || screenname.length() <= 0)) finish();
			
			progress = new ProgressDialog(this);
			progress.setMessage("Loading profile...");
			progress.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface di) {
					finish();
				}
			});
			progress.setCancelable(true);
			progress.show();
			
			if(userId > 0)
				asyncTwitter.showUser(userId);
			else if(screenname != "") {
				Log.w("SCREENNAME", screenname);
				asyncTwitter.showUser(screenname);
			}
		} else {
			LoadProfileInfo();
		}
        
        
	}
	
	
	public void LoadProfileInfo()
	{
		Boolean isAccountHolder = profile.getScreenName().compareToIgnoreCase(account.getScreenName()) == 0;
		
        final TweetList userList = new TweetList(this, userIndex);
        userList.RequestType = TwitterMethod.USER_TIMELINE;
        userList.userScreenname = profile.getScreenName();
        container.addView(userList);

        final TweetList mentionsList = new TweetList(this, userIndex);
        mentionsList.RequestType = TwitterMethod.SEARCH;
        mentionsList.search = "@"+profile.getScreenName();
        container.addView(mentionsList);
       
        
        /** FOLLOW AND UNFOLLOW REQUESTS **/
		final Button followBtn = (Button) findViewById(R.id.btn_follow);
        if(!isAccountHolder) {
        	asyncTwitter.showFriendship(account.getScreenName(), profile.getScreenName());
    		followBtn.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		String txt = (String) followBtn.getText();
            		if(txt.equalsIgnoreCase("Follow")) {
            			asyncTwitter.createFriendship(profile.getScreenName());
            		} else if(txt.equalsIgnoreCase("Unfollow")) {
            			asyncTwitter.destroyFriendship(profile.getScreenName());
            		} else {
            			return;
            		}
            		followBtn.setText("");
    		        ProgressBar progress = (ProgressBar) findViewById(R.id.follow_progress);
    		        progress.setVisibility(ProgressBar.VISIBLE);
            	}
            });
        } else {
        	followBtn.setVisibility(Button.GONE);
        }
        
		
        /** SETUP LISTENERS **/
        
        ImageView buttonHome = (ImageView)findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(0);
        		moveArrow((ImageView)v);
        	}
        });
        ImageView buttonTimeline = (ImageView)findViewById(R.id.button_timeline);
        buttonTimeline.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(1);
        		moveArrow((ImageView)v);
        		userList.onDisplay();
        	}
        });
        ImageView buttonMentions = (ImageView)findViewById(R.id.button_mentions);
        buttonMentions.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(2);
        		moveArrow((ImageView)v);
        		mentionsList.onDisplay();
        	}
        });
        
        ImageView buttonFavorites = (ImageView)findViewById(R.id.button_favorite);
        buttonFavorites.setVisibility(View.GONE);
        
        if(isAccountHolder) {
            final TweetList retweetsList = new TweetList(this, userIndex);
            retweetsList.RequestType = TwitterMethod.RETWEETS_OF_ME;
            retweetsList.search = "@"+profile.getScreenName();
            container.addView(retweetsList);
        	
        	ImageView buttonRetweets = (ImageView)findViewById(R.id.button_retweets);
        	buttonRetweets.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		container.setDisplayedChild(3);
            		moveArrow((ImageView)v);
            		((TweetList)container.getChildAt(3)).onDisplay();
            	}
            });
        	buttonRetweets.setVisibility(View.VISIBLE);
        }
       
        
        
        /** END LISTENERS **/
        
        
        TextView title = (TextView)findViewById(R.id.title);
        title.setText(profile.getName() + "'s Profile");
        
        
        ImageView icon = (ImageView)findViewById(R.id.icon);
        icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				String url = profile.getProfileImageURL().toString().replace("_normal.", ".");
    	        Intent myIntent = new Intent(v.getContext(), TrumpetImage.class);
    	        myIntent.putExtra("image", url);
    	        v.getContext().startActivity(myIntent);
			}
        });
        String icon_url = profile.getProfileImageURL().toString();
        icon_url = icon_url.replace("_normal.", "_bigger.");
        
        icon.setTag(icon_url);
        ((TrumpetBase)getApplication()).imageLoader.DisplayImage(icon_url, icon);
        
        TextView realname = (TextView)findViewById(R.id.realname);
        TextView screennametext = (TextView)findViewById(R.id.screenname);
        
        realname.setText(profile.getName());
        screennametext.setText("@" + profile.getScreenName());
        
        TextView profile_location = (TextView)findViewById(R.id.profile_location);
        TextView profile_web = (TextView)findViewById(R.id.profile_web);
        
        profile_location.setText(profile.getLocation());
        
        if(profile.getURL() != null)
        	profile_web.setText(profile.getURL().toString());
        
        profile_web.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if(profile.getURL() != null) {
        			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(profile.getURL().toString()));
        			startActivity(browserIntent);
        		}
        	}
        });

        TextView profile_following = (TextView)findViewById(R.id.profile_following);
        profile_following.setText(String.valueOf(profile.getFriendsCount()));
        profile_following.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
    	        Intent myIntent = new Intent(v.getContext(), TrumpetUserList.class);
    	        myIntent.putExtra("screenname", profile.getScreenName());
    	        myIntent.putExtra("method", TwitterMethod.FRIENDS_STATUSES);
    	        myIntent.putExtra("index", userIndex);
    	        v.getContext().startActivity(myIntent);
        	}
        });
        TextView profile_followers = (TextView)findViewById(R.id.profile_followers);
        profile_followers.setText(String.valueOf(profile.getFollowersCount()));
        profile_followers.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
    	        Intent myIntent = new Intent(v.getContext(), TrumpetUserList.class);
    	        myIntent.putExtra("screenname", profile.getScreenName());
    	        myIntent.putExtra("method", TwitterMethod.FOLLOWERS_STATUSES);
    	        myIntent.putExtra("index", userIndex);
    	        v.getContext().startActivity(myIntent);
        	}
        });
        TextView profile_tweets = (TextView)findViewById(R.id.profile_tweets);
        profile_tweets.setText(String.valueOf(profile.getStatusesCount()));
        profile_tweets.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		container.setDisplayedChild(1);
        		moveArrow((ImageView)findViewById(R.id.button_timeline));
        		userList.onDisplay();
        	}
        });
        TextView profile_favorites = (TextView)findViewById(R.id.profile_favorites);
        profile_favorites.setText(String.valueOf(profile.getFavouritesCount()));
        profile_favorites.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		
        	}
        });	
		
	}
	
	public TwitterListener twitAdapter = new TwitterAdapter() {
		public void gotUserDetail(User user) {
			profile = TwitUser.create(user);
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
					LoadProfileInfo();
					progress.dismiss();
    			}
    		});
		}
		public void gotShowFriendship(Relationship relationship) {
			final Boolean meFollows = relationship.isSourceFollowingTarget();
			final Boolean userFollowing = relationship.isSourceFollowedByTarget();
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
    		        Button followBtn = (Button) findViewById(R.id.btn_follow);
    		        if(meFollows) {
    		        	followBtn.setText("Unfollow");
    		        } else {
    		        	followBtn.setText("Follow");
    		        }
    		        TextView followTxt = (TextView) findViewById(R.id.txt_following);
    		        StringBuilder str = new StringBuilder();
    		        str.append("@").append(profile.getScreenName());
    		        if(userFollowing) {
    		        	str.append(" follows you.");
    		        } else {
    		        	str.append(" does not follow you.");
    		        }
    		        followTxt.setVisibility(TextView.VISIBLE);
    		        followTxt.setText(str.toString());
    		        ProgressBar progress = (ProgressBar) findViewById(R.id.follow_progress);
    		        progress.setVisibility(ProgressBar.GONE);
    			}
    		});
		}
		
		public void createdFriendship(User user) {
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
					ProgressBar progress = (ProgressBar) findViewById(R.id.follow_progress);
			        progress.setVisibility(ProgressBar.GONE);
			        Button followBtn = (Button) findViewById(R.id.btn_follow);
			        followBtn.setText("Unfollow");
					Toast.makeText(TrumpetProfile.this.getBaseContext(), "You are now following this person.", Toast.LENGTH_SHORT).show();
    			}
    		});
		}
		
		public void destroyedFriendship(User user) {
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
					ProgressBar progress = (ProgressBar) findViewById(R.id.follow_progress);
			        progress.setVisibility(ProgressBar.GONE);
			        Button followBtn = (Button) findViewById(R.id.btn_follow);
			        followBtn.setText("Follow");
					Toast.makeText(TrumpetProfile.this.getBaseContext(), "You are no longer following this person.", Toast.LENGTH_SHORT).show();
    			}
    		});
		}
		
		@Override
		public void createdBlock(User user) {
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
					Toast.makeText(TrumpetProfile.this.getBaseContext(), "You have blocked this user.", Toast.LENGTH_SHORT).show();
    			}
    		});
		}

		@Override
		public void reportedSpam(User user) {
    		TrumpetProfile.this.runOnUiThread(new Runnable(){ 
    			public void run() { 
					Toast.makeText(TrumpetProfile.this.getBaseContext(), "You have reported this user.", Toast.LENGTH_SHORT).show();
    			}
    		});
		}
	};
	
	
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
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.block:
        		asyncTwitter.createBlock(profile.getScreenName());
        		return true;
        	case R.id.spam:
        		asyncTwitter.reportSpam(profile.getScreenName());
        		return true;
        	case R.id.color:
        		final String[] items = {"None", "Red", "Orange", "Yellow", "Green", "Teal", "Blue", "Purple", "Pink"};

        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setTitle("Pick a color");
        		builder.setItems(items, new DialogInterface.OnClickListener() {
        		    public void onClick(DialogInterface dialog, int item) {
        		        account.setUserColor(profile.getScreenName(), items[item].toLowerCase());
        		    }
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
 	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

}
