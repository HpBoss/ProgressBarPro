package com.joson.progress.utils;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;

/**
 * @Auther: 何飘
 * @Date: 11/19/20 15:43
 * @Description:
 */
public class MiscUtil {
    // 求解：垂直中心点离BaseLine的距离（正）
    public static float getTextBaseLine(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
    }

    public static float getTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom - fontMetrics.top;
    }

    public static int measure(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    public static int dipToPx(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public static float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }

    public static float sp2px(Context context, float sp) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
}
