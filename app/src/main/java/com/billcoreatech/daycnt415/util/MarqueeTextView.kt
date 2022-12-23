package com.billcoreatech.daycnt415.util

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView : AppCompatTextView {
    var TAG = "MarqueeTextView"

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attributeSet: AttributeSet?) : super(
        context!!, attributeSet
    ) {
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

        //Log.d(TAG, "getMarqueeRepeatLimit onFocusChanged(" + this.getMarqueeRepeatLimit() + ")") ;
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(focused: Boolean) {

        //Log.d(TAG, "getMarqueeRepeatLimit onWindowFocusChanged (" + this.getMarqueeRepeatLimit() + ")") ;
        if (focused) super.onWindowFocusChanged(focused)
    }

    override fun isFocused(): Boolean {
        return true
    }
}