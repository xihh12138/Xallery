package com.xallery.common.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.xallery.common.R;


public class MaxHeightRecyclerView extends RecyclerView {

    int maxHeight = LayoutParams.WRAP_CONTENT;

    public MaxHeightRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public MaxHeightRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MaxHeightRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs == null) return;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView);
        maxHeight = array.getDimensionPixelSize(R.styleable.MaxHeightRecyclerView_maxHeight, LayoutParams.WRAP_CONTENT);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
    }

}
