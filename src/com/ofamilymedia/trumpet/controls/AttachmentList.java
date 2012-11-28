package com.ofamilymedia.trumpet.controls;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import com.ofamilymedia.trumpet.R;
import com.ofamilymedia.trumpet.TrumpetSendTweet;
import com.ofamilymedia.trumpet.classes.Utils;
import com.twitter.Twit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AttachmentList extends ListView {
	
	//public String screenname = "";
	public int userIndex = -1;
	
	public ProgressBar prog;
	
	
	public EfficientAdapter eAdapter;
	
	public AttachmentList(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
		eAdapter = new EfficientAdapter(context);
		
		setAdapter(eAdapter);
		
		this.setBackgroundColor(0x333333);
		
		setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		setBackgroundColor(0xFFFFFFFF);
		setDividerHeight(0);
		setCacheColorHint(0xFFFFFFFF);
		
		setSelector(getResources().getDrawable(R.drawable.list_background));
		
		setOnItemClickListener(onItemClick);
		setOnCreateContextMenuListener(onItemLongClick);
		//setOnScrollListener(onScroll);
		
		//this.setOnItemLongClickListener(onItemLongClick);
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
                holder.size = (TextView) convertView.findViewById(R.id.size);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.delete = (Button) convertView.findViewById(R.id.attach_delete_button);
                
                holder.delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = (Integer) v.getTag();
	    				DATA.remove(position);
	    				refreshList();
	    				
	    				Context parent = v.getContext();
	    				if(parent instanceof TrumpetSendTweet) {
	    					((TrumpetSendTweet)parent).updateAfterAttachment();
	    				}
					}
                	
                });
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            HashMap<String,String> info = DATA.get(position);
            
            holder.icon.setImageBitmap(null);
            
            holder.delete.setTag(position);
            
            String icon_url = info.get("file");
	        holder.icon.setTag(icon_url);
	        
	        File f = new File(info.get("file"));
	        Bitmap bmp = Utils.decodeFile(f, 81);
	        holder.icon.setImageBitmap(bmp); //(Uri.parse(info.get("file")));
	        //imageLoader.DisplayImage(icon_url, holder.icon);

            holder.name.setText(info.get("name"));
            
            holder.size.setText(info.get("size"));

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
            TextView size;
            ImageView icon;
            Button delete;
        }
    }
    
    OnScrollListener onScroll = new OnScrollListener() {
    	
    	@Override
        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
        }
    	
    	@Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    };
    
    
    OnItemClickListener onItemClick = new OnItemClickListener() {
    	
    	
    	public void onItemClick(AdapterView<?> adapter, View  view, int position, long id) {
    	
	    	//HashMap<String,String> tweet = eAdapter.DATA.get(position);
	    	
	    	
    	}
    	
    };

    OnCreateContextMenuListener onItemLongClick = new OnCreateContextMenuListener() {
    	
    	
    	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		menu.setHeaderTitle("Attachment Options");
    		MenuItem menu_reply = menu.add("Delete");
    		menu_reply.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    			@Override
    			public boolean onMenuItemClick(MenuItem item) {
    				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    				HashMap<String,String> status = eAdapter.getItem(info.position);
    				
    				eAdapter.DATA.remove(info.position);
    				eAdapter.refreshList();
    				
    				return true;
    			}
    		});
    	}
    	
    };


}
