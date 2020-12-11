package com.joson.progress.view.horizontal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.joson.progress.R;
import com.joson.progress.utils.MiscUtil;

/**
 * @Auther: hepiao
 * @CreatTime: 2020/9/23
 * @Description: 水平增加进度条
 */
public class HorizontalProgressView extends View {
    private int mMaxProgress;
    private int mCurrentProgress;
    private int mReachedBarColor;
    private int mUnReachedBarColor;
    private float mReachedBarHeight;
    private final float mOffset;
    private int mTextColor;
    private float mTextSize;
    private float mPercent;
    private boolean mIfDrawText = true;
    private boolean mDrawUnreachedBar = true;
    private boolean mDrawReachedBar = true;
    private String mSuffix = "%";
    private float mMaxDrawTextWidth;
    private long mAnimTime;
    private Context mContext;

    private Paint mTextPaint;
    private float mDrawTextStart;
    private Paint mReachedBarPaint;
    private Paint mUnReachedBarPaint;
    private RectF mReachedRectF;
    private RectF mUnReachedRectF;

    private String mCurrentDrawText;
    private final float default_reached_bar_height;
    private float mDrawTextHeight;

    public static final int default_animTime = 3000;
    public static final int default_max = 100;
    public static final int default_progress = 0;
    private static final int default_text_color = Color.rgb(66, 145, 241);
    private static final int default_reached_color = Color.rgb(66, 145, 241);
    private static final int default_unreached_color = Color.rgb(204, 204, 204);

    // 保存与恢复view状态
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";

    public HorizontalProgressView(Context context) {
        this(context, null);
    }

