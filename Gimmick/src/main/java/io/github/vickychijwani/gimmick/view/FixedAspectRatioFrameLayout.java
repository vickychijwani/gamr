package io.github.vickychijwani.gimmick.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import io.github.vickychijwani.gimmick.R;

/**
 * A {@link FrameLayout} with a fixed aspect ratio (width is taken from layout, height is calculated
 * dynamically), styleable via the attributes {@link R.styleable#FixedAspectRatioFrameLayout_aspect_ratio_width}
 * and {@link R.styleable#FixedAspectRatioFrameLayout_aspect_ratio_height}.
 */
public class FixedAspectRatioFrameLayout extends FrameLayout {
    private int mAspectRatioWidth;
    private int mAspectRatioHeight;

    public FixedAspectRatioFrameLayout(Context context) {
        super(context);
    }

    public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout);

        if (a == null) {
            return;
        }

        try {
            mAspectRatioWidth = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspect_ratio_width, 16);
            mAspectRatioHeight = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspect_ratio_height, 9);
        } finally {
            a.recycle();
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int finalWidth, finalHeight;

        finalWidth = MeasureSpec.getSize(widthMeasureSpec);
        finalHeight = finalWidth * mAspectRatioHeight / mAspectRatioWidth;

        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }
}
