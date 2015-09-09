package com.github.petropavel13.twophoto.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.etsy.android.grid.StaggeredGridView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.adapters.PostsAdapter
import com.github.petropavel13.twophoto.extensions.getRealAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsFilters
import com.github.petropavel13.twophoto.sources.DataSource
import com.github.petropavel13.twophoto.sources.PostsDataSource
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

    private var currentOffset = 0

    private var lastPageReached = false

    private var mListener: OnFragmentInteractionListener? = null
    private var postsGridView: StaggeredGridView? = null
    private var loadingMoreFooter: View? = null

    private var wasStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)

        val arguments = getArguments()

        if (arguments != null) {
            if (arguments.containsKey(ARG_POSTS_FILTERS)) {
                mPostsFilters = PostsFilters(arguments.getBundle(ARG_POSTS_FILTERS))
                postsPerPage = arguments.getInt(ARG_POSTS_PER_PAGE)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        with(inflater.inflate(R.layout.fragment_posts_grid, container, false)) {
            val ctx = getContext()

            loadingMoreFooter = inflater.inflate(R.layout.loading_more_layout, null)

            with(findViewById(R.id.posts_grid_view) as StaggeredGridView) {
                postsGridView = this

                addFooterView(loadingMoreFooter)

                setAdapter(PostsAdapter(ctx, emptyList<Post>()))

                setOnScrollListener(object: AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    }

                    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                        if (lastPageReached == false && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            loadingMoreFooter?.setVisibility(View.VISIBLE)

                            postsDataSource.requestList(this@PostsGridFragment, postsFilters, postsPerPage, currentOffset)
                        }
                    }
                })

                setOnItemClickListener { adapterView, view, i, l ->
                    val post = adapterView.getRealAdapter<PostsAdapter>()!!.getItem(i - getHeaderViewsCount())

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
            loadingMoreFooter?.setVisibility(View.INVISIBLE)
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

    override fun onAttach(activity: Activity) {
        super<Fragment>.onAttach(activity)

        try {
            mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement ${ javaClass<OnFragmentInteractionListener>().getSimpleName() }")
        }
    }

    override fun onStart() {
        super<Fragment>.onStart()

        if (wasStopped) {
            postsDataSource.retryUnfinishedRequest()

            postsGridView?.getRealAdapter<PostsAdapter>()?.loadItemsImages()
        }

        wasStopped = false
    }

    override fun onStop() {
        postsDataSource.stopRequest()

        wasStopped = true

        super<Fragment>.onStop()

        postsGridView?.getRealAdapter<PostsAdapter>()?.unloadItemsImages()
    }

    override fun onDetach() {
        super<Fragment>.onDetach()

        mListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super<Fragment>.onSaveInstanceState(outState)

        outState.putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)
        outState.putInt(ARG_POSTS_PER_PAGE, postsPerPage)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

        if(savedInstanceState != null) {
            postsFilters = PostsFilters(savedInstanceState.getBundle(ARG_POSTS_FILTERS))
            postsPerPage = savedInstanceState.getInt(ARG_POSTS_PER_PAGE)
        }
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

        postsGridView?.setAdapter(null)

        postsGridView?.addHeaderView(headerView)
        postsGridView?.addFooterView(loadingMoreFooter)

        postsGridView?.setAdapter(adapter)
    }

    public interface OnFragmentInteractionListener {
        public fun onPostSelected(post: Post)
        public fun onError(e: Exception)
    }

    companion object {
        private val ARG_POSTS_FILTERS = "posts_filters"
        private val ARG_POSTS_PER_PAGE = "posts_per_page"

        public fun newInstance(postsDataSource: PostsDataSource, postsPerPage: Int, postsFilters: PostsFilters = PostsFilters()): PostsGridFragment {
            val fragment = PostsGridFragment()

            with(Bundle()) {
                putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)
                putInt(ARG_POSTS_PER_PAGE, postsPerPage)

                fragment.setArguments(this)
            }

            fragment.postsDataSource = postsDataSource
            fragment.postsPerPage = postsPerPage

            return fragment
        }
    }
}
