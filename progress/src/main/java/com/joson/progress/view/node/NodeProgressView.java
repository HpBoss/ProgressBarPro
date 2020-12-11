package com.joson.progress.view.node;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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
 * @Auther: hepiao
 * @CreatTime: 2020/9/23
 * @Description: 节点型增加进度条
 */
public class NodeProgressView extends View {
    /**
     * 画笔宽度
     */
    private float mStrokeWidth;
    /**
     * 总的节点数
     */
    private int mStage;
    /**
     * 当前节点
     */
    private int mCurStage = 1;
    /**
     * 上一次节点
     */
    private int mOldStage = 1;
    /**
     * 圆半径
     */
    private float mRadius;
    /**
     * 前景画笔
     */
    private Paint mForeNodePaint;
    /**
     * 后景画笔
     */
    private Paint mBackNodePaint;
    /**
     * 前景节点Path
     */
    private Path mNodePath;
    /**
     * view中心点
     */
    private Point mCenterPoint;
    private RectF mRectF;
    /**
     * 后景绿色圆外接圆
     */
    private RectF mCircleRect;
    /**
     * 后景绿色矩形条
     */
    private RectF mBlockRect;
    /**
     * 绿色矩形条长度
     */
    private float mInterval;
    /**
     * 后景绿色圆水平进度实时长度
     */
    private float xWidth;
    /**
     * 后景绿色矩形进度条实时长度
     */
    private float xDistance;
    /**
     * 圆环上圆弧水平一半长度
     */
    private float xGap;
    /**
     * 圆心到矩形条上边的垂直距离
     */
    private float yGap;
    /**
     * 判定当前动画是否进行
     */
    private boolean isRunning;
    /**
     * 节点左端x坐标起点
     */
    private final float x0 = 3 * mStrokeWidth;
    /**
     * 默认最小stage，如果输入stage小于3，则stage设为3
     */
    private static final int default_stage = 3;
    private final Context mContext;

    public static final String INSTANCE_STATE = "saved_instance";
    public static final String INSTANCE_STROKE_WIDTH = "stroke_width";
    public static final String INSTANCE_STAGE = "stage";
    public static final String INSTANCE_CUR_STAGE = "cur_stage";
    public static final String INSTANCE_OLD_STAGE = "old_stage";
    public static final String INSTANCE_RADIUS = "radius";
    public static final String INSTANCE_INTERVAL = "interval";
    public static final String INSTANCE_X_WIDTH = "x_width";
    public static final String INSTANCE_X_DISTANCE = "x_distance";
    public static final String INSTANCE_X_GAP = "x_gap";
    public static final String INSTANCE_Y_GAP = "y_gap";
    public static final String INSTANCE_IS_RUNNING = "is_running";

    public NodeProgressView(Context context) {
        this(context, null);
    }

