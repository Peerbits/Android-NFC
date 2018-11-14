package com.bhargavms.dotloader;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * Isn't this a cool animation?
 */
public class DotLoader extends View {
    private static final int DELAY_BETWEEN_DOTS = 80;
    private static final int BOUNCE_ANIMATION_DURATION = 500;
    private static final int COLOR_ANIMATION_DURATION = 80;
    private Dot[] mDots;
    Integer[] mColors;
    private int mDotRadius;
    private Rect mClipBounds;
    private float mCalculatedGapBetweenDotCenters;
    private float mFromY;
    private float mToY;
    private Interpolator bounceAnimationInterpolator =
            new CubicBezierInterpolator(0.62f, 0.28f, 0.23f, 0.99f);

    public void setNumberOfDots(int numberOfDots) {
        Dot[] newDots = new Dot[numberOfDots];
        if (numberOfDots < mDots.length) {
            System.arraycopy(mDots, 0, newDots, 0, numberOfDots);
        } else {
            System.arraycopy(mDots, 0, newDots, 0, mDots.length);
            for (int i = mDots.length; i < numberOfDots; i++) {
                newDots[i] = new Dot(this, mDotRadius, i);
                newDots[i].cx = newDots[i - 1].cx + mCalculatedGapBetweenDotCenters;
                newDots[i].cy = newDots[i - 1].cy / 2;
                newDots[i].setColorIndex(newDots[i - 1].mCurrentColorIndex);
                newDots[i].positionAnimator =
                        clonePositionAnimatorForDot(newDots[0].positionAnimator, newDots[i]);
                newDots[i].colorAnimator =
                        cloneColorAnimatorForDot(newDots[0].colorAnimator, newDots[i]);
                newDots[i].positionAnimator.start();
            }
        }
        mDots = newDots;
    }

    private ValueAnimator cloneColorAnimatorForDot(ValueAnimator colorAnimator, Dot dot) {
        ValueAnimator valueAnimator = colorAnimator.clone();
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.addUpdateListener(new DotColorUpdater(dot, this));
        return valueAnimator;
    }

    private ValueAnimator clonePositionAnimatorForDot(ValueAnimator animator, final Dot dot) {
        ValueAnimator valueAnimator = animator.clone();
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.addUpdateListener(new DotYUpdater(dot, this));
        valueAnimator.setStartDelay(DELAY_BETWEEN_DOTS * dot.position);
        valueAnimator.removeAllListeners();
        valueAnimator.addListener(new AnimationRepeater(dot, mColors));
        return valueAnimator;
    }

    public void resetColors() {
        for (Dot dot : mDots) {
            dot.setColorIndex(0);
        }
    }

    public DotLoader(Context context) {
        super(context);
        init(context, null);
    }

