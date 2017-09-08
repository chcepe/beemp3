package com.cepe.bee.mp3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DownloadsDBAdapter {
	private static final String DATABASE_NAME = "downloads";
	private static final String DATABASE_TABLE = "downloadqueue";
	private static final int DATABASE_VERSION = 2;
	
	public static final String KEY_URL = "url";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DOWNLOADED = "downloaded";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SIZE = "dlsize";
	public static final String KEY_LISTVIEW = "lvid";
	public static final String KEY_SID = "sid";
	public static final String KEY_DATE = "date";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static final String DATABASE_CREATE = 
		"create table " + DATABASE_TABLE + " (" 
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_TITLE + " text not null, "
			+ KEY_URL + " text not null, "
			+ KEY_DOWNLOADED + " double, "
			+ KEY_SIZE + " double, "
			+ KEY_LISTVIEW + " integer, "
			+ KEY_SID + " integer, "
			+ KEY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP);";
	
	private final Context mCtx;
	
	public DownloadsDBAdapter(Context ctx){
		this.mCtx = ctx;
	}
	
	public DownloadsDBAdapter open() throws SQLException{
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDbHelper.close();
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
			if(oldVersion == 1){
				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_SID + " integer");
			}
		}
	}

	public long createDownload(String title, String url, int i, int a) {
		ContentValues initialValues = new ContentValues();
		
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_URL, url);
		initialValues.put(KEY_DOWNLOADED, i);
		initialValues.put(KEY_SIZE, a);
		
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
    public boolean deleteDownload(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteDownloads() {
        return mDb.delete(DATABASE_TABLE, KEY_DOWNLOADED + "<= 0", null) > 0;
    }
    
	public Cursor fetchAllDownloads(){
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
				 KEY_URL, KEY_DOWNLOADED, KEY_SIZE, KEY_DATE}, null, null, null, null, null);
	}
	
	public Cursor fetchAllDownloads(int downloaded){
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
				 KEY_URL, KEY_DOWNLOADED, KEY_SIZE, KEY_DATE}, "downloaded <= '"+downloaded+"'", null, null, null, KEY_DOWNLOADED+" DESC, "+KEY_DATE+" DESC");
	}
	
	public Cursor getEachDownload(int downloaded){
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
				 KEY_URL, KEY_DOWNLOADED, KEY_SIZE, KEY_DATE}, "downloaded = '"+downloaded+"'", null, null, null, KEY_DOWNLOADED+" DESC, "+KEY_DATE+" DESC");
	}
	
	public int fetchAllDownloads(String url){
		Cursor data = mDb.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE url=\""+url+"\" LIMIT 1;", null);
		int exists = data.getCount();
		data.close();
		return exists;
	}
	
	public Cursor fetchAllDownloadsa(String url){
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DOWNLOADED}, "url=\""+url+"\"", null, null, null, null);
	}
	
	public Cursor fetchId(long id){
        Cursor mCursor = mDb.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE _id="+id+" LIMIT 1;", null);
        if (mCursor != null) {
        	mCursor.moveToFirst();
        }
        return mCursor;
	}
	
    public boolean updateDownloaded() {
        ContentValues args = new ContentValues();
        args.put(KEY_DOWNLOADED, 1);
        return mDb.update(DATABASE_TABLE, args, "downloaded=2",null) > 0;
    }

    public boolean updateDownloaded(long rowId, String downloaded) {
        ContentValues args = new ContentValues();
        args.put(KEY_DOWNLOADED, downloaded);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateDownloaded(long rowId, String downloaded, double sid) {
        ContentValues args = new ContentValues();
        args.put(KEY_DOWNLOADED, downloaded);
        args.put(KEY_SID, sid);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateDownloadSize(long rowId, int size) {
        ContentValues args = new ContentValues();
        args.put(KEY_SIZE, size);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updatelvid(long rowId, int lvid) {
        ContentValues args = new ContentValues();
        args.put(KEY_LISTVIEW, lvid);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
	public Cursor fetchnondownloaded() throws SQLException {
        Cursor mCursor = mDb.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE downloaded=1 ORDER BY RANDOM() LIMIT 1;", null);
        if (mCursor != null) {
        	mCursor.moveToFirst();
        }
        return mCursor;
	}
	
	public int fetchtotal() {
	    SQLiteStatement dbJournalCountQuery;
	    dbJournalCountQuery = mDb.compileStatement("select count(*) from "+DATABASE_TABLE+" WHERE downloaded>=1");
	    return (int) dbJournalCountQuery.simpleQueryForLong();
	}
	
	public int fetchtotaler() {
	    SQLiteStatement dbJournalCountQuery;
	    dbJournalCountQuery = mDb.compileStatement("select count(*) from "+DATABASE_TABLE);
	    return (int) dbJournalCountQuery.simpleQueryForLong();
	}
}