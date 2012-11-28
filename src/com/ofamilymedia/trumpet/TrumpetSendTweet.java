package com.ofamilymedia.trumpet;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.CustomImageUpload;
import com.ofamilymedia.trumpet.classes.Utils;
import com.ofamilymedia.trumpet.controls.AttachmentList;
import com.ofamilymedia.trumpet.controls.TweetAutoComplete;
import com.ofamilymedia.trumpet.controls.UserAutoComplete;
import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Jmp;
import com.rosaloves.bitlyj.Url;
import com.rosaloves.bitlyj.Bitly.Provider;
import com.twitter.Extractor.Entity;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.AccessToken;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TrumpetSendTweet extends Activity {

	private static final int SELECT_PICTURE = 1;
	private static final int TAKE_PICTURE = 3;
	private static final int SELECT_VIDEO = 2;
	
	private static final int STATUS_LENGTH = 140;
	
	public Boolean isDM = false;
	public String dmUser = "";
	
	public Account account;
	public int userIndex = -1;
	public ProgressDialog dialog;
	long replyId;
	public AttachmentList attachList;
	String startText;
	
	public Boolean useLocation = false;
	public Location currentLocation = null;
	
	//public AsyncTwitter asyncTwitter;
	public TweetAutoComplete text;
	public UserAutoComplete dm_to;
	
	public List<HashMap<String,String>> attachments = new ArrayList<HashMap<String,String>>();
	private ProgressDialog locationDialog;
	
	private SendTweet asyncSendTweet;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        Bundle extras = getIntent().getExtras();
		userIndex = extras.getInt("index", -1); 
		isDM = extras.getBoolean("isDM", false);
		if(isDM) {
			dmUser = extras.getString("dmUser");
		}
		
        replyId = extras.getLong("replyId", 0);
		startText = extras.getString("text");
		
		List<Account> all_accounts = ((TrumpetBase)this.getApplication()).accounts;
		account = all_accounts.get(userIndex);
		
		dialog = new ProgressDialog(this);
 
        setContentView(R.layout.sendtweet);

    	text = (TweetAutoComplete)findViewById(R.id.text);

    	if(startText != null)
        {
        	text.setText(startText);
        }
    	String[] autocomplete_list = ((TrumpetBase)getApplication()).getUsernameList(account.getId());

    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.autocomplete_item, autocomplete_list);
    	text.setAdapter(adapter);
        
    	/** DM Specific Setup **/
    	if(isDM) {
    		dm_to = (UserAutoComplete)findViewById(R.id.dm_to);
    		dm_to.setVisibility(View.VISIBLE);
    		dm_to.setAdapter(adapter);
    		if(dmUser != null) {
    			dm_to.setText(dmUser);
    		}
       	}
       	/** END DM **/
    	
		final TextView status_count = (TextView) findViewById(R.id.status_count);
    	text.setOnKeyListener(new OnKeyListener() {
    		public boolean onKey(View view, int i, KeyEvent event) {
    			status_count.setText(String.valueOf(charactersRemaining()));
    			return false;
    		}
    	});
    	text.addTextChangedListener(new TextWatcher() {
    	    public void afterTextChanged(Editable s) { }
    	    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    	    public void onTextChanged(CharSequence s, int start, int before, int count) {
    	    	status_count.setText(String.valueOf(charactersRemaining()));         
    	    }
    	});
    	
        TextView title = (TextView)findViewById(R.id.title);
    	StringBuilder sb = new StringBuilder();
        if(isDM) {
    		sb.append("Message");
    	} else {
    		sb.append("Tweet");
    	}
        
        if(all_accounts.size() > 1) {
        	sb.append(" from @").append(account.getScreenName());
        }
        
        title.setText(sb.toString());
        
        Button send = (Button)findViewById(R.id.send_tweet);
        send.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        	    
        		TextView text = (TextView)findViewById(R.id.text);
        		TextView dm_to = (TextView)findViewById(R.id.dm_to);
        	    if(isDM && dm_to.getText().length() <= 0) {
    				Toast.makeText(TrumpetSendTweet.this, "Please enter a Twitter user.", Toast.LENGTH_LONG).show();
        	    	return;
        	    }
        		asyncSendTweet = new SendTweet();
        		asyncSendTweet.execute(text.getText().toString(), dm_to.getText().toString());
        	}
        });
        
        

        //final LinearLayout attachParent = (LinearLayout) findViewById(R.id.attachment_parent);

        /*ImageButton btnAttachments = (ImageButton)findViewById(R.id.title_attachments);
        btnAttachments.setVisibility(ImageButton.VISIBLE);
        btnAttachments.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if(attachList.eAdapter.getCount() > 0) {
        			Animation animation = AnimationUtils.loadAnimation(TrumpetSendTweet.this, R.anim.slide_up);
        			attachParent.startAnimation(animation);
        			attachParent.setVisibility(RelativeLayout.VISIBLE);
        		} else {
        			selectAttachment();
        		}
        	}
        });*/
        
        /** TOOLBAR LISTENERS **/
        if(!isDM) {
	        ImageButton btnLocation = (ImageButton)findViewById(R.id.ac_button_location);
	        btnLocation.setVisibility(ImageButton.VISIBLE);
	        btnLocation.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
	        		toggleTweetLocation();
	        	}
	        });
        }

        ImageButton btnAttachImage = (ImageButton) findViewById(R.id.ac_button_image);
        btnAttachImage.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		selectAttachment();
        	}
        });
        ImageButton btnAttachVideo = (ImageButton) findViewById(R.id.ac_button_video);
        btnAttachVideo.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		selectAttachmentVideo();
        	}
        });

        ImageButton btnShortenUrl = (ImageButton) findViewById(R.id.ac_button_shorten);
        btnShortenUrl.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		TextView text = (TextView)findViewById(R.id.text);
        		new ShortenURLs().execute(text.getText().toString());
        	}
        });
        
        /*Button btnCloseAttach = (Button)findViewById(R.id.close_attach);
        btnCloseAttach.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		closeAttachments();
        	}
        });*/

        /*ImageButton btnAddAttachment = (ImageButton)findViewById(R.id.title_add);
        btnAddAttachment.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		selectAttachment();
        	}
        });*/

        
        LinearLayout attachContainer = (LinearLayout) findViewById(R.id.tweet_panel);
        attachList = new AttachmentList(this);
        attachList.userIndex = userIndex;
        attachContainer.addView(attachList);

        locationDialog = new ProgressDialog(TrumpetSendTweet.this);
        
        
        /** Attach image if user is SHARING **/
        Object shareableObject = extras.get("shareable");
		if(shareableObject instanceof Parcelable) {
	        Parcelable shareable = (Parcelable) shareableObject;
			if(shareable!=null && shareable instanceof Uri) {
				addAttachment((Uri)shareable);
			}
		} else if(shareableObject instanceof String) {
			String shareable = (String) shareableObject;
			text.setText(text.getText().toString() + shareable);
		}

		status_count.setText(String.valueOf(charactersRemaining()));
	}

	public void toggleTweetLocation() {
		useLocation = !useLocation;
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		ImageButton btnLocation = (ImageButton)findViewById(R.id.ac_button_location);
		if(useLocation == true) {
			//currentLocation = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			//if(currentLocation == null) {
				locationDialog.setMessage("Obtaining Location");
				locationDialog.show();
			//}
			btnLocation.setImageResource(R.drawable.title_location_on);
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		} else {
			btnLocation.setImageResource(R.drawable.dk_icon_location);
			mlocManager.removeUpdates(myLocationListener);
			currentLocation = null;
		}
	}
	public LocationListener myLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;
			locationDialog.dismiss();
		}

		@Override
		public void onProviderDisabled(String provider) { }

		@Override
		public void onProviderEnabled(String provider) { }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		
	};
	
	public int charactersRemaining() {
		
		int used = 0;
		
		used += attachList.eAdapter.getCount() * 26;
		used += text.length();
		
		return STATUS_LENGTH - used;
	}
	
	
	@Override
	public boolean onKeyDown(int i, KeyEvent event) {

		// only intercept back button press
		if (i == KeyEvent.KEYCODE_BACK) {
			super.onKeyDown(i, event);
			return true;
		}
		return false; // propagate this keyevent
	}	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.sendtweet, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.shorten:
        		TextView text = (TextView)findViewById(R.id.text);
        		new ShortenURLs().execute(text.getText().toString());
        		return true;
        	case R.id.attach:
        		if(attachList.eAdapter.getCount() > 0) {
        			/*LinearLayout attachParent = (LinearLayout) findViewById(R.id.attachment_parent);
        			Animation animation = AnimationUtils.loadAnimation(TrumpetSendTweet.this, R.anim.slide_up);
        			attachParent.startAnimation(animation);
        			attachParent.setVisibility(RelativeLayout.VISIBLE);
        			*/
        		} else {
        			selectAttachment();
        		}
        		return true;
 	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    
    
    public void selectAttachment() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_PICTURE);
    }
    
    public void takePicture() {
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	startActivityForResult(intent, TAKE_PICTURE);
    }

    public void selectAttachmentVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	if (resultCode == RESULT_OK) {
    		/** ACTIVITY DATA RETURNED FROM GALLERY **/
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                addAttachment(selectedImageUri);
            }
            if (requestCode == SELECT_VIDEO) {
                Uri selectedImageUri = data.getData();
                addAttachment(selectedImageUri);
            }
        }
    }
    
    public void addAttachment(Uri attachment) {
    	String path = Utils.getPath(this, attachment);
    	addAttachment(path);
    }
    public void addAttachment(String path) {
    	HashMap<String,String> hash = new HashMap<String,String>();
        hash.put("name", path.substring(path.lastIndexOf("/")+1));
        hash.put("file", path);
        long file_bytes = new File(path).length();
        if(file_bytes > 1024*1024) {
        	hash.put("size", String.valueOf(file_bytes/(1024*1024)) + "Mb");
        } else if(file_bytes > 1024) {
        	hash.put("size", String.valueOf(file_bytes/1024) + "Kb");
        } else {
        	hash.put("size", String.valueOf(file_bytes) + "B");
        }
        attachList.eAdapter.addItem(hash);
        attachList.eAdapter.refreshList();
		
        /** REFRESH ITEMS **/
        updateAfterAttachment();
    	
    }
    
    public void updateAfterAttachment() {
        final TextView status_count = (TextView) findViewById(R.id.status_count);
    	status_count.setText(String.valueOf(charactersRemaining()));
    	
    	//RelativeLayout preview = (RelativeLayout) findViewById(R.id.attachments_preview);
    	//LinearLayout layout = (LinearLayout) findViewById(R.id.attachment_layout);
    	//layout.removeAllViews();
    	/*int mediaCount = attachList.eAdapter.getCount();
    	if(mediaCount>0) {
    		//preview.setVisibility(View.VISIBLE);
			float scale = getResources().getDisplayMetrics().density;
    		for(int i=0; i<mediaCount; i++) {
				HashMap<String,String> data = attachList.eAdapter.getItem(i);
				String imageURL = data.get("file");
				ImageView icon = new ImageView(this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(24*scale), (int)(24*scale));
				params.setMargins((int)(scale*4), 0, 0, 0);
				icon.setLayoutParams(params);
				File f = new File(imageURL);
				Bitmap bmp = Utils.decodeFile(f, 42);
				icon.setImageBitmap(bmp);
				//layout.addView(icon);
			}
    	} else {
    		//preview.setVisibility(View.GONE);
    		
    	}*/
    }
    
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
   
	public void onDestroy() {
		super.onDestroy();
		attachList.setAdapter(null);
		
		if(asyncSendTweet!=null)
			asyncSendTweet.cancel(true);
		
		((LocationManager)getSystemService(Context.LOCATION_SERVICE)).removeUpdates(myLocationListener);
	}
    
    /**
     * SEND THE TWEET ASYNC
     * 
     * @author dolfelt
     *
     */
    
	private class SendTweet extends AsyncTask<String, String, Boolean> {
	    private ProgressDialog dialog = new ProgressDialog(TrumpetSendTweet.this);

	    private TwitterException twitterError = null;
	    
	    protected void onPreExecute() {
    		dialog.setMessage("Begin sending...");
    		dialog.show();
	    }
		
		protected Boolean doInBackground(String... params) {
			/** Get status to send **/
			String status = params[0];
			String screenName = null;
			if(isDM) {
				screenName = params[1];
			}
			
			/** Get access token **/
			AccessToken token = ((TrumpetBase)TrumpetSendTweet.this.getApplication()).loadAccessToken(userIndex);
			int mediaCount = attachList.eAdapter.getCount();
			if(mediaCount > 0) {
				publishProgress("Uploading media...");
				/** Array to store the images **/
				ArrayList<String> urls = new ArrayList<String>();
				
				/** Configure and upload each file **/
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthAccessToken(token.getToken());
				builder.setOAuthAccessTokenSecret(token.getTokenSecret());
				
	        	
				SharedPreferences prefs = TrumpetSendTweet.this.getSharedPreferences(account.getScreenName(), MODE_PRIVATE);
				
				String imgUploader = prefs.getString("image_upload", "yFrog");
				ImageUpload upload = null;
				if(imgUploader.equalsIgnoreCase("custom")) {
					Properties cprops = new Properties();
					cprops.put("message", status);
					builder.setMediaProviderParameters(cprops);
					Configuration conf = builder.build();
					String endpoint = prefs.getString("image_upload_endpoint", "");
					
					if(endpoint.length() > 0) {
						upload = new CustomImageUpload(conf, endpoint);
					}
				} else {
					if(imgUploader.equalsIgnoreCase("yfrog")) {
						builder.setMediaProvider(MediaProvider.YFROG.getName());
						builder.setMediaProviderAPIKey(TrumpetSendTweet.this.getString(R.string.api_key_yfrog));
					} else if(imgUploader.equalsIgnoreCase("twitpic")) {
						builder.setMediaProvider(MediaProvider.TWITPIC.getName());
						builder.setMediaProviderAPIKey(TrumpetSendTweet.this.getString(R.string.api_key_twitpic));
					}
					Configuration conf = builder.build();
					upload = new ImageUploadFactory(conf).getInstance();
				}
				for(int i=0; i<mediaCount; i++) {
					HashMap<String,String> data = attachList.eAdapter.getItem(i);
					try {
						if(upload == null)
						{
							throw new TwitterException("There is a problem with your custom upload URL.");
						}
						//URI file = new URI(Uri.parse(data.get("file")).toString());
						String url = upload.upload(new File(data.get("file")));
						urls.add(url);
					} catch (TwitterException e) {
						twitterError = e;
						Log.w("UPLOAD", e.getMessage());
						e.printStackTrace();
						return false;
					}
				}

				String[] surls = new String[urls.size()-1];
				surls = urls.toArray(surls);
				status += " " + Utils.join(surls, " ");
			}
			
			if(isDM) {
				publishProgress("Sending direct message...");
			} else {
				publishProgress("Sending status update...");
			}
			//Configuration config = TrumpetBase.loadTwitterConfig(userIndex);
			Twitter twit = new TwitterFactory().getInstance(token);
			try {
				if(isDM) {
					if(screenName != null) {
						twit.sendDirectMessage(screenName, status);
					} else {
						return false;
					}
				} else {
					StatusUpdate update = new StatusUpdate(status);
					if(replyId > 0) {
						update.setInReplyToStatusId(replyId);
					}
					if(useLocation && currentLocation instanceof Location) {
						update.location(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
					}
					twit.updateStatus(update);
				}
			} catch (TwitterException e) {
				twitterError = e;
				e.printStackTrace();
				return false;
			}
		    
			return true;
		}
		
		@Override
		protected void onProgressUpdate(String... update) {
			dialog.setMessage(update[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			dialog.dismiss();
			
			if(success) {
				String successMessage = "";
				if(isDM) {
					successMessage = "Direct message sent successfully.";
				} else {
					successMessage = "Status updated successfully.";
				}
				Toast toast = Toast.makeText(TrumpetSendTweet.this, successMessage, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				
	    		finish();
	    		
		   	} else {
		   		
				Toast toast = Toast.makeText(TrumpetSendTweet.this, "There was an error sending your status. Please try again.", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
	    		
		   	}
		   
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
		
	 }


    /**
     * SHORTEN URLS
     * 
     * @author dolfelt
     *
     */
    
	private class ShortenURLs extends AsyncTask<String, String, Boolean> {
	    private ProgressDialog dialog = new ProgressDialog(TrumpetSendTweet.this);
	    private String content;
	    protected void onPreExecute() {
    		dialog.setMessage("Shortening URLs...");
    		dialog.show();
	    }
		
		protected Boolean doInBackground(String... params) {
			/** Get status to send **/
			content = params[0];
			List<Entity> urlMatches = com.twitter.Extractor.extractURLs(content);
			for(Entity urlMatch : urlMatches) {
	        	final String url_link = urlMatch.value;
	        	if(url_link.contains("http://bit.ly") || url_link.contains("http://j.mp")) continue;
	        	if(url_link.length() <= 20) continue;
	        	SharedPreferences prefs = TrumpetSendTweet.this.getSharedPreferences(account.getScreenName(), MODE_PRIVATE);
	        	String service = prefs.getString("shortening_service", "bitly");
	        	if(service.equals("jmp")) {
	        		Provider jmp = Jmp.as("trumpet", "R_f0ee15ee41b6721b9fd1ef164a046f3c");
	 	        	Url url = jmp.call(Bitly.shorten(url_link));
		        	if(url != null) {
		        		content = content.replace(url_link, url.getShortUrl());
		        	}
	        	} else if(service.equals("bitly")) {
		        	Provider bitly = Bitly.as("trumpet", "R_f0ee15ee41b6721b9fd1ef164a046f3c");
		        	Url url = bitly.call(Bitly.shorten(url_link));
		        	if(url != null) {
		        		content = content.replace(url_link, url.getShortUrl());
		        	}
	        	}
			}
		    
			return true;
		}
		
		protected void onProgressUpdate(String... update) {
			dialog.setMessage(update[0]);
		}
		
		protected void onPostExecute(Boolean success) {
			
			if(TrumpetSendTweet.this == null) return;
			
			dialog.dismiss();
			
			if(success) {
				TextView text = (TextView)findViewById(R.id.text);
				text.setText(content);
		   	} else {
		   		
				Toast toast = Toast.makeText(TrumpetSendTweet.this, "There was an error shortening your URLs. Please try again.", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
	    		
		   	}
		   
		}
		
		
	 }

	
}
