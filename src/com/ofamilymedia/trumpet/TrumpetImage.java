package com.ofamilymedia.trumpet;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.ofamilymedia.trumpet.classes.Utils;
import com.ofamilymedia.trumpet.controls.TouchImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TrumpetImage extends Activity {

	private String image;
	private TouchImageView viewer;
	private float scale;
	private File cacheDir;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        Bundle extras = getIntent().getExtras();
        
        image = extras.getString("image");
        
        if(image == null) {
        	finish();
        }
        
        /** SETUP VARS **/
    	scale = getResources().getDisplayMetrics().density;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"data/trumpet");
        else
            cacheDir=getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
        
        viewer = new TouchImageView(this);
        /** END SETUP **/
        
        setContentView(viewer);
        
        
	}
	
	@Override
	public void onResume() {
		super.onRestart();
		if(image != null) {
			new ImageLoader().execute(image);
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.refresh:
        		
        		return true;
        	case R.id.new_tweet:
        		
    	        return true;
 	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

    
    @Override
    public void onDestroy() {
		super.onDestroy();
    }
    
    
    public class ImageLoader extends AsyncTask<String, Float, Boolean> {

    	private Bitmap bitmap;
    	ProgressDialog progress;
    	
    	@Override
    	protected void onPreExecute() {
			progress = new ProgressDialog(TrumpetImage.this);
			progress.setMessage("Loading image...");
			progress.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface di) {
					finish();
				}
			});
			progress.setCancelable(true);
			progress.show();
    	}
    	
		@Override
		protected Boolean doInBackground(String... arg) {
			// TODO Auto-generated method stub
			String url = arg[0];
			bitmap = getBitmap(url);
			return true;
		}
		
    	@Override
		protected void onPostExecute(Boolean arg0) {
			viewer.setImage(bitmap);
			progress.dismiss();
		}
    	
    }
    
    
    private Bitmap getBitmap(String url) 
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());
        File f=new File(cacheDir, filename);
        
        //from SD cache
        Bitmap b = Utils.decodeFile(f, (int)(scale*400));
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            InputStream is=new URL(url).openStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = Utils.decodeFile(f, (int)(scale*400));
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    
}
