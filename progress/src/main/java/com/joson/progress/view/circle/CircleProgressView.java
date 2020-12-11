package com.joson.progress.view.circle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
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
 * @Description: 圆形增加进度条
 */
public class CircleProgressView extends View {
    private final int SEMI_CIRCLE = 0;
    private final int FULL_CIRCLE = 1;
    private int mCircleMode;
    private int mMaxProgress;
    private int mReachedRectColor;
    private int mUnReachedRectColor;
    private int mValueColor;
    private int mHintColor;
    private float mValueSize;
    private float mHintSize;
    private float mStrokeWidth;
    private String mUnit;
    private String mHint;
    private float mValueTextY;
    private float mHintTextY;
    private float mPercent;
    private int mCurrentProgress = 0;
    private int[] mGradientColors;
    private int mGradientArcColors;

    private Paint mReachedRectPaint;
    private Paint mUnReachedRectPaint;
    private Paint mValuePaint;
    private Paint mHintPaint;

    /**
     * 圆心坐标、半径
     */
    private Point mCenterPoint;

    /**
     * value与hint的垂直间隔(占据半径的比例)
     */
    private float mTextOffsetPercentInRadius;

    /**
     * mStartAngle：起始需要旋转的角度
     */
    private float mStartAngle;
    private RectF mRectF;

    private final Context mContext;
    /**
     * 动画执行一次的时间
     */
    private long mAnimTime;

    private static final int default_value_text_color = Color.rgb(66, 145, 241);
    private static final int default_hint_color = Color.rgb(66, 145, 241);
    private static final int default_reached_color = Color.rgb(66, 145, 241);
    private static final int default_unreached_color = Color.rgb(204, 204, 204);

