/**
 *
 * CobaltAbstractFontDrawable
 * Cobalt
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Cobaltians
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package org.cobaltians.cobalt.font;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

public abstract class CobaltAbstractFontDrawable extends Drawable {

    /***********************************************************************************************
     *
     * MEMBERS
     *
     **********************************************************************************************/

    protected  Context mContext;
    protected String mIdentifier;
    private TextPaint mPaint;
    private float mTextSize;
    private int mSize;

    /***********************************************************************************************
     *
     * CONSTRUCTOR
     *
     **********************************************************************************************/

    public CobaltAbstractFontDrawable(Context context, String identifier, int color, float textSize, float padding) {
        mContext = context;
        mIdentifier = identifier;
        mTextSize = textSize;
        mSize = (int) (textSize + padding * 2.0);

        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), getFontFilePath()));
        mPaint.setColor(color);
    }

    /***********************************************************************************************
     *
     * ABSTRACT METHODS
     *
     **********************************************************************************************/

    /**
     * Returns the font icon string from the string resources for the specified identifier
     * @param identifier the font icon identifier
     * @return the font icon string or null
     */
    protected abstract String getStringResource(String identifier);

    /**
     * Returns the font file path in assets folder
     * @return font file path in assets folder
     */
    protected abstract String getFontFilePath();

    /***********************************************************************************************
     *
     * DRAWABLE
     *
     **********************************************************************************************/

    @Override
    public void draw(Canvas canvas) {
        float xOffset = mSize / 2.0f;
        canvas.drawText(getStringResource(mIdentifier), xOffset, mTextSize, mPaint);
    }

    @Override
    public int getIntrinsicHeight() {
        return mSize;
    }

    @Override
    public int getIntrinsicWidth() {
        return mSize;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
