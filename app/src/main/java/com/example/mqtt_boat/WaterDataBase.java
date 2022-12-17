package com.example.mqtt_boat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class WaterDataBase extends SQLiteOpenHelper {
    private static String name="waterdata.db";
    private static int version = 1;
    public WaterDataBase(@Nullable Context context) {
        super(context, name, null, version);  //内容，数据库名称，null
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStr = "create table user(采样点 varchar(20),时间 varchar(20),水质 varchar(20))";
        db.execSQL(sqlStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
