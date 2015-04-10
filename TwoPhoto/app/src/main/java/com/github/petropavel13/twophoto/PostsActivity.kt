package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import com.github.petropavel13.twophoto.adapters.PostsAdapter
import com.github.petropavel13.twophoto.extensions.getRealAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsRequest
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener
import com.splunk.mint.Mint


public class PostsActivity : SpiceActivity() {
    var postsRefreshLayout: SwipeRefreshLayout? = null
    var postsListView: ListView? = null
    var postsListViewFooter: View? = null

    val POSTS_PER_PAGE = 16

    var isLoadingMore = true

    val postsListener = object: RequestListener<LimitedPostsList> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            postsRefreshLayout?.setRefreshing(false)

            isLoadingMore = false

            lastRequest = null
        }

        override fun onRequestSuccess(result: LimitedPostsList?) {
            postsRefreshLayout?.setRefreshing(false)

            with(postsListView?.getRealAdapter<PostsAdapter>()) {
                this?.addAll(result?.results)
                this?.notifyDataSetChanged()
            }

            lastRequest = null

            isLoadingMore = false
        }
    }

    var lastRequest: PostsRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mint.initAndStartSession(this, "4e4a18ab");

        setContentView(R.layout.activity_posts)

        val ctx = this

        postsListViewFooter = getLayoutInflater().inflate(R.layout.loading_more_layout, null)

        with(findViewById(R.id.posts_refresh_layout) as SwipeRefreshLayout) {
            postsRefreshLayout = this

            setOnRefreshListener {
                postsListViewFooter?.setVisibility(View.GONE)

                with(postsListView?.getRealAdapter<PostsAdapter>()) {
                    this?.clear()
                    this?.notifyDataSetChanged()
                }

                with(PostsRequest(limit=POSTS_PER_PAGE)) {
                    lastRequest = this

                    spiceManager.execute(this, postsListener)
                }
            }
        }

        with(postsRefreshLayout?.findViewById(R.id.posts_list_view) as ListView) {
            postsListView = this

            setAdapter(PostsAdapter(ctx, emptyList<Post>()))

            setOnScrollListener(object: AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if (isLoadingMore == false && firstVisibleItem + visibleItemCount == totalItemCount - 1) {
                        postsListViewFooter?.setVisibility(View.VISIBLE)

                        with(PostsRequest(limit=POSTS_PER_PAGE, page=totalItemCount / POSTS_PER_PAGE + 1)) {
                            lastRequest = this

                            spiceManager.execute(this, postsListener)
                        }

                        isLoadingMore = true
                    }
                }
            })

            setOnItemClickListener { adapterView, view, i, l ->
                val post = adapterView.getRealAdapter<PostsAdapter>()?.getItem(i) as Post

                val postDetailIntent = Intent(ctx, javaClass<PostDetailActivity>())
                postDetailIntent.putExtra(PostDetailActivity.POST_ID_KEY, post.id)
                startActivity(postDetailIntent)
            }
        }


        postsListView?.addFooterView(postsListViewFooter)

        with(PostsRequest(limit=POSTS_PER_PAGE)) {
            lastRequest = this

            spiceManager.execute(this, postsListener)
        }
    }

    override fun onRestart() {
        super.onRestart()

        if(lastRequest != null) {
            spiceManager.execute(lastRequest, postsListener)
        }
    }
}
