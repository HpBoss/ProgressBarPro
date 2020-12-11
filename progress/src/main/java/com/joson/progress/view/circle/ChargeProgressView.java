package com.joson.progress.view.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.joson.progress.R;
import com.joson.progress.utils.MiscUtil;

/**
 * @Auther: 何飘
 * @Date: 11/24/20 14:28
 * @Description:
 */
public class ChargeProgressView extends View {
    private int mMaxProgress;
    private int mReachedRectColor;
    private int mUnReachedRectColor;
    private int mValueColor;
    private float mValueSize;
    private float mStrokeWidth;
    private String mUnit;
    private String mHint;
    private float mValueTextY;
    private float mPercent;
    private int mCurrentProgress = 0;

    private Paint mReachedRectPaint;
    private Paint mUnReachedRectPaint;
    private Paint mValuePaint;
    private Paint mFlashPaint;
    private Path mFlashPath;

    private Point mCenterPoint;
    private RectF mRectF;

    private final Context mContext;
    private long mAnimTime;
    private float mRadius;

    private static final int default_color = Color.rgb(251, 238, 220);
    private static final int default_reached_color = Color.rgb(106, 227, 130);
    private static final int default_unreached_color = Color.rgb(180, 148, 154);

    public static final String INSTANCE_STATE = "saved_instance";
    public static final String INSTANCE_REACHED_RECT_COLOR = "reached_rect_color";
    public static final String INSTANCE_UNREACHED_RECT_COLOR = "unreached_rect_color";
    public static final String INSTANCE_VALUE_COLOR = "value_color";
    public static final String INSTANCE_VALUE_SIZE = "value_size";
    public static final String INSTANCE_STROKE_WIDTH = "stroke_width";
    public static final String INSTANCE_MAX_PROGRESS = "max_progress";
    public static final String INSTANCE_UNIT = "unit";
    public static final String INSTANCE_ANIM_TIME = "anim_time";


    public ChargeProgressView(Context context) {
        this(context, null);
    }

