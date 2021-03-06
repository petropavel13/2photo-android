package com.github.petropavel13.twophoto.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 25/03/15.
 */

class PostItemView: RelativeLayout {
    companion object {
        val LAYOUT_RESOURCE = R.layout.post_grid_item_layout

        @SuppressWarnings("deprecation")
        private fun setBackgroundDrawablePreJellyBean(view: View?, drawable: GradientDrawable) {
            view?.setBackgroundDrawable(drawable)
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun setBackgroundDrawableJellyBean(view: View?, drawable: GradientDrawable) {
            view?.background = drawable
        }
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    private var titleTextView: TextView? = null
    private var authorTextView: TextView? = null
    private var faceImageView: SimpleDraweeView? = null
    private var attributesLayout: LinearLayout? = null
    private var commentsCountTextView: TextView? = null
    private var ratingTextView: TextView? = null
    private var bgColor = 0xFF202020.toInt()
    private var borderColor = 0xFF444444.toInt()

    override fun onFinishInflate() {
        super.onFinishInflate()

        titleTextView = findViewById(R.id.post_item_title_text_view) as? TextView
        authorTextView = findViewById(R.id.post_item_author_text_view) as? TextView
        faceImageView = findViewById(R.id.post_item_face_image_view) as? SimpleDraweeView
        attributesLayout = findViewById(R.id.post_item_attributes_layout) as? LinearLayout
        commentsCountTextView = findViewById(R.id.post_item_comments_count_text_view) as? TextView
        ratingTextView = findViewById(R.id.post_item_rating_text_view) as? TextView


        bgColor = resources.getColor(R.color.post_item_bg_color)
        borderColor = resources.getColor(R.color.post_item_border_color)

        with(GradientDrawable()){
            setColor(bgColor)
            setStroke(1, borderColor)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawableJellyBean(this@PostItemView, this)
            } else {
                setBackgroundDrawablePreJellyBean(this@PostItemView, this)
            }
        }

        with(GradientDrawable()){
            setColor(bgColor)
            setStroke(1, borderColor)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawableJellyBean(attributesLayout, this)
            } else {
                setBackgroundDrawablePreJellyBean(attributesLayout, this)
            }
        }
    }

    private var _post = Post()

    private var request: ImageRequest? = null

    var post: Post
        get() = _post
        set(newValue) {
            _post = newValue

            request = ImageRequest.fromUri(newValue.face_image_url)


            faceImageView?.controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .build()

            val color = Color.parseColor(_post.color)

            titleTextView?.text = newValue.title
            titleTextView?.setTextColor(color)
            authorTextView?.text = "by ${newValue.author.name}"
            commentsCountTextView?.text = newValue.number_of_comments.toString()
            ratingTextView?.text = "${newValue.rating}%"

        }

    fun unloadEntryImage() {
        faceImageView?.setImageURI(null)
    }

    fun loadEntryImage() {
        faceImageView?.controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .build()
    }
}
