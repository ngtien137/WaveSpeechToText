package com.lhd.wavespeech.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.lhd.wavespeech.R;

import kotlin.jvm.Synchronized;

public class WaveSpeechView extends View {

    private final float MAX_VALUE_DEFAULT = 20;

    private Paint paintWave;
    private RectF rectView;

    private float maxValue = MAX_VALUE_DEFAULT;
    private float currentValue = 0;
    private float lastValue = 0;
    private boolean showNothingIfZeroValue = true;

    private float wavePadding;
    private float waveHeight;
    private float waveWidth;
    private int waveCount = 0;
    private float waveStartPadding = 0f;
    //Số vùng sóng
    private int waveAreaCount = 20;

    private WaveValueAnimation waveValueAnimation;

    public WaveSpeechView(Context context) {
        super(context);
        initView(context, null);
    }

    public WaveSpeechView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public WaveSpeechView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WaveSpeechView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private float dpToPixel(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        paintWave = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectView = new RectF();
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaveSpeechView);

            maxValue = ta.getFloat(R.styleable.WaveSpeechView_ws_waveMaxValue, MAX_VALUE_DEFAULT);
            currentValue = ta.getFloat(R.styleable.WaveSpeechView_ws_waveValue, 0f);
            waveAreaCount = ta.getInt(R.styleable.WaveSpeechView_ws_waveAreaCount, 20);
            showNothingIfZeroValue = ta.getBoolean(R.styleable.WaveSpeechView_ws_showNoThingIfZeroValue, true);

            waveWidth = ta.getDimension(R.styleable.WaveSpeechView_ws_waveWidth, dpToPixel(1));
            wavePadding = ta.getDimension(R.styleable.WaveSpeechView_ws_wavePadding, waveWidth / 2f);
            waveHeight = ta.getDimension(R.styleable.WaveSpeechView_ws_waveHeight, -1f);

            paintWave.setStyle(Paint.Style.FILL_AND_STROKE);
            paintWave.setColor(ta.getColor(R.styleable.WaveSpeechView_ws_waveColor, Color.BLACK));
            paintWave.setStrokeCap(Paint.Cap.ROUND);
            paintWave.setStrokeWidth(waveWidth);
            ta.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (waveHeight == -1) {
            waveHeight = getHeight();
        }
        waveCount = (int) (1f * (getWidth()) / (waveWidth + wavePadding));
        waveStartPadding = getWidth() - (waveWidth + wavePadding) * waveCount;
        rectView.set(0f, 0f, getWidth(), getHeight());
    }

    @Synchronized
    @Override
    protected void onDraw(Canvas canvas) {
        if (showNothingIfZeroValue && currentValue == 0)
            return;
        float offset = waveWidth / 2f + waveStartPadding;
        int halfWaveCount = waveCount / 2;
        int partCount = waveCount / waveAreaCount;
        int lastValuePart = waveCount / 4;

        for (int i = 0; i < waveCount; i++) {

            float value = currentValue;
            boolean isLastValue = false;
            if (i <= lastValuePart || i >= lastValuePart * 2) {
                value = lastValue;
                isLastValue = true;
            }

            float heightWave = value / maxValue * waveHeight - paintWave.getStrokeWidth();
            float percentOfTotal;
            if (isLastValue) {
                percentOfTotal = 1f * (i % (lastValuePart / 2)) / (lastValuePart / 2);
            } else {
                percentOfTotal = 1f * (i % halfWaveCount) / halfWaveCount;
            }
            if (i >= halfWaveCount) {
                percentOfTotal = 1f - percentOfTotal;
            }
            float percentHeight = 1f * (i % partCount) / (partCount);
            if (i >= partCount * waveAreaCount / 2) {
                percentHeight = 1 - percentHeight;
            }
            heightWave = percentHeight * heightWave * percentOfTotal;
            float topWave = rectView.centerY() - heightWave / 2f;
            float bottomWave = rectView.centerY() + heightWave / 2f;
            canvas.drawLine(offset, topWave, offset, bottomWave, paintWave);
            offset += waveWidth + wavePadding;
        }
    }

    public void setValue(float value) {
        lastValue = currentValue;
        currentValue = value;
        invalidate();
    }

    public void setValue(float value, float lastValue) {
        this.lastValue = lastValue;
        currentValue = value;
        invalidate();
    }

    public void reset() {
        lastValue = 0;
        currentValue = 0;
        invalidate();
    }

    public void reset(boolean hasAnimation) {
        if (hasAnimation) {
            waveValueAnimation = new WaveValueAnimation(this, currentValue, lastValue, 0f);
            waveValueAnimation.startAnim();
        } else {
            reset();
        }
    }

    public void cancelAnimation() {
        if (waveValueAnimation != null) {
            waveValueAnimation.cancel();
            waveValueAnimation = null;
        }
    }


    static class WaveValueAnimation extends Animation {
        private WaveSpeechView waveSpeechView;
        private float oldProgress;
        private float oldLastProgress;
        private float newProgress;
        private float currentProgress = 0F;
        private float currentLastProgress = 0F;

        public WaveValueAnimation(WaveSpeechView cornerCard, float oldProgress, float oldLastProgress, float newProgress) {
            this.waveSpeechView = cornerCard;
            this.oldProgress = oldProgress;
            this.oldLastProgress = oldLastProgress;
            this.newProgress = newProgress;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float progress = oldProgress + (newProgress - oldProgress) * interpolatedTime;
            currentProgress = progress;
            float lastProgress = oldLastProgress + (newProgress - oldLastProgress) * interpolatedTime;
            currentLastProgress = lastProgress;
            waveSpeechView.setValue(progress, lastProgress);
        }

        public void startAnim(long duration) {
            setDuration(duration);
            waveSpeechView.startAnimation(this);
        }

        public void startAnim() {
            startAnim(1000);
        }

        @Override
        public void cancel() {
            waveSpeechView.setValue(currentProgress, currentLastProgress);
            super.cancel();
        }

    }
}
