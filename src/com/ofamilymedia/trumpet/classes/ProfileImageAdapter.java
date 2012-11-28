package com.ofamilymedia.trumpet.classes;

import java.util.ArrayList;
import java.util.List;

import com.ofamilymedia.trumpet.TrumpetBase;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import twitter4j.Status;

public class ProfileImageAdapter extends BaseAdapter {
    private Context mContext;
    
    public List<Status> data = new ArrayList<Status>();
    
    private float scale = 1f;
    
    public ProfileImageAdapter(Context c) {
        mContext = c;
        scale = c.getResources().getDisplayMetrics().density;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	imageView = new ImageView(mContext);
        	GridView.LayoutParams params = new GridView.LayoutParams((int)(48*scale), (int)(48*scale));
		    imageView.setLayoutParams(params);
		    imageView.setScaleType(ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
        }

        String url = data.get(position).getUser().getProfileImageURL().toString();
        imageView.setTag(url);
        
        ((TrumpetBase)mContext.getApplicationContext()).imageLoader.DisplayImage(url, imageView);
        
        return imageView;
    }

}
