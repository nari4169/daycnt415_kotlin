package com.billcoreatech.daycnt415.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import com.billcoreatech.daycnt415.R
import com.billcoreatech.daycnt415.databinding.AppsFinishViewBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object StringUtil {

    var TAG = "StringUtil"
    @SuppressLint("StaticFieldLeak")
    lateinit var binding: AppsFinishViewBinding
    fun showSnackbarAd(
        context: Activity, adRequest: AdRequest?, mainTextStringId: Int, actionStringId: Int,
        isBill: Boolean,
        listener: View.OnClickListener?
    ) {
        val snackbar = Snackbar.make(
            context.findViewById(android.R.id.content),
            context.getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(context.getString(actionStringId), listener)
        binding = AppsFinishViewBinding.inflate(
            context.layoutInflater
        )
        binding.adView.loadAd(adRequest!!)
        if (isBill) {
            binding.adView.visibility = View.GONE
        }
        val views: View = binding.root
        val snackbarLayout = snackbar.view as SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(views)
        snackbar.setBackgroundTint(context.getColor(R.color.softblue))
        snackbar.show()
    }

    val today: String
        get() {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            return sdf.format(date)
        }

    fun getTodayTerm1(context: Context?, sD2: String?, sTime: String?): Long {
        var sec: Long = 0
        val f = SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA)
        val now = System.currentTimeMillis()
        val date = Date(now)
        try {
            val d1 = f.parse(f.format(date))
            val d2 = f.parse(sD2 + " " + sTime!!.replace(":".toRegex(), ""))
            val diff = d1.time - d2.time
            sec = diff / 1000 / 60
        } catch (e: Exception) {
        }
        return sec
    }

    fun getTimeTerm(
        context: Context?,
        sD1: String?,
        eTime: String?,
        sD2: String?,
        sTime: String?
    ): Long {
        var sec: Long = 0
        val f = SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA)
        try {
            val d1 = f.parse(sD1 + " " + eTime!!.replace(":".toRegex(), ""))
            val d2 = f.parse(sD2 + " " + sTime!!.replace(":".toRegex(), ""))
            val diff = d1.time - d2.time
            sec = diff / 1000 / 60
        } catch (e: Exception) {
        }
        return sec
    }

    fun getFriday(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = Date()
        try {
            date = sdf.parse(dateString)
        } catch (e: ParseException) {
        }
        val cal = Calendar.getInstance(Locale.KOREA)
        cal.time = date
        cal.add(Calendar.DATE, Calendar.FRIDAY - cal[Calendar.DAY_OF_WEEK])
        return sdf.format(cal.time)
    }

    fun getSunday(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = Date()
        try {
            date = sdf.parse(dateString)
        } catch (e: ParseException) {
        }
        val cal = Calendar.getInstance(Locale.KOREA)
        cal.time = date
        cal.add(Calendar.DATE, Calendar.SUNDAY - cal[Calendar.DAY_OF_WEEK])
        return sdf.format(cal.time)
    }

    fun getDispDay(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val sdf1 = SimpleDateFormat("MM-dd")
        var date: Date? = Date()
        try {
            date = sdf.parse(dateString)
        } catch (e: ParseException) {
        }
        return sdf1.format(date)
    }

    fun getDispDayYMD(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val sdf1 = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = Date()
        try {
            date = sdf.parse(dateString)
        } catch (e: ParseException) {
        }
        return sdf1.format(date)
    }

    fun addMonth(pDate: Date?, iTerm: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = pDate
        cal.add(Calendar.MONTH, iTerm)
        val sdf = SimpleDateFormat("yyyyMMdd")
        //Log.i(TAG, sdf.format(new Date(cal.getTimeInMillis()))) ;
        return Date(cal.timeInMillis)
    }

    fun getDay(pDate: Date?, iTerm: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = pDate
        cal[Calendar.MONTH] = iTerm - 1
        val sdf = SimpleDateFormat("yyyyMMdd")
        //Log.i(TAG, sdf.format(new Date(cal.getTimeInMillis()))) ;
        return Date(cal.timeInMillis)
    }

    fun getDate(ltime: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd")
        val nDate = Date(ltime)
        return sdf.format(nDate)
    }
}