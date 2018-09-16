package com.ojsb.webviewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by brady on 2018/9/15.
 */

public class CircleClipFrameLayout extends FrameLayout{

    final Path mClipPath = new Path();

    public CircleClipFrameLayout(@NonNull Context context) {
        super(context);
    }

    public CircleClipFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleClipFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircleClipFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        mClipPath.reset();
        mClipPath.addOval(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        canvas.clipPath(mClipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }
}
