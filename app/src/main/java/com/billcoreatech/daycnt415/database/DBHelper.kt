package com.billcoreatech.daycnt415.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_Ver) {

    var TAG = DB_NAME + ":"
    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuffer()
        sb.append("create table dayinfo (                    ")
        sb.append("   _id integer primary key autoincrement, ")
        sb.append("   mdate text,                            ")
        sb.append("   msg   text,                            ")
        sb.append("   dayOfweek text,                        ")
        sb.append("   isholiday text                         ")
        sb.append(" )                                        ")
        db.execSQL(sb.toString())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val DB_NAME = "HolidayInfo"
        private const val DB_Ver = 1 //
    }
}