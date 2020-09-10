package com.lhd.wavespeech.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

/**
 *  Created by AMBE on 15/2/2019 at 16:23 PM.
 */
class MarkerView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    private var mVelocity: Int = 0
    private var mListener: MarkerListener? = null

    init {

        // Make sure we get keys
        isFocusable = true

        mVelocity = 0
        mListener = null
    }

    fun setListener(listener: MarkerListener) {
        mListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                // We use raw x because this window itself is going to
                // move, which will screw up the "local" coordinates
                mListener!!.markerTouchStart(this, event.rawX)
            }
            MotionEvent.ACTION_MOVE ->
                // We use raw x because this window itself is going to
                // move, which will screw up the "local" coordinates
                mListener!!.markerTouchMove(this, event.rawX)
            MotionEvent.ACTION_UP -> mListener!!.markerTouchEnd(this)
        }
        return true
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int,
                                previouslyFocusedRect: Rect?) {
        if (gainFocus && mListener != null)
            mListener!!.markerFocus(this)
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mListener != null)
            mListener!!.markerDraw()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        mVelocity++
        val v = Math.sqrt((1 + mVelocity / 2).toDouble()).toInt()
        if (mListener != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mListener!!.markerLeft(this, v)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mListener!!.markerRight(this, v)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mListener!!.markerEnter(this)
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        mVelocity = 0
        if (mListener != null)
            mListener!!.markerKeyUp()
        return super.onKeyDown(keyCode, event)
    }

    interface MarkerListener {
        fun markerTouchStart(marker: MarkerView, pos: Float)

        fun markerTouchMove(marker: MarkerView, pos: Float)

        fun markerTouchEnd(marker: MarkerView)

        fun markerFocus(marker: MarkerView)

        fun markerLeft(marker: MarkerView, velocity: Int)

        fun markerRight(marker: MarkerView, velocity: Int)

        fun markerEnter(marker: MarkerView)

        fun markerKeyUp()

        fun markerDraw()
    }
}