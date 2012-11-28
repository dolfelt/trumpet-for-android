package com.ofamilymedia.trumpet.classes;


import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class DrawableManager {
   private final Map<String, SoftReference<Drawable>> drawableMap;

   public DrawableManager() {
   	drawableMap = new HashMap<String, SoftReference<Drawable>>();
   }

   public Drawable fetchDrawable(String urlString) {
   	if (drawableMap.containsKey(urlString)) {
        SoftReference<Drawable> softReference = drawableMap.get(urlString);
        Drawable drawable = softReference.get();
        if (drawable != null) {
        	return drawable;
        }
   		//return drawableMap.get(urlString);
   	}

   	Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
   	try {
   		InputStream is = fetch(urlString);
   		Drawable drawable = Drawable.createFromStream(is, "src");
   		if(drawable != null) {
	   		drawableMap.put(urlString, new SoftReference<Drawable>(drawable));
	   		Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
	   				+ drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
	   				+ drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
   		}
   		return drawable;
   	} catch (MalformedURLException e) {
   		Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
   		return null;
   	} catch (IOException e) {
   		Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
   		return null;
   	}
   }

   public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
   	/*if (drawableMap.containsKey(urlString)) {
   		imageView.setImageDrawable(drawableMap.get(urlString));
   	}*/
    if (drawableMap.containsKey(urlString)) {
        SoftReference<Drawable> softReference = drawableMap.get(urlString);
        Drawable drawable = softReference.get();
        if (drawable != null) {
        	imageView.setImageDrawable(drawable);
        }
    }

   	final Handler handler = new Handler() {
   		@Override
   		public void handleMessage(Message message) {
   			imageView.setImageDrawable((Drawable) message.obj);
   		}
   	};

   	Thread thread = new Thread() {
   		@Override
   		public void run() {
   			//TODO : set imageView to a "pending" image
   			Drawable drawable = fetchDrawable(urlString);
   			drawableMap.put(urlString, new SoftReference<Drawable>(drawable));
   			Message message = handler.obtainMessage(1, drawable);
   			handler.sendMessage(message);
   		}
   	};
   	thread.start();
   }

   
   /*public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	        bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);
	 
	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    final RectF rectF = new RectF(rect);
	    final float roundPx = 12;
	 
	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	 
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	 
	    return output;
	  }*/
   
   private InputStream fetch(String urlString) throws MalformedURLException, IOException {
   	DefaultHttpClient httpClient = new DefaultHttpClient();
   	HttpGet request = new HttpGet(urlString);
   	HttpResponse response = httpClient.execute(request);
   	return response.getEntity().getContent();
   }

}
