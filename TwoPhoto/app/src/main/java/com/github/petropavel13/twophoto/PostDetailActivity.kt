package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.etsy.android.grid.StaggeredGridView
import com.github.petropavel13.twophoto.adapters.EntriesAdapter
import com.github.petropavel13.twophoto.extensions.getRealAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.network.PostRequest
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener


public class PostDetailActivity : SpiceActivity() {

    companion object {
        val POST_ID_KEY ="post_id"
    }

    var postId = 0

    val postListener = object: RequestListener<PostDetail> {
        override fun onRequestFailure(spiceException: SpiceException?) {
        }

        override fun onRequestSuccess(result: PostDetail?) {
            titleTextView?.setText(result?.title)
            descriptionTextView?.setText(result?.description)

            with(entriesGridView?.getRealAdapter<EntriesAdapter>()) {
                this?.addAll(result?.entries)
                this?.notifyDataSetChanged()
            }

            loadingProgressBar?.setVisibility(View.INVISIBLE)
            titleTextView?.setVisibility(View.VISIBLE)
            descriptionTextView?.setVisibility(View.VISIBLE)
            entriesGridView?.setVisibility(View.VISIBLE)
        }
    }

    var titleTextView: TextView? = null
    var descriptionTextView: TextView? = null
    var entriesGridView: StaggeredGridView? = null
    var loadingProgressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        with(findViewById(R.id.post_detail_loading_progress_bar) as ProgressBar) {
            loadingProgressBar = this

            setVisibility(View.VISIBLE)
        }

        titleTextView = findViewById(R.id.post_detail_title_text_view) as? TextView
        descriptionTextView = findViewById(R.id.post_detail_description_text_view) as? TextView

        val ctx = this

        with(findViewById(R.id.post_detail_entries_grid_view) as StaggeredGridView) {
            entriesGridView = this

            with(getLayoutInflater().inflate(R.layout.post_detail_header_layout, null)) {
                titleTextView = findViewById(R.id.post_detail_title_text_view) as? TextView
                descriptionTextView = findViewById(R.id.post_detail_description_text_view) as? TextView

                addHeaderView(this)
            }

            setAdapter(EntriesAdapter(ctx, emptyList<Post.Entry>()))

            setOnItemClickListener { adapterView, view, i, l ->
                with(adapterView.getRealAdapter<EntriesAdapter>()) {
                    val postEntriesIntent = Intent(ctx, javaClass<PostEntriesActivity>())
                    postEntriesIntent.putParcelableArrayListExtra(PostEntriesActivity.POST_ENTRIES_KEY, this?.entries?.toArrayList())
                    postEntriesIntent.putExtra(PostEntriesActivity.SELECTED_ENTRY_INDEX, i - 1)
                    startActivity(postEntriesIntent)
                }
            }

            setVisibility(View.GONE)
        }

        titleTextView?.setVisibility(View.GONE)
        descriptionTextView?.setVisibility(View.GONE)

        postId = getIntent().getIntExtra(POST_ID_KEY, postId)

        spiceManager.execute(PostRequest(postId), postListener)
    }
}
