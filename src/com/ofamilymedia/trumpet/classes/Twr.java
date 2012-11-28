package com.ofamilymedia.trumpet.classes;

import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

import com.ofamilymedia.trumpet.NotificationService;
import com.ofamilymedia.trumpet.TrumpetBase;
import com.ofamilymedia.trumpet.TrumpetSendTweet;
import com.twitter.Twit;
import com.twitter.Extractor.Entity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


public class Twr {
    	
	public static void setupNotifications(Context context, Account acct) {
		SharedPreferences settings = context.getSharedPreferences(acct.getScreenName(), 0);
		
		Intent i = new Intent(context, NotificationService.class);
		i.putExtra("accountId", acct.getId());
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi); // cancel any existing alarms

		if(settings.getBoolean("enable_notifications", true) == false) {
			return;
		}
		
		long interval = Long.parseLong(settings.getString("notify_update", "15")) * 60000;
		
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
		    System.currentTimeMillis() + interval,
		    interval, pi);

	}
	
	public static Intent getQuote(Context context, int userIndex, Twit status) {
		return getReply(context, userIndex, status, false, true);
	}
	public static Intent getReplyAll(Context context, int userIndex, Twit status) {
		return getReply(context, userIndex, status, true, false);
	}
	public static Intent getReply(Context context, int userIndex, Twit status) {
		return getReply(context, userIndex, status, false, false);
	}
	public static Intent getReply(Context context, int userIndex, Twit status, Boolean replyAll, Boolean quote) {
		
		Intent myIntent = new Intent(context, TrumpetSendTweet.class);
		
		if(status.getRetweetedStatus() != null) {
			status = status.getRetweetedStatus();
		}
		
        myIntent.putExtra("index", userIndex);
        if(quote) {
        	myIntent.putExtra("text", "RT @"+status.getUser().getScreenName()+": "+status.getText());
        } else if(replyAll) {
        	Account acct = ((TrumpetBase)context.getApplicationContext()).accounts.get(userIndex);
            myIntent.putExtra("replyId", status.getId());
        	List<Entity> usernames = com.twitter.Extractor.extractMentionedScreennames(status.getText());
            StringBuilder sb = new StringBuilder();
            String delimiter = " ";
            sb.append("@" + status.getUser().getScreenName() + delimiter);
            for (Entity user : usernames) {
            	if(user.value.compareToIgnoreCase(acct.getScreenName()) != 0)
            		sb.append("@" + user.value + delimiter);
            }
         
            myIntent.putExtra("text", sb.toString());

        } else {
            myIntent.putExtra("replyId", status.getId());
        	myIntent.putExtra("text", "@"+status.getUser().getScreenName()+" ");
        }
        return myIntent;
	}
	
	public static Intent getNewTweet(Context context, int userIndex) {
		
		Intent myIntent = new Intent(context, TrumpetSendTweet.class);
		
        myIntent.putExtra("index", userIndex);
        return myIntent;
	}

	
	public static void sendRetweet(final Context context, int userIndex, Twit status) {
	    final ProgressDialog retweetDialog = new ProgressDialog(context);
	    retweetDialog.setMessage("Retweeting status...");
	    retweetDialog.show();
	    AsyncTwitter asyncTwitter = ((TrumpetBase)context.getApplicationContext()).getAsyncTwitter(userIndex, new TwitterAdapter() {
			@Override
			public void retweetedStatus(Status retweetedStatus) {
				retweetDialog.dismiss();
				Toast.makeText(context, "Status retweeted successfully!.", Toast.LENGTH_LONG).show();
			}
			@Override
	        public void onException(TwitterException e, TwitterMethod method) {
				retweetDialog.dismiss();
				Toast.makeText(context, "There was an error retweeting. Please try again.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
	        }
		});
		asyncTwitter.retweetStatus(status.getId());
	}

	
	

	
	
}