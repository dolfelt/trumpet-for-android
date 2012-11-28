package com.ofamilymedia.trumpet.controls;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import twitter4j.AsyncTwitter;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.User;

import com.ofamilymedia.trumpet.R;
import com.ofamilymedia.trumpet.TrumpetBase;
import com.ofamilymedia.trumpet.TrumpetItem;
import com.ofamilymedia.trumpet.TrumpetProfile;
import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.Ads;
import com.ofamilymedia.trumpet.classes.Colors;
import com.ofamilymedia.trumpet.classes.Twr;
import com.twitter.Twit;
import com.twitter.TwitUser;
import com.twitter.Extractor.Entity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TweetList extends ListView {
	
	//public String screenname = "";
	public int userIndex = -1;
	public Account account;
	private Context cntx;
	
	/** FOR USER_TIMELINE **/
	public String userScreenname;
	
	/** FOR CONVERSATION **/
	public long statusID = 0;
	
	/** FOR SEARCHES **/
	public String search;
	
	public TwitterMethod RequestType = TwitterMethod.HOME_TIMELINE;
	
	public Date LastLoad;
	
	public ProgressBar prog;
	
	public Boolean maxReached = false;
	public int itemsPerLoad = 50;
	public LoadMethod isLoading = null;
	
	// Cursor for loading users
	public long cursor = -1;
	
	public enum LoadMethod {
		LOAD_NEW, LOAD_OLD
	};
	
	public int UpdateDelay = 300; // seconds between requests.
	
	public EfficientAdapter eAdapter;

	public Boolean highlightLinks;
	
	private Boolean isUserList = false;
   	
	private AsyncTwitter request;
	
	public TweetList(Context context, int index) {
		super(context);
		
		cntx = context;
		
		// TODO Auto-generated constructor stub
		this.userIndex = index;
		account = ((TrumpetBase)context.getApplicationContext()).accounts.get(userIndex);
		
		eAdapter = new EfficientAdapter(context, account);
		request = ((TrumpetBase)context.getApplicationContext()).getAsyncTwitter(userIndex, listener);
		
		setAdapter(eAdapter);
		
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setBackgroundColor(0xFFFFFFFF);
		setDividerHeight(0);
		//setDivider(getResources().getDrawable(R.drawable.list_divider));
		setCacheColorHint(0x00FFFFFF);
		
		setSelector(getResources().getDrawable(R.drawable.list_background));
		
		setOnItemClickListener(onItemClick);
		setOnCreateContextMenuListener(onItemLongClick);
		setOnScrollListener(onScroll);

        SharedPreferences settings = getContext().getSharedPreferences(account.getScreenName(), 0);
        highlightLinks = settings.getBoolean("list_highlight", false);
        

		//this.setOnItemLongClickListener(onItemLongClick);
	}
	
	public void onDisplay() {
		
		long seconds = (LastLoad == null) ? -1 : (new Date().getTime() - LastLoad.getTime()) / 1000;
		if(seconds == -1 || seconds >= UpdateDelay) {
			LoadList(LoadMethod.LOAD_NEW);
		}
		
	}
	

	
	
	
	public void LoadList() {
		LoadList(LoadMethod.LOAD_NEW);
	}
	public void LoadList(LoadMethod load) {
		
		if(isLoading != null) return;
		
		isLoading = load;
		
		/** LOAD AN AD **/
		LoadAd();
		
		prog = (ProgressBar)TweetList.this.getRootView().findViewById(R.id.header_progress);
		
		if(prog != null) 
			prog.setVisibility(VISIBLE);
				
	   	/** Setup paging for requests **/
	   	Paging page = new Paging();
	   	Query query = null;
	   	if(RequestType == TwitterMethod.SEARCH)
	   		query = new Query();
	   	
	   	page.setCount(itemsPerLoad);
	   	if(load == LoadMethod.LOAD_NEW) {
	   		Twit topItem = null;
	   		if(eAdapter.getItem(0) != null && eAdapter.getItem(0).getAd() == null){
	   			topItem = eAdapter.getItem(0);
	   		} else {
	   			topItem = eAdapter.getItem(1);
	   		}
	   		if(topItem != null) {
	   			page.setSinceId(topItem.getId());
	   			if(query!=null) query.setSinceId(topItem.getId());
	   		}
	   	} else if(load == LoadMethod.LOAD_OLD && eAdapter.getLastItem() != null) {
	   		page.setMaxId(eAdapter.getLastItem().getId());
	   		if(query!=null) query.setMaxId(eAdapter.getLastItem().getId());
	   	}
	   	
		LastLoad = new Date();

	   	/** Load the correct twitter list **/
		if(RequestType == TwitterMethod.HOME_TIMELINE) {
			request.getHomeTimeline(page);
		} else if(RequestType == TwitterMethod.MENTIONS) {
			request.getMentions(page);
		} else if(RequestType == TwitterMethod.USER_TIMELINE) {
			request.getUserTimeline(userScreenname, page);
		} else if(RequestType == TwitterMethod.SHOW_STATUS) {
			if(statusID > 0) {
				request.showStatus(statusID);
			} else if(prog != null) {
				prog.setVisibility(INVISIBLE);
			}
		} else if(RequestType == TwitterMethod.SEARCH) {
			query.setQuery(search);
			request.search(query);
		} else if(RequestType == TwitterMethod.SEARCH_USERS) {
			request.searchUsers(search, 0);
		} else if(RequestType == TwitterMethod.FRIENDS_STATUSES) {
			isUserList = true;
			request.getFriendsStatuses(userScreenname, cursor);
		} else if(RequestType == TwitterMethod.FOLLOWERS_STATUSES) {
			isUserList = true;
			request.getFollowersStatuses(userScreenname, cursor);
		} else if(RequestType == TwitterMethod.RETWEETS_OF_ME) {
			request.getRetweetsOfMe(page);
		} else if(RequestType == TwitterMethod.RETWEETS) {
			request.getRetweets(statusID);
		}
		
	}
	
    TwitterListener listener = new TwitterAdapter() {

    	@Override
    	public void gotHomeTimeline(ResponseList<Status> status) {
    		updateTweetList(status);
    	}

    	@Override
    	public void gotMentions(ResponseList<Status> status) {
    		updateTweetList(status);
    	}
    	
    	@Override
    	public void gotUserTimeline(ResponseList<Status> status) {
    		updateTweetList(status);
    	}
    	
    	@Override 
    	public void gotRetweetsOfMe(ResponseList<Status> status) {
    		updateTweetList(status);
    	}
    	
    	@Override
    	public void gotRetweets(ResponseList<Status> status) {
    		updateTweetList(status);
    	}
    	
    	@Override
    	public void searched(QueryResult result) {
    		
    		List<Tweet> tweets = result.getTweets();
    		
    		updateSearchList(tweets);
    		
    	}
    	
    	@Override
    	public void searchedUser(ResponseList<User> users) {
    		updateUserList(users);
    	}
    	
    	@Override
    	public void gotFollowersStatuses(PagableResponseList<User> users) {
    		updateUserList(users);
    	}
       	@Override
    	public void gotFriendsStatuses(PagableResponseList<User> users) {
       		updateUserList(users);
    	}
    	
       	@Override
       	public void destroyedStatus(final Status destroyedStatus) {
       		TweetList.this.post(new Runnable(){ 
    			public void run() { 
    	       		eAdapter.DATA.remove(Twit.create(destroyedStatus));
    				refreshTweetList(false);
    			}
       		});
       	}
       	
    	@Override
    	public void gotShowStatus(Status status) {
    		List<Status> list = new ArrayList<Status>();
    		list.add(status);
    		updateTweetList(list);
    		statusID = status.getInReplyToStatusId();
    		if(statusID > 0) {
        		TweetList.this.post(new Runnable(){ 
        			public void run() { 
        				LoadList();
        			}
        		});
    		}
    	}
    	
    	public void updateUserList(final ResponseList<User> users) {
    		if(users instanceof PagableResponseList<?>) {
    			cursor = ((PagableResponseList<User>)users).getNextCursor();
    			if(isLoading == LoadMethod.LOAD_OLD && users.size() < itemsPerLoad-5) {
    				maxReached = true;
    			}
    		}
    		setCurrentItem();
    		for(User u : users) {
    			if(u == null) continue;
    			
    			
    			final Twit status = Twit.create(u.getStatus());
    			if(status == null) continue;
    			
    			status.setRetweetedStatus(null);
    			status.setUser(TwitUser.create(u));
    			status.setCreatedAt(u.getCreatedAt());
    			status.setText(u.getDescription());
    			
    			
        		TweetList.this.post(new Runnable(){ 
        			public void run() { 
        				eAdapter.addStatusItem(status);
        			}
        		});
        		
    		}
    		
    		TweetList.this.post(new Runnable(){ 
    			public void run() { 
    				refreshTweetList(users.size() > 0);
    			}
    		});
    		
    	}

    	public void updateTweetList(final List<Status> statuses) {
    		if(isLoading == LoadMethod.LOAD_OLD && statuses.size() < itemsPerLoad-5) {
    			maxReached = true;
    		}
    		
    		setCurrentItem();
    		TweetList.this.post(new Runnable(){ 
    			public void run() { 
		    		List<String> hashtags = new ArrayList<String>();
    				
		    		for(Status s : statuses) {
		    			Twit stat = Twit.create(s);
		    			
		    			if(RequestType == TwitterMethod.RETWEETS && stat.getRetweetedStatus()!=null) {
		    				stat.setText(stat.getRetweetedStatus().getText());
		    				stat.setRetweetedStatus(null);
		    			}
		    			
		    			if(RequestType == TwitterMethod.HOME_TIMELINE || RequestType == TwitterMethod.MENTIONS) {
		    				HashtagEntity[] entities = s.getHashtagEntities();
		    				if(entities != null) {
		    					for(HashtagEntity entity : entities) {
		    						hashtags.add("#" + entity.getText());
		    						Log.w("TAG", entity.getText());
		    					}
		    				}
		    				
		    			}
		    			
		    			eAdapter.addStatusItem(stat);
		        		
		    		}
		    		
		    		/** BUILD AUTOCOMPLETE CACHES **/
					if(RequestType == TwitterMethod.HOME_TIMELINE || RequestType == TwitterMethod.MENTIONS) {
						((TrumpetBase)cntx.getApplicationContext()).addUsernameList(account.getId(), statuses);
						((TrumpetBase)cntx.getApplicationContext()).addAutoCompleteItems(account.getId(), hashtags);
					}
		    		refreshTweetList(statuses.size() > 0);
    			}
    		});
    		
    		
    	}
    	
    	public void updateSearchList(final List<Tweet> statuses) {
    		setCurrentItem();
    		
    		TweetList.this.post(new Runnable(){ 
    			public void run() { 
		    		for(Tweet s : statuses) {
		    			eAdapter.addStatusItem(Twit.create(s));
		    		}
		    		
		    		refreshTweetList(statuses.size() > 0);   
		    	}
    		});
    		
    		 		
    	}

   		public void refreshTweetList(final Boolean setSel) {
   			
    		Boolean _sort = true;
    		if(RequestType == TwitterMethod.FOLLOWERS_STATUSES ||
        	   RequestType == TwitterMethod.FRIENDS_STATUSES) {
    			_sort = false;
    		}
    		final Boolean fsort = _sort;

			eAdapter.refreshList(fsort); 
    		
			if(prog != null) 
    			prog.setVisibility(INVISIBLE);
    		
    		if(isLoading == LoadMethod.LOAD_NEW && setSel) {
    			setSelection();
    		}
    		
    		isLoading = null;

    		if(RequestType == TwitterMethod.HOME_TIMELINE || RequestType == TwitterMethod.MENTIONS) {
    			setCache(String.valueOf(account.getId()));
    	    }
    		
    	}

 
    	@Override
    	public void tested(boolean ok) {
    		Log.w("TAG", "TESTED");
    	}

        @Override
        public void onException(TwitterException e, TwitterMethod method) {
        	e.printStackTrace();
        	refreshTweetList(false);
        }
    };

	
    
    public void setCache(String name) {
    	if(eAdapter.getCount() <= 10) return;
    	
    	SharedPreferences settings = getContext().getSharedPreferences(account.getScreenName(), 0);//.getDefaultSharedPreferences(this.getContext());
        
    	File cache = new File(getContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+name+"_"+RequestType.name()+".txt");
    	int saveNum = Integer.valueOf(settings.getString("cache_length", "50"));
        int maxSubList = Math.min(saveNum, eAdapter.getCount());
        ArrayList<Twit> writeData = new ArrayList<Twit>(eAdapter.getData().subList(0, maxSubList));
        
        Twit.toJSONFile(cache, writeData);
        
        //Utils.ObjectToFile(cache, writeData);
    }

	public void getCache() {
		getCache(String.valueOf(account.getId()), false);
	}
	public void getCache(Boolean loadListAfter) {
		getCache(String.valueOf(account.getId()), loadListAfter);
	}
	public void getCache(final String name, final Boolean loadListAfter) {
		isLoading = LoadMethod.LOAD_NEW;
		File cache = new File(getContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+name+"_"+RequestType.name()+".txt");
		
		new AsyncGetCache(loadListAfter).execute(cache);

    }
	
    public class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<Twit> DATA = new ArrayList<Twit>();
        
        private Context context;
        private Account acct;
        
        //private BitmapDrawable mentionBitmap = null;
        
        public Twit currentItem = null;
        public EfficientAdapter(Context c, Account acct) {
        	this.acct = acct;
        	context = c;
        	
            mInflater = LayoutInflater.from(c);
        }
        public EfficientAdapter(Context c) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
        	context = c;
        	
            mInflater = LayoutInflater.from(c);
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return DATA.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Twit getItem(int position) {
        	if(DATA.size() > position)
        		return DATA.get(position);
        	else
        		return null;
        }
        
        public int getItemPosition(Twit item) {
        	return DATA.indexOf(item);
        }
        
        public Twit getLastItem() {
        	if(DATA.size() > 0)
        		return DATA.get(DATA.size()-1);
        	else
        		return null;
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            final ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.tweet_list_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.screenname = (TextView) convertView.findViewById(R.id.screenname);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.more_info = (TextView) convertView.findViewById(R.id.more_info);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.corner = (ImageView) convertView.findViewById(R.id.corner);
                holder.has_media = (ImageView) convertView.findViewById(R.id.has_media);
                holder.has_location = (ImageView) convertView.findViewById(R.id.has_location);
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            Twit info = DATA.get(position);
            
            Colors.Gradient userColor;
            if(info.getAd() != null) {
            	userColor = Colors.getGradient("ads");
            } else {
	            if((userColor = acct.getUserColor(info.getUser().getScreenName())) == null) {
	            	userColor = Colors.getGradient("white");
	            }
            }
        	int statePressed = android.R.attr.state_pressed;
        	StateListDrawable bg = new StateListDrawable();
        	Drawable normalDrawable = null;
    		
        	int gcolors[] = {userColor.start, userColor.start, userColor.end};
    		GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gcolors);
        	//gradient.setDither(true);
    		if(RequestType == TwitterMethod.MENTIONS) {
    			normalDrawable = gradient;
    		} else {
	    		Matcher menMatch = Pattern.compile(Pattern.quote("@"+account.getScreenName()), Pattern.CASE_INSENSITIVE).matcher(info.getText());
	    		if(info.getText().length()>0 && menMatch != null && menMatch.find()) {
	        		// Has a mention
	        		BitmapDrawable hashBit = (BitmapDrawable) getResources().getDrawable(R.drawable.hash_background);
	        		hashBit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
	        		normalDrawable = new LayerDrawable(new Drawable[]{gradient, hashBit});
	    		} else {
	        		normalDrawable = gradient;
	        	}
    		}
        	normalDrawable.setDither(true);
        	bg.addState(new int[]{statePressed}, context.getResources().getDrawable(R.drawable.list_background_pressed));
        	bg.addState(new int[]{-statePressed}, normalDrawable);
        	convertView.setBackgroundDrawable(bg);
            /*} else {
            	convertView.setBackgroundResource(R.drawable.list_background);
            }*/
            
            Twit original_status = null;
            
            holder.more_info.setVisibility(GONE);
        	holder.corner.setVisibility(GONE);
       	
            if(info.getRetweetedStatus() != null) {
            	holder.corner.setVisibility(VISIBLE);
            	holder.corner.setImageDrawable(context.getResources().getDrawable(R.drawable.list_corner_retweet));
            	holder.more_info.setVisibility(TextView.VISIBLE);
            	holder.more_info.setText("Retweeted by " + info.getUser().getScreenName());
            	original_status = info;
            	info = info.getRetweetedStatus();
            } else if(info.getInReplyToStatusId() > 0) {
            	holder.corner.setVisibility(VISIBLE);
            	holder.corner.setImageDrawable(context.getResources().getDrawable(R.drawable.list_corner_convo));
            }
            
            holder.icon.setImageBitmap(null);
            
            if(info.getUser().getProfileImageURL() != null) {
	            String icon_url = info.getUser().getProfileImageURL().toString();
	            icon_url = icon_url.replace("_normal.", "_bigger.");
	            
	            holder.icon.setTag(icon_url);
	            
	            if(info.getAd() == null) {
		            ((TrumpetBase)context.getApplicationContext()).imageLoader.DisplayImage(icon_url, holder.icon);
		        } else {
		        	((TrumpetBase)context.getApplicationContext()).imageLoader.DisplayImage(icon_url, holder.icon, false);
		        }
           }

            holder.screenname.setText(info.getUser().getScreenName());
            
            String tweetMessage = info.getText();
            
            
            Boolean mediaExists = false;
            
            int linkColor = 0xFF4f7800;
            
            if(tweetMessage!=null && tweetMessage.length() > 0) {
            
	            Spannable tweetSpan = new SpannableString(tweetMessage);
	            
	            List<Entity> urlMatches = com.twitter.Extractor.extractURLs(tweetMessage);
	            for(Entity urlMatch : urlMatches) {
	            	if(urlMatch.value.matches("(?i).*(yfrog.com|twitpic.com|plixi.com|tweetphoto.com).*"))
	            	{
	            		mediaExists = true;
	            	}
	        		if(highlightLinks) {
	        			tweetSpan.setSpan(new ForegroundColorSpan(linkColor), urlMatch.start, urlMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        		}
	            }
	
	            
	            if(highlightLinks) {
		            List<Entity> hashMatches = com.twitter.Extractor.extractHashtags(tweetMessage);
		            
		            for(Entity hashMatch : hashMatches) {
		        		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), hashMatch.start-1, hashMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            }
		
		            List<Entity> userMatches = com.twitter.Extractor.extractMentionedScreennames(tweetMessage);
		            for(Entity userMatch : userMatches) {
		        		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), userMatch.start-1, userMatch.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            }
	            }
	
	            
	            holder.text.setText(tweetSpan);
	            
            }
            
            Date tweetTime;
            if(original_status == null) {
            	tweetTime = info.getCreatedAt();
            } else {
            	tweetTime = original_status.getCreatedAt();
            }
            
            if(info.getAd() == null) {
            	String time;
	        	long diff = (new Date().getTime() - tweetTime.getTime()) / (1000);
	        	
	        	if(diff <= 36*3600) {
	        		if(diff / 60 >= 59) {
	        			time = Integer.toString(Math.round(diff/3600)) + "h";
	        		} else {
	        			time = Integer.toString(Math.round(diff/60)) + "m";
	        		}
	        	} else if(diff <= 5*24*3600) {
	        		time = Integer.toString(Math.round(diff/(24*3600))) + "d";
	        	} else {
	        		time = new SimpleDateFormat("M.d.yyyy").format(info.getCreatedAt());
	        	}
	            holder.time.setText(time);
            } else {
                holder.time.setText(Html.fromHtml(info.getSource()));
            }
            

            if(mediaExists) {
            	holder.has_media.setVisibility(VISIBLE);
            } else {
            	holder.has_media.setVisibility(GONE);
            }
            
            if(info.getGeoLocation() instanceof GeoLocation) {
            	holder.has_location.setVisibility(VISIBLE);
            } else {
            	holder.has_location.setVisibility(GONE);
            }
            
            if(info.getAd() != null) {
            	new Ads().ImpressionMade(info.getAd().getImpressionURL());
            }
            

            return convertView;
        }
        
        public void addStatusItem(Twit data) {
            if(DATA.contains(data)) {
            	DATA.remove(data);
            }
            DATA.add(data);
        }
        
        public void refreshList() {
        	refreshList(true);
        }
        
        public void refreshList(Boolean sort) {
        	notifyDataSetChanged();
        	
        	if(sort) {
	        	Collections.sort(DATA, new Comparator<Twit>(){
	        		 
	                public int compare(Twit o1, Twit o2) {
	                   return (o1.getId() > o2.getId()) ? -1 : 1;
	                }
	     
	            });
        	}
        	
        	//notifyDataSetChanged();
        	
        }
        
        public void clearList() {
        	DATA.clear();
        }
        
        public ArrayList<Twit> getData() {
        	return DATA;
        }

        class ViewHolder {
            TextView screenname;
            TextView text;
            TextView time;
            TextView more_info;
            ImageView icon;
            ImageView corner;
            ImageView has_media;
            ImageView has_location;
        }
    }
    
    OnScrollListener onScroll = new OnScrollListener() {
    	
    	@Override
        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
            boolean loadMore = firstVisible + visibleCount >= totalCount;

            if(totalCount > 10 && loadMore && maxReached == false) {
                LoadList(LoadMethod.LOAD_OLD);
            }
        }
    	
    	@Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    };
    
    
    OnItemClickListener onItemClick = new OnItemClickListener() {
    	
    	
    	public void onItemClick(AdapterView<?> adapter, View  view, int position, long id) {
    	
	    	Twit tweet = eAdapter.DATA.get(position);
	    	
	    	if(!isUserList) {
		        Intent myIntent = new Intent(view.getContext(), TrumpetItem.class);
		        myIntent.putExtra("tweet", tweet);
		        myIntent.putExtra("index", userIndex);
		        view.getContext().startActivity(myIntent);
	    	} else {
    	        Intent myIntent = new Intent(TweetList.this.getContext(), TrumpetProfile.class);
    	        myIntent.putExtra("profile", tweet.getUser());
    	        myIntent.putExtra("index", userIndex);
    	        TweetList.this.getContext().startActivity(myIntent);
	    	}
    	}
    	
    };

    OnCreateContextMenuListener onItemLongClick = new OnCreateContextMenuListener() {
    	
    	
    	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		if(!isUserList) {
    			
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
				Twit status = eAdapter.getItem(info.position);
    			
        		menu.setHeaderTitle("Tweet Options");
	    		MenuItem menu_reply = menu.add("Reply");
	    		menu_reply.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    			@Override
	    			public boolean onMenuItemClick(MenuItem item) {
	    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    				Twit status = eAdapter.getItem(info.position);
	    				Intent myIntent = Twr.getReply(TweetList.this.getContext(), userIndex, status);
	                    TweetList.this.getContext().startActivity(myIntent);
	    				return true;
	    			}
	    		});
	
	    		MenuItem menu_retweet = menu.add("Retweet");
	    		menu_retweet.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    			@Override
	    			public boolean onMenuItemClick(MenuItem item) {
	    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    				Twit status = eAdapter.getItem(info.position);
	    				Twr.sendRetweet(TweetList.this.getContext(), userIndex, status);
	    				return true;
	    			}
	    		});
	    		
	    		MenuItem menu_reply_all = menu.add("Reply All");
	    		menu_reply_all.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    			@Override
	    			public boolean onMenuItemClick(MenuItem item) {
	    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    				Twit status = eAdapter.getItem(info.position);
	    				Intent myIntent = Twr.getReplyAll(TweetList.this.getContext(), userIndex, status);
	                    TweetList.this.getContext().startActivity(myIntent);
	    				return true;
	    			}
	    		});
	
	    		MenuItem menu_quote = menu.add("Quote...");
	    		menu_quote.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	    			@Override
	    			public boolean onMenuItemClick(MenuItem item) {
	    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    				Twit status = eAdapter.getItem(info.position);
	    				Intent myIntent = Twr.getQuote(TweetList.this.getContext(), userIndex, status);
	                    TweetList.this.getContext().startActivity(myIntent);
	    				return true;
	    			}
	    		});
	    		
	    		if(status.getUser().getScreenName().equalsIgnoreCase(account.getScreenName())) {
		    		MenuItem menu_delete = menu.add("Delete");
		    		menu_delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		    			@Override
		    			public boolean onMenuItemClick(final MenuItem item) {
		    				
		    				new AlertDialog.Builder(TweetList.this.getContext())
		    		        .setIcon(android.R.drawable.ic_dialog_alert)
		    		        .setTitle("Delete")
		    		        .setMessage("Are you sure you want to delete this item?")
		    		        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		        	@Override
		    		            public void onClick(DialogInterface dialog, int which) {
				    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				    				Twit status = eAdapter.getItem(info.position);
				    				
				    				//AsyncTwitter request = TrumpetBase.getAsyncTwitter(userIndex, listener);
				    				prog.setVisibility(VISIBLE);
				    				request.destroyStatus(status.getId());
		    		            }
		    		        })
		    		        .setNegativeButton("No", null)
		    		        .show();
		    				
		    				return true;
		    			}
		    		});
	    		}
	    		
    		} else {
        		menu.setHeaderTitle("User Options");
    		}
    		MenuItem menu_profile = menu.add("View Profile");
    		menu_profile.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    				Twit status = eAdapter.getItem(info.position);
    				TwitUser profile;
    				if(status.getRetweetedStatus() == null) {
    					profile = status.getUser();
    				} else {
    					profile = status.getRetweetedStatus().getUser();
    				}
        	    	
        	        Intent myIntent = new Intent(TweetList.this.getContext(), TrumpetProfile.class);
        	        myIntent.putExtra("profile", profile);
        	        myIntent.putExtra("index", userIndex);
        	        TweetList.this.getContext().startActivity(myIntent);
    				return true;
    			}
    		});

    	
    	}
    	
    };
    
    
    
    private Twit current_item = null;
    
    public Twit getCurrentListItem() {
    	if(eAdapter.getCount() > 0) {
    		Twit item = eAdapter.getItem(getFirstVisiblePosition());
    		if(item.getAd() != null && eAdapter.getCount() > 1) {
    			return eAdapter.getItem(getFirstVisiblePosition()+1);
    		} else {
    			return item;
    		}
    	}
    	return null;
    }
    
    public void setCurrentItem() {
    	Twit item = getCurrentListItem();
    	if(item != null) {
    		setCurrentItem(item);
    	}
    }
    public void setCurrentItem(Twit value) {
    	current_item = value;
    }
    public Twit getCurrentItem() {
    	return current_item;
    }
    
    public void setSelection() {
    	if(getCurrentItem() == null) return;
    	int position = eAdapter.getItemPosition(getCurrentItem());
    	if(position < 0) position = eAdapter.getCount()-1;
    	setSelection(position);
    }
    
    
    private class AsyncGetCache extends AsyncTask<File, Void, List<Twit>> {
        public Boolean loadListAfter = false;
    	public AsyncGetCache(Boolean loadList) {
    		this.loadListAfter = loadList;
    	}
        protected List<Twit> doInBackground(File... files) {
        	return Twit.fromJSONFile(files[0]);
        }

        protected void onPostExecute(List<Twit> data) {
			
   			if(eAdapter.getCount() > 0) {
   				setCurrentItem();
   			}
			
			for(Twit s : data) {
   				eAdapter.addStatusItem(s);
   				
   			}
   			
			eAdapter.refreshList();
			
			if(getCurrentItem()!=null && eAdapter.DATA.contains(getCurrentItem())) {
    			setSelection(eAdapter.getItemPosition(getCurrentItem()));
    		}
			
			isLoading = null;
			
			if(loadListAfter) {
				onDisplay();
			}
		
        }
    }
    
    public void destroy() {
    	
    	//eAdapter.imageLoader.clearCache();
    	setAdapter(null);
    	try {
    		request.shutdown();
    	} catch(IllegalStateException e) {
    		e.printStackTrace();
    	}
    }
    
    
    
    /** AD FUNCTIONS **/
    public Date lastAdLoad;
    public Boolean paidCustomer = true;
    public void LoadAd() {
    	
    	if(paidCustomer)
    		return;
    	
    	if(RequestType != TwitterMethod.HOME_TIMELINE) return;
    	
		long seconds = (LastLoad == null) ? -1 : (new Date().getTime() - LastLoad.getTime()) / 1000;
		if(seconds == -1 || seconds >= 600) {
			Ads ads = new Ads();
			ads.InsertAd(account.getScreenName(), this);
		}
    	
    }

}
