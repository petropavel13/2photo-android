package com.github.petropavel13.twophoto.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.samples.zoomable.ZoomableDraweeView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post

/**
 * Created by petropavel on 31/03/15.
 */

class EntryView: RelativeLayout {

    constructor(ctx: Context): super(ctx) {}

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {}

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {}

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) {}

    private var imageView: ZoomableDraweeView? = null
    private var descriptionTextView: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.entry_image_view) as? ZoomableDraweeView

        descriptionTextView = findViewById(R.id.entry_description_text_view) as? TextView
    }

    private var _showDescriptionText = false

    var showDescriptionText: Boolean
        get() = _showDescriptionText
        set(newValue) {
            _showDescriptionText = newValue

            if(showDescriptionText && entry.description?.isEmpty() == false) {
                descriptionTextView?.setVisibility(View.VISIBLE)
            } else {
                descriptionTextView?.setVisibility(View.INVISIBLE)
            }
        }

    private var _onTapListener: View.OnClickListener? = null

    var onTapListener: View.OnClickListener?
        get() = _onTapListener
        set(newValue) {
            _onTapListener = newValue

            imageView?.setOnClickListener {
                showDescriptionText = !showDescriptionText

                newValue?.onClick(it)
            }
        }

    private var _entry = Post.Entry()

    var entry: Post.Entry
        get() = _entry
        set(newValue) {
            _entry = newValue

            if(newValue.description?.isEmpty() == true) {
                descriptionTextView?.setVisibility(View.GONE)
            } else {
                descriptionTextView?.setText(newValue.description)
                descriptionTextView?.setVisibility(View.VISIBLE)
            }

            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(entry.big_img_url))
                    .setOldController(imageView?.getController())
                    .build()

            imageView?.setController(controller)
        }
}