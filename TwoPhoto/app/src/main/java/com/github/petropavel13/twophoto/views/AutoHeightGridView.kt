package com.github.petropavel13.twophoto.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.GridView

/**
 * Created by petropavel on 27/03/15.
 */

class AutoHeightGridView: GridView {
    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // The great Android "hackatlon", the love, the magic.
            // The two leftmost bits in the height measure spec have
            // a special meaning, hence we can't use them to describe height.
            heightSpec = View.MeasureSpec.makeMeasureSpec(View.MEASURED_SIZE_MASK, View.MeasureSpec.AT_MOST)
        }

        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}