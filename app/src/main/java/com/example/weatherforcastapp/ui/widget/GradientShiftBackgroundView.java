package com.example.weatherforcastapp.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Nền gradient chuyển động nhẹ (khung UI — tham khảo Figma / glass style).
 */
public class GradientShiftBackgroundView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private LinearGradient gradient;
    private float shift;
    private ValueAnimator animator;

    private int colorTop = 0xFF5DBBFF;
    private int colorMid = 0xFF3D8BFF;
    private int colorBottom = 0xFF1E4A9A;

    public GradientShiftBackgroundView(Context context) {
        super(context);
        init();
    }

    public GradientShiftBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientShiftBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
    }

    public void setGradientColors(int top, int mid, int bottom) {
        this.colorTop = top;
        this.colorMid = mid;
        this.colorBottom = bottom;
        gradient = null;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gradient = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startShift();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShift();
    }

    private void ensureGradient(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (gradient == null) {
            float span = h * 1.35f;
            gradient = new LinearGradient(
                    0, 0, 0, span,
                    new int[]{colorTop, colorMid, colorBottom},
                    new float[]{0f, 0.45f, 1f},
                    Shader.TileMode.CLAMP
            );
            paint.setShader(gradient);
        }
    }

    private void startShift() {
        if (animator != null) return;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(10_000L);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> {
            shift = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopShift() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        ensureGradient(w, h);
        if (gradient == null) return;
        matrix.reset();
        float dy = (shift - 0.5f) * h * 0.08f;
        matrix.setTranslate(0, dy);
        gradient.setLocalMatrix(matrix);
        canvas.drawRect(0, 0, w, h, paint);
    }
}