    public DotLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DotLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public DotLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context c, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.DotLoader, 0, 0);
        try {
            float dotRadius = a.getDimension(R.styleable.DotLoader_dot_radius, 0);
            int numberOfPods = a.getInteger(R.styleable.DotLoader_number_of_dots, 1);
            int resId = a.getResourceId(R.styleable.DotLoader_color_array, 0);
            Integer[] colors;
            if (resId == 0) {
                colors = new Integer[numberOfPods];
                for (int i = 0; i < numberOfPods; i++) {
                    colors[i] = 0x0;
                }
            } else {
                int[] temp = getResources().getIntArray(resId);
                colors = new Integer[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    colors[i] = temp[i];
                }
            }
            init(numberOfPods, colors, (int) dotRadius);
        } finally {
            a.recycle();
        }
    }

    private void _stopAnimations() {
        for (Dot dot : mDots) {
            dot.positionAnimator.end();
            dot.colorAnimator.end();
        }
    }

    private void init(int numberOfDots, Integer[] colors, int dotRadius) {
        mColors = colors;
        mClipBounds = new Rect(0, 0, 0, 0);
        mDots = new Dot[numberOfDots];
        mDotRadius = dotRadius;
        for (int i = 0; i < numberOfDots; i++) {
            mDots[i] = new Dot(this, dotRadius, i);
        }
        //noinspection deprecation
        startAnimation();
    }

    public void initAnimation() {
        for (int i = 0, size = mDots.length; i < size; i++) {
            mDots[i].positionAnimator = createValueAnimatorForDot(mDots[i]);
            mDots[i].positionAnimator.setStartDelay(DELAY_BETWEEN_DOTS * i);

            mDots[i].colorAnimator = createColorAnimatorForDot(mDots[i]);
        }
    }

    /**
     * Won't support starting and stopping animations for now, until I figure out how to sync animation
     * delays.
     *
     * @deprecated
     */
    private void startAnimation() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                _startAnimation();
            }
        }, 10);
    }

    /**
     * Won't support starting and stopping animations for now, until I figure out how to sync animation
     * delays.
     *
     * @deprecated
     */
    @SuppressWarnings("unused")
    private void stopAnimations() {
        post(new Runnable() {
            @Override
            public void run() {
                _stopAnimations();
            }
        });
    }

    private void _startAnimation() {
        initAnimation();
        for (Dot mDot : mDots) {
            mDot.positionAnimator.start();
        }
    }

    private ValueAnimator createValueAnimatorForDot(final Dot dot) {
        ValueAnimator animator = ValueAnimator.ofFloat(
                mFromY, mToY
        );
        animator.setInterpolator(bounceAnimationInterpolator);
        animator.setDuration(BOUNCE_ANIMATION_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new DotYUpdater(dot, this));
        animator.addListener(new AnimationRepeater(dot, mColors));
        return animator;
    }

    private ValueAnimator createColorAnimatorForDot(Dot dot) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), mColors[dot.mCurrentColorIndex],
                mColors[dot.incrementColorIndex()]);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(COLOR_ANIMATION_DURATION);
        animator.addUpdateListener(new DotColorUpdater(dot, this));
        return animator;
    }

    private static class DotColorUpdater implements ValueAnimator.AnimatorUpdateListener {
        private Dot mDot;
        private WeakReference<DotLoader> mDotLoaderRef;

        private DotColorUpdater(Dot dot, DotLoader dotLoader) {
            mDot = dot;
            mDotLoaderRef = new WeakReference<>(dotLoader);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mDot.setColor((Integer) valueAnimator.getAnimatedValue());
            DotLoader dotLoader = mDotLoaderRef.get();
            if (dotLoader != null) {
                dotLoader.invalidateOnlyRectIfPossible();
            }
        }
    }

    private static class DotYUpdater implements ValueAnimator.AnimatorUpdateListener {
        private Dot mDot;
        private WeakReference<DotLoader> mDotLoaderRef;

        private DotYUpdater(Dot dot, DotLoader dotLoader) {
            mDot = dot;
            mDotLoaderRef = new WeakReference<>(dotLoader);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mDot.cy = (float) valueAnimator.getAnimatedValue();
            DotLoader dotLoader = mDotLoaderRef.get();
            if (dotLoader != null) {
                dotLoader.invalidateOnlyRectIfPossible();
            }
        }
    }


    private void invalidateOnlyRectIfPossible() {
        if (mClipBounds != null && mClipBounds.left != 0 &&
                mClipBounds.top != 0 && mClipBounds.right != 0 && mClipBounds.bottom != 0)
            invalidate(mClipBounds);
        else
            invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(mClipBounds);
        for (Dot mDot : mDots) {
            mDot.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        // desired height is 6 times the diameter of a dot.
        int desiredHeight = (mDotRadius * 2 * 3) + getPaddingTop() + getPaddingBottom();

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        mCalculatedGapBetweenDotCenters = calculateGapBetweenDotCenters();
        int desiredWidth = (int) (mCalculatedGapBetweenDotCenters * mDots.length);
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }
        for (int i = 0, size = mDots.length; i < size; i++) {
            mDots[i].cx = (mDotRadius + i * mCalculatedGapBetweenDotCenters);
            mDots[i].cy = height - mDotRadius;
        }
        mFromY = height - mDotRadius;
        mToY = mDotRadius;
        setMeasuredDimension(width, height);
    }

    private float calculateGapBetweenDotCenters() {
        return (mDotRadius * 2) + mDotRadius;
    }
}
