package com.github.petropavel13.twophoto.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.squareup.picasso.Picasso

/**
 * Created by petropavel on 25/03/15.
 */

class PostItemView: LinearLayout {
    var titleTextView: TextView? = null
    var authorTextView: TextView? = null
    var tagsTextView: TextView? = null
    var faceImageView: ImageView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        titleTextView = findViewById(R.id.post_item_title_text_view) as? TextView
        authorTextView = findViewById(R.id.post_item_author_text_view) as? TextView
        tagsTextView = findViewById(R.id.post_item_tags_text_view) as? TextView
        faceImageView = findViewById(R.id.post_item_face_image_view) as? ImageView
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    var _post = Post()

    var post: Post
        get() = _post
        set(newValue) {
            _post = newValue

            titleTextView?.setText(newValue.title)
            titleTextView?.setTextColor(Color.parseColor(_post.color))
            authorTextView?.setText("Автор: ${newValue.author.name}")
            tagsTextView?.setText("Теги: ${newValue.tags.map { it.title }.join(", ")}")

            Picasso.with(getContext())
                    .cancelRequest(faceImageView)

            Picasso.with(getContext())
                    .load("http://${newValue.face_image_url}")
                    .priority(Picasso.Priority.HIGH)
                    .into(faceImageView)
        }
}
