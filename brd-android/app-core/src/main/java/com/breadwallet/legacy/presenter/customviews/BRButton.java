package com.breadwallet.legacy.presenter.customviews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

import com.breadwallet.appcore.R;
import com.breadwallet.tools.util.Utils;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 5/3/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class BRButton extends Button {
    private static final String TAG = BRButton.class.getName();
    private static int ANIMATION_DURATION = 30;
    private Bitmap shadow;
    private Rect shadowRect;
    private RectF bRect;
    private int width;
    private int height;
    private int modifiedWidth;
    private int modifiedHeight;
    private Paint bPaint;
    private Paint bPaintStroke;
    private int type = 2;
    private static final float SHADOW_PRESSED = 0.88f;
    private static final float SHADOW_UNPRESSED = 0.95f;
    private float shadowOffSet = SHADOW_UNPRESSED;
    private static final int ROUND_PIXELS = 8;
    private boolean isBreadButton; //meaning is has the special animation and shadow
    private boolean hasShadow; // allows us to add/remove the drop shadow from the button without affecting the animation
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    public BRButton(Context context) {
        super(context);
        init(context, null);
    }

    public BRButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BRButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BRButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        shadow = BitmapFactory.decodeResource(getResources(), R.drawable.shadow);
        bPaint = new Paint();
        bPaintStroke = new Paint();
        shadowRect = new Rect(0, 0, 100, 100);
        bRect = new RectF(0, 0, 100, 100);
        TypedArray attributes = ctx.obtainStyledAttributes(attrs, R.styleable.BRButton);
        float px16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        //check attributes you need, for example all paddings
        int[] attributeArray = new int[]{android.R.attr.paddingStart, android.R.attr.paddingTop, android.R.attr.paddingEnd,
                android.R.attr.paddingBottom, R.attr.isBreadButton, R.attr.buttonType};
        //then obtain typed array
        TypedArray arr = ctx.obtainStyledAttributes(attrs, attributeArray);
        //You can check if attribute exists (in this example checking paddingRight)

        isBreadButton = attributes.getBoolean(R.styleable.BRButton_isBreadButton, false);
        int buttonColor = attributes.getResourceId(R.styleable.BRButton_buttonColor, 0);
        int paddingLeft = arr.hasValue(0) ? arr.getDimensionPixelOffset(0, -1) : (int) px16;
        int paddingTop = arr.hasValue(1) ? arr.getDimensionPixelOffset(1, -1) : 0;
        int paddingRight = arr.hasValue(2) ? arr.getDimensionPixelOffset(2, -1) : (int) px16;
        int paddingBottom = arr.hasValue(3) ? arr.getDimensionPixelOffset(3, -1) + (isBreadButton ? (int) px16 : 0) : (isBreadButton ? (int) px16 : 0);

        hasShadow = attributes.getBoolean(R.styleable.BRButton_hasShadow, true);

        int type = attributes.getInteger(R.styleable.BRButton_buttonType, 0);
        setType(type);

        bPaint.setAntiAlias(true);
        bPaintStroke.setAntiAlias(true);

        if (isBreadButton) {
            setBackground(getContext().getDrawable(R.drawable.shadow_trans));
        }

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        attributes.recycle();
        arr.recycle();

        if (buttonColor > 0) {
            bPaint.setColor(getContext().getColor(buttonColor));
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewTreeObserver observer = getViewTreeObserver();
        layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                Utils.correctTextSizeIfNeeded(BRButton.this);
                correctTextBalance();
            }
        };
        observer.addOnGlobalLayoutListener(layoutListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        final ViewTreeObserver observer = getViewTreeObserver();
        observer.removeOnGlobalLayoutListener(layoutListener);
        layoutListener = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isBreadButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (type != 3)
                    press(ANIMATION_DURATION);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                unPress(ANIMATION_DURATION);
            }
        }

        return super.onTouchEvent(event);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

    }

    private void correctTextBalance() {
        //implement if needed in the future
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isBreadButton) {
            if (hasShadow) {
                shadowRect.set(5, height / 4, width - 5, (int) (height * shadowOffSet));
                canvas.drawBitmap(shadow, null, shadowRect, null);

            }
            modifiedWidth = width - 10;
            modifiedHeight = height - height / 4 - 5;
            bRect.set(5, 5, modifiedWidth, modifiedHeight + 5);
            if (type == 2 || type == 3)
                canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaintStroke);
            if (type == 8 || type == 9) {
                canvas.drawRoundRect(bRect, 22, 22, bPaint);
            } else {
                canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaint);
            }

        }
        super.onDraw(canvas);

    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        invalidate();
    }

    public void setColor(int color) {
        bPaint.setColor(color);
        invalidate();
    }

    public void setType(int type) {
        if (type == 3) press(1);
        this.type = type;

        if (type == 1) { //blue
            bPaint.setColor(getContext().getColor(R.color.button_primary_normal));
            setTextColor(getContext().getColor(R.color.white));
        } else if (type == 2) { //gray stroke
            bPaintStroke.setColor(getContext().getColor(R.color.extra_light_gray));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            setTextColor(getContext().getColor(R.color.light_gray));
            bPaint.setColor(getContext().getColor(R.color.button_secondary));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 3) { //blue stroke
            bPaintStroke.setColor(getContext().getColor(R.color.button_primary_normal));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            setTextColor(getContext().getColor(R.color.button_primary_normal));
            bPaint.setColor(getContext().getColor(R.color.button_secondary));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 4) {
            bPaintStroke.setColor(getContext().getColor(R.color.currency_buttons_color));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            setTextColor(getContext().getColor(R.color.white));
            bPaint.setColor(getContext().getColor(R.color.currency_buttons_color));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 5) {
            bPaintStroke.setColor(getContext().getColor(R.color.blue));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            setTextColor(getContext().getColor(R.color.blue));
            bPaint.setColor(getContext().getColor(R.color.white));
            bPaint.setStyle(Paint.Style.FILL);

        }
        // Create new wallet button
        else if (type == 6) {
            setHasShadow(false);
            TypedValue buttonColorValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.create_new_wallet_background, buttonColorValue, true);
            setTextColor(getContext().getColor(R.color.white));
            bPaint.setColor(getContext().getColor(buttonColorValue.resourceId));
            bPaint.setStyle(Paint.Style.FILL);
        }

        // Recover wallet button
        else if (type == 7) {
            setHasShadow(false);
            TypedValue buttonColorValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.recover_wallet_background, buttonColorValue, true);
            setTextColor(getContext().getColor(R.color.white));
            bPaint.setColor(getContext().getColor(buttonColorValue.resourceId));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 8) {
            setHasShadow(false);
            TypedValue buttonColorValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.deny_wallet_link_button, buttonColorValue, true);
            setTextColor(getContext().getColor(R.color.white));
            bPaint.setColor(getContext().getColor(buttonColorValue.resourceId));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 9) {
            setHasShadow(false);
            TypedValue buttonColorValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.approve_wallet_link_button, buttonColorValue, true);
            setTextColor(getContext().getColor(R.color.white));
            bPaint.setColor(getContext().getColor(buttonColorValue.resourceId));
            bPaint.setStyle(Paint.Style.FILL);
        }
        invalidate();
    }

    public int getType() {
        return type;
    }

    public void makeGradient(int startColor, int endColor) {
        bPaint.setShader(new LinearGradient(0, 0, getWidth(), 0, startColor, endColor, Shader.TileMode.MIRROR));
        invalidate();
    }

    private void press(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                1f, 0.96f,
                1f, 0.96f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_UNPRESSED, SHADOW_PRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(animation -> {
            shadowOffSet = (float) animation.getAnimatedValue();
            invalidate();
        });
        shadowAnim.addListener(new ClearAnimationListener());

        startAnimation(scaleAnim);
        shadowAnim.start();

    }

    private void unPress(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.96f, 1f,
                0.96f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_PRESSED, SHADOW_UNPRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(animation -> {
            shadowOffSet = (float) animation.getAnimatedValue();
            invalidate();
        });
        shadowAnim.addListener(new ClearAnimationListener());

        startAnimation(scaleAnim);
        shadowAnim.start();
    }

    private class ClearAnimationListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            clearAnimation();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
