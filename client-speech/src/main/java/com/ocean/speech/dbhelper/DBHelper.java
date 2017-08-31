package com.ocean.speech.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ocean.speech.dao.DataBaseDao;


/**
 * Created by zhangyuanyuan on 2017/7/11.
 */

public class DBHelper extends SQLiteOpenHelper {

    /**
     * 数据库版本
     */
    public final static int DB_VERSION = 10;

    /**
     * 数据库名称
     */
    private final static String DB_NAME = "seabreeze.db";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    /**
     * 多线程下关闭
     */
    public synchronized void closeDatabase() {

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBaseDao.createSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