    public NodeProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        float default_stroke_width = MiscUtil.dp2px(context, 1f);
        float default_radius = MiscUtil.dp2px(context, 10f);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NodeProgressView, defStyleAttr, 0);
        mStage = typedArray.getInt(R.styleable.NodeProgressView_progressStage, default_stage);
        if (mStage < 1) mStage = default_stage;
        mStrokeWidth = typedArray.getFloat(R.styleable.NodeProgressView_progressStrokeWidth, default_stroke_width);
        mRadius = typedArray.getFloat(R.styleable.NodeProgressView_progressRadius, default_radius);
        typedArray.recycle();
        initializePainters();
    }

    private void initializePainters() {
        mCenterPoint = new Point();

        mForeNodePaint = new Paint();
        mForeNodePaint.setColor(getResources().getColor(R.color.darkGray));
        mForeNodePaint.setAntiAlias(true);
        mForeNodePaint.setStrokeWidth(mStrokeWidth);
        mForeNodePaint.setStyle(Paint.Style.FILL);

        mBackNodePaint = new Paint();
        mBackNodePaint.setColor(getResources().getColor(R.color.lightGreen));
        mBackNodePaint.setAntiAlias(true);

        mNodePath = new Path();
        mRectF = new RectF();
        mBlockRect = new RectF();
        mCircleRect = new RectF();

        xGap = (float) (mRadius * Math.cos(Math.PI / 12));
        yGap = (float) (mRadius * Math.sin(Math.PI / 12));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, MiscUtil.dipToPx(mContext, 350)),
                MiscUtil.measure(heightMeasureSpec, MiscUtil.dipToPx(mContext, 50)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        mInterval = (w - (mRadius + mStrokeWidth) * 2 * mStage - 4 * mStrokeWidth) / (mStage - 1);

        mBlockRect.top = mCenterPoint.y - yGap;
        mBlockRect.bottom = mCenterPoint.y + yGap;

        mCircleRect.top = mCenterPoint.y - mRadius - mStrokeWidth;
        mCircleRect.bottom = mCenterPoint.y + mRadius + mStrokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制前景，这里我采用了一种比较笨的方法，但是绘制出的效果极佳，前景可空心、可实心，设置画笔stroke即可
        drawForeground(canvas, mStage - 1, mInterval);
        // 绘制后景第一个绿色圆，这是每次必绘制的，逻辑上当你使用节点进度条时，进来显示的肯定是「第一步」
        mRectF.set(x0, mCenterPoint.y - mRadius - mStrokeWidth,
                x0 + 2 * (mRadius + mStrokeWidth), mCenterPoint.y + mRadius + mStrokeWidth);
        canvas.drawArc(mRectF, 0, 360, false, mBackNodePaint);
        // 接下来绘制剩余的后景，这里要分为三种情形：上一步、下一步、无任何操作，通过mOldStage和mCurStage大小关系去判断
        if (mOldStage < mCurStage) {
            drawFinishProgress(canvas, mOldStage);
        } else if (mOldStage > mCurStage) {
            drawFinishProgress(canvas, mCurStage);
        } else {
            drawBackground(canvas, mOldStage);
        }
    }

    /**
     * 绘制不变的部分
     * @param canvas
     * @param stage 根据当前情形的不同，选择传入的stage也不同
     */
    private void drawBackground(Canvas canvas, int stage) {
        mBlockRect.left = x0 + mRadius + xGap + 2 * mStrokeWidth;
        mCircleRect.left = x0 + (mRadius + mStrokeWidth) * 2 + mInterval - 2 * (mRadius - xGap);
        for (int i = 0; i < stage - 1; i++) {
            mCircleRect.right = mCircleRect.left + 2 * (mRadius + mStrokeWidth);
            mBlockRect.right = mBlockRect.left + mInterval;
            canvas.drawRect(mBlockRect, mBackNodePaint);
            canvas.drawArc(mCircleRect, 0, 360, false, mBackNodePaint);
            mBlockRect.left = mBlockRect.right + 2 * (xGap + mStrokeWidth);
            mCircleRect.left = mCircleRect.right + mInterval - 2 * (mRadius - xGap);
        }
    }

    /**
     * 绘制后景实时进度：先绘制不变的部分，再绘制当前变化的部分
     * @param canvas
     * @param stage
     */
    private void drawFinishProgress(Canvas canvas, int stage) {
        drawBackground(canvas, stage);
        mBlockRect.right = mBlockRect.left + xDistance;
        mCircleRect.right = mCircleRect.left + 2 * (mRadius + mStrokeWidth);
        canvas.drawRect(mBlockRect, mBackNodePaint);
        drawFinishCircle(canvas, mCircleRect);
    }

    /**
     * 绘制圆进度水平变化
     * @param canvas
     * @param rectF
     */
    private void drawFinishCircle(Canvas canvas, RectF rectF) {
        float angle = (float) (Math.acos((mRadius - xWidth) / mRadius) * 180 / Math.PI);
        float startAngle = 180 - angle;
        float sweepAngle = angle * 2;
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mBackNodePaint);
    }

    /**
     * 前景圆水平进度变化属性动画
     */
    private void xWidthAnimator(float start, float end, long animTime, final float max) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        if (mCurStage > mOldStage) mAnimator.setStartDelay(1000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mPercent = (float) animation.getAnimatedValue();
                xWidth = (int) (mPercent * max);
                invalidate();
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mCurStage > mOldStage) {
                    isRunning = false;
                    mOldStage++;
                    xWidth = xWidth == 2 * mRadius ? 0 : xWidth;
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mCurStage < mOldStage) {
                    isRunning = true;
                    xDistance = xDistance == 0 ? mInterval : xDistance;
                }
            }
        });
        mAnimator.start();
    }

    /**
     * 前景矩形进度条属性动画
     */
    private void xDistanceAnimator(float start, float end, long animTime, final float max) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        if (mCurStage < mOldStage) mAnimator.setStartDelay(500);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mPercent = (float) animation.getAnimatedValue();
                xDistance = (int) (mPercent * max);
                invalidate();
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mCurStage > mOldStage) isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mCurStage < mOldStage) {
                    isRunning = false;
                    mOldStage--;
                }
            }
        });
        mAnimator.start();
    }

    private void drawHead() {
        mNodePath.moveTo(x0, mCenterPoint.y);
        mRectF.left = x0;
        mRectF.top = mCenterPoint.y - mRadius - mStrokeWidth;
        mRectF.right = (mRadius + mStrokeWidth) * 2 + x0;
        mRectF.bottom = mCenterPoint.y + mRadius + mStrokeWidth;
        mNodePath.arcTo(mRectF, 180, 165, false);
    }

    private void drawForeground(Canvas canvas, int stage, float interval) {
        drawHead();
        float x = x0 + mRadius + xGap + 2 * mStrokeWidth;
        float y = mCenterPoint.y - yGap;
        int st = stage;
        // 从左至右绘制上半部分路径
        while (stage != 0) {
            x += interval;
            mNodePath.lineTo(x, y);
            mRectF.left = x - (mRadius - xGap);
            mRectF.right = x + mRadius + xGap + 2 * mStrokeWidth;
            if (stage == 1) {
                mNodePath.arcTo(mRectF, 195, 330, false);
                y += 2 * yGap;
            } else {
                mNodePath.arcTo(mRectF, 195, 150, false);
                x += 2 * (xGap + mStrokeWidth);
            }
            stage--;
        }
        // 从右至左绘制下半部分路径
        while (stage != st) {
            x -= interval;
            mNodePath.lineTo(x, y);
            mRectF.left = x - (mRadius + xGap + 2 * mStrokeWidth);
            mRectF.right = x + (mRadius - xGap);
            if (stage == st - 1) {
                mNodePath.arcTo(mRectF, 15, 165, false);
            } else {
                mNodePath.arcTo(mRectF, 15, 150, false);
                x -= (2 * (xGap + mStrokeWidth));
            }
            stage++;
        }
        canvas.drawPath(mNodePath, mForeNodePaint);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putFloat(INSTANCE_STROKE_WIDTH, mStrokeWidth);
        bundle.putInt(INSTANCE_STAGE, mStage);
        bundle.putInt(INSTANCE_CUR_STAGE, mCurStage);
        bundle.putInt(INSTANCE_OLD_STAGE, mOldStage);
        bundle.putFloat(INSTANCE_RADIUS, mRadius);
        bundle.putFloat(INSTANCE_INTERVAL, mInterval);
        bundle.putFloat(INSTANCE_X_WIDTH, xWidth);
        bundle.putFloat(INSTANCE_X_DISTANCE, xDistance);
        bundle.putFloat(INSTANCE_X_GAP, xGap);
        bundle.putFloat(INSTANCE_Y_GAP, yGap);
        bundle.putBoolean(INSTANCE_IS_RUNNING, isRunning);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mStrokeWidth = bundle.getFloat(INSTANCE_STROKE_WIDTH);
            mStage = bundle.getInt(INSTANCE_STAGE);
            mCurStage = bundle.getInt(INSTANCE_CUR_STAGE);
            mOldStage = bundle.getInt(INSTANCE_OLD_STAGE);
            mRadius = bundle.getFloat(INSTANCE_RADIUS);
            mInterval = bundle.getFloat(INSTANCE_INTERVAL);
            xWidth = bundle.getFloat(INSTANCE_X_WIDTH);
            xDistance = bundle.getFloat(INSTANCE_X_DISTANCE);
            xGap = bundle.getFloat(INSTANCE_X_GAP);
            yGap = bundle.getFloat(INSTANCE_Y_GAP);
            isRunning = bundle.getBoolean(INSTANCE_IS_RUNNING);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * 下一步
     */
    public void addStage() {
        if (mCurStage + 1 <= mStage && !isRunning) {
            mCurStage++;
            xDistanceAnimator(0, 1, 1000, mInterval);
            xWidthAnimator(0, 1, 500, 2 * mRadius);
        }
    }

    /**
     * 上一步
     */
    public void backStage() {
        if (mCurStage - 1 >= 1 && !isRunning) {
            mCurStage--;
            xWidthAnimator(1, 0, 500, 2 * mRadius);
            xDistanceAnimator(1, 0, 1000, mInterval);
        }
    }

    public int getCurStage() {
        return mCurStage;
    }
}
