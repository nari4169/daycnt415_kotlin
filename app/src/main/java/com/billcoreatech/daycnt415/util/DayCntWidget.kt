package com.billcoreatech.daycnt415.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.billcoreatech.daycnt415.R
import com.billcoreatech.daycnt415.database.DBHandler
import com.billcoreatech.daycnt415.util.DayCntWidget
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [DayCntWidgetConfigureActivity]
 */
class DayCntWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        var action: String = intent.action.toString()
        val option = context.getSharedPreferences("option", Context.MODE_PRIVATE)
        val editor = option.edit()
        WIDGET_UPDATE_INTERVAL = option.getInt("term", 60000) * 60000
        Log.i(TAG, "onReceive -------------" + WIDGET_UPDATE_INTERVAL)
        if (action == null) {
        } else if (action == "android.appwidget.action.APPWIDGET_UPDATE") {
            Log.i(TAG, "android.appwidget.action.APPWIDGET_UPDATE")
            val nexttime = SystemClock.elapsedRealtime() + WIDGET_UPDATE_INTERVAL
            removePreviousAlarm()
            pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            mManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mManager!![AlarmManager.ELAPSED_REALTIME, nexttime] = pendingIntent
            if (!option.getBoolean("isBill", false)) {
                KakaoToast.makeToast(
                    context,
                    context.getString(R.string.msgAdView),
                    Toast.LENGTH_LONG
                ).show()
                KakaoToast.makeToast(
                    context,
                    context.getString(R.string.msgAdView),
                    Toast.LENGTH_LONG
                ).show()
            }
            val oldDate = option.getLong("billTimeStamp", System.currentTimeMillis())
            try {
                /**
                 * 29일 경과 하면 다시 구매하도록 광고를 보여야 함.
                 */
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val termDate = (System.currentTimeMillis() - oldDate) / 1000 / 60 / 60 / 24
                Log.i(
                    TAG, "결제일자=" + sdf.format(Date(System.currentTimeMillis())) + "~" + sdf.format(
                        Date(oldDate)
                    ) + " 경과일수=" + termDate
                )
                Log.i(TAG, "경과시간(ms)=" + (System.currentTimeMillis() - oldDate) + " ")
                if (termDate > 29) {
                    editor.putBoolean("isBill", false)
                    editor.commit()
                    Log.i(TAG, "isBill=" + option.getBoolean("isBill", false))
                }
            } catch (e: Exception) {
            }
            val views = RemoteViews(context.packageName, R.layout.day_cnt_widget)
            onDispDayTerm(context, views)
        } else if (action == "android.appwidget.action.APPWIDGET_DISABLED") {
            Log.w(TAG, "android.appwidget.action.APPWIDGET_DISABLED")
            removePreviousAlarm()
        }
    }

    private fun removePreviousAlarm() {
        if (mManager != null && mSender != null) {
            mSender.cancel()
            mManager.cancel(mSender)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        /*SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        WIDGET_UPDATE_INTERVAL = option.getInt("term", 60000) * 60000;

        Log.i(TAG, "WIDGET_UPDATE_INTERVAL=" + WIDGET_UPDATE_INTERVAL) ;

        Intent intent = new Intent(context, DayCntWidget.class);
        intent.putExtra("mode","time");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), WIDGET_UPDATE_INTERVAL, pendingIntent);*/
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DayCntWidget::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent) //알람 해제
        pendingIntent.cancel() //인텐트 해제
    }

    companion object {
        private var WIDGET_UPDATE_INTERVAL = 60000 // 1분 주기 갱신
        private lateinit var mSender: PendingIntent
        private lateinit var mManager: AlarmManager
        lateinit var pendingIntent: PendingIntent
        var TAG = "DayCntWidget---"
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetText: CharSequence = ""
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.day_cnt_widget)
            onDispDayTerm(context, views)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun onDispDayTerm(context: Context, views: RemoteViews) {
            var dbHandler: DBHandler
            var mDate = StringUtil.today
            dbHandler = DBHandler.Companion.open(context)
            var bfDay = dbHandler.getBfDay(mDate)
            var afDay = dbHandler.getAfDay(mDate)
            var isHoliday = dbHandler.getIsHoliday(mDate)
            dbHandler.close()
            val option = context.getSharedPreferences("option", Context.MODE_PRIVATE)
            var sTime = option.getString("startTime", "18:00")
            var eTime = option.getString("closeTime", "24:00")
            if ("N" == isHoliday) {
                sTime = option.getString("closeTime", "24:00")
                eTime = option.getString("startTime", "18:00")
            }
            Log.i(TAG, "bfDay=$bfDay $sTime afDay=$afDay $eTime")
            val sdf = SimpleDateFormat("yyyyMMdd HH:mm")
            val sdf1 = SimpleDateFormat("yyyyMMdd")
            try {
                val endTime = sdf.parse("$afDay $eTime")
                val endTimeValue = endTime.time
                val now = System.currentTimeMillis()
                dbHandler = DBHandler.Companion.open(context)
                val ckDay = sdf1.format(now)
                /**
                 * 끝나는 날 : 끝나는 시간이 지나갔는지 확인하고 지나갔으면 평일/휴일을 변경해 주어야 함.
                 */
                Log.i(TAG, "bfDay = 날자가 지나갔나 ??? $endTimeValue $now")
                if (endTimeValue < now && afDay == ckDay) {
                    mDate = dbHandler.getTomorrow(mDate)
                    bfDay = dbHandler.getBfDay(mDate)
                    afDay = dbHandler.getAfDay(mDate)
                    isHoliday = dbHandler.getIsHoliday(mDate)
                    sTime = option.getString("startTime", "18:00")
                    eTime = option.getString("closeTime", "24:00")
                    if ("N" == isHoliday) {
                        sTime = option.getString("closeTime", "24:00")
                        eTime = option.getString("startTime", "18:00")
                    }
                }
                dbHandler.close()
            } catch (e: Exception) {
            }
            val progress = option.getInt("transparent", 100)
            when (Math.floorDiv(progress, 10)) {
                10 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_10
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_10
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.black))
                }
                9 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_9
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_9
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.black))
                }
                8 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_8
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_8
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.black))
                }
                7 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_7
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_7
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.black))
                }
                6 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_6
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_6
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.black))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.black))
                }
                5 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_5
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_5
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.softblue))
                }
                4 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_4
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_4
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.softblue))
                }
                3 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_3
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_3
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.softblue))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.softblue))
                }
                2 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_2
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_2
                    )
                    views.setInt(R.id.txtDayToDay1, "setTextColor", R.color.white)
                    views.setInt(R.id.txtHourTerm1, "setTextColor", R.color.white)
                    views.setInt(R.id.txtRate1, "setTextColor", R.color.white)
                    views.setTextColor(R.id.textView12, context.getColor(R.color.white))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.white))
                }
                1 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_1
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_1
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.white))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.white))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.white))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.white))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.white))
                }
                0 -> {
                    views.setInt(
                        R.id.layout1,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_0
                    )
                    views.setInt(
                        R.id.layout2,
                        "setBackgroundResource",
                        R.drawable.backgroud_border_0
                    )
                    views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.white))
                    views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.white))
                    views.setTextColor(R.id.txtRate1, context.getColor(R.color.white))
                    views.setTextColor(R.id.textView12, context.getColor(R.color.white))
                    views.setTextColor(R.id.textView13, context.getColor(R.color.white))
                }
            }
            views.setTextViewText(
                R.id.txtDayToDay1,
                StringUtil.getDispDay(bfDay) + " " + sTime + " ~ " + StringUtil.getDispDay(afDay) + " " + eTime
            )
            val b = StringUtil.getTimeTerm(context, afDay, eTime, bfDay, sTime).toDouble()
            val j = StringUtil.getTodayTerm1(context, bfDay, sTime).toDouble()
            views.setTextViewText(
                R.id.txtHourTerm1,
                Math.round(j / 60).toString() + "/" + Math.round(b / 60).toString()
            )
            views.setTextViewText(R.id.txtRate1, String.format("%.2f", j / b * 100))
            views.setProgressBar(R.id.progressBar1, 100, Math.round(j / b * 100).toInt(), false)
            Log.i(TAG, "rate=" + String.format("%.2f", j / b * 100))
        }

        fun GetDrawable(drawableResId: Int, color: Int, context: Context): Drawable {
            val drawable = context.resources.getDrawable(drawableResId)
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            return drawable
        }
    }
}