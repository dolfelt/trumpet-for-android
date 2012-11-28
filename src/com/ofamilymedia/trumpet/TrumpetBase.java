package com.ofamilymedia.trumpet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterListener;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.CustomExceptionHandler;
import com.ofamilymedia.trumpet.classes.ImageLoader;
import com.ofamilymedia.trumpet.classes.ObjectSerializer;
import com.ofamilymedia.trumpet.classes.TweetAutoList;
import com.ofamilymedia.trumpet.classes.Twr;
import com.ofamilymedia.trumpet.classes.NotifyData;
import com.twitter.Twit;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TrumpetBase extends Application {
    /** Called when the activity is first created. */
    
	public static String CONSUMER_KEY = "5EGyg0wvXJ6dXp3LoJz2ZQ";
    public static String CONSUMER_SECRET = "1aHHHkxNfuL4oLjfBaTd7vyIeCYSloHzLQr9A2TtU";
 
	public ArrayList<Account> accounts;
	//public static HashMap<Integer, List<String>> usernameStore = new HashMap<Integer, List<String>>();
	//public static HashMap<Integer, List<String>> hashtagStore = new HashMap<Integer, List<String>>();
	
	public TweetAutoList tweetAutoList = null;
	
	public ImageLoader imageLoader;
	
	//public static ViewFlipper container;
	
	//private static HashMap<Integer, NotifyData> notifyCount = new HashMap<Integer, NotifyData>();
		
	public static int defaultAccount = -1;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler("/sdcard/data/trumpet", "http://ofamilymedia.com/trumpet/stacktrace.php"));
        
        imageLoader = new ImageLoader(this);
        
        System.setProperty("twitter4j.oauth.consumerKey",TrumpetBase.CONSUMER_KEY); 
        System.setProperty("twitter4j.oauth.consumerSecret",TrumpetBase.CONSUMER_SECRET); 
        System.setProperty("twitter4j.debug", "true");        
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        /** DO UPDATE STUFF **
        int savedVersion = settings.getInt("current_version", 0);
        int versionCode;
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
	        if(savedVersion < versionCode) {
	        	
	        }
	        settings.edit().putInt("current_version", versionCode).commit();
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		} **/
        
        try {
        	accounts = (ArrayList<Account>) ObjectSerializer.deserialize(settings.getString("accounts", ObjectSerializer.serialize(new ArrayList<Account>())));
		} catch (IOException e) {
			accounts = new ArrayList<Account>();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			accounts = new ArrayList<Account>();
			e.printStackTrace();
		} catch (ClassCastException e) {
			accounts = new ArrayList<Account>();
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			accounts = new ArrayList<Account>();
			e.printStackTrace();
		}

        defaultAccount = settings.getInt("defaultAccount", -1);
        
        for(int i=0; i<accounts.size(); i++)
        	Twr.setupNotifications(this, accounts.get(i));
        
        tweetAutoList = new TweetAutoList(this);
	}
    
    @Override
    public void onTerminate() {
    	//container = null;
    	accounts = null;
    	imageLoader.stopThread();
    	
    	super.onTerminate();
    }
    
	
	public AccessToken loadAccessToken(int index) {
		Account acct = accounts.get(index);
		return new AccessToken(acct.getToken(), acct.getSecret());
	}
	
	public Account getAccount(long id)
	{
		for(Account a : accounts) {
			if(a.getId() == id)
				return a;
		}
		return null;
	}
	public Account getAccount(String screenname)
	{
		for(Account a : accounts) {
			if(a.getScreenName().equalsIgnoreCase(screenname))
				return a;
		}
		return null;
	}	
	public Configuration loadTwitterConfig(int index) {
		Account acct = accounts.get(index);
		return loadTwitterConfig(acct);
	}
	public Configuration loadTwitterConfig(long id) {
		return loadTwitterConfig(getAccount(id));
	}
	public Configuration loadTwitterConfig(String screenname) {
		return loadTwitterConfig(getAccount(screenname));
	}
	public Configuration loadTwitterConfig(Account acct) {
		ConfigurationBuilder config = new ConfigurationBuilder();
		config.setOAuthAccessToken(acct.getToken());
		config.setOAuthAccessTokenSecret(acct.getSecret());
		config.setIncludeEntitiesEnabled(true);
		
		return config.build();
	}
	
	public AsyncTwitter getAsyncTwitter(int index, TwitterListener listener) {
		Configuration config = loadTwitterConfig(index);
		AsyncTwitterFactory factory = new AsyncTwitterFactory(config);
		AsyncTwitter asyncTwitter = factory.getInstance(); //getOAuthAuthorizedInstance(accessToken);
		asyncTwitter.addListener(listener);
		return asyncTwitter;
	}
	
	public String[] getUsernameList(long id) {
		String[] output = new String[] {};
		
		output = tweetAutoList.selectAll(id).toArray(output);
		
		return output;
	}
	//public void addUsernameList(long id, String username) {
		//tweetAutoList.insert(id, username);
	//}
	
	public void addUsernameList(long id, List<?> list) {
		tweetAutoList.beginTransaction();
		for(Object item : list) {
			if(item instanceof Twit) {
				tweetAutoList.insert(id, "@"+((Twit)item).getUser().getScreenName());
			} else if(item instanceof Status) {
				tweetAutoList.insert(id, "@"+((Status)item).getUser().getScreenName());
			} else if(item instanceof User) {
				tweetAutoList.insert(id, "@"+((User)item).getScreenName());
			}
		}
		tweetAutoList.endTransaction();
	}
	
	public void addAutoCompleteItems(long id, List<String> list) {
		tweetAutoList.beginTransaction();
		for(String item : list) {
			tweetAutoList.insert(id, item);
		}
		tweetAutoList.endTransaction();
	}
	
	public void SaveAccounts() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
      	SharedPreferences.Editor editor = settings.edit();
		try {
			editor.putString("accounts", ObjectSerializer.serialize(accounts));
		} catch (IOException e) {
			e.printStackTrace();
		}

		editor.commit();

	}
    

	
	@SuppressWarnings("unchecked")
	/*private HashMap<Long, NotifyData> getNotifyData() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			return (HashMap<Long, NotifyData>)ObjectSerializer.deserialize(settings.getString("notifyData", ObjectSerializer.serialize(new HashMap<Long, NotifyData>())));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new HashMap<Long, NotifyData>();
	}*/
	
	public NotifyData getNotifyData(Account account) {		
		return getNotifyData(account.getScreenName());
	}
	public NotifyData getNotifyData(String screenname) {
		SharedPreferences settings = this.getSharedPreferences(screenname, MODE_PRIVATE);
		try {
			return (NotifyData) ObjectSerializer.deserialize(settings.getString("notifyData", ObjectSerializer.serialize(new NotifyData())));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void setNotifyData(Account account, NotifyData data) {
		setNotifyData(account.getScreenName(), data);
	}
	public void setNotifyData(String screenname, NotifyData data) {
		SharedPreferences.Editor editor = getSharedPreferences(screenname, MODE_PRIVATE).edit();
        try {
			editor.putString("notifyData", ObjectSerializer.serialize(data));
	        editor.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}