package com.github.petropavel13.twophoto

import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.etsy.android.grid.StaggeredGridView
import com.github.petropavel13.twophoto.adapters.EntriesAdapter
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.events.PostDeletedEvent
import com.github.petropavel13.twophoto.events.PostSavedEvent
import com.github.petropavel13.twophoto.extensions.*
import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.sources.DataSource
import com.github.petropavel13.twophoto.sources.ORMLitePostsDataSource
import com.github.petropavel13.twophoto.sources.PostsDataSource
import com.github.petropavel13.twophoto.sources.SpicePostsDataSource
import com.github.petropavel13.twophoto.views.AuthorItemView
import com.github.petropavel13.twophoto.views.RetryView
import com.ns.developer.tagview.entity.Tag
import com.ns.developer.tagview.widget.TagCloudLinkView
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager
import kotlin.properties.Delegates


public class PostDetailActivity : AppCompatActivity(), DataSource.ResponseListener<PostDetail> {

    companion object {
        val POST_ID_KEY ="post_id"
        val FETCH_FROM_DB_KEY = "fetch_from_db"
    }

    private var postId = 0

    private var post: PostDetail by Delegates.notNull()

    private var spiceManager: SpiceManager? = null
    private var dataSource: PostsDataSource by Delegates.notNull()

    private var toolbar: Toolbar? = null

    private var titleTextView: TextView? = null
    private var descriptionTextView: TextView? = null
    private var entriesGridView: StaggeredGridView? = null
    private var loadingProgressBar: ProgressBar? = null
    private var retryView: RetryView? = null
    private var authorItemView: AuthorItemView? = null
    private var tagCloudView: TagCloudLinkView? = null
    private var headerView: View? = null
    private var footerView: View? = null

