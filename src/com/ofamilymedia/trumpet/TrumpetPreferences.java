package com.ofamilymedia.trumpet;

import com.ofamilymedia.trumpet.classes.Account;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TrumpetPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	final static String SHORTENING_SERVICE = "shortening_service";
	final static String IMAGE_UPLOAD = "image_upload";
	final static String IMAGE_UPLOAD_ENDPOINT = "image_upload_endpoint";
	final static String VIDEO_UPLOAD = "video_upload";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        Account account = (Account)extras.getSerializable("account");
        
        getPreferenceManager().setSharedPreferencesName(account.getScreenName());
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        
        onSharedPreferenceChanged(prefs, SHORTENING_SERVICE);
        onSharedPreferenceChanged(prefs, IMAGE_UPLOAD);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(SHORTENING_SERVICE)) {
        	String selected = prefs.getString(key, "bitly");
            if (selected.equals("bitly") || selected.equals("jmp")) {
                findPreference("bitly_username").setEnabled(true);
                findPreference("bitly_key").setEnabled(true);
            } else {
                findPreference("bitly_username").setEnabled(false);
                findPreference("bitly_key").setEnabled(false);
            }
        } else if(key.equals(IMAGE_UPLOAD)) {
        	String selected = prefs.getString(key, "yFrog");
        	findPreference(key).setSummary(selected);
        	if(selected.equalsIgnoreCase("custom")) {
        		findPreference(IMAGE_UPLOAD_ENDPOINT).setEnabled(true);
        	} else {
        		findPreference(IMAGE_UPLOAD_ENDPOINT).setEnabled(false);
        	}
        }
    }

    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	// Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    	
    }
    
}
