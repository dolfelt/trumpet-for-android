package com.ofamilymedia.trumpet.classes;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TweetAutoList {

   private static final String DATABASE_NAME = "trumpet.db";
   private static final int DATABASE_VERSION = 2;
   private static final String TABLE_NAME = "autocomplete";

   private Context context;
   private SQLiteDatabase db;

   private SQLiteStatement insertStmt;
   private static final String INSERT = "INSERT INTO " + TABLE_NAME + "(id,name) VALUES (?,?)";

   public TweetAutoList(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
      this.insertStmt = this.db.compileStatement(INSERT);
   }
   public void beginTransaction() {
	   this.db.beginTransaction();
   }
   public long insert(long id, String name) {
      this.insertStmt.bindLong(1, id);
      this.insertStmt.bindString(2, name);
      try {
    	  return this.insertStmt.executeInsert();
      } catch(SQLiteConstraintException e) {
    	  return 0;
      }
   }

   public void endTransaction() {
	   this.db.setTransactionSuccessful();
	   this.db.endTransaction();
   }
   public void deleteAll() {
      this.db.delete(TABLE_NAME, null, null);
   }

   public List<String> selectAll(long account_id) {
      List<String> list = new ArrayList<String>();
      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" }, 
        "id="+String.valueOf(account_id), null, null, null, "name desc");
      if (cursor.moveToFirst()) {
         do {
            list.add(cursor.getString(0)); 
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }

   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + TABLE_NAME + " (\"id\" INTEGER NOT NULL, \"name\" TEXT NOT NULL, PRIMARY KEY (\"id\", \"name\"))");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("DATABASE", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}