    private var menuItemSave: MenuItem? = null
    private var menuItemRemove: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)

        setContentView(R.layout.activity_post_detail)

        with(findViewById(R.id.post_detail_toolbar) as Toolbar) {
            toolbar = this

            // inflateMenu(R.menu.menu_post_entries) // for some reason "standalone" toolbar menu doesn't work

            // so fallback to actionbar flavor
            setSupportActionBar(this)
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)

        with(findViewById(R.id.post_detail_loading_progress_bar) as ProgressBar) {
            loadingProgressBar = this

            setVisibility(View.VISIBLE)
        }

        titleTextView = findViewById(R.id.post_detail_title_text_view) as? TextView
        descriptionTextView = findViewById(R.id.post_detail_description_text_view) as? TextView

        val ctx = this

        with(getIntent()) {
            postId = getIntExtra(POST_ID_KEY, postId)
            dataSource = if (getBooleanExtra(FETCH_FROM_DB_KEY, false)) {
                ORMLitePostsDataSource(DatabaseOpenHelper(ctx))
            } else {
                with(SpiceManager(Jackson2GoogleHttpClientSpiceService::class.java)) {
                    spiceManager = this
                    SpicePostsDataSource(this)
                }
            }
        }

        with(findViewById(R.id.post_detail_entries_grid_view) as StaggeredGridView) {
            entriesGridView = this

            with(getLayoutInflater().inflate(R.layout.post_detail_header_layout, null)) {
                headerView = this
                titleTextView = findViewById(R.id.post_detail_title_text_view) as? TextView
                descriptionTextView = findViewById(R.id.post_detail_description_text_view) as? TextView
            }

            with(getLayoutInflater().inflate(R.layout.post_detail_footer_layout, null)) {
                footerView = this

                authorItemView = findViewById(R.id.post_detail_footer_author_item_view) as? AuthorItemView

                authorItemView?.setOnClickListener {
                    with(Intent(ctx, AuthorDetailActivity::class.java)) {
                        putExtra(AuthorDetailActivity.AUTHOR_KEY, authorItemView!!.author)
                        startActivity(this)
                    }
                }

                tagCloudView = findViewById(R.id.post_detail_tag_cloud_view) as? TagCloudLinkView
            }

            setOnItemClickListener { adapterView, view, i, l ->
                with(adapterView.getRealAdapter<EntriesAdapter>()) {
                    val postEntriesIntent = Intent(ctx, PostEntriesActivity::class.java)
                    postEntriesIntent.putParcelableArrayListExtra(PostEntriesActivity.POST_ENTRIES_KEY, this?.entries?.toArrayList())
                    postEntriesIntent.putExtra(PostEntriesActivity.SELECTED_ENTRY_INDEX, i - 1)
                    postEntriesIntent.putExtra(POST_ID_KEY, postId)
                    startActivity(postEntriesIntent)
                }
            }

            setVisibility(View.INVISIBLE)
        }

        with(findViewById(R.id.post_detail_retry_view) as RetryView) {
            retryView = this

            onRetryListener = object: View.OnClickListener{
                override fun onClick(view: View) {
                    retryView?.setVisibility(View.INVISIBLE)
                    loadingProgressBar?.setVisibility(View.VISIBLE)

                    dataSource.requestDetail(this@PostDetailActivity, postId)
                }
            }

            setVisibility(View.INVISIBLE)
        }

        dataSource.requestDetail(this, postId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_post_detail, menu)

        menuItemSave = menu?.findItem(R.id.menu_post_detail_action_save_post)
        menuItemRemove = menu?.findItem(R.id.menu_post_detail_action_remove_post)

        return super<AppCompatActivity>.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.getItemId() == android.R.id.home) {
            finish()

            return super<AppCompatActivity>.onOptionsItemSelected(item)
        }

        val ctx = this

        when(item?.getItemId()) {
            R.id.menu_post_detail_action_save_post -> {
                with(DatabaseOpenHelper(ctx)) {
                    try {
                        post.savePostImages(ctx)
                        post.createInDatabase(this)

                        menuItemRemove?.setVisible(true)
                        menuItemSave?.setVisible(false)

                        eventsBus.post(PostSavedEvent(post))

                        Toast.makeText(ctx, "Successful saved post", Toast.LENGTH_LONG)
                    } catch(e: SQLiteException) {
                        Toast.makeText(ctx, "Failed to save post", Toast.LENGTH_LONG)
                    } finally {
                        close()
                    }
                }
            }
            R.id.menu_post_detail_action_remove_post -> {
                with(DatabaseOpenHelper(ctx)) {
                    try {
                        post.deletePostImages(ctx)
                        post.deleteFromDatabase(this)

                        menuItemRemove?.setVisible(false)
                        menuItemSave?.setVisible(true)

                        eventsBus.post(PostDeletedEvent(post))

                        Toast.makeText(ctx, "Post was successfully removed", Toast.LENGTH_LONG)
                        finish()
                    } catch(e: SQLiteException) {
                        Toast.makeText(ctx, "Failed to remove post", Toast.LENGTH_LONG)
                    } finally {
                        close()
                    }
                }
            }
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    override fun onResponse(result: PostDetail?) {
        post = result ?: PostDetail()

        titleTextView?.setText(post.title)
        descriptionTextView?.setText(post.description)

        authorItemView?.author = post.author

        post.tags.map { it.title }.forEachIndexed { i, s -> tagCloudView?.add(Tag(i, s)) }
        tagCloudView?.drawTags()

        entriesGridView?.addHeaderView(headerView)
        entriesGridView?.addFooterView(footerView)

        entriesGridView?.setAdapter(EntriesAdapter(this, post.entries))

        loadingProgressBar?.setVisibility(View.INVISIBLE)
        entriesGridView?.setVisibility(View.VISIBLE)

        if(dataSource is ORMLitePostsDataSource) {
            menuItemRemove?.setVisible(true)
            menuItemSave?.setVisible(false)
        } else {
            // set default
            menuItemRemove?.setVisible(false)
            menuItemSave?.setVisible(true)

            // and then check existence in db
            with(ORMLitePostsDataSource(DatabaseOpenHelper(this))) {
                requestDetail(object: DataSource.ResponseListener<PostDetail>{
                    override fun onResponse(result: PostDetail?) {
                        val exists = result != null

                        menuItemRemove?.setVisible(exists)
                        menuItemSave?.setVisible(!exists)
                    }

                    override fun onError(exception: Exception) {
                        //
                    }

                }, post.id)
            }
        }
    }

    override fun onError(exception: Exception) {
        loadingProgressBar?.setVisibility(View.INVISIBLE)
        retryView?.setVisibility(View.VISIBLE)
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()

        spiceManager?.start(this)
    }

    override fun onStop() {
        super<AppCompatActivity>.onStop()

        spiceManager?.shouldStop()

        entriesGridView?.getRealAdapter<EntriesAdapter>()?.unloadItemsImages()
    }

    override fun onRestart() {
        super<AppCompatActivity>.onRestart()

        entriesGridView?.getRealAdapter<EntriesAdapter>()?.loadItemsImages()
    }
}
