package com.billcoreatech.daycnt415

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.billcoreatech.daycnt415.billing.BillingManager
import com.billcoreatech.daycnt415.billing.BillingManager.connectStatusTypes
import com.billcoreatech.daycnt415.databinding.ActivitySettingBinding
import com.github.anrwatchdog.ANRWatchDog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import java.text.SimpleDateFormat
import java.util.*

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding
    lateinit var option: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var hour: Int = 0
    var min: Int = 0
    var TAG: String = "SettingActivity"
    lateinit var billingManager: BillingManager
    lateinit var sdf: SimpleDateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(getLayoutInflater())
        val view: View = binding.getRoot()
        setContentView(view)
        sdf = SimpleDateFormat("yyyy-MM-dd")
        option = getSharedPreferences("option", MODE_PRIVATE)
        billingManager = BillingManager(this@SettingActivity)
        if (option.getBoolean("isBill", false)) {
            binding.adView.setVisibility(View.GONE)
            binding.btnAdPay.setVisibility(View.GONE)
        } else {
            binding.adView.setVisibility(View.VISIBLE)
            binding.btnAdPay.setVisibility(View.VISIBLE)
        }
        MobileAds.initialize(this, object : OnInitializationCompleteListener {
            public override fun onInitializationComplete(initializationStatus: InitializationStatus) {}
        })
        ANRWatchDog().start()
        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        editor = option.edit()
        binding.edStartTime.text = option.getString("startTime", "18:00")
        binding.edCloseTime.text = option.getString("closeTime", "24:00")
        binding.editTermLength.setText(option.getInt("term", 1).toString())
        binding.seekTransparent.max = 100
        binding.seekTransparent.progress = option.getInt("transparent", 100)
        binding.progressTextView.text = option.getInt("transparent", 100).toString() + "%"
        doSeekProgressDisp(option.getInt("transparent", 100))
        binding.btnAdPay.setOnClickListener {
            Log.i(TAG, "clicked...")
            if (billingManager.connectStatus == connectStatusTypes.connected) {
                Log.i(TAG, "connected ..")
                try {
                    billingManager.skuDetailList
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        binding.btnOK.setOnClickListener {
            editor.putString("startTime", binding.edStartTime.getText().toString())
            editor.putString("closeTime", binding.edCloseTime.getText().toString())
            editor.putInt(
                "term",
                binding.editTermLength.text.toString().replace("[^0-9]".toRegex(), "")
                    .toInt()
            )
            editor.putInt("transparent", binding.seekTransparent.progress)
            editor.commit()
            finish()
        }
        binding.edStartTime.setOnClickListener {
            Log.i(TAG, "edStartTime")
            val cal: Calendar = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR_OF_DAY)
            min = cal.get(Calendar.MINUTE)
            val timePickerDialog: TimePickerDialog =
                TimePickerDialog(this@SettingActivity,
                    { view, hourOfDay, minute ->
                        binding.edStartTime.text = pad(hourOfDay) + ":" + pad(minute)
                    }, hour, min, true)
            timePickerDialog.show()
        }
        binding.edCloseTime.setOnClickListener {
            Log.i(TAG, "edCloseTime")
            val cal: Calendar = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR_OF_DAY)
            min = cal.get(Calendar.MINUTE)
            val timePickerDialog: TimePickerDialog =
                TimePickerDialog(this@SettingActivity, object : OnTimeSetListener {
                    @SuppressLint("SetTextI18n")
                    public override fun onTimeSet(
                        view: TimePicker,
                        hourOfDay: Int,
                        minute: Int
                    ) {
                        var hourOfDay: Int = hourOfDay
                        if (hourOfDay == 0 && minute == 0) {
                            hourOfDay = 24
                        }
                        binding.edCloseTime.text = pad(hourOfDay) + ":" + pad(minute)
                    }
                }, hour, min, true)
            timePickerDialog.show()
        }
        binding.seekTransparent.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.O)
            public override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                Log.i(TAG, "progress=" + progress)
                doSeekProgressDisp(progress)
                binding.progressTextView.text = "$progress%"
            }

            public override fun onStartTrackingTouch(seekBar: SeekBar) {}
            public override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun doSeekProgressDisp(progress: Int) {
        when (Math.floorDiv(progress, 10)) {
            10 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white100))
            9 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white90))
            8 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white80))
            7 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white70))
            6 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white60))
            5 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white50))
            4 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white40))
            3 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white30))
            2 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white20))
            1 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white10))
            0 -> binding.transparentTest.setBackgroundColor(getColor(R.color.white00))
        }
    }

    private fun pad(pValue: Int): String {
        return if (pValue < 10) "0" + pValue else pValue.toString()
    }
}