package com.github.petropavel13.twophoto.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 25/03/15.
 */

class PostItemView: LinearLayout {
    var titleTextView: TextView? = null
    var authorTextView: TextView? = null
    var tagsTextView: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        titleTextView = findViewById(R.id.post_title_text_view) as? TextView
        authorTextView = findViewById(R.id.post_author_text_view) as? TextView
        tagsTextView = findViewById(R.id.post_tags_text_view) as? TextView
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    var post: Post = Post() // empty
        set(newValue) {
            titleTextView?.setText(newValue.title)
            authorTextView?.setText("Автор: ${newValue.author.name}")
            tagsTextView?.setText("Теги: ${newValue.tags.map { it.title }.join(", ")}")
        }
}
