package com.github.petropavel13.twophoto

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.github.petropavel13.twophoto.adapters.PostEntriesPagerAdapter
import com.github.petropavel13.twophoto.model.Post
import com.splunk.mint.Mint
import java.util.HashSet


public class PostEntriesActivity : AppCompatActivity() {

    companion object {
        val POST_ENTRIES_KEY ="post_entries"
        val SELECTED_ENTRY_INDEX = "selected_entry_index"
        private val FULLSCREEN_KEY = "fullscreen"
    }

    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    private val wallpapersInProgress = HashSet<Int>()
    private val downloadsInProgress = HashSet<Int>()

    private var postId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_post_entries)

        val ctx = this

        val adapter = PostEntriesPagerAdapter(ctx, getIntent().getParcelableArrayListExtra<Post.Entry>(POST_ENTRIES_KEY))

        val selectedItemIndex = getIntent().getIntExtra(SELECTED_ENTRY_INDEX, 0)

        postId = getIntent().getIntExtra(PostDetailActivity.POST_ID_KEY, 0)

        with(findViewById(R.id.post_entries_view_pager) as ViewPager) {
            viewPager = this

            adapter.onEntryTapListener = object: View.OnClickListener {
                override fun onClick(view: View) {
                    with(getSupportActionBar()) {
                        if(isShowing()) {
                            hide()

                            adapter.showEntriesDescription = false
                        } else {
                            show()

                            adapter.showEntriesDescription = true
                        }
                    }
                }
            }

            if (savedInstanceState?.getBoolean(FULLSCREEN_KEY, false) ?: false) {
                getSupportActionBar().hide()

                adapter.showEntriesDescription = false
            }

            setAdapter(adapter)

            setCurrentItem(selectedItemIndex)

            setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //
                }

                override fun onPageSelected(position: Int) {
                    toolbar?.setTitle("${position + 1} из ${getAdapter().getCount()}")
                }

                override fun onPageScrollStateChanged(state: Int) {
                    //
                }
            })
        }

        with(findViewById(R.id.post_entries_toolbar) as Toolbar) {
            toolbar = this

//            inflateMenu(R.menu.menu_post_entries) // for some reason "standalone" toolbar menu doesn't work

            setTitle("${selectedItemIndex + 1} из ${adapter.getCount()}")

            // so fallback to actionbar flavor
            setSupportActionBar(this)
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putBoolean(FULLSCREEN_KEY, getSupportActionBar().isShowing())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_post_entries, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.getItemId() == android.R.id.home) {
            finish()

            return super.onOptionsItemSelected(item)
        }

        val ctx = this

        val pager = viewPager!!

        val adapter = pager.getAdapter() as PostEntriesPagerAdapter
        val currentItemIndex = pager.getCurrentItem()

        val entry = adapter.getViewForAtPosition(currentItemIndex)?.entry

        if(entry != null) {
            when(item?.getItemId()) {
                R.id.menu_post_entries_action_set_wallpaper -> {
                    if(wallpapersInProgress.contains(currentItemIndex) == false) {
                        wallpapersInProgress.add(currentItemIndex)

                        Fresco.getImagePipeline()
                                .fetchDecodedImage(ImageRequest.fromUri(entry.big_img_url), null)
                                .subscribe(object: BaseBitmapDataSubscriber() {
                                    override fun onNewResultImpl(bitmap: Bitmap?) {
                                        var completed = false

                                        try {
                                            WallpaperManager.getInstance(ctx).setBitmap(bitmap)
                                            completed = true
                                        } catch(e: Exception) {
                                            Mint.logException(e)
                                        }

                                        Handler(Looper.getMainLooper()).post {
                                            if(completed) {
                                                Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_complete, Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_failed, Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        wallpapersInProgress.remove(currentItemIndex)
                                    }

                                    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_failed, Toast.LENGTH_LONG).show()
                                        }

                                        wallpapersInProgress.remove(currentItemIndex)
                                    }
                                }, CallerThreadExecutor.getInstance())
                    }
                }

                R.id.menu_post_entries_action_download_picture -> {
                    if(downloadsInProgress.contains(currentItemIndex) == false) {
                        downloadsInProgress.add(currentItemIndex)

                        Fresco.getImagePipeline()
                                .fetchDecodedImage(ImageRequest.fromUri(entry.big_img_url), null)
                                .subscribe(object: BaseBitmapDataSubscriber() {
                                    override fun onNewResultImpl(bitmap: Bitmap?) {
                                        val completed = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "2photo-$postId-${entry.id}", entry.description) != null

                                        Handler(Looper.getMainLooper()).post {
                                            if(completed) {
                                                Toast.makeText(ctx, R.string.post_entries_action_download_picture_complete, Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(ctx, R.string.post_entries_action_download_picture_failed, Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        downloadsInProgress.remove(currentItemIndex)
                                    }

                                    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(ctx, R.string.post_entries_action_download_picture_failed, Toast.LENGTH_LONG).show()
                                        }

                                        downloadsInProgress.remove(currentItemIndex)
                                    }
                                }, CallerThreadExecutor.getInstance())
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
