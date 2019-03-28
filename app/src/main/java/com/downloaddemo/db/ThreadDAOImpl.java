package com.downloaddemo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.downloaddemo.bean.ThreadBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库接口是实现类
 */
public class ThreadDAOImpl implements ThreadDAO{

    private Context mContext;
    private DBHelper mHelper;
    private static final String TAG = "ThreadDAOImpl";

    public ThreadDAOImpl(Context context) {
        mContext = context;
        mHelper = new DBHelper(context);
    }

    @Override
    public void insertThread(ThreadBean threadBean) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,endl,loaded) values(?,?,?,?,?)",
                new Object[]{threadBean.getId(),threadBean.getUrl(),threadBean.getStart(),threadBean.getEnd(),threadBean.getLoaded()});
        db.close();
    }

    @Override
    public void deleteThread(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url=? and thread_id=?",
                new Object[]{url,thread_id});
        db.close();
    }

    @Override
    public void updateThread(String url, int thread_id, int loaded) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set loaded=? where url=? and thread_id=?",
                new Object[]{loaded,url,thread_id});
        db.close();
    }

    @Override
    public List<ThreadBean> getThreads(String url) {
        List<ThreadBean> list = new ArrayList<>();
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url=?",new String[]{url});
        while (cursor.moveToNext()) {
            ThreadBean bean = new ThreadBean();
            bean.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            bean.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            bean.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            bean.setEnd(cursor.getInt(cursor.getColumnIndex("endl")));
            bean.setLoaded(cursor.getInt(cursor.getColumnIndex("loaded")));
            list.add(bean);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url=?",new String[]{url});
        boolean exists = cursor.moveToNext();
        cursor.close();
        return exists;
    }
}
