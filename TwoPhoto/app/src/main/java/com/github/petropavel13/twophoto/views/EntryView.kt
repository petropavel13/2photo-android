package com.github.petropavel13.twophoto.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.getMemoryClass
import com.github.petropavel13.twophoto.model.Post
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * Created by petropavel on 31/03/15.
 */

class EntryView: RelativeLayout {

    private val isLowMemory: Boolean

    constructor(ctx: Context): super(ctx) {
        isLowMemory = getMemoryClass(ctx) < 24
    }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {
        isLowMemory = getMemoryClass(ctx) < 24
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        isLowMemory = getMemoryClass(ctx) < 24
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) {
        isLowMemory = getMemoryClass(ctx) < 24
    }

    var imageView: SubsamplingScaleImageView? = null
    var descriptionTextView: TextView? = null
    var retryView: RetryView? = null
    var progressBar: ProgressBar? = null

    var recycled = false

    abstract class EntryTarget(var loadingBigImage: Boolean = false,
                               var seamlessLoading: Boolean = false): Target

    val target = object: EntryTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            imageView?.setImage(ImageSource.bitmap(bitmap))

            progressBar?.setVisibility(View.INVISIBLE)
            imageView?.setVisibility(View.VISIBLE)
            retryView?.setVisibility(View.INVISIBLE)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            if (loadingBigImage) {
                // maybe we just don't have enough memory for big one
                // let's try load small image
                loadImage("http://${entry.medium_img_url}", bigImage = false, seamlessLoading = true)
            } else {
                progressBar?.setVisibility(View.INVISIBLE)
                imageView?.setVisibility(View.INVISIBLE)
                retryView?.setVisibility(View.VISIBLE)
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            retryView?.setVisibility(View.INVISIBLE)

            if(seamlessLoading == false) {
                progressBar?.setVisibility(View.VISIBLE)
                imageView?.setVisibility(View.INVISIBLE)
            }
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
            if(newValue.equals(_entry)) return

            _entry = newValue

            if(newValue.description.isEmpty()) {
                descriptionTextView?.setVisibility(View.GONE)
            } else {
                descriptionTextView?.setText(newValue.description)
                descriptionTextView?.setVisibility(View.VISIBLE)
            }

            if(isLowMemory) {
                loadImage("http://${entry.medium_img_url}", bigImage = false)
            } else {
                loadImage("http://${entry.big_img_url}")
            }
        }

    fun viewWillShow() {
        if(isLowMemory) {
            Picasso.with(getContext())
                    .cancelRequest(target)

            loadImage("http://${entry.big_img_url}", bigImage = true, seamlessLoading = !recycled)

            recycled = false
        }
    }

    fun viewWillHide() {
        if(isLowMemory) {
            Picasso.with(getContext())
                    .cancelRequest(target)

            imageView?.recycle()

            recycled = true
        }
    }

    fun loadImage(imageUrl: String, bigImage: Boolean = true, seamlessLoading: Boolean = false) {
        Picasso.with(getContext())
                .cancelRequest(target)

        target.loadingBigImage = bigImage
        target.seamlessLoading = seamlessLoading

        Picasso.with(getContext())
                .load(imageUrl)
                .priority(Picasso.Priority.HIGH)
                .into(target)
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.entry_image_view) as? SubsamplingScaleImageView
        descriptionTextView = findViewById(R.id.entry_description_text_view) as? TextView

        progressBar = findViewById(R.id.entry_progress_bar) as? ProgressBar

        with(findViewById(R.id.entry_retry_view) as RetryView) {
            retryView = this

            errorTextResource = R.string.entry_failed_to_load_image

            onRetryListener = object: View.OnClickListener{
                override fun onClick(view: View) {
                    loadImage("http://${entry.big_img_url}")
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        Picasso.with(getContext())
                .cancelRequest(target)

        imageView?.recycle()
    }
}