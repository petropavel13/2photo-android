package com.github.petropavel13.twophoto.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 14/04/15.
 */

class AuthorItemView: FrameLayout {
    var avatarImageView: SimpleDraweeView? = null
    var nameTextView: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        avatarImageView = findViewById(R.id.post_detail_author_avatar_image_view) as? SimpleDraweeView
        nameTextView = findViewById(R.id.post_detail_author_name_text_view) as? TextView
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    private var _author = Post.Author()

    var author: Post.Author
        get() = _author
        set(newValue) {
            _author = newValue

            nameTextView?.setText(newValue.name)

            avatarImageView?.setImageURI(Uri.parse("http://${newValue.avatar_url}"))
        }
}