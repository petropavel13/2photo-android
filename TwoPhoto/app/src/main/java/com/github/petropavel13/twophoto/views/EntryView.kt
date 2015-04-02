package com.github.petropavel13.twophoto.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * Created by petropavel on 31/03/15.
 */

class EntryView: RelativeLayout {

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    var imageView: SubsamplingScaleImageView? = null
    var descriptionTextView: TextView? = null

    val target = object: Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            imageView?.setImage(ImageSource.bitmap(bitmap))
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            //
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            //
        }
    }

    var _showDescriptionText = false

    var showDescriptionText: Boolean
        get() = _showDescriptionText
        set(newValue) {
            _showDescriptionText = newValue

            if(showDescriptionText && entry.description.isEmpty() == false) {
                descriptionTextView?.setVisibility(View.VISIBLE)
            } else {
                descriptionTextView?.setVisibility(View.INVISIBLE)
            }
        }

    var _onTapListener: View.OnClickListener? = null

    var onTapListener: View.OnClickListener?
        get() = _onTapListener
        set(newValue) {
            _onTapListener = newValue

            imageView?.setOnClickListener {
                showDescriptionText = !showDescriptionText

                newValue?.onClick(it)
            }
        }

    var _entry = Post.Entry()

    var entry: Post.Entry
        get() = _entry
        set(newValue) {
            _entry = newValue

            if(newValue.description.isEmpty()) {
                descriptionTextView?.setVisibility(View.GONE)
            } else {
                descriptionTextView?.setText(newValue.description)
                descriptionTextView?.setVisibility(View.VISIBLE)
            }

            Picasso.with(getContext())
                    .cancelRequest(target)

            Picasso.with(getContext())
                    .load("http://${newValue.big_img_url}")
                    .priority(Picasso.Priority.HIGH)
                    .into(target)
        }

    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.entry_image_view) as? SubsamplingScaleImageView
        descriptionTextView = findViewById(R.id.entry_description_text_view) as? TextView
    }
}