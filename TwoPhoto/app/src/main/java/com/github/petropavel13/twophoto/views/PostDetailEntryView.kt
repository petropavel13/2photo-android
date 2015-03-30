package com.github.petropavel13.twophoto.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.squareup.picasso.Picasso

/**
 * Created by petropavel on 27/03/15.
 */

class PostDetailEntryView: LinearLayout {
    var imageView: ImageView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.post_detail_entry_image_view) as? ImageView
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    var _entry = Post.Entry()

    var entry: Post.Entry
        get() = _entry
        set(newValue) {
            _entry = newValue

            Picasso.with(getContext())
                    .cancelRequest(imageView)

            Picasso.with(getContext())
                    .load("http://${newValue.medium_img_url}")
                    .priority(Picasso.Priority.NORMAL)
                    .resizeDimen(R.dimen.post_detail_entry_width, R.dimen.post_detail_entry_height)
                    .into(imageView)
        }
}