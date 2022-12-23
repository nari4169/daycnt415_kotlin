package com.billcoreatech.daycnt415.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.billcoreatech.daycnt415.R
import com.google.android.gms.ads.*

/**
 *
 */
object KakaoToast {
    private lateinit var mAdView: AdView
    var TAG = "KakaoToast"
    lateinit var option: SharedPreferences

    fun makeToast(context: Context, body: String, duration: Int): Toast {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.view_toast, null)
        val text = v.findViewById<TextView>(R.id.message)
        text.text = body
        mAdView = v.findViewById(R.id.adView)
        option = context.getSharedPreferences("option", Context.MODE_PRIVATE)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        if (option.getBoolean("isBill", false)) {
            mAdView.setVisibility(View.GONE)
        } else {
            mAdView.setVisibility(View.VISIBLE)
        }
        mAdView.setAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG, "onAdLoaded")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.e(TAG, "onAdClosed")
            }

            override fun onAdOpened() {
                Log.e(TAG, "onAdOpened")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.e(TAG, "onAdClicked")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.e(TAG, "onAdImpression")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                val errorDomain = error.domain
                val errorCode = error.code
                val errorMessage = error.message
                val responseInfo = error.responseInfo
                val cause = error.cause
                Log.i(TAG, "------------------------------------")
                Log.i(TAG, "error=$error")
                Log.i(TAG, "errorDomain=$errorDomain")
                Log.i(TAG, "errorCode=$errorCode")
                Log.i(TAG, "errorMessage=$errorMessage")
                Log.i(TAG, "responseInfo=" + responseInfo!!.responseId)
                Log.i(TAG, "responseInfo=" + responseInfo.mediationAdapterClassName)
                Log.i(TAG, "------------------------------------")
            }
        })
        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.setView(v)
        toast.duration = duration
        return toast
    }
}