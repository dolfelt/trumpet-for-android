package com.ofamilymedia.trumpet;


import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.DirectMessage;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterListener;
import twitter4j.Status;
import twitter4j.ResponseList;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.ImageLoader;
import com.ofamilymedia.trumpet.classes.Twr;
import com.twitter.Twit;
import com.twitter.TwitUser;
import com.twitter.Extractor.Entity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ImageView.ScaleType;

public class TrumpetItem extends Activity {

	//public JSONObject account;
	public int userIndex = -1;
	public Account account;
	
    ImageLoader mediaLoader;

	ViewFlipper container;
	
	Twit tweet;
	
	DirectMessage direct;
	
	Twit original_tweet;
	
	ArrayList<MediaItem> mediaUrls;
	
	List<Status> retweets = new ArrayList<Status>();
	
	AsyncTwitter asyncTwitter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
		userIndex = extras.getInt("index"); 
		account = ((TrumpetBase)this.getApplication()).accounts.get(userIndex);
		
		tweet = (Twit)extras.getSerializable("tweet");
		direct = (DirectMessage)extras.getSerializable("direct");

        /** SETUP ASYNC TWITTER CLIENT **/
		asyncTwitter = ((TrumpetBase)getApplication()).getAsyncTwitter(userIndex, twitAdapter);

        if(tweet!=null && tweet.getRetweetedStatus() != null) {
        	/** TODO: ADD USER LINK OF PERSON WHO RETWEETED IT **/
        	original_tweet = tweet;
        	tweet = tweet.getRetweetedStatus();
        }
		
        mediaLoader = new ImageLoader(getApplicationContext());
        
        setContentView(R.layout.tweet);
        

        TextView title = (TextView)findViewById(R.id.title);
        title.setText("Tweet");

        ImageView icon = (ImageView)findViewById(R.id.icon);
        
        TwitUser tweetUser;
        if(tweet!=null)
        {
        	tweetUser = tweet.getUser();
        } else {
        	tweetUser = TwitUser.create(direct.getSender());
        }
        
        URL profileImage = tweetUser.getProfileImageURL();
        if(profileImage != null) {
	        String icon_url = profileImage.toString();
	        icon_url = icon_url.replace("_normal.", "_bigger.");
	        
	        icon.setTag(icon_url);
	        mediaLoader.DisplayImage(icon_url, icon);
        }
        
        /**
         * LOAD RETWEETS IF THERE ARE ANY
         */
        
        if(tweet != null && tweet.getRetweetCount() > 0) {
        	asyncTwitter.getRetweets(tweet.getId());
        }
        
        TextView realname = (TextView)findViewById(R.id.realname);
        TextView screennametext = (TextView)findViewById(R.id.screenname);
        
        realname.setText(tweetUser.getName());
        screennametext.setText("@" + tweetUser.getScreenName());
        
        
        TextView text = (TextView)findViewById(R.id.text);
        
        String tweetMessage = (tweet!=null) ? tweet.getText() : direct.getText();

        Spannable tweetSpan = new SpannableString(tweetMessage);
        
        mediaUrls = new ArrayList<MediaItem>();
        
        if(tweet!=null && (tweet.getGeoLocation() instanceof twitter4j.GeoLocation)) {
        	StringBuilder map_builder = new StringBuilder();
        	map_builder.append("http://maps.google.com/maps/api/staticmap?size=120x200&zoom=14&sensor=true&center=");
        	map_builder.append(tweet.getGeoLocation().getLatitude());
        	map_builder.append(",");
        	map_builder.append(tweet.getGeoLocation().getLongitude());
        	map_builder.append("&markers=size:small|color:0xfe776b|");
        	map_builder.append(tweet.getGeoLocation().getLatitude());
        	map_builder.append(",");
        	map_builder.append(tweet.getGeoLocation().getLongitude());
        	StringBuilder map_url = new StringBuilder();
        	map_url.append("http://maps.google.com/?ie=UTF8&z=14&ll=")
        			.append(tweet.getGeoLocation().getLatitude()).append(",")
        			.append(tweet.getGeoLocation().getLongitude());
        	mediaUrls.add(new MediaItem(map_builder.toString(), null, map_url.toString()));
        }
        
        int linkColor = 0xFF4f7800;
        
