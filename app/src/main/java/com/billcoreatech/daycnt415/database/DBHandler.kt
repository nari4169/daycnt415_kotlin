package com.billcoreatech.daycnt415.database

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

class DBHandler(context: Context) : RuntimeException() {
    var helper: DBHelper
    var db: SQLiteDatabase
    var tableName = "dayinfo"
    var TAG = "dayinfo"

    init {
        helper = DBHelper(context)
        db = helper.writableDatabase
    }

    fun close() {
        helper.close()
    }

    fun selectAll(): Cursor {
        val sb = StringBuffer()
        sb.append("select * from $tableName ")
        sb.append("order by mdate desc   ")
        return db.rawQuery(sb.toString(), null)
    }

    fun getTodayMsg(mDate: String): Cursor {
        val strMsg = ""
        val sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate <= '" + mDate.replace("-".toRegex(), "") + "' ")
        sb.append(" order by mdate desc            ")
        return db.rawQuery(sb.toString(), null)
    }

    @SuppressLint("Range")
    fun getIsHoliday(mDate: String): String {
        var isHoliday = "N"
        val sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate = '" + mDate!!.replace("-".toRegex(), "") + "' ")
        val rs = db.rawQuery(sb.toString(), null)
        if (rs.moveToNext()) {
            isHoliday = rs.getString(rs.getColumnIndex("isholiday"))
        }
        return isHoliday
    }

    @SuppressLint("Range")
    fun getBfDay(mDate: String): String {
        var isHoliday = "M"
        var bfDay = ""
        val sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate <= '" + mDate!!.replace("-".toRegex(), "") + "' ")
        sb.append(" order by mdate desc            ")
        val rs = db.rawQuery(sb.toString(), null)
        while (rs.moveToNext()) {
            if ("M" == isHoliday) {
                isHoliday = rs.getString(rs.getColumnIndex("isholiday"))
            }
            //Log.i(TAG, "bfDay=" + bfDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (isHoliday != rs.getString(rs.getColumnIndex("isholiday"))) {
                bfDay = rs.getString(rs.getColumnIndex("mdate"))
                break
            }
        }
        //Log.i(TAG, "Result bfDay =" + bfDay) ;
        return bfDay
    }

    @SuppressLint("Range")
    fun getAfDay(mDate: String): String {
        var isHoliday = "M"
        var afDay = ""
        var sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate >= '" + mDate!!.replace("-".toRegex(), "") + "' ")
        sb.append(" order by mdate ")
        var rs = db.rawQuery(sb.toString(), null)
        while (rs.moveToNext()) {
            if ("M" == isHoliday) {
                isHoliday = rs.getString(rs.getColumnIndex("isholiday"))
            }
            //Log.i(TAG, "afDay=" + afDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (isHoliday != rs.getString(rs.getColumnIndex("isholiday"))) {
                afDay = rs.getString(rs.getColumnIndex("mdate"))
                isHoliday = rs.getString(rs.getColumnIndex("isholiday"))
                break
            }
        }
        // 마지막 날짜를 찾아야 하기 떄문애 다시 역순으로 찾아감.
        sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate <= '$afDay' ")
        sb.append(" order by mdate desc ")
        rs = db.rawQuery(sb.toString(), null)
        while (rs.moveToNext()) {
            //Log.i(TAG, "afDay=" + afDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (isHoliday != rs.getString(rs.getColumnIndex("isholiday"))) {
                afDay = rs.getString(rs.getColumnIndex("mdate"))
                break
            }
        }
        //Log.i(TAG, "Result afDay =" + afDay) ;
        return afDay
    }

    fun insertDayinfo(mDate: String, msg: String, dayOfweek: String, isHoliday: String): Long {
        var _id: Long = 0
        val values = ContentValues()
        values.put("mdate", mDate)
        values.put("msg", msg)
        values.put("dayOfweek", dayOfweek)
        values.put("isholiday", isHoliday)
        _id = db.insert(tableName, null, values)
        return _id
    }

    fun updateDayinfo(mDate: String, msg: String, isHoliday: String): Long {
        var _id: Long = 0
        val values = ContentValues()
        values.put("mdate", mDate)
        values.put("msg", msg)
        values.put("isholiday", isHoliday)
        _id = db.update(tableName, values, " mdate = '$mDate' ", null).toLong()
        return _id
    }

    fun deleteDayinfo(mDate: String): Long {
        var _id: Long = 0
        _id = db.delete(tableName, " mdate = '$mDate' ", null).toLong()
        return _id
    }

    @SuppressLint("Range")
    fun getTomorrow(mDate: String): String {
        var afDay = ""
        val sb = StringBuffer()
        sb.append(" select * from $tableName ")
        sb.append(" where mdate > '" + mDate!!.replace("-".toRegex(), "") + "' ")
        sb.append(" order by mdate ")
        val rs = db.rawQuery(sb.toString(), null)
        if (rs.moveToNext()) {
            afDay = rs.getString(rs.getColumnIndex("mdate"))
        }
        return afDay
    }

    companion object {
        @Throws(SQLException::class)
        fun open(ctx: Context): DBHandler {
            return DBHandler(ctx)
        }
    }
}