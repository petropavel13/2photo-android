package com.github.petropavel13.twophoto

import android.app.Activity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
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

    inner class PostsListener: RequestListener<LimitedPostsList> {
        override fun onRequestFailure(spiceException: SpiceException?) {
            postsRefreshLayout?.setRefreshing(false)
        }

        override fun onRequestSuccess(result: LimitedPostsList?) {
            postsRefreshLayout?.setRefreshing(false)

            val adapter = postsListView?.getAdapter() as PostsAdapter
            adapter.clear()
            adapter.addAll(result?.results)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        with(findViewById(R.id.posts_refresh_layout) as SwipeRefreshLayout) {
            postsRefreshLayout = this

            setOnRefreshListener {
                spiceManager.execute(PostsRequest(limit=16), PostsListener())
            }
        }

        postsListView = postsRefreshLayout?.findViewById(R.id.posts_list_view) as ListView

        postsListView?.setAdapter(PostsAdapter(this, emptyList<Post>()))

        spiceManager.execute(PostsRequest(limit=4), PostsListener())
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
