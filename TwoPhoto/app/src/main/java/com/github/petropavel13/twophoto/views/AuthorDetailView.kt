package com.github.petropavel13.twophoto.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.AuthorDetail

/**
 * Created by petropavel on 22/04/15.
 */

class AuthorDetailView: LinearLayout {
    private var avatarImageView: SimpleDraweeView? = null
    private var nameTextView: TextView? = null

    private var locationLayout: ViewGroup? = null
    private var locationTextView: TextView? = null

    private var siteLayout: ViewGroup? = null
    private var siteTextView: TextView? = null

    private var carmaTextView: TextView? = null
    private var commentsTextView: TextView? = null
    private var postsTextView: TextView? = null

    private var descriptionTextView: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        avatarImageView = findViewById(R.id.author_detail_avatar_image_view) as? SimpleDraweeView

        nameTextView = findViewById(R.id.author_detail_name_text_view) as? TextView

        with(findViewById(R.id.author_detail_location_layout) as? ViewGroup) {
            locationLayout = this

            locationTextView = findViewById(R.id.author_detail_location_text_view) as? TextView
        }

        with(findViewById(R.id.author_detail_site_layout) as? ViewGroup) {
            siteLayout = this

            siteTextView = findViewById(R.id.author_detail_site_text_view) as? TextView
        }

        carmaTextView = findViewById(R.id.author_detail_carma_text_view) as? TextView
        commentsTextView = findViewById(R.id.author_detail_comments_text_view) as? TextView
        postsTextView = findViewById(R.id.author_detail_posts_text_view) as? TextView

        with(findViewById(R.id.author_detail_description_text_view) as? TextView) {
            descriptionTextView = this
        }
    }

    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    private var _author = AuthorDetail()

    var author: AuthorDetail
        get() = _author
        set(newValue) {
            _author = newValue

            avatarImageView?.setImageURI(Uri.parse(newValue.avatar_url))

            nameTextView?.setText(newValue.name)

            val fullLocation = (newValue.country?.isNotEmpty() ?: false) and (newValue.city?.isNotEmpty() ?: false)
            val noLocation = (newValue.country?.isEmpty() ?: true) and (newValue.city?.isEmpty() ?: true)

            if (fullLocation) {
                locationTextView?.setText("${newValue.country}, ${newValue.city}")
            } else if (noLocation) {
                locationLayout?.setVisibility(View.GONE)
            } else {
                val country = if (newValue.country?.isNotEmpty() ?: false) newValue.country else null
                val city = if (newValue.city?.isNotEmpty() ?: false) newValue.city else null

                locationTextView?.setText(country ?: city)
            }

            if (newValue.site?.isNotEmpty() ?: false) {
                siteTextView?.setText(newValue.site)
            } else {
                siteLayout?.setVisibility(View.GONE)
            }

            carmaTextView?.setText(newValue.carma.toString())
            commentsTextView?.setText(newValue.number_of_comments.toString())
            postsTextView?.setText(newValue.number_of_posts.toString())

            descriptionTextView?.setText(newValue.description)
        }
}
