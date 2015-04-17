package com.github.petropavel13.twophoto.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
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
import com.octo.android.robospice.request.listener.SpiceServiceListener

public class PostsListFragment : Fragment() {

    private val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    private var mPostsFilters = PostsFilters()

    var postsFilters: PostsFilters
        get() = mPostsFilters
        set(newValue) {
            mPostsFilters = newValue

            currentPage = 1

            reload()
        }

    private var currentPage = 1

    private var mListener: OnFragmentInteractionListener? = null

    private var postsRefreshLayout: SwipeRefreshLayout? = null
    private var postsListView: ListView? = null
    private var postsListViewFooter: View? = null

    private val postsListener = object: RequestListener<LimitedPostsList> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            postsRefreshLayout?.setRefreshing(false)

            unfinishedRequest = null
        }

        override fun onRequestSuccess(result: LimitedPostsList) {
            postsRefreshLayout?.setRefreshing(false)

            with(postsListView?.getRealAdapter<PostsAdapter>()) {
                this?.addAll(result.results)
                this?.notifyDataSetChanged()
            }

            unfinishedRequest = null
        }
    }

    private var unfinishedRequest: PostsRequest? = null

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
        with(inflater.inflate(R.layout.fragment_posts_list, container, false)) {
            val ctx = getContext()

            postsListViewFooter = inflater.inflate(R.layout.loading_more_layout, null)

            with(findViewById(R.id.posts_refresh_layout) as SwipeRefreshLayout) {
                postsRefreshLayout = this

                setOnRefreshListener {
                    postsListViewFooter?.setVisibility(View.GONE)

                    currentPage = 1

                    reload()
                }
            }

            with(postsRefreshLayout?.findViewById(R.id.posts_list_view) as ListView) {
                postsListView = this

                setAdapter(PostsAdapter(ctx, emptyList<Post>()))

                setOnScrollListener(object: AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    }

                    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                        if (unfinishedRequest == null && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                            postsListViewFooter?.setVisibility(View.VISIBLE)

                            currentPage = totalItemCount / POSTS_PER_PAGE + 1

                            with(PostsRequest(limit=POSTS_PER_PAGE, page=currentPage)) {
                                unfinishedRequest = this

                                spiceManager.execute(this, postsListener)
                            }
                        }
                    }
                })

                setOnItemClickListener { adapterView, view, i, l ->
                    val post = adapterView.getRealAdapter<PostsAdapter>()!!.getItem(i)

                    mListener?.onPostSelected(post)
                }
            }


            postsListView?.addFooterView(postsListViewFooter)

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

        spiceManager.execute(PostsRequest(POSTS_PER_PAGE * currentPage, 1, postsFilters), postsListener)
    }

    override fun onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }

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
        with(postsListView?.getRealAdapter<PostsAdapter>()) {
            this?.clear()
            this?.notifyDataSetChanged()
        }

        with(PostsRequest(POSTS_PER_PAGE * currentPage, 1, postsFilters)) {
            unfinishedRequest = this

            spiceManager.execute(this, postsListener)
        }
    }

    public trait OnFragmentInteractionListener {
        public fun onPostSelected(post: Post)
    }

    companion object {
        val ARG_POSTS_FILTERS = "posts_filters"
        val POSTS_PER_PAGE = 16

        public fun newInstance(postsFilters: PostsFilters = PostsFilters()): PostsListFragment {
            val fragment = PostsListFragment()

            with(Bundle()) {
                putBundle(ARG_POSTS_FILTERS, postsFilters.bundle)

                fragment.setArguments(this)
            }

            return fragment
        }
    }
}
