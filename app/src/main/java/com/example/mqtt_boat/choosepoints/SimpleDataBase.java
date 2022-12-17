package com.example.mqtt_boat.choosepoints;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SimpleDataBase extends SQLiteOpenHelper {
    private static String name="simpleData.db";
    private static int version = 1;
    public SimpleDataBase(@Nullable Context context) {
        super(context, name, null, version);  //内容，数据库名称，null
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStr = "create table user(编号 varchar(20),经度 varchar(20),纬度 varchar(20))";
        db.execSQL(sqlStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
