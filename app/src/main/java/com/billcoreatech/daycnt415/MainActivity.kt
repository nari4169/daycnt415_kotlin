package com.billcoreatech.daycnt415

import android.annotation.SuppressLint
import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.billcoreatech.daycnt415.billing.BillingManager
import com.billcoreatech.daycnt415.database.DBHandler
import com.billcoreatech.daycnt415.databinding.ActivityMainBinding
import com.billcoreatech.daycnt415.databinding.DayinfoitemBinding
import com.billcoreatech.daycnt415.databinding.PopupWindowBinding
import com.billcoreatech.daycnt415.dayManager.DayinfoBean
import com.billcoreatech.daycnt415.util.GridAdapter
import com.billcoreatech.daycnt415.util.Holidays
import com.billcoreatech.daycnt415.util.StringUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.appset.AppSet
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var popupBinding: PopupWindowBinding
    lateinit var dayInfoBinding: DayinfoitemBinding
    val TAG = "MainActivity"
    private lateinit var option: SharedPreferences
    private lateinit var strUtil: StringUtil
    private lateinit var gridAdapter: GridAdapter
    private lateinit var dayinfoLists: ArrayList<DayinfoBean>
    private lateinit var holidays: ArrayList<Holidays>
    lateinit var dbHandler: DBHandler
    lateinit var dayList: ArrayList<String>
    lateinit var curYearFormat: SimpleDateFormat
    private lateinit var curMonthFormat: SimpleDateFormat
    private lateinit var detector: GestureDetectorCompat
    lateinit var pDate: Date
    private lateinit var sdf: SimpleDateFormat
    private lateinit var adRequest: AdRequest
    private lateinit var billingManager: BillingManager
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 101
    private val UPDATE_APP_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        option = getSharedPreferences("option", MODE_PRIVATE)
        sdf = SimpleDateFormat("yyyy-MM-dd")
        Log.i(
            TAG,
            "billTimeStamp=" + sdf.format(
                Date(
                    option.getLong(
                        "billTimeStamp",
                        System.currentTimeMillis()
                    )
                )
            )
        )

        dayinfoLists = ArrayList()
        holidays = ArrayList()
        curYearFormat = SimpleDateFormat("yyyy")
        curMonthFormat = SimpleDateFormat("MM")
        detector = GestureDetectorCompat(this, MyGestureListener())
        strUtil = StringUtil

        MobileAds.initialize(this, OnInitializationCompleteListener { })
        adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // This example applies an immediate update. To apply a flexible update
                        // instead, pass in AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            ) {
                // Request the update.
                try {
                    doUpdateApps(appUpdateManager, appUpdateInfo)
                } catch (e: SendIntentException) {
                    Log.e(TAG, "update Error...")
                }
            }
        }
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task: Task<ReviewInfo> ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                doReviewUpdate(manager, this@MainActivity, reviewInfo)
            } else {
                @ReviewErrorCode val reviewErrorCode: Int = task.getException().hashCode()
            }
        }

        binding.gridView.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                detector.onTouchEvent(event)
                return false
            }
        })
        binding.btnSetting.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.txtYearMonth.setOnClickListener { v -> onButtonShowPopupWindowClick(v, pDate) }
        binding.gridView.onItemClickListener = object : OnItemClickListener {
            @SuppressLint("Range")
            override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                dayInfoBinding = DayinfoitemBinding.inflate(
                    layoutInflater
                )
                val dayInfoView: View = dayInfoBinding.root
                dbHandler = DBHandler.open(applicationContext)
                val rs: Cursor = dbHandler.getTodayMsg(dayList[position])
                var msg = ""
                var isHoliday = "N"
                if (rs.moveToNext()) {
                    msg = rs.getString(rs.getColumnIndex("msg"))
                    isHoliday = rs.getString(rs.getColumnIndex("isholiday"))
                }
                dbHandler.close()
                dayInfoBinding.txtMsg.setText(msg)
                dayInfoBinding.checkBox.isChecked = ("Y" == isHoliday)
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(StringUtil.getDispDayYMD(dayList.get(position)))
                    .setMessage(getString(R.string.msgEnterHoliday))
                    .setView(dayInfoView)
                    .setPositiveButton(
                        getString(R.string.OK)
                    ) { dialog, which ->
                        dbHandler = DBHandler.Companion.open(applicationContext)
                        var isHoliday = "N"
                        if (dayInfoBinding.checkBox.isChecked) {
                            isHoliday = "Y"
                        }
                        dbHandler.updateDayinfo(
                            dayList.get(position),
                            dayInfoBinding.txtMsg.text.toString(),
                            isHoliday
                        )
                        dbHandler.close()
                        getDispMonth(pDate)
                    }
                    .setNegativeButton(getString(R.string.close), null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(getColor(R.color.softblue))
            }
        }
        idAndLAT
    }

    @Throws(SendIntentException::class)
    private fun doUpdateApps(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,  // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            AppUpdateType.IMMEDIATE,  // The current activity making the update request.
            this,  // Include a request code to later monitor this update request.
            UPDATE_APP_REQUEST
        )
    }

    private fun doReviewUpdate(
        manager: ReviewManager,
        mainActivity: MainActivity,
        reviewInfo: ReviewInfo
    ) {
        val flow = manager.launchReviewFlow(mainActivity, (reviewInfo))
        flow.addOnCompleteListener { task: Task<Void?> ->
            Log.e(
                TAG,
                "result=" + task.isSuccessful
            )
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_APP_REQUEST) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.msgAppUpdateCompletedForRestart),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
//        long tempTime = System.currentTimeMillis();
//        long intervalTime = tempTime - backPressedTime;
//
//        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
//        {
//            super.onBackPressed();
//        }
//        else
//        {
//            backPressedTime = tempTime;
//            KakaoToast.makeToast(getApplicationContext(), getString(R.string.msgBackPress), Toast.LENGTH_LONG).show();
//        }
        // 종료 메시지 처리 방법 변경
        StringUtil.showSnackbarAd(this, adRequest,
            R.string.msgBackPress, R.string.Ok,
            option.getBoolean("isBill", false),
            object : View.OnClickListener {
                override fun onClick(view: View) {
                    finish()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        billingManager = BillingManager(this@MainActivity)
        val mDate = StringUtil.today
        pDate = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        try {
            pDate = sdf.parse(mDate)
            getDispMonth(pDate)
        } catch (e: ParseException) {
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "isBill=" + option.getBoolean("isBill", false))
        if (option.getBoolean("isBill", false)) {
            binding.adView.visibility = View.GONE
        } else {
            binding.adView.visibility = View.VISIBLE
        }
    }

    fun getDispMonth(pDate: Date) {
        var mDate = StringUtil.today
        dbHandler = DBHandler.Companion.open(applicationContext)
        var bfDay = dbHandler.getBfDay(mDate)
        var afDay = dbHandler.getAfDay(mDate)
        var isHoliday = dbHandler.getIsHoliday(mDate)
        dbHandler.close()
        var sTime = option.getString("startTime", "18:00")
        var eTime = option.getString("closeTime", "24:00")
        if (("N" == isHoliday)) {
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
            dbHandler = DBHandler.Companion.open(applicationContext)
            val ckDay = sdf1.format(now)
            /**
             * 끝나는 날 : 끝나는 시간이 지나갔는지 확인하고 지나갔으면 평일/휴일을 변경해 주어야 함.
             */
            Log.i(TAG, "bfDay = 시간이 지나갔나 ??? $endTimeValue $now $afDay $ckDay")
            if (endTimeValue < now && (afDay == ckDay)) {
                mDate = dbHandler.getTomorrow(mDate)
                bfDay = dbHandler.getBfDay(mDate)
                afDay = dbHandler.getAfDay(mDate)
                isHoliday = dbHandler.getIsHoliday(mDate)
                sTime = option.getString("startTime", "18:00")
                eTime = option.getString("closeTime", "24:00")
                if (("N" == isHoliday)) {
                    sTime = option.getString("closeTime", "24:00")
                    eTime = option.getString("startTime", "18:00")
                }
            }
            dbHandler.close()
        } catch (e: Exception) {
        }
        binding.txtDayToDay.text = (StringUtil.getDispDay(bfDay) + " " + sTime + " ~ "
                + StringUtil.getDispDay(afDay) + " " + eTime)
        val b = StringUtil.getTimeTerm(applicationContext, afDay, eTime, bfDay, sTime).toDouble()
        val j = StringUtil.getTodayTerm1(applicationContext, bfDay, sTime).toDouble()
        binding.txtHourTerm.text =
            Math.round(j / 60).toString() + "/" + Math.round(b / 60).toString() + " Hour"
        binding.txtRate.text = String.format("%.2f", j / b * 100) + "%"
        binding.progressBar.max = 100
        binding.progressBar.progress = Math.round(j / b * 100).toInt()
        binding.txtYearMonth.text =
            curYearFormat.format(pDate) + "." + curMonthFormat.format(pDate)
        dayList = ArrayList()
        val mCal = Calendar.getInstance()
        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)
        mCal[curYearFormat.format(pDate).toInt(), curMonthFormat.format(pDate).toInt() - 1] = 1
        val dayNum = mCal[Calendar.DAY_OF_WEEK]
        //1일 - 요일 매칭 시키기 위해 공백 add
        for (i in 1 until dayNum) {
            dayList.add("")
        }
        setCalendarDate(mCal[Calendar.YEAR], mCal[Calendar.MONTH] + 1)
        gridAdapter = GridAdapter(applicationContext, dayList)
        gridAdapter.updateReceiptsList(dayList)
        binding.gridView.adapter = gridAdapter
    }

    private fun setCalendarDate(year: Int, month: Int) {
        val mCal = Calendar.getInstance()
        mCal[Calendar.YEAR] = year
        mCal[Calendar.MONTH] = month - 1
        val sdf = SimpleDateFormat("yyyyMMdd")
        var iNext = 0
        for (i in 0 until mCal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            mCal[Calendar.DAY_OF_MONTH] = i + 1
            dayList.add(sdf.format(Date(mCal.timeInMillis)))
            iNext = mCal[Calendar.DAY_OF_WEEK]
            Log.d(TAG, "week :" + mCal[Calendar.DAY_OF_WEEK])
        }
        // 나머지 빈칸도 채우기 위해서
        for (i in iNext..6) {
            dayList.add("")
        }
    }

    private val idAndLAT: Unit
        private get() {
            Thread {
                lateinit var adInfo: AdvertisingIdClient.Info
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this@MainActivity)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: GooglePlayServicesRepairableException) {
                    e.printStackTrace()
                } catch (e: GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                }
                val GAID = adInfo.id
                val limitAdTracking = adInfo.isLimitAdTrackingEnabled
                Log.e(TAG, "$GAID=$limitAdTracking")
                val client = AppSet.getClient(this@MainActivity)
                val task = client.appSetIdInfo
                task.addOnSuccessListener { appSetIdInfo ->
                    val scope = appSetIdInfo.scope
                    val id = appSetIdInfo.id
                    Log.e(TAG, "" + id + "" + scope)
                }
            }.start()
        }

    internal inner class MyGestureListener() : SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            event1: MotionEvent, event2: MotionEvent,
            velocityX: Float, velocityY: Float
        ): Boolean {
            val diffY = event2.y - event1.y
            val diffX = event2.x - event1.x
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                }
            }
            return true
        }

    }

    private fun onSwipeLeft() {
        pDate = StringUtil.addMonth(pDate, 1)
        getDispMonth(pDate)
        binding.gridView.isFocusable = true
    }

    private fun onSwipeRight() {
        pDate = StringUtil.addMonth(pDate, -1)
        getDispMonth(pDate)
        binding.gridView.isFocusable = true
    }

    private fun onSwipeTop() {
        pDate = StringUtil.addMonth(pDate, 12)
        getDispMonth(pDate)
        binding.gridView.isFocusable = true
    }

    private fun onSwipeBottom() {
        pDate = StringUtil.addMonth(pDate, -12)
        getDispMonth(pDate)
        binding.gridView.isFocusable = true
    }

    fun onButtonShowPopupWindowClick(view: View?, ppDate: Date?) {
        popupBinding = PopupWindowBinding.inflate(
            layoutInflater
        )
        val popupView: View = popupBinding.root

        // create the popup window
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 300, 600)
        popupBinding.txtMonth.text = curYearFormat.format(pDate)
        val mCal = Calendar.getInstance()
        mCal[curYearFormat.format(pDate).toInt(), curMonthFormat.format(pDate).toInt() - 1] = 1
        when (mCal[Calendar.MONTH]) {
            0 -> onSetColor(popupBinding.txtMonth1)
            1 -> onSetColor(popupBinding.txtMonth2)
            2 -> onSetColor(popupBinding.txtMonth3)
            3 -> onSetColor(popupBinding.txtMonth4)
            4 -> onSetColor(popupBinding.txtMonth5)
            5 -> onSetColor(popupBinding.txtMonth6)
            6 -> onSetColor(popupBinding.txtMonth7)
            7 -> onSetColor(popupBinding.txtMonth8)
            8 -> onSetColor(popupBinding.txtMonth9)
            9 -> onSetColor(popupBinding.txtMonth10)
            10 -> onSetColor(popupBinding.txtMonth11)
            11 -> onSetColor(popupBinding.txtMonth12)
        }
        popupBinding.btnPrev.setOnClickListener {
            pDate = StringUtil.addMonth(pDate, -12)
            popupBinding.txtMonth.text = curYearFormat.format(pDate)
        }
        popupBinding.btnNext.setOnClickListener {
            pDate = StringUtil.addMonth(pDate, 12)
            popupBinding.txtMonth.text = curYearFormat.format(pDate)
        }
        popupBinding.txtMonth1.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 1)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth2.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 2)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth3.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 3)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth4.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 4)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth5.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 5)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth6.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 6)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth7.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 7)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth8.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 8)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth9.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 9)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth10.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 10)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth11.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 11)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.txtMonth12.setOnClickListener {
            pDate = StringUtil.getDay(pDate, 12)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
        popupBinding.btnToday.setOnClickListener {
            val now = System.currentTimeMillis()
            pDate = Date(now)
            popupWindow.dismiss()
            getDispMonth(pDate)
        }
    }

    private fun onSetColor(txtMonth: TextView) {
        txtMonth.setBackgroundColor(getColor(R.color.softblue))
        txtMonth.setTextColor(Color.YELLOW)
    }

    companion object {

    }
}