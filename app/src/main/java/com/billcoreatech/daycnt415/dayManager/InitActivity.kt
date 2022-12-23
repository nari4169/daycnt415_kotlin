package com.billcoreatech.daycnt415.dayManager

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.billcoreatech.daycnt415.MainActivity
import com.billcoreatech.daycnt415.R
import com.billcoreatech.daycnt415.billing.BillingManager
import com.billcoreatech.daycnt415.database.DBHandler
import com.billcoreatech.daycnt415.databinding.ActivityInitBinding
import com.billcoreatech.daycnt415.util.Holidays
import com.billcoreatech.daycnt415.util.LunarCalendar
import com.github.anrwatchdog.ANRWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InitActivity : AppCompatActivity() {
    var TAG = "InitActivity="
    private lateinit var binding: ActivityInitBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var holidays: ArrayList<Holidays>
    var sdf = SimpleDateFormat("yyyyMMdd")
    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        holidays = ArrayList()

        // new ANRWatchDog().start();
        ANRWatchDog().setANRListener { error -> // Handle the error. For example, log it to HockeyApp:
            // ExceptionHandler.saveException(error, new CrashManager());
            Log.e(TAG, "ANR ERROR = $error")
        }.start()

        sharedPreferences = getSharedPreferences("holidayData", MODE_PRIVATE)
        if ("N" != sharedPreferences.getString("INIT", "N")) {
            val intent = Intent(this@InitActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }



        binding.btnInit.setOnClickListener {
            val builder = AlertDialog.Builder(this@InitActivity)
            builder.setTitle(getString(R.string.InitOK))
                .setMessage(getString(R.string.msgInitOK))
                .setPositiveButton(getString(R.string.OK)) { dialog, which ->

                    binding.baseProgressBar.visibility = View.VISIBLE
                    binding.btnInit.visibility = View.GONE

// 대기 시간이 너무 올래 걸리기 때문에 수정
                    CoroutineScope(Dispatchers.Main).launch {
                        val html = CoroutineScope(Dispatchers.IO).async {

                            val cal = Calendar.getInstance()
                            val year = cal[Calendar.YEAR]
                            dbHandler = DBHandler.open(applicationContext)
                            for (iYear in year until year + 5) {
                                holidays.clear()
                                holidays = LunarCalendar.holidayArray(iYear.toString())
                                for (iMonth in 1..12) {
                                    cal[Calendar.YEAR] = iYear
                                    cal[Calendar.MONTH] = iMonth
                                    for (iDay in 0..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                                        cal[Calendar.DAY_OF_MONTH] = iDay
                                        val todayMsg = getDayMsg(sdf.format(cal.time)).toString()
                                        val dayOfweek = cal[Calendar.DAY_OF_WEEK]
                                        var isHoliday = "N"
                                        if ("" != todayMsg) {
                                            isHoliday = "Y"
                                        } else if (dayOfweek == Calendar.SATURDAY || dayOfweek == Calendar.SUNDAY) {
                                            isHoliday = "Y"
                                        }
                                        dbHandler.deleteDayinfo(sdf.format(cal.time))
                                        val id = dbHandler.insertDayinfo(
                                            sdf.format(cal.time),
                                            todayMsg,
                                            dayOfweek.toString(),
                                            isHoliday
                                        )
                                        Log.i(
                                            TAG,
                                            id.toString() + "=" + sdf.format(cal.time) + "," + todayMsg + "," + dayOfweek.toString() + "," + isHoliday
                                        )
                                    }
                                }
                            }
                            dbHandler.close()
                            val editor = sharedPreferences.edit()
                            editor.putString("INIT", "Y")
                            editor.commit()
                        }.await()

                        binding.baseProgressBar.visibility = View.GONE
                        val intent = Intent(this@InitActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getDayMsg(pDate: String): String {
        var sMsg = ""
        for (i in holidays.indices) {
            if (pDate == holidays[i].year + holidays[i].date) {
                sMsg = holidays[i].name
                break
            }
        }
        return sMsg
    }
}