    public static final String INSTANCE_STATE = "saved_instance";
    public static final String INSTANCE_CIRCLE_MODE = "circle_mode";
    public static final String INSTANCE_REACHED_RECT_COLOR = "reached_rect_color";
    public static final String INSTANCE_UNREACHED_RECT_COLOR = "unreached_rect_color";
    public static final String INSTANCE_VALUE_COLOR = "value_color";
    public static final String INSTANCE_HINT_COLOR = "hint_color";
    public static final String INSTANCE_VALUE_SIZE = "value_size";
    public static final String INSTANCE_HINT_SIZE = "hint_size";
    public static final String INSTANCE_STROKE_WIDTH = "stroke_width";
    public static final String INSTANCE_TEXT_OFFSET_PERCENT_IN_RADIUS = "text_offset_percent_in_radius";
    public static final String INSTANCE_MAX_PROGRESS = "max_progress";
    public static final String INSTANCE_UNIT = "unit";
    public static final String INSTANCE_HINT = "hint";
    public static final String INSTANCE_ANIM_TIME = "anim_time";
    public static final String INSTANCE_GRADIENT_ARC_COLORS = "gradient_arc_colors";


    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        float default_value_text_size = MiscUtil.sp2px(mContext, 10);
        float default_hint_text_size = MiscUtil.sp2px(mContext, 20);
        float default_stroke_width = MiscUtil.dp2px(mContext, 5f);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleProgress, defStyleAttr, 0);
        // 从xml中获取属性所对应的值，传递到view内部。
        mCircleMode = typedArray.getInt(R.styleable.CircleProgress_progress_mode, SEMI_CIRCLE);
        mReachedRectColor = typedArray.getColor(R.styleable.CircleProgress_progress_reached_color, default_reached_color);
        mUnReachedRectColor = typedArray.getColor(R.styleable.CircleProgress_progress_unreached_color, default_unreached_color);
        mValueColor = typedArray.getColor(R.styleable.CircleProgress_progress_value_text_color, default_value_text_color);
        mHintColor = typedArray.getColor(R.styleable.CircleProgress_progress_hint_color, default_hint_color);
        mValueSize = typedArray.getDimension(R.styleable.CircleProgress_progress_value_text_size, default_value_text_size);
        mHintSize = typedArray.getDimension(R.styleable.CircleProgress_progress_hint_text_size, default_hint_text_size);
        mStrokeWidth = typedArray.getDimension(R.styleable.CircleProgress_progress_stroke_width, default_stroke_width);
        mTextOffsetPercentInRadius = typedArray.getFloat(R.styleable.CircleProgress_text_Offset_Percent_In_Radius, 0.44f);
        mMaxProgress = typedArray.getInt(R.styleable.CircleProgress_progress_max, 100);
        mUnit = typedArray.getString(R.styleable.CircleProgress_progress_unit);
        mHint = typedArray.getString(R.styleable.CircleProgress_progress_hint);
        mAnimTime = typedArray.getInteger(R.styleable.CircleProgress_progress_animTime, 1000);
        //setPadding(0,(int) MiscUtil.dp2px(mContext, 5f),0,(int) MiscUtil.dp2px(mContext, 5f));
        mGradientArcColors = typedArray.getResourceId(R.styleable.CircleProgress_arcColors, 0);
        if (mGradientArcColors != 0) {
            try {
                int[] gradientColors = getResources().getIntArray(mGradientArcColors);
                if (gradientColors.length == 1) {
                    mGradientColors = new int[1];
                    mGradientColors[0] = gradientColors[0];
                } else if (gradientColors.length == 2) {
                    mGradientColors = new int[2];
                    mGradientColors[0] = gradientColors[0];
                    mGradientColors[1] = gradientColors[1];
                } else {
                    mGradientColors = new int[3];
                    mGradientColors[0] = gradientColors[0];
                    mGradientColors[1] = gradientColors[1];
                    mGradientColors[2] = gradientColors[2];
                }
            } catch (Resources.NotFoundException e) {
                throw new Resources.NotFoundException("the give resource not found.");
            }
        }

        typedArray.recycle();
        initializePainters();
    }

    private void initializePainters() {
        // 设置画笔是抗锯齿的
        mReachedRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedRectPaint.setColor(mReachedRectColor);
        // 设置画笔的style为STROKE也是画圆环的必备设置
        mReachedRectPaint.setStyle(Paint.Style.STROKE);
        mReachedRectPaint.setStrokeWidth(mStrokeWidth);
        // 设置画笔绘制图案的边界线的形状，ROUND代表图案的边角是圆角的，如果是SQUARE那么就是方形的。
        mReachedRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mUnReachedRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnReachedRectPaint.setColor(mUnReachedRectColor);
        mUnReachedRectPaint.setStyle(Paint.Style.STROKE);
        mUnReachedRectPaint.setStrokeWidth(mStrokeWidth);
        mUnReachedRectPaint.setStrokeCap(Paint.Cap.ROUND);

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setTextAlign(Paint.Align.CENTER);
        mValuePaint.setColor(mValueColor);
        mValuePaint.setTextSize(mValueSize);

        mHintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 设置画笔从文字的中间开始draw，也就是说x坐标为文字的中间点
        // 如果没有这样设置，那么绘制文字的时候就是从文字的左端开始绘制。
        mHintPaint.setTextAlign(Paint.Align.CENTER);
        mHintPaint.setColor(mHintColor);
        mHintPaint.setTextSize(mHintSize);
        //mHintPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mCenterPoint = new Point();
        mRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 注意：我们做自定义view的时候，对于外界没有给定view明确的大小的时候，我们需要实现设定一个最小值，来做兜底。
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, MiscUtil.dipToPx(mContext, 150)),
                MiscUtil.measure(heightMeasureSpec, MiscUtil.dipToPx(mContext, 150)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 为了让progressView更好的贴合四周，也不超出指定范围大小，因此要选取w、h中最小的那一个，并除以2作为半径
        // 首先我们要明白想要画出一个圆环，所必须要的条件，这个mRadius属于哪一部分，mRadius = 里层背景圆的半径 + 外圆环宽度的一半
        float mRadius = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) mStrokeWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) mStrokeWidth) / (float)2;
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;

        // 绘制矩形是绘制圆环最为重要的一步，而且以mRadius为半径的圆是这个矩形的内截圆
        mRectF.left = mCenterPoint.x - mRadius - mStrokeWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - mStrokeWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + mStrokeWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + mStrokeWidth / 2;

        // 下面计算value和hint文字绘制时所需的y坐标值。
        // value的y坐标值并不能简单的认为是mCenterPoint.y，对于文字的绘制我们需要找BaseLine。
        // 因为文字的排版顶部、底部都不是一样高的。例如：my、ah，但我们找到基准线，我的理解就是这是一连串文字垂直方向上的重心。
        mValueTextY = mCenterPoint.y + MiscUtil.getTextBaseLine(mValuePaint);
        mHintTextY = mCenterPoint.y + MiscUtil.getTextBaseLine(mHintPaint) - mRadius * mTextOffsetPercentInRadius;

        if (mGradientColors != null) {
            setArcGradientPaint();
            mGradientColors =  null;
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_CIRCLE_MODE, mCircleMode);
        bundle.putInt(INSTANCE_REACHED_RECT_COLOR, mReachedRectColor);
        bundle.putInt(INSTANCE_UNREACHED_RECT_COLOR, mUnReachedRectColor);
        bundle.putInt(INSTANCE_VALUE_COLOR, mValueColor);
        bundle.putInt(INSTANCE_HINT_COLOR, mHintColor);
        bundle.putFloat(INSTANCE_VALUE_SIZE, mValueSize);
        bundle.putFloat(INSTANCE_HINT_SIZE, mHintSize);
        bundle.putFloat(INSTANCE_STROKE_WIDTH, mStrokeWidth);
        bundle.putFloat(INSTANCE_TEXT_OFFSET_PERCENT_IN_RADIUS, mTextOffsetPercentInRadius);
        bundle.putInt(INSTANCE_MAX_PROGRESS, mMaxProgress);
        bundle.putString(INSTANCE_UNIT, mUnit);
        bundle.putString(INSTANCE_HINT, mHint);
        bundle.putLong(INSTANCE_ANIM_TIME, mAnimTime);
        bundle.putInt(INSTANCE_GRADIENT_ARC_COLORS, mGradientArcColors);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mCircleMode = bundle.getInt(INSTANCE_CIRCLE_MODE);
            mReachedRectColor = bundle.getInt(INSTANCE_REACHED_RECT_COLOR);
            mUnReachedRectColor = bundle.getInt(INSTANCE_UNREACHED_RECT_COLOR);
            mValueColor = bundle.getInt(INSTANCE_VALUE_COLOR);
            mHintColor = bundle.getInt(INSTANCE_HINT_COLOR);
            mValueSize = bundle.getInt(INSTANCE_VALUE_SIZE);
            mHintSize = bundle.getInt(INSTANCE_HINT_SIZE);
            mStrokeWidth = bundle.getFloat(INSTANCE_STROKE_WIDTH);
            mTextOffsetPercentInRadius = bundle.getFloat(INSTANCE_TEXT_OFFSET_PERCENT_IN_RADIUS);
            mMaxProgress = bundle.getInt(INSTANCE_MAX_PROGRESS);
            mUnit = bundle.getString(INSTANCE_UNIT);
            mHint = bundle.getString(INSTANCE_HINT);
            mAnimTime = bundle.getLong(INSTANCE_ANIM_TIME);
            mGradientArcColors = bundle.getInt(INSTANCE_GRADIENT_ARC_COLORS);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawArc(canvas);
    }

    private void drawText(Canvas canvas) {
        // 绘制百分比值的内容（包括单位，如：%、步、次）
        canvas.drawText(mCurrentProgress + mUnit, mCenterPoint.x, mValueTextY, mValuePaint);
        // 绘制提示文字
        if (mHint != null) {
            canvas.drawText(mHint, mCenterPoint.x, mHintTextY, mHintPaint);
        }
    }

    private void drawArc(Canvas canvas) {
        canvas.save();
        float mSweepAngle;
        if (mCircleMode == SEMI_CIRCLE) {
            // 起点位于三点钟方向，顺时针方向旋转为正，mStartAngle就代表从三点钟顺时针旋转的角度，旋转的目的是改变起点的位置
            canvas.rotate(135, mCenterPoint.x, mCenterPoint.y);
            mSweepAngle = 270;
        } else {
            canvas.rotate(-90, mCenterPoint.x, mCenterPoint.y);
            mSweepAngle = 360;
        }
        // mSweepAngle代表这个progress总的度数，百分比乘以总的度数（mSweepAngle）就等于当前的角度（currentAngle）
        float currentAngle = mSweepAngle * mPercent;
        // 绘制背景色（进度条未达到的区域的颜色）
        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle + 2, false, mUnReachedRectPaint);
        // 绘制进度条
        canvas.drawArc(mRectF, 2, currentAngle, false, mReachedRectPaint);
        // 防止本次设置了渐变色，但下一次并不需要渐变色
        //mReachedBarPaint.setShader(null);
        canvas.restore();
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

    public void setProgress(float progress) {
        if (progress > mMaxProgress) {
            progress = mMaxProgress;
        }
        float start = mPercent;
        float end =  progress / mMaxProgress;
        startAnimator(start, end, mAnimTime);
    }

    private void setArcGradientPaint() {
        SweepGradient mSweepGradient = new SweepGradient(mCenterPoint.x, mCenterPoint.y, mGradientColors, null);
        mReachedRectPaint.setShader(mSweepGradient);
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
