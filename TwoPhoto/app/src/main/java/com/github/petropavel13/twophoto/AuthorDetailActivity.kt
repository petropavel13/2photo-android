package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.github.petropavel13.twophoto.fragments.PostsListFragment
import com.github.petropavel13.twophoto.model.AuthorDetail
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.AuthorRequest
import com.github.petropavel13.twophoto.network.PostsFilters
import com.github.petropavel13.twophoto.views.RetryView
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener
import com.squareup.picasso.Picasso


public class AuthorDetailActivity : FragmentActivity(), PostsListFragment.OnFragmentInteractionListener {
    override fun onPostSelected(post: Post) {
        val postDetailIntent = Intent(this, javaClass<PostDetailActivity>())
        postDetailIntent.putExtra(PostDetailActivity.POST_ID_KEY, post.id)
        startActivity(postDetailIntent)
    }

    private val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    companion object {
        val AUTHOR_KEY = "author"
    }

    private var author = Post.Author()

    private val authorListener = object: RequestListener<AuthorDetail> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            loadingProgressBar?.setVisibility(View.INVISIBLE)
            retryView?.setVisibility(View.VISIBLE)
            contentLayout?.setVisibility(View.INVISIBLE)
        }

        override fun onRequestSuccess(result: AuthorDetail) {
            val fullLocation = (result.country?.isNotEmpty() ?: false) and (result.city?.isNotEmpty() ?: false)
            val noLocation = (result.country?.isEmpty() ?: true) and (result.city?.isEmpty() ?: true)

            if (fullLocation) {
                locationTextView?.setText("${result.country}, ${result.city}")
            } else if (noLocation) {
                locationLayout?.setVisibility(View.GONE)
            } else {
                locationTextView?.setText(result.country ?: result.city)
            }

            if (result.site?.isNotEmpty() ?: false) {
                siteTextView?.setText(result.site)
            } else {
                siteLayout?.setVisibility(View.GONE)
            }

            carmaTextView?.setText(result.carma.toString())
            commentsTextView?.setText(result.number_of_comments.toString())
            postsTextView?.setText(result.number_of_posts.toString())

            descriptionTextView?.setText(result.description)

            loadingProgressBar?.setVisibility(View.INVISIBLE)
            contentLayout?.setVisibility(View.VISIBLE)
        }
    }

    private var avatarImageView: ImageView? = null
    private var nameTextView: TextView? = null

    private var locationLayout: ViewGroup? = null
    private var locationTextView: TextView? = null

    private var siteLayout: ViewGroup? = null
    private var siteTextView: TextView? = null

    private var carmaTextView: TextView? = null
    private var commentsTextView: TextView? = null
    private var postsTextView: TextView? = null

    private var descriptionTextView: TextView? = null

    private var loadingProgressBar: ProgressBar? = null
    private var retryView: RetryView? = null
    private var contentLayout: LinearLayout? = null
    private var postsList: PostsListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<FragmentActivity>.onCreate(savedInstanceState)

        setContentView(R.layout.activity_author_detail)

        author = getIntent().getParcelableExtra(AUTHOR_KEY)

        with(findViewById(R.id.author_detail_loading_progress_bar) as ProgressBar) {
            loadingProgressBar = this

            setVisibility(View.VISIBLE)
        }

        with(findViewById(R.id.author_detail_retry_view) as RetryView){
            retryView = this

            onRetryListener = object: View.OnClickListener {
                override fun onClick(view: View) {
                    setVisibility(View.INVISIBLE)

                    spiceManager.execute(AuthorRequest(author.id), authorListener)
                }
            }

            setVisibility(View.INVISIBLE)
        }

        with(findViewById(R.id.author_detail_content_layout) as LinearLayout) {
            contentLayout = this

            setVisibility(View.INVISIBLE)

            avatarImageView = findViewById(R.id.author_detail_avatar_image_view) as? ImageView

            Picasso.with(getContext())
                    .load("http://${author.avatar_url}")
                    .priority(Picasso.Priority.HIGH)
                    .into(avatarImageView)

            nameTextView = findViewById(R.id.author_detail_name_text_view) as? TextView
            nameTextView?.setText(author.name)
        }

        with(getSupportFragmentManager().findFragmentById(R.id.author_detail_posts_fragment) as PostsListFragment) {
            postsList = this
            pullToRefreshEnabled = false
            postsFilters = PostsFilters(authorId = author.id)
            reload()
        }

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

            this?.setMovementMethod(ScrollingMovementMethod())
        }

        spiceManager.execute(AuthorRequest(author.id), authorListener)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_author_detail, menu)

        return super<FragmentActivity>.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super<FragmentActivity>.onOptionsItemSelected(item)
    }

    override fun onStart() {
        spiceManager.start(this)

        super<FragmentActivity>.onStart()
    }

    override fun onStop() {
        spiceManager.shouldStop()

        super<FragmentActivity>.onStop()
    }
}
