package com.example.abhishekjpr.newmovieproject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by abhishekjpr on 14/6/16.
 */
public class MovieDataProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.example.abhishekjpr.provider";
    static final String URL = "content://" + PROVIDER_NAME + "/moviedata";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String MOVIE_ID = "movie_id"; //Movie ID as Primary Key
    static final String MOVIE_POSTER = "movie_poster";

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "my_movie_database";
    static final String MOVIE_TABLE_NAME = "fav_movies";
    static final int DATABASE_VERSION = 2;
    static final String CREATE_DB_TABLE = "CREATE TABLE " + MOVIE_TABLE_NAME + "("+
            MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MOVIE_POSTER + " BLOB);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(CREATE_DB_TABLE);
            }
            catch (Exception e){
                System.out.println("Error Creating Table: "+e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  MOVIE_TABLE_NAME);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	MOVIE_TABLE_NAME, "", values);
        /**
         * If record is added successfully
         */
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MOVIE_TABLE_NAME);
        if (sortOrder == null || sortOrder == ""){
            /**
             * By default sort on student names
             */
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, null);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public String getType(Uri uri) {
        return null;
    }

}
