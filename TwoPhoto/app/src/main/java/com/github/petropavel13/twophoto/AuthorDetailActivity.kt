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
import com.github.petropavel13.twophoto.fragments.PostsGridFragment
import com.github.petropavel13.twophoto.model.AuthorDetail
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.AuthorRequest
import com.github.petropavel13.twophoto.network.PostsFilters
import com.github.petropavel13.twophoto.views.AuthorDetailView
import com.github.petropavel13.twophoto.views.RetryView
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener
import com.squareup.picasso.Picasso


public class AuthorDetailActivity : FragmentActivity(), PostsGridFragment.OnFragmentInteractionListener {
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

            postsList?.getView()?.setVisibility(View.INVISIBLE)
        }

        override fun onRequestSuccess(result: AuthorDetail) {
            authorDetailView?.author = result

            loadingProgressBar?.setVisibility(View.INVISIBLE)

            postsList?.getView()?.setVisibility(View.VISIBLE)
        }
    }

    private var authorDetailView: AuthorDetailView? = null

    private var loadingProgressBar: ProgressBar? = null
    private var retryView: RetryView? = null
    private var postsList: PostsGridFragment? = null

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

                    loadingProgressBar?.setVisibility(View.VISIBLE)

                    spiceManager.execute(AuthorRequest(author.id), authorListener)
                }
            }

            setVisibility(View.INVISIBLE)
        }

        with(getSupportFragmentManager().findFragmentById(R.id.author_detail_posts_fragment) as PostsGridFragment) {
            getView().setVisibility(View.INVISIBLE)

            postsList = this
            postsFilters = PostsFilters(authorId = author.id)

            with(getLayoutInflater().inflate(R.layout.author_detail_layout, null) as AuthorDetailView) {
                authorDetailView = this

                addHeaderView(this)
            }

            reload()
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
