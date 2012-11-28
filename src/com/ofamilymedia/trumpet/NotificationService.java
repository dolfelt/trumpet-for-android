package com.ofamilymedia.trumpet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterMethod;
import twitter4j.conf.Configuration;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.NotifyData;
import com.ofamilymedia.trumpet.classes.ObjectSerializer;
import com.ofamilymedia.trumpet.classes.Utils;
import com.twitter.Twit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;

public class NotificationService extends Service {
    private WakeLock mWakeLock;
    
    /**
     * Simply return null, since our Service will not be communicating with
     * any other components. It just does its work silently.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * This is where we initialize. We call this when onStart/onStartCommand is
     * called by the system. We won't do anything with the intent here, and you
     * probably won't, either.
     */ 
    private void handleIntent(Intent intent) {
        // obtain the wake lock 
    	PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TRUMPET_SERVICE");
        mWakeLock.acquire();
        
        // check the global background data setting 
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return;
        }
        // do the actual work, in a separate thread
        
        Bundle extras = intent.getExtras();
        long account_id = extras.getLong("accountId");
        
        Account acct = ((TrumpetBase)getApplication()).getAccount(account_id);
        
        //Toast.makeText(this, "Background Check", Toast.LENGTH_LONG).show();
        
        new PollTask(acct).execute();
        
    }
    private class PollTask extends AsyncTask<Void, Void, Void>
    {
    	//private List<Account> accounts;
        private Account account;
    	public PollTask(Account acct) {
    		account = acct;
    	}
    	
    	@SuppressWarnings("unchecked")
		@Override 
    	protected Void doInBackground(Void... params) {

            //SharedPreferences global_settings = PreferenceManager.getDefaultSharedPreferences(NotificationService.this.getBaseContext());
            
            /*try {
    			accounts = (ArrayList<Account>) ObjectSerializer.deserialize(global_settings.getString("accounts", ObjectSerializer.serialize(new ArrayList<Account>())));
    		} catch (IOException e) {
    			accounts = new ArrayList<Account>();
    			e.printStackTrace();
    		} catch (ClassNotFoundException e) {
    			accounts = new ArrayList<Account>();
    			e.printStackTrace();
    		} catch (ArrayIndexOutOfBoundsException e) {
    			accounts = new ArrayList<Account>();
    			e.printStackTrace();
    		}*/
    		
    		//for(int i = 0; i < accounts.size(); i++) {
    			Account acct = account; //accounts.get(i);
    			
    			if(acct == null) return null;
    			
    			SharedPreferences settings = getSharedPreferences(acct.getScreenName(), 0);
    			
    			long id = acct.getId();
    			NotifyData count = ((TrumpetBase)NotificationService.this.getApplication()).getNotifyData(acct);
    			
    			Configuration config = ((TrumpetBase)NotificationService.this.getApplication()).loadTwitterConfig(acct.getId());
    			Twitter twitter = new TwitterFactory(config).getInstance();
    			
    			if(settings.getBoolean("notify_tweets", false)) {
    		    	File cache = new File(NotificationService.this.getBaseContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+id+"_"+TwitterMethod.HOME_TIMELINE.name()+".txt");
    				List<twitter4j.Status> statuses;
    				List<Twit> list = Twit.fromJSONFile(cache);
    				try {
    					if(list != null && list.size() > 0)
    					{
	    					Twit recent = Collections.max(list,comparatorMax);
	    					Paging page = new Paging();
	    					page.setCount(100);
	    					page.setSinceId(recent.getId());
	    					statuses = twitter.getHomeTimeline(page);
    					} else {
    						statuses = twitter.getHomeTimeline();
    					}
    					
    					if(statuses.size() > 0) {
    						count.addTweets(statuses.size());
    						count.setReloadTweets(true);
    						
    						for(twitter4j.Status item : statuses) {
    							list.add(Twit.create(item));
							}
    						
    						Twit.toJSONFile(cache, list);
    						//Utils.ObjectToFile(cache, list);
    						
    					}
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					
    			}

    			if(settings.getBoolean("notify_mentions", true)) {
    		    	File cache = new File(NotificationService.this.getBaseContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+id+"_"+TwitterMethod.MENTIONS.name()+".txt");
    				List<twitter4j.Status> statuses;
    				List<Twit> list = Twit.fromJSONFile(cache);
    				try {
    					if(list != null && list.size() > 0)
    					{
	    					Twit recent = Collections.max(list,comparatorMax);
	    					Paging page = new Paging();
	    					page.setCount(100);
	    					page.setSinceId(recent.getId());
	    					statuses = twitter.getMentions(page);
    					} else {
    						list = new ArrayList<Twit>();
    						statuses = twitter.getMentions();
    					}
    					if(statuses.size() > 0) {
    						count.addMentions(statuses.size());
    						count.setReloadMentions(true);
    						
    						for(twitter4j.Status item : statuses) {
    							list.add(Twit.create(item));
							}
    						
    						Twit.toJSONFile(cache, list);
    						//Utils.ObjectToFile(cache, list);
    						
    					}
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					
    			}

    			if(settings.getBoolean("notify_messages", true)) {
    		    	File cache = new File(NotificationService.this.getBaseContext().getCacheDir().getAbsolutePath() + "/trumpet_cache_"+id+"_"+TwitterMethod.DIRECT_MESSAGES.name()+".txt");
    				List<twitter4j.DirectMessage> messages;
    				List<twitter4j.DirectMessage> list = (ArrayList<twitter4j.DirectMessage>)Utils.ObjectFromFile(cache);
    				try {
    					if(list != null && list.size() > 0)
    					{
    						Paging page = new Paging();
	    					page.setCount(100);
	    					if(list.size() > 0) {
	    						twitter4j.DirectMessage recent = Collections.max(list,comparatorDMax);
	    						if(recent.getId() > 0)
	    							page.setSinceId(recent.getId());
	    					}
	    					messages = twitter.getDirectMessages(page);
    					} else {
    						list = new ArrayList<twitter4j.DirectMessage>();
    						messages = twitter.getDirectMessages();
    					}
    					if(messages.size() > 0) {
    						count.addMessages(messages.size());
    						count.setReloadMessages(true);
    						
    						for(twitter4j.DirectMessage item : messages) {
    							list.add(item);
							}
    						
    						//Twit.toJSONFile(cache, list);
    						Utils.ObjectToFile(cache, list);
    						
    					}
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					
    			}
    			
    			// SAVE COUNT UPDATES
    			((TrumpetBase)NotificationService.this.getApplication()).setNotifyData(acct, count);
    			
				if(TrumpetUser.isFront == false && (count.getTweets() > 0 || count.getMentions() > 0 || count.getMessages() > 0)) {
					int icon = R.drawable.icon;        // icon from resources
					CharSequence tickerText = "@" + acct.getScreenName() + " updated...";              // ticker-text
					long when = System.currentTimeMillis();         // notification time
					Context context = getApplicationContext();      // application Context
					CharSequence contentTitle = acct.getScreenName() + " has new items";  // expanded message title
					String contentText = "";      // expanded message text
					
					if(count.getTweets() > 0) {
						contentText += count.getTweets() + " new tweet(s), ";
					}
					if(count.getMentions() > 0) {
						contentText += count.getMentions() + " new mentions(s), ";
					}
					if(count.getMessages() > 0) {
						contentText += count.getMessages() + " new messages(s), ";
					}
					
					contentText = contentText.substring(0, contentText.length()-2);
					Intent notificationIntent = new Intent(NotificationService.this, TrumpetApp.class);
					notificationIntent.putExtra("notifyAccount", acct.getId());
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, notificationIntent, 0);
					
					// the next two lines initialize the Notification, using the configurations above
					Notification notification = new Notification(icon, tickerText, when);
					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					notification.defaults |= Notification.DEFAULT_LIGHTS;
					if(settings.getBoolean("notify_vibrate", true)) {
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
					
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					
					mNotificationManager.notify("TRUMPET_" + acct.getScreenName(), 0, notification);
					/*if(Integer.valueOf(android.os.Build.VERSION.SDK) > 4)
					{
						mNotificationManager.notify("TRUMPET_BG", id, notification);
					}
					else
					{
						mNotificationManager.notify(id, notification);
					}*/
				}
    			
    		//}
    		

    		return null;
        }
        /** 
         * In here you should interpret whatever you fetched in doInBackground 
         * and push any notifications you need to the status bar, using the 
         * NotificationManager. I will not cover this here, go check the docs on
         * NotificationManager.
         *
         * What you HAVE to do is call stopSelf() after you've pushed your
         * notification(s). This will:
         * 1) Kill the service so it doesn't waste precious resources
         * 2) Call onDestroy() which will release the wake lock, so the device
         * can go to sleep again and save precious battery. 
         */ 
    	@Override 
    	protected void onPostExecute(Void result) {
            // handle your data 
    		
    		stopSelf();
        }
        
    }
    
    /**
     * This is deprecated, but you have to implement it if you're planning on
     * supporting devices with an API level lower than 5 (Android 2.0).
     */
    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
    }
    /**
     * This is called on 2.0+ (API level 5 or higher). Returning
     * START_NOT_STICKY tells the system to not restart the service if it is
     * killed because of poor resource (memory/cpu) conditions.
     */ 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }
    /**
     * In onDestroy() we release our wake lock. This ensures that whenever the
     * Service stops (killed for resources, stopSelf() called, etc.), the wake
     * lock will be released.
     */
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }
    
    public Comparator<Twit> comparatorMax = new Comparator<Twit>(){    		        		 
        public int compare(Twit o1, Twit o2) {
            return (o1.getId() > o2.getId()) ? 1 : -1;
        }
    };
    public Comparator<twitter4j.DirectMessage> comparatorDMax = new Comparator<twitter4j.DirectMessage>(){    		        		 
        public int compare(twitter4j.DirectMessage o1, twitter4j.DirectMessage o2) {
            return (o1.getId() > o2.getId()) ? 1 : -1;
        }
    };    
}