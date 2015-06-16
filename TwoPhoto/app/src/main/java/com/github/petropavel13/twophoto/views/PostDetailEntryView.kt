package com.github.petropavel13.twophoto.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.LinearLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 27/03/15.
 */

class PostDetailEntryView: LinearLayout {

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    init {
        imageSize = getContext().getResources().getDimension(R.dimen.post_detail_entry_width).toInt()
    }

    private var imageView: SimpleDraweeView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.post_detail_entry_image_view) as? SimpleDraweeView
    }

    private val imageSize: Int


    private var _entry = Post.Entry()

    private var request: ImageRequest? = null

    var entry: Post.Entry
        get() = _entry
        set(newValue) {
            _entry = newValue

            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(newValue.medium_img_url))
                    .setResizeOptions(ResizeOptions(imageSize, imageSize))
                    .build()


            imageView?.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .build())
        }

    fun unloadEntryImage() {
        imageView?.setImageURI(null)
    }

    fun loadEntryImage() {
        imageView?.setController(Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .build())
    }
}