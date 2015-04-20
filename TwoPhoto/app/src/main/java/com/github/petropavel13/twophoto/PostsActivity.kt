package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.github.petropavel13.twophoto.fragments.PostsListFragment
import com.github.petropavel13.twophoto.model.Post
import com.splunk.mint.Mint


public class PostsActivity : FragmentActivity(), PostsListFragment.OnFragmentInteractionListener {

    override fun onPostSelected(post: Post) {
        val postDetailIntent = Intent(this, javaClass<PostDetailActivity>())
        postDetailIntent.putExtra(PostDetailActivity.POST_ID_KEY, post.id)
        startActivity(postDetailIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<FragmentActivity>.onCreate(savedInstanceState)

        Mint.initAndStartSession(this, "4e4a18ab");

        setContentView(R.layout.activity_posts)

        (getSupportFragmentManager().findFragmentById(R.id.posts_fragment) as? PostsListFragment)?.reload()
    }
}
