package com.kokufu.android.sample.sqliteviewer.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DummyProvider extends ContentProvider {
    private static final String DB_NAME = "dummy.db";
    private static final String TABLE_NAME = "dummy_table";

    DatabaseOpenHelper mDatabaseOpenHelper;

    @Override
    public boolean onCreate() {
        mDatabaseOpenHelper = new DatabaseOpenHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        public DatabaseOpenHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " +
                       TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, data REAL)");
            db.execSQL("INSERT INTO " + TABLE_NAME + " values(1, \"a\", 0.5)");
            db.execSQL("INSERT INTO " + TABLE_NAME + " values(2, \"b\", 0.4)");
            db.execSQL("INSERT INTO " + TABLE_NAME + " values(3, \"c\", 0.3)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }
}