    public ChargeProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargeProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        float default_value_text_size = MiscUtil.sp2px(mContext, 10);
        float default_stroke_width = MiscUtil.dp2px(mContext, 5f);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleProgress, defStyleAttr, 0);
        // 从xml中获取属性所对应的值，传递到view内部。
        mReachedRectColor = typedArray.getColor(R.styleable.CircleProgress_progressReachedColor, default_reached_color);
        mUnReachedRectColor = typedArray.getColor(R.styleable.CircleProgress_progressUnreachedColor, default_unreached_color);
        mValueColor = typedArray.getColor(R.styleable.CircleProgress_progressValueTextColor, default_color);
        mValueSize = typedArray.getDimension(R.styleable.CircleProgress_progressValueTextSize, default_value_text_size);
        mStrokeWidth = typedArray.getDimension(R.styleable.CircleProgress_progressStrokeWidth, default_stroke_width);
        mMaxProgress = typedArray.getInt(R.styleable.CircleProgress_progressMax, 100);
        mUnit = typedArray.getString(R.styleable.CircleProgress_progressUnit);
        mHint = typedArray.getString(R.styleable.CircleProgress_progressHint);
        mAnimTime = typedArray.getInteger(R.styleable.CircleProgress_progressAnimTime, 1000);

        typedArray.recycle();
        initializePainters();
    }

    private void initializePainters() {
        mReachedRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedRectPaint.setColor(default_color);
        mReachedRectPaint.setStyle(Paint.Style.STROKE);
        mReachedRectPaint.setStrokeWidth(mStrokeWidth);
        mReachedRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mUnReachedRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnReachedRectPaint.setColor(mUnReachedRectColor);
        mUnReachedRectPaint.setStyle(Paint.Style.STROKE);
        mUnReachedRectPaint.setStrokeWidth(mStrokeWidth);
        mUnReachedRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setTextAlign(Paint.Align.CENTER);
        mValuePaint.setColor(default_color);
        mValuePaint.setTextSize(mValueSize);

        mCenterPoint = new Point();
        mRectF = new RectF();

        // 初始化画闪电的画笔
        mFlashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFlashPaint.setColor(default_color);
        mFlashPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, MiscUtil.dipToPx(mContext, 150)),
                MiscUtil.measure(heightMeasureSpec, MiscUtil.dipToPx(mContext, 150)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 为了让progressView更好的贴合四周，也不超出指定范围大小，因此要选取w、h中最小的那一个，并除以2作为半径
        // 首先我们要明白想要画出一个圆环，所必须要的条件，这个mRadius属于哪一部分，mRadius = 里层背景圆的半径 + 外圆环宽度的一半 - 两倍字体的高度
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) mStrokeWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) mStrokeWidth) / (float) 2 - 2 * MiscUtil.getTextHeight(mValuePaint);
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;

        // 绘制矩形是绘制圆环最为重要的一步，而且以mRadius为半径的圆是这个矩形的内截圆
        mRectF.left = mCenterPoint.x - mRadius - mStrokeWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - mStrokeWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + mStrokeWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + mStrokeWidth / 2;
        // 让电量显示位于view的底部
        mValueTextY = 2 * mCenterPoint.y - MiscUtil.getTextBaseLine(mValuePaint);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_REACHED_RECT_COLOR, mReachedRectColor);
        bundle.putInt(INSTANCE_UNREACHED_RECT_COLOR, mUnReachedRectColor);
        bundle.putInt(INSTANCE_VALUE_COLOR, mValueColor);
        bundle.putFloat(INSTANCE_VALUE_SIZE, mValueSize);
        bundle.putFloat(INSTANCE_STROKE_WIDTH, mStrokeWidth);
        bundle.putInt(INSTANCE_MAX_PROGRESS, mMaxProgress);
        bundle.putString(INSTANCE_UNIT, mUnit);
        bundle.putLong(INSTANCE_ANIM_TIME, mAnimTime);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mReachedRectColor = bundle.getInt(INSTANCE_REACHED_RECT_COLOR);
            mUnReachedRectColor = bundle.getInt(INSTANCE_UNREACHED_RECT_COLOR);
            mValueColor = bundle.getInt(INSTANCE_VALUE_COLOR);
            mValueSize = bundle.getInt(INSTANCE_VALUE_SIZE);
            mStrokeWidth = bundle.getFloat(INSTANCE_STROKE_WIDTH);
            mMaxProgress = bundle.getInt(INSTANCE_MAX_PROGRESS);
            mUnit = bundle.getString(INSTANCE_UNIT);
            mAnimTime = bundle.getLong(INSTANCE_ANIM_TIME);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFlash(canvas);
        drawArc(canvas);
        drawText(canvas);
    }

    private void drawFlash(Canvas canvas) {
        int point1X = mCenterPoint.x + (int) (mRadius * 1 / 12);
        int point1Y = mCenterPoint.y - (int) (mRadius * 5 / 12);
        int point2X = point1X - (int) (mRadius * 4 / 12);
        int point2Y = point1Y + (int) (mRadius * 5 / 12);
        int point3X = point2X + (int) (mRadius * 3 / 12);
        int point3Y = point2Y;
        int point4X = point3X - (int) (mRadius * 1 / 12);
        int point4Y = point3Y + (int) (mRadius * 4 / 12);
        int point5X = point3X + (int) (mRadius * 3 / 12);
        int point5Y = point3Y - (int) (mRadius * 1 / 12);
        int point6X = point5X - (int) (mRadius * 3 / 12);
        int point6Y = point5Y;
        mFlashPath.moveTo(point1X, point1Y);
        mFlashPath.lineTo(point2X, point2Y);
        mFlashPath.lineTo(point3X, point3Y);
        mFlashPath.lineTo(point4X, point4Y);
        mFlashPath.lineTo(point5X, point5Y);
        mFlashPath.lineTo(point6X, point6Y);

        mFlashPath.close();
        canvas.drawPath(mFlashPath, mFlashPaint);
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(mHint + mCurrentProgress + mUnit, mCenterPoint.x, mValueTextY, mValuePaint);
    }

    private void drawArc(Canvas canvas) {
        canvas.save();
        float mSweepAngle = 360;
        canvas.rotate(-90, mCenterPoint.x, mCenterPoint.y);
        float currentAngle = mSweepAngle * mPercent;
        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle + 2, false, mUnReachedRectPaint);
        canvas.drawArc(mRectF, 2, currentAngle, false, mReachedRectPaint);
        canvas.restore();
    }

    private void startAnimator(float start, float end, long animTime) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mCurrentProgress = (int) (mPercent * mMaxProgress);
                if (mCurrentProgress >= 20) {
                    mReachedRectPaint.setColor(mReachedRectColor);
                    mFlashPaint.setColor(mReachedRectColor);
                } else {
                    mReachedRectPaint.setColor(default_color);
                    mFlashPaint.setColor(default_color);
                }
                invalidate();
            }
        });
        mAnimator.start();
    }

    public void setProgress(float progress) {
        if (progress > mMaxProgress) {
            progress = mMaxProgress;
        }
        float start = mPercent;
        float end = progress / mMaxProgress;
        startAnimator(start, end, mAnimTime);
    }

    public void setAnimTime(long mAnimTime) {
        if (mAnimTime > 0) {
            this.mAnimTime = mAnimTime;
        }
    }

    public int getProgress() {
        return mCurrentProgress;
    }

    public int getMax() {
        return mMaxProgress;
    }

    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
        }
    }
}
