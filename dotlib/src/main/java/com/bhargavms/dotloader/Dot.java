package com.bhargavms.dotloader;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Bhargav on 7/20/2016.
 */
class Dot {
    private Paint mPaint;
    int mCurrentColorIndex;
    private int mDotRadius;
    private Integer[] mColors;
    float cx;
    float cy;
    int position;
    ValueAnimator positionAnimator;
    ValueAnimator colorAnimator;

    Dot(DotLoader parent, int dotRadius, int position) {
        this.position = position;
        mColors = parent.mColors;
        mCurrentColorIndex = 0;
        mDotRadius = dotRadius;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColors[mCurrentColorIndex]);
        mPaint.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    void setColorIndex(int index) {
        mCurrentColorIndex = index;
        mPaint.setColor(mColors[index]);
    }

    void setColor(int color) {
        mPaint.setColor(color);
    }

    private int getCurrentColor() {
        return mColors[mCurrentColorIndex];
    }

    public int incrementAndGetColor() {
        incrementColorIndex();
        return getCurrentColor();
    }

    void applyNextColor() {
        mCurrentColorIndex++;
        if (mCurrentColorIndex >= mColors.length)
            mCurrentColorIndex = 0;
        mPaint.setColor(mColors[mCurrentColorIndex]);
    }

    int incrementColorIndex() {
        mCurrentColorIndex++;
        if (mCurrentColorIndex >= mColors.length)
            mCurrentColorIndex = 0;
        return mCurrentColorIndex;
    }

    void draw(Canvas canvas) {
        canvas.drawCircle(cx, cy, mDotRadius, mPaint);
    }
}