    public HorizontalProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        default_reached_bar_height = MiscUtil.dp2px(context, 1.5f);
        float default_text_size = MiscUtil.sp2px(context, 10);
        float default_progress_text_offset = MiscUtil.dp2px(context, 3.0f);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalProgress,
                defStyleAttr, 0);

        mReachedBarColor = typedArray.getColor(R.styleable.HorizontalProgress_progressReachedColor, default_reached_color);
        mUnReachedBarColor = typedArray.getColor(R.styleable.HorizontalProgress_progressUnreachedColor, default_unreached_color);
        mTextColor = typedArray.getColor(R.styleable.HorizontalProgress_progressTextColor, default_text_color);
        mTextSize = typedArray.getDimension(R.styleable.HorizontalProgress_progressTextSize, default_text_size);
        mReachedBarHeight = typedArray.getDimension(R.styleable.HorizontalProgress_progressBarHeight, default_reached_bar_height);
        mOffset = typedArray.getDimension(R.styleable.HorizontalProgress_progressTextOffset, default_progress_text_offset);
        mAnimTime = typedArray.getInt(R.styleable.HorizontalProgress_progressAnimTime, default_animTime);
        mMaxProgress = typedArray.getInt(R.styleable.HorizontalProgress_progressMax, default_max);

        //setPadding(0,(int) MiscUtil.dp2px(context,5f),0,(int) MiscUtil.dp2px(context, 5f));
        setProgress(typedArray.getInt(R.styleable.HorizontalProgress_progressCurrent, default_progress));
        setMax(typedArray.getInt(R.styleable.HorizontalProgress_progressMax, default_max));
        typedArray.recycle();
        initializePainters();
    }

    private void initializePainters() {
        mReachedRectF = new RectF();
        mUnReachedRectF = new RectF();

        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setColor(mReachedBarColor);

        mUnReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnReachedBarPaint.setColor(mUnReachedBarColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mMaxDrawTextWidth = mTextPaint.measureText("100" + mSuffix);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, MiscUtil.dipToPx(mContext, 200)),
                MiscUtil.measure(heightMeasureSpec, MiscUtil.dipToPx(mContext, 20)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIfDrawText) {
            calculateDrawRectF();
        } else {
            calculateDrawRectFWithoutProgressText();
        }

        if (mDrawReachedBar) {
            canvas.drawRect(mReachedRectF, mReachedBarPaint);
        }

        if (mDrawUnreachedBar) {
            canvas.drawRect(mUnReachedRectF, mUnReachedBarPaint);
        }

        if (mIfDrawText) {
            mDrawTextHeight = MiscUtil.getTextHeight(mTextPaint);
            float mDrawTextY = (float) (getHeight() / 2) + MiscUtil.getTextBaseLine(mTextPaint);
            mCurrentDrawText = String.format("%s",getProgress() * 100 / getMax() + mSuffix);
            canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextY, mTextPaint);
        }
    }

    private void calculateDrawRectF() {
        mCurrentDrawText = String.valueOf(getProgress() * 100 / getMax());
        mCurrentDrawText = mCurrentDrawText + mSuffix;
        float mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);

        if (getProgress() == 0) {
            mDrawReachedBar = false;
            mDrawTextStart = getPaddingLeft() + mOffset;
        } else {
            mDrawReachedBar = true;
            mReachedRectF.left = getPaddingLeft();
            mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight() -
                    mDrawTextWidth) / (getMax() * 1.0f) * getProgress() - mOffset + getPaddingLeft();
            mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
            mDrawTextStart = (mReachedRectF.right + mOffset);
        }

        if ((mDrawTextStart + mDrawTextWidth) >= getWidth() - getPaddingRight()) {
            mDrawTextStart = getWidth() - getPaddingRight() - mMaxDrawTextWidth;
            mReachedRectF.right = mDrawTextStart - mOffset;
        }

        // 当mReachedBarHeight用户没有设置大小，那么会使用默认大小，并且百分比下面没有unReachedBar；
        // 当mReachedBarHeight值小于mDrawTextHeight时，unReachedBar同样会位于百分比下方；
        float unreachedBarStart;
        if (mReachedBarHeight <= default_reached_bar_height || mDrawTextHeight > mReachedBarHeight) {
            unreachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
        } else {
            unreachedBarStart = mReachedRectF.right;
        }

        if (unreachedBarStart >= getWidth() - getPaddingRight() - mMaxDrawTextWidth) {
            mDrawUnreachedBar = false;
        } else {
            mDrawUnreachedBar = true;
            mUnReachedRectF.left = unreachedBarStart;
            mUnReachedRectF.right = getWidth() - getPaddingRight() - mMaxDrawTextWidth - mOffset;
            mUnReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mUnReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
        }
    }

    private void calculateDrawRectFWithoutProgressText() {
        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
        mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight()) /
                (getMax() * 1.0f) * getProgress() + getPaddingLeft();
        mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;

        mUnReachedRectF.left = mReachedRectF.right;
        mUnReachedRectF.right = getWidth() - getPaddingRight();
        mUnReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
        mUnReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize());
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight());
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight());
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor());
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffix());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
            mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
            mUnReachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
            initializePainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            setSuffix(bundle.getString(INSTANCE_SUFFIX));
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private void startAnimator(float start, float end, long animTime) {
        // 属性动画
        ValueAnimator mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mCurrentProgress = (int) (mPercent * mMaxProgress);
                invalidate();
            }
        });
        mAnimator.start();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            mSuffix = "";
        } else {
            mSuffix = suffix;
        }
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setProgress(float progress) {
        if (progress > mMaxProgress) {
            progress = mMaxProgress;
        }
        float start = mPercent;
        float end =  progress / mMaxProgress;
        startAnimator(start, end, mAnimTime);
    }

    public int getMax() {
        return mMaxProgress;
    }

    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
        }
    }

    public void setAnimTime(long mAnimTime) {
        if (mAnimTime > 0) {
            this.mAnimTime = mAnimTime;
        }
    }

    public void setMaxProgress(int mMaxProgress) {
        if (mMaxProgress > 0) {
            this.mMaxProgress = mMaxProgress;
        }
    }

    public float getReachedBarHeight() {
        return mReachedBarHeight;
    }

    public float getUnreachedBarHeight() {
        return mReachedBarHeight;
    }

    public float getProgressTextSize() {
        return mTextSize;
    }

    public int getUnreachedBarColor() {
        return mUnReachedBarColor;
    }

    public int getReachedBarColor() {
        return mReachedBarColor;
    }

    public int getProgress() {
        return mCurrentProgress;
    }

}
