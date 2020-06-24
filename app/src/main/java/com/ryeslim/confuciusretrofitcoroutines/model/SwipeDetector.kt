package com.ryeslim.confuciusretrofitcoroutines.model

import android.view.MotionEvent
import android.view.View
import com.ryeslim.confuciusretrofitcoroutines.activities.MainActivity
import java.lang.Math.abs


class SwipeDetector(private val activity: MainActivity) : View.OnTouchListener {
    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()
    private var upX: Float = 0.toFloat()
    private var upY: Float = 0.toFloat()

    private fun onRightToLeftSwipe(v: View) {
        //Log.i(logTag, "RightToLeftSwipe!");
        activity.goForward()
    }

    private fun onLeftToRightSwipe(v: View) {
        //Log.i(logTag, "LeftToRightSwipe!");
        activity.goBackwards()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                upX = event.x
                upY = event.y

                val deltaX = downX - upX
                val deltaY = downY - upY

                // swipe horizontal?
                if (abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        this.onLeftToRightSwipe(v)
                        return true
                    }
                    if (deltaX > 0) {
                        this.onRightToLeftSwipe(v)
                        return true
                    }
                } else {
                    //Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
                }

                // swipe vertical?
                if (abs(deltaY) > MIN_DISTANCE) {
                    // top or down
                    if (deltaY < 0) {

                    }
                    if (deltaY > 0) {

                    }
                } else {
                    //Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
                    v.performClick()
                }
            }
        }
        return false
    }

    companion object {

        internal const val MIN_DISTANCE = 100
    }
}