package com.github.petropavel13.twophoto.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 25/03/15.
 */

class PostItemView: RelativeLayout {
    companion object {
        val LAYOUT_RESOURCE = R.layout.post_grid_item_layout
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


        bgColor = getResources().getColor(R.color.post_item_bg_color)
        borderColor = getResources().getColor(R.color.post_item_border_color)

        with(GradientDrawable()){
            setColor(bgColor)
            setStroke(1, borderColor)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(this);
            } else {
                setBackgroundDrawable(this);
            }
        }

        with(GradientDrawable()){
            setColor(bgColor)
            setStroke(1, borderColor)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                attributesLayout?.setBackground(this)
            } else {
                attributesLayout?.setBackgroundDrawable(this)
            }
        }
    }

    private var _post = Post()

    private var request: ImageRequest? = null

    var post: Post
        get() = _post
        set(newValue) {
            _post = newValue

            request = ImageRequest.fromUri("http://${newValue.face_image_url}")


            faceImageView?.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .build())

            val color = Color.parseColor(_post.color)

            titleTextView?.setText(newValue.title)
            titleTextView?.setTextColor(color)
            authorTextView?.setText("by ${newValue.author.name}")
            commentsCountTextView?.setText(newValue.number_of_comments.toString())
            ratingTextView?.setText("${newValue.rating}%")

        }

    fun unloadEntryImage() {
        faceImageView?.setImageURI(null)
    }

    fun loadEntryImage() {
        faceImageView?.setController(Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .build())
    }
}
