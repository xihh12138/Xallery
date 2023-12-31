package com.xihh.base.util.span;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;

public abstract class DynamicDrawableSpan extends ReplacementSpan {

    /**
     * A constant indicating that the bottom of this span should be aligned
     * with the bottom of the surrounding text, i.e., at the same level as the
     * lowest descender in the text.
     */
    public static final int ALIGN_BOTTOM = 0;

    /**
     * A constant indicating that the bottom of this span should be aligned
     * with the baseline of the surrounding text.
     */
    public static final int ALIGN_BASELINE = 1;

    /**
     * A constant indicating that this span should be vertically centered between
     * the top and the lowest descender.
     */
    public static final int ALIGN_CENTER = 2;

    /**
     * Defines acceptable alignment types.
     * @hide
     */
    @Retention(SOURCE)
    @IntDef(value = {
            ALIGN_BOTTOM,
            ALIGN_BASELINE,
            ALIGN_CENTER
    })
    public @interface AlignmentType {}

    protected final int mVerticalAlignment;

    private WeakReference<Drawable> mDrawableRef;

    /**
     * Creates a {@link DynamicDrawableSpan}. The default vertical alignment is
     * {@link #ALIGN_BOTTOM}
     */
    public DynamicDrawableSpan() {
        mVerticalAlignment = ALIGN_BOTTOM;
    }

    /**
     * Creates a {@link DynamicDrawableSpan} based on a vertical alignment.\
     *
     * @param verticalAlignment one of {@link #ALIGN_BOTTOM}, {@link #ALIGN_BASELINE} or
     *                          {@link #ALIGN_CENTER}
     */
    protected DynamicDrawableSpan(@AlignmentType int verticalAlignment) {
        mVerticalAlignment = verticalAlignment;
    }

    /**
     * Returns the vertical alignment of this span, one of {@link #ALIGN_BOTTOM},
     * {@link #ALIGN_BASELINE} or {@link #ALIGN_CENTER}.
     */
    public @AlignmentType int getVerticalAlignment() {
        return mVerticalAlignment;
    }

    /**
     * Your subclass must implement this method to provide the bitmap
     * to be drawn.  The dimensions of the bitmap must be the same
     * from each call to the next.
     */
    public abstract Drawable getDrawable();

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text,
                       @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            fm.ascent = -rect.bottom;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     @IntRange(from = 0) int start, @IntRange(from = 0) int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        } else if (mVerticalAlignment == ALIGN_CENTER) {
            transY = top + (bottom - top) / 2 - b.getBounds().height() / 2;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null) {
            d = wr.get();
        }

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }

        return d;
    }
}

