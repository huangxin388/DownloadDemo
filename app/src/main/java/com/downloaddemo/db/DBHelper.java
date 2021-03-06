package com.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper mInstance = null;
    public static final String DB_NAME = "download.db";
    public static final int DB_VERSION = 2;
    public static final String CREATE_TABLE = "create table thread_info(" +
            "_id integer primary key autoincrement,thread_id integer,url text," +
            "start integer,endl integer,loaded integer)";
    public static final String DROP_TABLE = "drop table if exists thread_info";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if(mInstance == null) {
            synchronized (DBHelper.class) {
                if(mInstance == null) {
                    mInstance = new DBHelper(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
