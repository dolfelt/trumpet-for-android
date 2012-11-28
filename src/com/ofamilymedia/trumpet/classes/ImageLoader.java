package com.ofamilymedia.trumpet.classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import com.ofamilymedia.trumpet.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.widget.ImageView;

public class ImageLoader {
    
    //the simplest in-memory cache implementation. This should be replaced with something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
    private static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
    
    public int targetWidth = 48;
    public int targetHeight = 48;
    
    private File cacheDir;
    private float scale;
    public ImageLoader(Context context){
        
    	scale = context.getResources().getDisplayMetrics().density;
    	
    	//Make the background thead low priority. This way it will not affect the UI performance
        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-3);
        
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"data/trumpet");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
    public float roundPx = 4;
    public int stub_id = R.drawable.tweet_icon_stub;
    public void DisplayImage(String url, ImageView imageView)
    {
    	DisplayImage(url, imageView, true);
    }
    public void DisplayImage(String url, ImageView imageView, Boolean useCache) {
        if(cache.containsKey(url) && useCache) {
            Bitmap bmp = cache.get(url).get();
            if(bmp != null) {
	        	imageView.setImageBitmap(bmp);
	            return;
            }
        }
        
        queuePhoto(url, imageView, useCache);
        imageView.setImageResource(stub_id);
    
    }
        
    private void queuePhoto(String url, ImageView imageView, Boolean useCache)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p = new PhotoToLoad(url, imageView, useCache);
        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState()==Thread.State.NEW)
            photoLoaderThread.start();
    }
    
    private Bitmap getBitmap(String url, Boolean useCache) 
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        
        //from SD cache
        if(useCache) {
	        Bitmap b = Utils.decodeFile(f, (int)(scale*81));
	        if(b!=null)
	            return b;
        }
        
        //from web
        try {
            Bitmap bitmap=null;
            InputStream is=new URL(url).openStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            is.close();
            bitmap = Utils.decodeFile(f, (int)(scale*81));
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public Boolean useCache;
        public PhotoToLoad(String u, ImageView i, Boolean c){
            url=u; 
            imageView=i;
            useCache=c;
        }
    }
    
    PhotosQueue photosQueue=new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
            for(int j=0; j<photosToLoad.size();){
                if(photosToLoad.get(j).imageView==image)
                    photosToLoad.remove(j);
                else
                    ++j;
            }
        }
    }
    
    class PhotosLoader extends Thread {
        public void run() {
            try {
                while(true)
                {
                    //thread waits until there are any images to load in the queue
                    if(photosQueue.photosToLoad.size()==0)
                        synchronized(photosQueue.photosToLoad){
                            photosQueue.photosToLoad.wait();
                        }
                    if(photosQueue.photosToLoad.size()!=0)
                    {
                        PhotoToLoad photoToLoad;
                        synchronized(photosQueue.photosToLoad){
                            photoToLoad=photosQueue.photosToLoad.pop();
                        }
                        Bitmap bmp=getBitmap(photoToLoad.url, photoToLoad.useCache);
                        if(bmp != null)
                        	bmp = Utils.getRoundedCornerBitmap(bmp, roundPx*scale, new Rect(0,0,(int)(targetWidth*scale),(int)(targetHeight*scale)));
                        
                        cache.put(photoToLoad.url, new SoftReference<Bitmap>(bmp));
                        if(((String)photoToLoad.imageView.getTag()).equals(photoToLoad.url)){
                            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad.imageView);
                            Activity a = (Activity)photoToLoad.imageView.getContext();
                            a.runOnUiThread(bd);
                            a = null;
                        }
                    }
                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }
    
    PhotosLoader photoLoaderThread=new PhotosLoader();
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        ImageView imageView;
        public BitmapDisplayer(Bitmap b, ImageView i){bitmap=b;imageView=i;}
        public void run()
        {
            if(bitmap!=null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(stub_id);
        }
    }
    
    public void clearMemory() {
    	cache.clear();
    }
    
    public void clearCache() {
        //clear memory cache
        cache.clear();
        
        //clear SD cache
        File[] files=cacheDir.listFiles();
        for(File f:files)
            f.delete();
    }

}
