package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.events.PostDeletedEvent
import com.github.petropavel13.twophoto.events.PostSavedEvent
import com.github.petropavel13.twophoto.fragments.PostsGridFragment
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.sources.ORMLitePostsDataSource
import com.github.petropavel13.twophoto.sources.SpicePostsDataSource
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import com.splunk.mint.Mint
import com.squareup.otto.Subscribe


public class PostsActivity : FragmentActivity(), PostsGridFragment.OnFragmentInteractionListener {
    companion object {
        val POSTS_PER_PAGE = 32
    }

    override fun onPostSelected(post: Post) {
        with(Intent(this, javaClass<PostDetailActivity>())) {
            putExtra(PostDetailActivity.POST_ID_KEY, post.id)
            putExtra(PostDetailActivity.FETCH_FROM_DB_KEY, fragmentsSources[viewPager?.getCurrentItem() ?: 0] is ORMLitePostsDataSource)
            startActivity(this)
        }
    }

    override fun onError(e: Exception) {
        // TODO: handle somehow
    }

    private var viewPager: ViewPager? = null

    private val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    private val fragmentsSources = arrayOf(SpicePostsDataSource(spiceManager, POSTS_PER_PAGE), ORMLitePostsDataSource(DatabaseOpenHelper(this)))

    private val fragments = Array(fragmentsSources.count(), { PostsGridFragment.newInstance(fragmentsSources[it], POSTS_PER_PAGE) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super<FragmentActivity>.onCreate(savedInstanceState)

        Mint.initAndStartSession(this, "4e4a18ab")

        Fresco.initialize(this)

        setContentView(R.layout.activity_posts)

        viewPager = findViewById(R.id.posts_pager) as? ViewPager
        viewPager?.setAdapter(object: FragmentPagerAdapter(getSupportFragmentManager()) {
            val titles = arrayOf(getResources().getString(R.string.posts_feed), getResources().getString(R.string.posts_favorites))

            override fun getCount(): Int = fragments.count()

            override fun getItem(position: Int): Fragment? {
                with(fragments[position]) {
                    reload()
                    return this
                }
            }

            override fun getPageTitle(position: Int): CharSequence? = titles[position]
        })

        eventsBus.register(this);
    }

    Subscribe
    fun postAdded(event: PostSavedEvent) {
        fragmentsSources.forEachIndexed { idx, postsDataSource ->
            if(postsDataSource is ORMLitePostsDataSource) {
                fragments[idx].reload()
            }
        }
    }

    Subscribe
    fun postRemoved(event: PostDeletedEvent) {
        fragmentsSources.forEachIndexed { idx, postsDataSource ->
            if(postsDataSource is ORMLitePostsDataSource) {
                fragments[idx].reload()
            }
        }
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
