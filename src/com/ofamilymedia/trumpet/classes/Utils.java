package com.ofamilymedia.trumpet.classes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
    	return getRoundedCornerBitmap(bitmap, 4, null);
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
    	return getRoundedCornerBitmap(bitmap, roundPx, null);
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, Rect targetSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if(width != height) {
        	if(width > height) {
        		width = height;
        	} else {
        		height = width;
        	}
        }

        int finalWidth = width;
        int finalHeight = height;
        
        if(targetSize != null) {
        	finalWidth = targetSize.width();
        	finalHeight= targetSize.height();
        }
        
    	Bitmap output = Bitmap.createBitmap(finalWidth, finalHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
     
        final int color = 0xff424242;
        final Paint paint = new Paint();
        
        int startX = (bitmap.getWidth()-width)/2;
        int startY = (bitmap.getHeight()-height)/2;
        
        final Rect rect = new Rect(startX, startY, width+startX, height+startY);
        final Rect destRect = new Rect(0, 0, finalWidth, finalHeight);
        
        final RectF rectF = new RectF(destRect);
        //final float roundPx = 4;
     
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
     
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, destRect, paint);
        canvas = null;
        
        return output;
    }

    
    public static Bitmap decodeFile(File file, int size){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPurgeable = true;
            o.inInputShareable = true;
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = size;

            //Find the correct scale value. It should be the power of 2.
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public static String getPath(Activity act, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = act.managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
   

    
    public static String join(String[] strings, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < strings.length; i++) {
            if (i != 0) sb.append(separator);
      	    sb.append(strings[i]);
      	}
      	return sb.toString();
    }
    
    public static void ObjectToFile(File file, Object object) {
    	if(file.exists()) {
    		file.delete();
    	}
    	
    	ByteArrayOutputStream bos = null;
    	try {
	        bos = new ByteArrayOutputStream();
	        ObjectOutputStream obj_out = new ObjectOutputStream(bos);
	        
	        obj_out.writeObject(object);
           	final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file), 8192);
            out.write(bos.toByteArray());
        	out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static Object ObjectFromFile(File file) {
    	
    	if(file.exists() == false) return null;
    	
    	try {
           	final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), 8192);

           	byte[] fromCache = new byte[(int)file.length()];
           	in.read(fromCache);
           	
    		ByteArrayInputStream bis = new ByteArrayInputStream(fromCache);
    		Object data = null;
       		try {
       			ObjectInputStream obj_in = new ObjectInputStream(bis);
       			data = obj_in.readObject();
       			in.close();
       			
       			return data;
		   	} catch (IOException e) {
		   		e.printStackTrace();
		   	} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
    		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
    }
    
    
}