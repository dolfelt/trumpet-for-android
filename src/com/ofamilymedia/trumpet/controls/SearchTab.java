package com.ofamilymedia.trumpet.controls;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ofamilymedia.trumpet.R;
import com.ofamilymedia.trumpet.TrumpetSearch;
import com.twitter.Twit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchTab extends LinearLayout {
	
	public int userIndex = -1;
	
	public ProgressBar prog;
	
	public EfficientAdapter eAdapter;
	
	public SearchTab(Context context) {
		super(context);
		//eAdapter = new EfficientAdapter(context);
		
		initView(context);
	}

	
	public void initView(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setBackgroundColor(0xFFFFFFFF);
		
		View.inflate(context, R.layout.search_tab, this);
		Log.i("COUNT", "Num Children: " + String.valueOf(this.getChildCount()));
		
		final EditText searchText = (EditText) findViewById(R.id.search_input);
		Button search = (Button) findViewById(R.id.search_button);
		search.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String searchTerm = searchText.getText().toString();
				Intent myIntent = new Intent(v.getContext(), TrumpetSearch.class);
                myIntent.putExtra("index", userIndex);
				if(1==1) { // Searching tweets
                  	myIntent.putExtra("search", searchTerm);
				} else {
					myIntent.putExtra("searchUser", searchTerm);
				}
    	        v.getContext().startActivity(myIntent);
			}
		});
	}
	
	public void onDisplay() {
		
	}
	


     public static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String,String>> DATA = new ArrayList<HashMap<String,String>>();
        
        //private ImageThreadLoader imageLoader = new ImageThreadLoader();
        //private DrawableManager imageManager = new DrawableManager();
        
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
        public HashMap<String,String> getItem(int position) {
        	if(DATA.size() > position)
        		return DATA.get(position);
        	else
        		return null;
        }
        
        public int getItemPosition(Twit item) {
        	return DATA.indexOf(item);
        }
        
        public HashMap<String,String> getLastItem() {
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
                convertView = mInflater.inflate(R.layout.attachment_list_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            HashMap<String,String> info = DATA.get(position);
            
            holder.icon.setImageBitmap(null);
            
            String icon_url = info.get("file");
	        holder.icon.setTag(icon_url);
	        holder.icon.setImageURI(Uri.parse(info.get("file")));
	        //imageLoader.DisplayImage(icon_url, holder.icon);

            holder.name.setText(info.get("name"));

            return convertView;
        }
        
        public void addItem(HashMap<String,String> data) {
            if(!DATA.contains(data))
            	DATA.add(data);
        }
        
        public void refreshList() {
        	notifyDataSetChanged();
        }
        
        public void clearList() {
        	DATA.clear();
        }
        
        public List<HashMap<String,String>> getData() {
        	return DATA;
        }
        public void setData(List<HashMap<String,String>> data) {
        	DATA = data;
        }

        static class ViewHolder {
            TextView name;
            ImageView icon;
        }
    }
    

    /*OnCreateContextMenuListener onItemLongClick = new OnCreateContextMenuListener() {
    	
    	
    	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		MenuItem menu_reply = menu.add("Delete");
    		menu_reply.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    				HashMap<String,String> status = eAdapter.getItem(info.position);
    				//Intent myIntent = Twr.getReply(AttachmentList.this.getContext(), userIndex, status);
                    //AttachmentList.this.getContext().startActivity(myIntent);
    				return true;
    			}
    		});
    	}
    	
    };*/

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for(int i = 0 ; i < getChildCount() ; i++){
            getChildAt(i).layout(l, t, r, b);
        }
	}

}
