package com.billcoreatech.daycnt415.util

import com.billcoreatech.daycnt415.util.Holidays
import com.google.android.gms.ads.AdView
import android.content.SharedPreferences
import android.widget.Toast
import android.view.LayoutInflater
import com.billcoreatech.daycnt415.R
import android.widget.TextView
import com.billcoreatech.daycnt415.util.KakaoToast
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import android.view.Gravity
import android.widget.GridView
import android.widget.AbsListView
import android.view.View.MeasureSpec
import android.app.Activity
import com.google.android.material.snackbar.Snackbar
import com.billcoreatech.daycnt415.util.StringUtil
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import android.widget.BaseAdapter
import com.billcoreatech.daycnt415.database.DBHandler
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import android.appwidget.AppWidgetProvider
import android.content.Intent
import com.billcoreatech.daycnt415.util.DayCntWidget
import android.app.PendingIntent
import android.app.AlarmManager
import android.widget.RemoteViews
import android.appwidget.AppWidgetManager
import android.graphics.drawable.Drawable
import android.graphics.PorterDuff
import android.icu.util.ChineseCalendar
import com.billcoreatech.daycnt415.util.LunarCalendar
import androidx.appcompat.widget.AppCompatTextView
import android.widget.EditText
import com.billcoreatech.daycnt415.util.DayCntWidgetConfigureActivity
import android.os.Bundle
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billcoreatech.daycnt415.billing.BillingManager.connectStatusTypes
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import android.database.sqlite.SQLiteOpenHelper
import com.billcoreatech.daycnt415.database.DBHelper
import android.database.sqlite.SQLiteDatabase
import android.annotation.SuppressLint
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import com.billcoreatech.daycnt415.billing.BillingManager
import com.github.anrwatchdog.ANRWatchDog
import com.github.anrwatchdog.ANRWatchDog.ANRListener
import com.github.anrwatchdog.ANRError
import com.billcoreatech.daycnt415.MainActivity
import android.content.DialogInterface
import com.billcoreatech.daycnt415.util.GridAdapter
import com.billcoreatech.daycnt415.dayManager.DayinfoBean
import androidx.core.view.GestureDetectorCompat
import com.billcoreatech.daycnt415.MainActivity.MyGestureListener
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.AppUpdateType
import android.content.IntentSender.SendIntentException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.ReviewInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.review.model.ReviewErrorCode
import android.view.View.OnTouchListener
import android.view.MotionEvent
import com.billcoreatech.daycnt415.SettingActivity
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdInfo
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.widget.TimePicker
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import android.os.Build
import android.widget.SeekBar

class Holidays : Comparable<Holidays> {
    // ArrayList의 type이 Comparable을 implements한 경우에만 sort 메소드의 정렬 기능을 사용할 수 있다
    lateinit var year // 연도
            : String
    lateinit var date // 월일
            : String
    lateinit var name // 휴일 명칭
            : String

    constructor() {}
    constructor(year: String, date: String, name: String) {
        this.year = year
        this.date = date
        this.name = name
    }

    override fun compareTo(o: Holidays): Int {
        return date!!.compareTo(o.date!!)
    }
}