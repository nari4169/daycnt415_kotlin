package com.billcoreatech.daycnt415.util;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    String TAG = "MarqueeTextView" ;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect
            previouslyFocusedRect) {

        //Log.d(TAG, "getMarqueeRepeatLimit onFocusChanged(" + this.getMarqueeRepeatLimit() + ")") ;

        if(focused)
            super.onFocusChanged(focused, direction, previouslyFocusedRect);

    }

    @Override
    public void onWindowFocusChanged(boolean focused) {

        //Log.d(TAG, "getMarqueeRepeatLimit onWindowFocusChanged (" + this.getMarqueeRepeatLimit() + ")") ;

        if(focused)
            super.onWindowFocusChanged(focused);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
