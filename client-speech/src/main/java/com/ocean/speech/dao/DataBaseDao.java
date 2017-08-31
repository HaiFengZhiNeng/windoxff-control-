package com.ocean.speech.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.ocean.speech.SpeechApplication;
import com.ocean.speech.control.InterfaceBean;
import com.ocean.speech.dbhelper.DBHelper;

import java.util.ArrayList;

/**
 * Created by zhangyuanyuan on 2017/7/11.
 */

public class DataBaseDao {
    private static String TABLE_NAME = "INTERFACE";

    private Context context;
    private final DBHelper dbHelper;
    private SpeechApplication application;

    private SQLiteDatabase db;

    public DataBaseDao(Context context) {
        this.context = context;
        application = SpeechApplication.from(context);
        dbHelper = application.getDataBase();
    }

    /**
     * 插入数据
     *
     * @param beans 数据集
     */
    public void insert(ArrayList<InterfaceBean> beans) {
        synchronized (dbHelper) {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();//开启事务

            try {
                int len = beans.size();
                /**
                 * 插入问题
                 */
                for (int i = 0; i < len; i++) {
                    ContentValues values = new ContentValues();
                    values.put("code", beans.get(i).getId());
                    values.put("content", beans.get(i).getContent());
                    db.insert(TABLE_NAME, null, values);
                }


                /**
                 * 设置批量插入成功
                 */
                db.setTransactionSuccessful();
            } finally {
                /**
                 * 结束事务
                 */
                db.endTransaction();
                /**
                 * 关闭数据库
                 */
                dbHelper.closeDatabase();
            }

        }
    }

    /**
     * 查询所有的数据
     * @return 数据集
     */
    public ArrayList<InterfaceBean> queryAll() {
        synchronized (dbHelper) {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ArrayList<InterfaceBean> beans = null;
            try {
                Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
                if (cursor != null) {
                    beans = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        String content = cursor.getString(cursor.getColumnIndex("content"));
                        int id = cursor.getInt(cursor.getColumnIndex("code"));
                        beans.add(new InterfaceBean(content,id));
                    }
                    cursor.close();
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                dbHelper.closeDatabase();
            }
            return beans;
        }

    }

    /**
     * 清空数据库
     */
    public void clear(){
        db = dbHelper.getWritableDatabase();
        /**
         * 开启事务
         */
        try {
            db.beginTransaction();
            db.delete(TABLE_NAME, null, null);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static String createSQL() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(_id  integer primary key autoincrement," +
                "code integer," +
                "content text)";
    }
}
