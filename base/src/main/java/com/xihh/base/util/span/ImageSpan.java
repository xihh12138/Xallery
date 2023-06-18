package com.xihh.base.util.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.io.InputStream;

public class ImageSpan extends DynamicDrawableSpan {

    private Drawable mDrawable;
    private Uri mContentUri;
    @DrawableRes
    private int mResourceId;
    private Context mContext;
    private String mSource;

    /**
     * @deprecated Use {@link #ImageSpan(Context, Bitmap)} instead.
     */
    @Deprecated
    public ImageSpan(@NonNull Bitmap b) {
        this(null, b, ALIGN_BOTTOM);
    }

    /**
     * @deprecated Use {@link #ImageSpan(Context, Bitmap, int)} instead.
     */
    @Deprecated
    public ImageSpan(@NonNull Bitmap b, int verticalAlignment) {
        this(null, b, verticalAlignment);
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context} and a {@link Bitmap} with the default
     * alignment {@link DynamicDrawableSpan#ALIGN_BOTTOM}
     *
     * @param context context used to create a drawable from {@param bitmap} based on the display
     *                metrics of the resources
     * @param bitmap  bitmap to be rendered
     */
    public ImageSpan(@NonNull Context context, @NonNull Bitmap bitmap) {
        this(context, bitmap, ALIGN_BOTTOM);
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context}, a {@link Bitmap} and a vertical
     * alignment.
     *
     * @param context           context used to create a drawable from {@param bitmap} based on
     *                          the display metrics of the resources
     * @param bitmap            bitmap to be rendered
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     *                          {@link DynamicDrawableSpan#ALIGN_BASELINE}
     */
    public ImageSpan(@NonNull Context context, @NonNull Bitmap bitmap, int verticalAlignment) {
        super(verticalAlignment);
        mContext = context;
        mDrawable = context != null
                ? new BitmapDrawable(context.getResources(), bitmap)
                : new BitmapDrawable(bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
    }

    /**
     * Constructs an {@link ImageSpan} from a drawable with the default
     * alignment {@link DynamicDrawableSpan#ALIGN_BOTTOM}.
     *
     * @param drawable drawable to be rendered
     */
    public ImageSpan(@NonNull Drawable drawable) {
        this(drawable, ALIGN_BOTTOM);
    }

    /**
     * Constructs an {@link ImageSpan} from a drawable and a vertical alignment.
     *
     * @param drawable          drawable to be rendered
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     *                          {@link DynamicDrawableSpan#ALIGN_BASELINE}
     */
    public ImageSpan(@NonNull Drawable drawable, int verticalAlignment) {
        super(verticalAlignment);
        mDrawable = drawable;
    }

    /**
     * Constructs an {@link ImageSpan} from a drawable and a source with the default
     * alignment {@link DynamicDrawableSpan#ALIGN_BOTTOM}
     *
     * @param drawable drawable to be rendered
     * @param source   drawable's Uri source
     */
    public ImageSpan(@NonNull Drawable drawable, @NonNull String source) {
        this(drawable, source, ALIGN_BOTTOM);
    }

    /**
     * Constructs an {@link ImageSpan} from a drawable, a source and a vertical alignment.
     *
     * @param drawable          drawable to be rendered
     * @param source            drawable's uri source
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     *                          {@link DynamicDrawableSpan#ALIGN_BASELINE}
     */
    public ImageSpan(@NonNull Drawable drawable, @NonNull String source, int verticalAlignment) {
        super(verticalAlignment);
        mDrawable = drawable;
        mSource = source;
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context} and a {@link Uri} with the default
     * alignment {@link DynamicDrawableSpan#ALIGN_BOTTOM}. The Uri source can be retrieved via
     * {@link #getSource()}
     *
     * @param context context used to create a drawable from {@param bitmap} based on the display
     *                metrics of the resources
     * @param uri     {@link Uri} used to construct the drawable that will be rendered
     */
    public ImageSpan(@NonNull Context context, @NonNull Uri uri) {
        this(context, uri, ALIGN_BOTTOM);
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context}, a {@link Uri} and a vertical
     * alignment. The Uri source can be retrieved via {@link #getSource()}
     *
     * @param context           context used to create a drawable from {@param bitmap} based on
     *                          the display
     *                          metrics of the resources
     * @param uri               {@link Uri} used to construct the drawable that will be rendered.
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     *                          {@link DynamicDrawableSpan#ALIGN_BASELINE}
     */
    public ImageSpan(@NonNull Context context, @NonNull Uri uri, int verticalAlignment) {
        super(verticalAlignment);
        mContext = context;
        mContentUri = uri;
        mSource = uri.toString();
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context} and a resource id with the default
     * alignment {@link DynamicDrawableSpan#ALIGN_BOTTOM}
     *
     * @param context    context used to retrieve the drawable from resources
     * @param resourceId drawable resource id based on which the drawable is retrieved
     */
    public ImageSpan(@NonNull Context context, @DrawableRes int resourceId) {
        this(context, resourceId, ALIGN_BOTTOM);
    }

    /**
     * Constructs an {@link ImageSpan} from a {@link Context}, a resource id and a vertical
     * alignment.
     *
     * @param context           context used to retrieve the drawable from resources
     * @param resourceId        drawable resource id based on which the drawable is retrieved.
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     *                          {@link DynamicDrawableSpan#ALIGN_BASELINE}
     */
    public ImageSpan(@NonNull Context context, @DrawableRes int resourceId,
            int verticalAlignment) {
        super(verticalAlignment);
        mContext = context;
        mResourceId = resourceId;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = null;

        if (mDrawable != null) {
            drawable = mDrawable;
        } else if (mContentUri != null) {
            Bitmap bitmap = null;
            try {
                InputStream is = mContext.getContentResolver().openInputStream(
                        mContentUri);
                bitmap = BitmapFactory.decodeStream(is);
                drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                is.close();
            } catch (Exception e) {
                Log.e("ImageSpan", "Failed to loaded content " + mContentUri, e);
            }
        } else {
            try {
                drawable = mContext.getDrawable(mResourceId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
            } catch (Exception e) {
                Log.e("ImageSpan", "Unable to find resource: " + mResourceId);
            }
        }

        return drawable;
    }

    /**
     * Returns the source string that was saved during construction.
     *
     * @return the source string that was saved during construction
     * @see #ImageSpan(Drawable, String)
     * @see #ImageSpan(Context, Uri)
     */
    public String getSource() {
        return mSource;
    }
}


