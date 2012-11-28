package com.ofamilymedia.trumpet;

import java.util.ArrayList;
import java.util.List;


import com.ofamilymedia.trumpet.classes.Account;
import com.ofamilymedia.trumpet.classes.Twr;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.AuthorizationFactory;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TrumpetApp extends ListActivity {
    /** Called when the activity is first created. */
    
	static final int TRUMPET_USER_ACTIVITY = 298634;
	
	EfficientAdapter eAdapter;
	
	//public static ArrayList<Account> accounts;
	public List<String> loadedAccounts = new ArrayList<String>();
	
	
	public Uri sharedImage;
	public String sharedString;
	public Uri sharedUri;
	
	private Boolean fromNotify = false;
	
    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        
        private ArrayList<Account> DATA = new ArrayList<Account>();
        
        public EfficientAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
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
        public Object getItem(int position) {
            return position;
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
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.account_list_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            Account info = DATA.get(position);
            
            holder.text.setText(info.getScreenName());
            //holder.icon.setImageBitmap((position & 1) == 1 ? mIcon1 : mIcon2);

            return convertView;
        }
        
        public void addAccountItem(Account data) {
            DATA.add(data);
        }
        
        public void refreshList() {
        	notifyDataSetChanged();
        }
        
        public void clearList() {
        	DATA.clear();
        }

        static class ViewHolder {
            TextView text;
            //TextView count;
            //ImageView icon;
        }
    }

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fromNotify = false;
        
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
        	if(extras.get(Intent.EXTRA_STREAM) instanceof Uri) {
        		sharedImage = (Uri) extras.get(Intent.EXTRA_STREAM);
        	}
        	if(extras.getString(Intent.EXTRA_TEXT) != null && extras.getString(Intent.EXTRA_TEXT).length() > 0) {
        		sharedString = extras.getString(Intent.EXTRA_TEXT);
        	}
        	
        	if(extras.getInt("notifyAccount", -1) >= 0) {
        		TrumpetBase.defaultAccount = extras.getInt("notifyAccount");
        		fromNotify = true;
        	}
        }
        if((sharedUri = getIntent().getData()) != null) {
    		
        }
        
        
        setContentView(R.layout.main);
        
        registerForContextMenu(getListView());

        eAdapter = new EfficientAdapter(this);
        
        setListAdapter(eAdapter);
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                
	    loadAccounts();
        
	    /** LOAD DEFAULT ACCOUNT IF ONLY ONE, REGARDLESS OF SHARING **/
	    if(TrumpetBase.defaultAccount>=-1 && ((TrumpetBase)this.getApplication()).accounts.size() == 1) {
    	    Account account = eAdapter.DATA.get(0);
        	if(sharedImage != null || sharedString != null) {
        		loadTweetForSharing(account, 0);
        	} else {
        		loadTwitterUser(account, 0);
        	}
        }
        /** LOAD DEFAULT ACCOUNT ONLY IF NOT SHARING **/
        else if(sharedImage==null && sharedString==null && TrumpetBase.defaultAccount >= 0) {
        	if(eAdapter.getCount() > TrumpetBase.defaultAccount) {
        		Account account = eAdapter.DATA.get(TrumpetBase.defaultAccount);
        		loadTwitterUser(account, TrumpetBase.defaultAccount);
        	}
        }


        
        // ADD HANDLERS
        
        Button add_account_button = (Button) findViewById(R.id.add_account_button);
        add_account_button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		beginCreateAccount();
        	}
        });
	}
	
	@Override
	public void onResume() {
		super.onResume();
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        TrumpetBase.defaultAccount = -1;
    	editor.putInt("defaultAccount", -1);
        editor.commit();
        
    	LinearLayout no_account = (LinearLayout) findViewById(R.id.no_accounts);
        if(eAdapter.getCount() <= 0) {
        	no_account.setVisibility(LinearLayout.VISIBLE);
        } else {
        	no_account.setVisibility(LinearLayout.GONE);
        }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == 2492) {
			//Twr.setupNotifications(this);
		}
		if(requestCode == TRUMPET_USER_ACTIVITY) {
			TrumpetBase.defaultAccount = -2;
		}
	}
	
	public void loadAccounts() {
        for(int i=0; i < ((TrumpetBase)this.getApplication()).accounts.size(); i++) {
        	Account acct = ((TrumpetBase)this.getApplication()).accounts.get(i);
	       	if(loadedAccounts.contains(acct.getScreenName()) == false) {
        		eAdapter.addAccountItem(acct);
        		//TrumpetBase.usernameStore.put(i, new ArrayList<String>());
        		//TrumpetBase.hashtagStore.put(i, new ArrayList<String>());
        		loadedAccounts.add(acct.getScreenName());
        	}
         }
	}
	
	
    @Override
    public void onListItemClick(ListView listview, View view, int position, long id) {
    	
    	Account account = eAdapter.DATA.get(position);
    	
    	if(sharedImage != null || sharedString != null) {
    		loadTweetForSharing(account, position);
    	} else {
    		loadTwitterUser(account, position);
    	}
    }
    
    public void loadTweetForSharing(Account account, int index) {
		Intent myIntent = Twr.getNewTweet(this, index);
		if(sharedImage != null) {
			myIntent.putExtra("shareable", sharedImage);
		} else if(sharedString != null) {
			myIntent.putExtra("shareable", sharedString);
		}
		startActivity(myIntent);
		finish();
    }
    
    public void loadTwitterUser(Account account, int index) {
    	if(!fromNotify) {
	    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
	        editor.putInt("defaultAccount", index);
	        TrumpetBase.defaultAccount = index;
	        editor.commit();
    	}
    	Intent myIntent = new Intent(getBaseContext(), TrumpetUser.class);
    	myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("index", index);
        myIntent.putExtra("account", account);
        
        startActivityForResult(myIntent, TRUMPET_USER_ACTIVITY);
        
        //if(fromNotify) {
        	finish();
        //}
   }

    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    	menu.setHeaderTitle("Account Options");
    	menu.add(0, 5, 0, "Delete");
    	
    }
    @Override
    public boolean onContextItemSelected (MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info;
    	try {
    	    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	} catch (ClassCastException e) {
    	    return false;
    	}
    	//long id = getListAdapter().getItemId(info.position);
    	int id = info.position;
    	switch(item.getItemId()) {
    		case 5:
    			((TrumpetBase)this.getApplication()).accounts.remove(id);
    			eAdapter.DATA.remove(id);
    			eAdapter.refreshList();
    			((TrumpetBase) this.getApplication()).SaveAccounts();
    			return true;
    	}
    	return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu); 
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accounts, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.add_account:
        		beginCreateAccount();
        		break;
        	case R.id.settings:
        		Intent myIntent = new Intent(TrumpetApp.this.getApplicationContext(), TrumpetPreferences.class);
                startActivityForResult(myIntent, 2492);
        		break;
        }
        return super.onOptionsItemSelected(item);
    }
 
    
    public void beginCreateAccount() {
    	beginCreateAccount("");
    }
    public void beginCreateAccount(String user) {
		AlertDialog.Builder builder = new AlertDialog.Builder(TrumpetApp.this);
		
		LayoutInflater inflate = LayoutInflater.from(this);
		final View dialogView = inflate.inflate(R.layout.dialog_add_account, null);
		
		EditText userText = (EditText)dialogView.findViewById(R.id.username);
		userText.setText(user);
		
		builder.setTitle("Add Account")
			   .setView(dialogView)
		       .setCancelable(false)
		       .setPositiveButton("Add", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   EditText user = (EditText)dialogView.findViewById(R.id.username);
		        	   EditText pass = (EditText)dialogView.findViewById(R.id.password);
		        	   new AddAccount().execute(user.getText().toString(), pass.getText().toString());
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    
	private class AddAccount extends AsyncTask<String, Void, AccessToken> {
	    
	    private ProgressDialog dialog = new ProgressDialog(TrumpetApp.this);
	    
	    private AccessToken token;
	    private String usernameStore;
	    private TwitterException twitterError = null;
	    
		protected void onPreExecute() {
			dialog.setMessage("Verifying account...");
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					AddAccount.this.cancel(true);
				}
			});
			dialog.show();
		}
		
		protected AccessToken doInBackground(String... params) {
			
			usernameStore = params[0];
			
			Twitter twitter = new TwitterFactory().getInstance();
		    //twitter.setOAuthConsumer(TrumpetBase.CONSUMER_KEY, TrumpetBase.CONSUMER_SECRET);
		    
		    try {
		    	token = twitter.getOAuthAccessToken(params[0], params[1]);
			} catch (TwitterException e) {
				twitterError = e;
				e.printStackTrace();
			}
		    
			return token;
		}
		
		protected void onPostExecute(AccessToken token) {
			dialog.dismiss();
		   
		   	if(twitterError != null) {
				Toast toast = Toast.makeText(TrumpetApp.this, "There was an error with your username and password. Please try again.", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				beginCreateAccount(usernameStore);
		   	} else {
		   		
				Account newAccount = new Account();
				newAccount.setToken(token.getToken());
				newAccount.setSecret(token.getTokenSecret());
				newAccount.setScreenName(token.getScreenName());
				newAccount.setId(token.getUserId());
				
				((TrumpetBase)TrumpetApp.this.getApplication()).accounts.add(newAccount);
				
				loadTwitterUser(newAccount, ((TrumpetBase)TrumpetApp.this.getApplication()).accounts.size()-1);
				
				loadAccounts();

		   	}
		   
		}
	 }
    
	
	
	@Override
    protected void onPause(){
		super.onPause();
		
		((TrumpetBase) this.getApplication()).SaveAccounts();
		
    }
    
}