package com.github.petropavel13.twophoto.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
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
import com.github.petropavel13.twophoto.network.PostsRequest
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener

public class PostsGridFragment : Fragment() {

    private val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    private var mPostsFilters = PostsFilters()

    var postsFilters: PostsFilters
        get() = mPostsFilters
        set(newValue) {
            mPostsFilters = newValue

            currentPage = 1

            lastPageReached = false
        }

    private var currentPage = 1

    private var lastPageReached = false

    private var mListener: OnFragmentInteractionListener? = null

    private var postsRefreshLayout: SwipeRefreshLayout? = null
    private var postsGridView: StaggeredGridView? = null
    private var loadingMoreFooter: View? = null

    var pullToRefreshEnabled: Boolean
        get() = postsRefreshLayout?.isEnabled() ?: false
        set(newValue) {
            postsRefreshLayout?.setEnabled(newValue)
        }

    private val postsListener = object: RequestListener<LimitedPostsList> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            postsRefreshLayout?.setRefreshing(false)

            unfinishedRequest = null
        }

        override fun onRequestSuccess(result: LimitedPostsList) {
            if (unfinishedRequest == null) return // I have no idea why, but robospice call listener twice

            postsRefreshLayout?.setRefreshing(false)

            lastPageReached = result.next?.isEmpty() ?: true // no next page (null or empty)

            if (lastPageReached) {
                postsGridView?.removeFooterView(loadingMoreFooter)
            } else {
                loadingMoreFooter?.setVisibility(View.INVISIBLE)
            }

            with(postsGridView?.getRealAdapter<PostsAdapter>()) {
                this?.addAll(result.results)
                this?.notifyDataSetChanged()
            }

            unfinishedRequest = null

            currentPage++
        }
    }

    private var unfinishedRequest: PostsRequest? = null

    private var wasStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = getArguments()

        if (arguments != null) {
            if (arguments.containsKey(ARG_POSTS_FILTERS)) {
                mPostsFilters = PostsFilters(arguments.getBundle(ARG_POSTS_FILTERS))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        with(inflater.inflate(R.layout.fragment_posts_grid, container, false)) {
            val ctx = getContext()

            loadingMoreFooter = inflater.inflate(R.layout.loading_more_layout, null)

            with(findViewById(R.id.posts_refresh_layout) as SwipeRefreshLayout) {
                postsRefreshLayout = this

                setOnRefreshListener {
                    loadingMoreFooter?.setVisibility(View.GONE)

                    currentPage = 1

                    reload()
                }
            }

            with(postsRefreshLayout?.findViewById(R.id.posts_grid_view) as StaggeredGridView) {
                postsGridView = this

                addFooterView(loadingMoreFooter)

                setAdapter(PostsAdapter(ctx, emptyList<Post>()))

                setOnScrollListener(object: AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    }

                    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                        if (lastPageReached == false && unfinishedRequest == null && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            loadingMoreFooter?.setVisibility(View.VISIBLE)

                            with(PostsRequest(limit=POSTS_PER_PAGE, page=currentPage, postsFilters=postsFilters)) {
                                unfinishedRequest = this

                                spiceManager.execute(this, postsListener)
                            }
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

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        try {
            mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onStart() {
        super.onStart()

        spiceManager.start(getActivity())

        if (wasStopped && unfinishedRequest != null) {
            spiceManager.execute(unfinishedRequest, postsListener)

            wasStopped = false
        }
    }

    override fun onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }

        wasStopped = true

        super.onStop()
    }

    override fun onDetach() {
        super.onDetach()

        mListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(savedInstanceState != null) {
            postsFilters = PostsFilters(savedInstanceState.getBundle(ARG_POSTS_FILTERS))
        }
    }

    fun reload() {
        with(postsGridView?.getRealAdapter<PostsAdapter>()) {
            this?.clear()
            this?.notifyDataSetChanged()
        }

        with(PostsRequest(POSTS_PER_PAGE * currentPage, 1, postsFilters)) {
            unfinishedRequest = this

            spiceManager.execute(this, postsListener)
        }
    }

    fun addHeaderView(headerView: View) {
        val adapter = postsGridView?.getRealAdapter<PostsAdapter>()

        postsGridView?.setAdapter(null)

        postsGridView?.addHeaderView(headerView)
        postsGridView?.addFooterView(loadingMoreFooter)

        postsGridView?.setAdapter(adapter)
    }

    public trait OnFragmentInteractionListener {
        public fun onPostSelected(post: Post)
    }

    companion object {
        val ARG_POSTS_FILTERS = "posts_filters"
        val POSTS_PER_PAGE = 32

        public fun newInstance(postsFilters: PostsFilters = PostsFilters()): PostsGridFragment {
            val fragment = PostsGridFragment()

            with(Bundle()) {
                putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)

                fragment.setArguments(this)
            }

            return fragment
        }
    }
}
