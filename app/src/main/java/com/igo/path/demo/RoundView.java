package com.igo.path.demo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Edgar on 2018/8/17.
 */
public class RoundView extends View {

    private static final int RADIUS_ARRAY_LENGTH = 8;
    private float[] mRadius;
    private float mRoundRadius;

    private Path mRoundPath;
    private RectF mRoundRect;
    private Paint mRoundPaint;
    private int mRoundWidth;
    private int mRoundHeight;
    private int mRoundSolidColor;

    private Path mStrokePath;
    private PathMeasure mStrokePathMeasure;
    private Paint mStrokePaint;
    private int mStrokeWidth;
    private int mStrokeColor;

    private Paint mMovePaint;
    private Path mMovePath;
    private int mMoveBarColor;

    private ValueAnimator mMoveAnimator;

    public RoundView(Context context) {
        this(context, null);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();
        mRoundWidth = res.getDimensionPixelOffset(R.dimen.round_width);
        mRoundHeight = res.getDimensionPixelOffset(R.dimen.round_height);
        mRoundRadius = res.getDimension(R.dimen.round_radius);
        mRoundSolidColor = ContextCompat.getColor(context,R.color.round_solid_color);
        mRadius = createFloatArray(mRoundRadius, RADIUS_ARRAY_LENGTH);
        mRoundRect = new RectF();
        mRoundPath = new Path();
        mRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRoundPaint.setColor(mRoundSolidColor);

        mStrokeWidth = res.getDimensionPixelOffset(R.dimen.round_stroke_width);
        mStrokeColor = ContextCompat.getColor(context,R.color.round_stroke_color);
        mStrokePath = new Path();
        mStrokePathMeasure = new PathMeasure();
        mStrokePaint = makeStrokePaint(mStrokeColor);

        mMoveBarColor = ContextCompat.getColor(context,R.color.round_move_bar_color);
        mMovePaint = makeStrokePaint(mMoveBarColor);
        mMovePath = new Path();
    }

    private static float[] createFloatArray(float value, int length) {
        float[] array = new float[length];
        for (int i=0; i < length; i++) {
            array[i] = value;
        }
        return array;
    }

    private Paint makeStrokePaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        return paint;
    }

    public void startMoveAnimator() {
        if (mMoveAnimator != null && mMoveAnimator.isRunning()) {
            return;
        }
        final float length = mStrokePathMeasure.getLength();
        if (mMoveAnimator == null) {
            mMoveAnimator = ValueAnimator.ofFloat(0.0f, length);
            mMoveAnimator.setDuration(5000);
            mMoveAnimator.setRepeatMode(ValueAnimator.RESTART);
            mMoveAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mMoveAnimator.setInterpolator(new LinearInterpolator());
        }
        mMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private float segmentLength = length / 10.0f;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float start = (float) animation.getAnimatedValue();
                float end = start + segmentLength;
                mMovePath.reset();
                if (end > length) {
                    float distance1 = end - start;
                    float distance2 = length - start;
                    mStrokePathMeasure.getSegment(0, distance1 - distance2, mMovePath, true);
                    end = length;
                }
                mStrokePathMeasure.getSegment(start, end, mMovePath, true);
                postInvalidate();
            }
        });
        mMoveAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float arcLeft = getPaddingLeft() + (getMeasuredWidth() - getPaddingLeft() - getPaddingLeft() - mRoundWidth) / 2;
        float arcTop = getPaddingTop() + (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - mRoundHeight) / 2;
        mRoundRect.set(arcLeft, arcTop, mRoundWidth + arcLeft, arcTop + mRoundHeight);

        mRoundPath.addRoundRect(mRoundRect, mRadius, Path.Direction.CW);
        mStrokePath.addRoundRect(mRoundRect, mRadius, Path.Direction.CW);
        mStrokePathMeasure.setPath(mStrokePath, false);
        startMoveAnimator();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawStroke(canvas);
        drawMovePath(canvas);
        canvas.drawPath(mRoundPath, mRoundPaint);
    }

    private void drawStroke(Canvas canvas) {
        canvas.drawPath(mStrokePath, mStrokePaint);
    }

    private void drawMovePath(Canvas canvas) {
        if (mMovePath.isEmpty()) {
            return;
        }
        canvas.drawPath(mMovePath,mMovePaint);
    }
}
