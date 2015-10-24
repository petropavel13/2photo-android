package com.github.petropavel13.twophoto.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.etsy.android.grid.StaggeredGridView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.adapters.PostsAdapter
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.extensions.getRealAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsFilters
import com.github.petropavel13.twophoto.sources.DataSource
import com.github.petropavel13.twophoto.sources.ORMLitePostsDataSource
import com.github.petropavel13.twophoto.sources.PostsDataSource
import com.github.petropavel13.twophoto.sources.SpicePostsDataSource
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import kotlin.properties.Delegates

public class PostsGridFragment : Fragment(), DataSource.ResponseListener<LimitedPostsList> {
    var postsDataSource: PostsDataSource by Delegates.notNull()

    private var mPostsFilters = PostsFilters()

    var postsFilters: PostsFilters
        get() = mPostsFilters
        set(newValue) {
            mPostsFilters = newValue

            currentOffset = 0

            lastPageReached = false
        }

    var postsPerPage: Int by Delegates.notNull()

    var useORMLiteDataSource = false

    private val spiceManager = SpiceManager(Jackson2GoogleHttpClientSpiceService::class.java)

    private var currentOffset = 0

    private var lastPageReached = false

    private var mListener: OnFragmentInteractionListener? = null
    private var postsGridView: StaggeredGridView? = null
    private var loadingMoreFooter: View? = null

    private var wasStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments

        if (arguments != null) {
            loadState(arguments)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        with(inflater.inflate(R.layout.fragment_posts_grid, container, false)) {
            val ctx = context

            loadingMoreFooter = inflater.inflate(R.layout.loading_more_layout, null)

            with(findViewById(R.id.posts_grid_view) as StaggeredGridView) {
                postsGridView = this

                addFooterView(loadingMoreFooter)

                adapter = PostsAdapter(ctx, emptyList<Post>())

                setOnScrollListener(object: AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    }

                    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                        if (lastPageReached == false && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            loadingMoreFooter?.visibility = View.VISIBLE

                            postsDataSource.requestList(this@PostsGridFragment, postsFilters, postsPerPage, currentOffset)
                        }
                    }
                })

                setOnItemClickListener { adapterView, view, i, l ->
                    val post = adapterView.getRealAdapter<PostsAdapter>()!!.getItem(i - headerViewsCount)

                    mListener?.onPostSelected(post)
                }
            }

            return this
        }
    }

    override fun onResponse(result: LimitedPostsList?) {
        lastPageReached = result?.next?.isEmpty() ?: true // no next page (null or empty)

        if (lastPageReached) {
            postsGridView?.removeFooterView(loadingMoreFooter)
        } else {
            loadingMoreFooter?.visibility = View.INVISIBLE
        }

        val results = result?.results ?: emptyList<Post>()

        with(postsGridView?.getRealAdapter<PostsAdapter>()) {
            this?.addAll(results)
            this?.notifyDataSetChanged()
        }

        currentOffset += results.count()
    }

    override fun onError(exception: Exception) {
        mListener?.onError(exception)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mListener = context as OnFragmentInteractionListener

            val arguments = arguments

            if (arguments != null) {
                loadState(arguments)
            }

            createDataSource(context)
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement ${ OnFragmentInteractionListener::class.java.simpleName }")
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            mListener = activity as OnFragmentInteractionListener

            val arguments = arguments

            if (arguments != null) {
                loadState(arguments)
            }

            createDataSource(activity)
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement ${ OnFragmentInteractionListener::class.java.simpleName }")
        }
    }

    private fun createDataSource(ctx: Context) {
        if (useORMLiteDataSource) {
            postsDataSource = ORMLitePostsDataSource(DatabaseOpenHelper(ctx))
        } else {
            postsDataSource = SpicePostsDataSource(spiceManager, postsPerPage)
        }
    }

    override fun onStart() {
        super.onStart()

        spiceManager.start(activity)

        if (wasStopped) {
            postsDataSource.retryUnfinishedRequest()

            postsGridView?.getRealAdapter<PostsAdapter>()?.loadItemsImages()
        }

        wasStopped = false
    }

    override fun onStop() {
        postsDataSource.stopRequest()

        wasStopped = true

        if (spiceManager.isStarted) {
            spiceManager.shouldStop()
        }

        super.onStop()

        postsGridView?.getRealAdapter<PostsAdapter>()?.unloadItemsImages()
    }

    override fun onDetach() {
        super.onDetach()

        mListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        saveState(outState)
    }

    private fun saveState(outState: Bundle) {
        outState.putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)
        outState.putInt(ARG_POSTS_PER_PAGE, postsPerPage)
        outState.putBoolean(ARG_USE_ORMLITE_DATASOURCE, useORMLiteDataSource)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(savedInstanceState != null) {
            loadState(savedInstanceState)
        }
    }

    private fun loadState(savedInstanceState: Bundle) {
        postsFilters = PostsFilters(savedInstanceState.getBundle(ARG_POSTS_FILTERS))
        postsPerPage = savedInstanceState.getInt(ARG_POSTS_PER_PAGE)
        useORMLiteDataSource = savedInstanceState.getBoolean(ARG_USE_ORMLITE_DATASOURCE)
    }

    fun reload() {
        with(postsGridView?.getRealAdapter<PostsAdapter>()) {
            this?.clear()
            this?.notifyDataSetChanged()
        }

        currentOffset = 0

        postsDataSource.requestList(this, postsFilters, postsPerPage, currentOffset)
    }

    fun addHeaderView(headerView: View) {
        val adapter = postsGridView?.getRealAdapter<PostsAdapter>()

        postsGridView?.adapter = null

        postsGridView?.addHeaderView(headerView)
        postsGridView?.addFooterView(loadingMoreFooter)

        postsGridView?.adapter = adapter
    }

    public interface OnFragmentInteractionListener {
        public fun onPostSelected(post: Post)
        public fun onError(e: Exception)
    }

    companion object {
        private val ARG_POSTS_FILTERS = "posts_filters"
        private val ARG_POSTS_PER_PAGE = "posts_per_page"
        private val ARG_USE_ORMLITE_DATASOURCE = "ormlite_datasource"

        public fun newInstance(postsPerPage: Int, useORMLiteDataSource: Boolean = false, postsFilters: PostsFilters = PostsFilters()): PostsGridFragment {
            val fragment = PostsGridFragment()

            with(Bundle()) {
                putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)
                putInt(ARG_POSTS_PER_PAGE, postsPerPage)
                putBoolean(ARG_USE_ORMLITE_DATASOURCE, useORMLiteDataSource)

                fragment.arguments = this
            }

            return fragment
        }
    }
}
