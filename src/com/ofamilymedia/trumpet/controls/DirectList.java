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
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.DirectMessage;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.R;
import com.ofamilymedia.trumpet.TrumpetBase;
import com.ofamilymedia.trumpet.TrumpetItem;
import com.ofamilymedia.trumpet.TrumpetProfile;
import com.ofamilymedia.trumpet.TrumpetSendTweet;
import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.Utils;
import com.twitter.TwitUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DirectList extends ListView {
	
	//public String screenname = "";
	public int userIndex = -1;
	public Account account;
	
	/** FOR USER_TIMELINE **/
	public String userScreenname;
	
	public TwitterMethod RequestType = TwitterMethod.DIRECT_MESSAGES;
	
	public Date LastLoad;

	public ProgressBar prog;

	public enum LoadMethod {
		LOAD_NEW, LOAD_OLD
	}
	
	public int UpdateDelay = 240; // seconds between requests.
	
	public EfficientAdapter eAdapter;
	
	public Boolean highlightLinks;
	private AsyncTwitter request;

	public DirectList(Context context, int userIndex) {
		super(context);
		// TODO Auto-generated constructor stub
		this.userIndex = userIndex;
		
		account = ((TrumpetBase)context.getApplicationContext()).accounts.get(userIndex);
		
		eAdapter = new EfficientAdapter(context);
	   	request = ((TrumpetBase)context.getApplicationContext()).getAsyncTwitter(userIndex, listener);
		
		setAdapter(eAdapter);
		
		setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setBackgroundColor(0xFFFFFFFF);
		setDividerHeight(0);
		setCacheColorHint(0xFFFFFFFF);
		
		setSelector(getResources().getDrawable(R.drawable.list_background));
		
		setOnItemClickListener(onItemClick);
		setOnCreateContextMenuListener(onItemLongClick);
		
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
		
		
		prog = (ProgressBar)this.getRootView().findViewById(R.id.header_progress);
		Log.w("PROG", prog.toString());
		if(prog != null) 
			prog.setVisibility(VISIBLE);
		
		
	   	/** Setup paging for requests **/
	   	Paging page = new Paging();
	   	Paging page_sent = new Paging();
	   	page.setCount(75);
	   	page_sent.setCount(75);
	   	if(load == LoadMethod.LOAD_NEW) {
	   		if(eAdapter.getItem(0) != null)
	   		{
	   			if(eAdapter.userDMIds.size() > 0)
	   				page.setSinceId(Collections.max(eAdapter.userDMIds));
	   			if(eAdapter.myDMIds.size() > 0)
	   				page_sent.setSinceId(Collections.max(eAdapter.myDMIds));
	   		}
	   	} else if(load == LoadMethod.LOAD_OLD && eAdapter.getLastItem() != null) {
	   		if(eAdapter.getLastItem().getId() > 0) {
	   			if(eAdapter.userDMIds.size() > 0)
	   				page.setMaxId(Collections.min(eAdapter.userDMIds));
	   			if(eAdapter.myDMIds.size() > 0)
	   				page_sent.setMaxId(Collections.min(eAdapter.myDMIds));
	   		}
	   	}
	   	
		LastLoad = new Date();

	   	/** Load the correct twitter list **/
		
		request.getDirectMessages(page);
		request.getSentDirectMessages(page_sent);
	}
	
    TwitterListener listener = new TwitterAdapter() {
    	
    	@Override
    	public void gotDirectMessages(ResponseList<DirectMessage> messages) {
    		
    		updateTweetList(messages);

    	}
    	
    	@Override
    	public void gotSentDirectMessages(ResponseList<DirectMessage> messages) {
    		
    		updateTweetList(messages);

    	}
   	
    	

    	public void updateTweetList(ResponseList<DirectMessage> messages) {
    		
    		
    		final DirectMessage currentItem = eAdapter.getItem(getFirstVisiblePosition());
    		for(DirectMessage s : messages) {
    			eAdapter.addStatusItem(s, account.getId()==s.getSenderId());
    		}
    		
    		DirectList.this.post(new Runnable(){ 
    			public void run() { 
    				eAdapter.refreshList(); 
    	    		if(prog != null) 
    	    			prog.setVisibility(INVISIBLE);
    	    		setSelection(eAdapter.getItemPosition(currentItem));
    			}
    		});

    		setCache(String.valueOf(account.getId()));
    	}

        @Override
        public void onException(TwitterException e, TwitterMethod method) {
        	e.printStackTrace();
        	Log.w("TAG", "EXCEPTION");
        }
    };

	
    
    public void setCache(String name) {
    	File cache = new File(getContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+name+"_"+RequestType.name()+".txt");
    	
        int maxSubList = Math.min(50, eAdapter.getCount());
        ArrayList<DirectMessage> writeData = new ArrayList<DirectMessage>(eAdapter.getData().subList(0, maxSubList));

        Utils.ObjectToFile(cache, writeData);

    }

	public void getCache() {
		getCache(String.valueOf(account.getId()));
	}
	@SuppressWarnings("unchecked")
	public void getCache(String name) {
    	File cache = new File(getContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+name+"_"+RequestType.name()+".txt");
    	ArrayList<DirectMessage> data = (ArrayList<DirectMessage>) Utils.ObjectFromFile(cache);
    	
    	if(data != null) {
   			for(DirectMessage s : data) {
   				eAdapter.addStatusItem(s, account.getId()==s.getSenderId());
   			}
   			
   			eAdapter.refreshList();
    	}

    }


	
    public class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<DirectMessage> DATA = new ArrayList<DirectMessage>();
        
        private List<Long> userDMIds = new ArrayList<Long>();
        private List<Long> myDMIds = new ArrayList<Long>();
        
        private Context context;
        
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
        public DirectMessage getItem(int position) {
        	if(DATA.size() > position)
        		return DATA.get(position);
        	else
        		return null;
        }
        
        public int getItemPosition(DirectMessage item) {
        	return DATA.indexOf(item);
        }
        
        public DirectMessage getLastItem() {
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
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            
            // Bind the data efficiently with the holder.
            DirectMessage info = DATA.get(position);
            
            
            holder.more_info.setVisibility(GONE);
        	holder.corner.setVisibility(GONE);
       	
            holder.icon.setImageBitmap(null);
            
            //imageManager.fetchDrawableOnThread(info.getUser().getProfileImageURL().toString(), holder.icon);
            String icon_url = info.getSender().getProfileImageURL().toString();
            icon_url = icon_url.replace("_normal.", "_bigger.");
            
            holder.icon.setTag(icon_url);
            ((TrumpetBase)context.getApplicationContext()).imageLoader.DisplayImage(icon_url, holder.icon);


            holder.screenname.setText(info.getSender().getScreenName());
            
            String tweetMessage = info.getText();
            
            
            Spannable tweetSpan = new SpannableString(tweetMessage);
            
            Boolean mediaExists = false;
            
            int linkColor = 0xFF4f7800;
            
            Pattern urlPattern = Pattern.compile("http://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
            Matcher urlMatch = urlPattern.matcher(tweetMessage);
            while(urlMatch.find()) {
            	if(urlMatch.group().matches("(?i).*(yfrog.com|twitpic.com).*"))
            	{
            		mediaExists = true;
            	}
        		if(highlightLinks) tweetSpan.setSpan(new ForegroundColorSpan(linkColor), urlMatch.start(), urlMatch.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            if(highlightLinks) {
	            Pattern hashPattern = Pattern.compile("(\\s|\\A)#(\\w+)");
	            Matcher hashMatch = hashPattern.matcher(tweetMessage);
	            while(hashMatch.find()) {
	        		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), hashMatch.start(), hashMatch.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	
	            Pattern userPattern = Pattern.compile("@(\\w+)");
	            Matcher userMatch = userPattern.matcher(tweetMessage);
	            while(userMatch.find()) {
	        		tweetSpan.setSpan(new ForegroundColorSpan(linkColor), userMatch.start(), userMatch.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
            }

            
            holder.text.setText(tweetSpan);
            
        	long diff = (new Date().getTime() - info.getCreatedAt().getTime()) / (1000);
        	
        	String time;
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

            if(mediaExists) {
            	holder.has_media.setVisibility(VISIBLE);
            } else {
            	holder.has_media.setVisibility(GONE);
            }

            return convertView;
        }
        
        public void addStatusItem(DirectMessage data, Boolean currentUser) {
            if(!DATA.contains(data))
            {
            	DATA.add(data);
            }
            if(currentUser) {
            	myDMIds.add(data.getSenderId());
            } else {
            	userDMIds.add(data.getSenderId());
            }
        }
        
        public void refreshList() {
        	
        	Collections.sort(DATA, new Comparator<DirectMessage>(){
        		 
                public int compare(DirectMessage o1, DirectMessage o2) {
                   return (o1.getId() > o2.getId()) ? -1 : 1;
                }
     
            });
        	
        	notifyDataSetChanged();
        }
        
        public void clearList() {
        	DATA.clear();
        }
        
        public ArrayList<DirectMessage> getData() {
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
        }
    }
    
    
    OnItemClickListener onItemClick = new OnItemClickListener() {
    	
    	
    	public void onItemClick(AdapterView<?> adapter, View  view, int position, long id) {
    	
    		DirectMessage tweet = eAdapter.DATA.get(position);
	    	
	        Intent myIntent = new Intent(view.getContext(), TrumpetItem.class);
	        myIntent.putExtra("direct", tweet);
	        myIntent.putExtra("index", userIndex);
	        view.getContext().startActivity(myIntent);
	        
    	}
    	
    };

    OnCreateContextMenuListener onItemLongClick = new OnCreateContextMenuListener() {
    	
    	
    	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		  //super.onCreateContextMenu(menu, v, menuInfo);
    		//Activity parent = (Activity)v.getContext();
    		//  MenuInflater inflater = parent.getMenuInflater();
    		//  inflater.inflate(R.menu.tweet_context, menu);
    		MenuItem menu_reply = menu.add("Reply");
    		menu_reply.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    				DirectMessage direct = eAdapter.getItem(info.position);
    				
    				Intent myIntent = new Intent(DirectList.this.getContext(), TrumpetSendTweet.class);
    				myIntent.putExtra("index", userIndex);
        			myIntent.putExtra("isDM", true);
        			myIntent.putExtra("dmUser", direct.getSenderScreenName());
        			DirectList.this.getContext().startActivity(myIntent);
    				return true;
    			}
    		});

    		MenuItem menu_profile = menu.add("View Profile");
    		menu_profile.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    				DirectMessage direct = eAdapter.getItem(info.position);
    				TwitUser profile = TwitUser.create(direct.getSender());
        	    	
        	        Intent myIntent = new Intent(DirectList.this.getContext(), TrumpetProfile.class);
        	        myIntent.putExtra("profile", profile);
        	        myIntent.putExtra("index", userIndex);
        	        DirectList.this.getContext().startActivity(myIntent);
    				return true;
  			}
    		});

    	
    	}
    	
    };

    
    
    private DirectMessage current_item = null;
    public void setCurrentItem() {
    	setCurrentItem(eAdapter.getItem(getFirstVisiblePosition()));
    }
    public void setCurrentItem(DirectMessage value) {
    	current_item = value;
    }
    
    public DirectMessage getCurrentItem() {
    	return current_item;
    }
    
    public void setSelection() {
    	if(getCurrentItem() == null) return;
    	int position = eAdapter.getItemPosition(getCurrentItem());
    	setSelection(position);
    }
    
    
    public void destroy() {
    	setAdapter(null);
    	request.shutdown();
    }

}