        List<Entity> urlMatches = com.twitter.Extractor.extractURLs(tweetMessage);
        for(Entity urlMatch : urlMatches) {
        	if(urlMatch.value.startsWith("http")==false) {
        		urlMatch.value = "http://" + urlMatch.value;
        	}
        	final String url_link = urlMatch.value;
        	
        	if(url_link.contains("yfrog.com")) {
			    mediaUrls.add(new MediaItem(url_link + ":small", url_link + ":iphone", null));
        	} else if(url_link.contains("twitpic.com")) {
			    String[] hash = url_link.split("\\/");
			    String api_url = "http://twitpic.com/show/mini/" + hash[hash.length-1];
			    String api_full_url = "http://twitpic.com/show/full/" + hash[hash.length-1];
        		mediaUrls.add(new MediaItem(api_url, api_full_url, null));
        	} else if(url_link.contains("tweetphoto.com") || url_link.contains("plixi.com")) {
			    String api_url = "http://api.plixi.com/api/tpapi.svc/imagefromurl?size=thumbnail&url=" + url_link;
			    String api_full_url = "http://api.plixi.com/api/tpapi.svc/imagefromurl?size=medium&url=" + url_link;
			    mediaUrls.add(new MediaItem(api_url, api_full_url, null));
        	}
        	
        	ClickableSpan urlSpan = new ClickableSpan() {
        		@Override
        		public void onClick(View v) {  
        			Intent browserIntent;
        			if(tweet != null && tweet.getAd() != null) {
        				browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(tweet.getAd().getClickURL()));
        			} else {
           				browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url_link));
        			}
        			startActivity(browserIntent);
         		} 
        	};
    		tweetSpan.setSpan(urlSpan, urlMatch.start, urlMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), urlMatch.start, urlMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        List<Entity> hashMatches = com.twitter.Extractor.extractHashtags(tweetMessage);
        for(Entity hashMatch : hashMatches) {
        	final String hash_link = hashMatch.value;
        	ClickableSpan hashSpan = new ClickableSpan() {
        		@Override
        		public void onClick(View v) {  
                    Intent myIntent = new Intent(v.getContext(), TrumpetSearch.class);
                    myIntent.putExtra("index", userIndex);
                    myIntent.putExtra("search", "#"+hash_link);
                    startActivity(myIntent);
         		} 
        	};
    		tweetSpan.setSpan(hashSpan, hashMatch.start-1, hashMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), hashMatch.start-1, hashMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        List<Entity> userMatches = com.twitter.Extractor.extractMentionedScreennames(tweetMessage);
        for(final Entity userMatch : userMatches) {
        	ClickableSpan userSpan = new ClickableSpan() {
        		@Override
        		public void onClick(View v) {  
        	    	String screenname = userMatch.value;
        	        Intent myIntent = new Intent(v.getContext(), TrumpetProfile.class);
        	        myIntent.putExtra("screenname", screenname);
        	        v.getContext().startActivity(myIntent);
         		} 
        	};
    		tweetSpan.setSpan(userSpan, userMatch.start-1, userMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), userMatch.start-1, userMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setText(tweetSpan);
        
        LinearLayout mediaHolder = (LinearLayout) findViewById(R.id.media_holder);
        
        if(mediaUrls.size() > 0) {
	        mediaLoader.stub_id = R.drawable.media_icon_stub;
	        mediaLoader.roundPx = 10;
	        
	        for(MediaItem item : mediaUrls) {
	        	addMediaItem(item);
	        }
        } else {
        	mediaHolder.setVisibility(View.GONE);
        	//mediaHolder.setLayoutParams(new LayoutParams(1, 1));
        }
        
        String time = new SimpleDateFormat("M/d/yyyy h:mm a").format((tweet!=null) ? tweet.getCreatedAt() : direct.getCreatedAt());
        
        if(tweet!=null) {
        	TextView origin = (TextView)findViewById(R.id.origin);
        	origin.setMovementMethod(LinkMovementMethod.getInstance());
        	origin.setText(Html.fromHtml(tweet.getSource()) + " • ");
        	TextView text_time = (TextView)findViewById(R.id.time);
        	text_time.setText(time);
        }
        
        Button convoButton = (Button)findViewById(R.id.convo_button);
        //Button retweetButton = (Button)findViewById(R.id.retweet_button);
        if(tweet!=null && (tweet.getInReplyToStatusId() > 0)) {
        	if(tweet.getInReplyToStatusId() > 0) {
	        	convoButton.setVisibility(Button.VISIBLE);
	        	convoButton.setOnClickListener(new View.OnClickListener() {
	            	public void onClick(View v) {
	                    Intent myIntent = new Intent(v.getContext(), TrumpetList.class);
	                    myIntent.putExtra("type", TrumpetList.Type.CONVO);
	                    myIntent.putExtra("index", userIndex);
	                    myIntent.putExtra("status", tweet);
	                    startActivity(myIntent);
	            	}
	            });
        	}/* else if(tweet.getRetweetCount() > 0) {
        		long t = tweet.getRetweetCount();
        		retweetButton.setText("retweeted "+t+" time"+(t>1 ? "s" : ""));
        		retweetButton.setVisibility(Button.VISIBLE);
        		retweetButton.setOnClickListener(new View.OnClickListener() {
	            	public void onClick(View v) {
	                    Intent myIntent = new Intent(v.getContext(), TrumpetList.class);
	                    myIntent.putExtra("type", TrumpetList.Type.RETWEETS);
	                    myIntent.putExtra("index", userIndex);
	                    myIntent.putExtra("status", tweet);
	                    startActivity(myIntent);
	            	}
	            });
         	}*/
        } else {
        	RelativeLayout convoButtonHolder = (RelativeLayout)findViewById(R.id.convo_button_holder);
        	convoButtonHolder.setVisibility(View.GONE);
        }
        
        RelativeLayout toolbar = (RelativeLayout)findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
    	    	TwitUser profile = (tweet!=null) ? tweet.getUser() : TwitUser.create(direct.getSender());
    	    	
    	        Intent myIntent = new Intent(v.getContext(), TrumpetProfile.class);
    	        myIntent.putExtra("profile", profile);
    	        myIntent.putExtra("index", userIndex);
    	        v.getContext().startActivity(myIntent);
        	}
        });

        ImageButton btnTweet = (ImageButton)findViewById(R.id.title_tweet);
        btnTweet.setVisibility(ImageButton.VISIBLE);
        btnTweet.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if(tweet!=null) {
        	        Intent myIntent = Twr.getNewTweet(v.getContext(), userIndex);
        	        startActivity(myIntent);
        		} else {
        			Intent myIntent = Twr.getNewTweet(v.getContext(), userIndex);
        			myIntent.putExtra("isDM", true);
        	        startActivity(myIntent);
        		}
        	}
        });
        ImageButton btnReply = (ImageButton)findViewById(R.id.title_reply);
        btnReply.setVisibility(ImageButton.VISIBLE);
        btnReply.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if(tweet!=null) {
        	        Intent myIntent = Twr.getReply(v.getContext(), userIndex, tweet);
        	        startActivity(myIntent);
        		} else {
    				Intent myIntent = new Intent(v.getContext(), TrumpetSendTweet.class);
    				myIntent.putExtra("index", userIndex);
        			myIntent.putExtra("isDM", true);
        			myIntent.putExtra("dmUser", direct.getSenderScreenName());
        	        startActivity(myIntent);
        		}
        	}
        });
        
        
	}
	
	
	/** ADDS MEDIA ITEM TO THE MEDIA ITEM CONTAINER **/
	
	public void addMediaItem(MediaItem item) {
        LinearLayout mediaHolder = (LinearLayout) findViewById(R.id.media_holder);
        final float scale = getResources().getDisplayMetrics().density;

		ImageView mediaImg = new ImageView(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(72*scale), (int)(72*scale));
	    params.setMargins((int)(10*scale), 0, 0, 0);
		mediaImg.setLayoutParams(params);
	    mediaImg.setScaleType(ScaleType.FIT_XY);
	    mediaHolder.addView(mediaImg);
    	
		mediaImg.setTag(item.thumb);
		
		mediaImg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MediaItem obj = null;
				for(MediaItem i : mediaUrls) {
					if(i.thumb == v.getTag()) {
						obj = i;
						break;
					}
				}
				if(obj == null) return;
				
				if(obj.image != null) {
	    	        Intent myIntent = new Intent(v.getContext(), TrumpetImage.class);
	    	        myIntent.putExtra("image", obj.image);
	    	        v.getContext().startActivity(myIntent);
	    	        return;
				}
				if(obj.url != null) {
        			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(obj.url));
        			startActivity(browserIntent);
				}
				
			}
		});
		
		mediaLoader.DisplayImage(item.thumb, mediaImg);
    	
	}
	
	
	
	public void moveArrow(ImageView view) {
		ImageView arrow = (ImageView)findViewById(R.id.arrow);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(view.getLeft()+20, 0, 0, 0);
		arrow.setLayoutParams(lp);
	}

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(tweet!=null) {
        	inflater.inflate(R.menu.tweet, menu);
        } else {
        	inflater.inflate(R.menu.direct, menu);
        }
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.message:
				Intent dmIntent = new Intent(this, TrumpetSendTweet.class);
				dmIntent.putExtra("index", userIndex);
				dmIntent.putExtra("isDM", true);
				dmIntent.putExtra("dmUser", tweet.getUser().getScreenName());
    	        startActivity(dmIntent);
        		return true;
        	case R.id.retweet:
        	    Twr.sendRetweet(this, userIndex, tweet);
        		return true;
        	case R.id.reply_all:
				Intent replyAllIntent = Twr.getReplyAll(this, userIndex, tweet);
                startActivity(replyAllIntent);
        		return true;
        	case R.id.quote:
				Intent quoteIntent = Twr.getQuote(this, userIndex, tweet);
                startActivity(quoteIntent);
        		return true;
        	case R.id.copy:
        		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
        		clipboard.setText("@" + tweet.getUser().getScreenName() + ": " + tweet.getText());
        		return true;
 	        default:
	            return super.onOptionsItemSelected(item);
        }
    }


	@Override
	public void onDestroy() {
		mediaLoader.stopThread();
		mediaLoader.clearMemory();
		
		super.onDestroy();
	}
	
	private class MediaItem {
		public MediaItem(String t, String i, String u) {
			thumb = t;
			image = i;
			url = u;
		}
		public String thumb;
		public String image;
		public String url;
	}
	
	public TwitterListener twitAdapter = new TwitterAdapter() {
		@Override
		public void gotRetweets(final ResponseList<Status> status) {
			final LinearLayout retweet_users = (LinearLayout)findViewById(R.id.retweet_users);
						
			retweets = status;
			
			final List<Status> smretweets = status.subList(0, Math.min(status.size(), 10));
	        final float scale = getResources().getDisplayMetrics().density;
			
			for(final Status item : smretweets) {
				
				TrumpetItem.this.runOnUiThread(new Runnable() {
					public void run() {

						ImageView mediaImg = new ImageView(TrumpetItem.this);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(48*scale), (int)(48*scale));
					    params.setMargins(0, 0, (int)(8*scale), (int)(10*scale));
						mediaImg.setLayoutParams(params);
					    mediaImg.setScaleType(ScaleType.FIT_XY);
					    retweet_users.addView(mediaImg);
					    
				    	String image_url = item.getUser().getProfileImageURL().toString();
						mediaImg.setTag(image_url);
						
						mediaImg.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Status obj = null;
								for(Status i : retweets) {
									if(i.getUser().getProfileImageURL().toString().compareTo(v.getTag().toString())==0) {
										obj = i;
										break;
									}
								}
								if(obj == null) return;
								
				    	        Intent myIntent = new Intent(v.getContext(), TrumpetProfile.class);
				    	        myIntent.putExtra("profile", TwitUser.create(obj.getUser()));
				    	        myIntent.putExtra("index", userIndex);
				    	        v.getContext().startActivity(myIntent);
								
							}
						});
						
						mediaLoader.DisplayImage(image_url, mediaImg);
					}
				});
				
			}

			TrumpetItem.this.runOnUiThread(new Runnable() {
				public void run() {
		        	
					if(smretweets.size() < retweets.size()) {
						Button btn = new Button(TrumpetItem.this);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(26*scale));
						params.setMargins((int)(8*scale), (int)(8*scale), 0, 0);
						btn.setLayoutParams(params);
						btn.setTextColor(0xFF777777);
						btn.setTextSize(12);
						btn.setText(Html.fromHtml("<b>show all</b>"));
						btn.setBackgroundResource(R.drawable.btn_standard);
						btn.setOnClickListener(new View.OnClickListener() {
			            	public void onClick(View v) {
			                    Intent myIntent = new Intent(v.getContext(), TrumpetList.class);
			                    myIntent.putExtra("type", TrumpetList.Type.RETWEETS);
			                    myIntent.putExtra("index", userIndex);
			                    myIntent.putExtra("status", tweet);
			                    startActivity(myIntent);
			            	}
			            });
						retweet_users.addView(btn);
					}
					
					RelativeLayout retweets_holder = (RelativeLayout) findViewById(R.id.retweet_users_holder);
		        	retweets_holder.setVisibility(View.VISIBLE);
				}
			});

		}
	};
	
}
