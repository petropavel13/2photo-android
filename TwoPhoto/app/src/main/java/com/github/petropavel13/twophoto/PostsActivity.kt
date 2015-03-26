package com.github.petropavel13.twophoto

import android.app.Activity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.AbsListView
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import com.github.petropavel13.twophoto.adapters.PostsAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsRequest
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener


public class PostsActivity : Activity() {

    val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    var postsRefreshLayout: SwipeRefreshLayout? = null
    var postsListView: ListView? = null

    val POSTS_PER_PAGE = 16

    var isLoadingMore = true

    val postsListener = object: RequestListener<LimitedPostsList> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            postsRefreshLayout?.setRefreshing(false)

            isLoadingMore = false
        }

        override fun onRequestSuccess(result: LimitedPostsList?) {
            postsRefreshLayout?.setRefreshing(false)

            val adapter = (postsListView?.getAdapter() as HeaderViewListAdapter).getWrappedAdapter() as PostsAdapter
            adapter.addAll(result?.results)
            adapter.notifyDataSetChanged()

            isLoadingMore = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        with(findViewById(R.id.posts_refresh_layout) as SwipeRefreshLayout) {
            postsRefreshLayout = this

            setOnRefreshListener {
                spiceManager.execute(PostsRequest(limit=POSTS_PER_PAGE), postsListener)
            }
        }

        postsListView = postsRefreshLayout?.findViewById(R.id.posts_list_view) as ListView

        postsListView?.setAdapter(PostsAdapter(this, emptyList<Post>()))

        postsListView?.setOnScrollListener(object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (isLoadingMore == false && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                    spiceManager.execute(PostsRequest(limit=POSTS_PER_PAGE, page=totalItemCount / POSTS_PER_PAGE + 1), postsListener)

                    isLoadingMore = true
                }
            }
        })

        postsListView?.addFooterView(getLayoutInflater().inflate(R.layout.loading_more_layout, null))

        spiceManager.execute(PostsRequest(limit=POSTS_PER_PAGE), postsListener)
    }

    override fun onStart() {
        spiceManager.start(this)
        super.onStart()
    }

    override fun onStop() {
        spiceManager.shouldStop()
        super.onStop()
    }
}
