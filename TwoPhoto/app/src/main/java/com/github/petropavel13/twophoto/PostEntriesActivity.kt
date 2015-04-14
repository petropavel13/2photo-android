package com.github.petropavel13.twophoto

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.github.petropavel13.twophoto.adapters.PostEntriesPagerAdapter
import com.github.petropavel13.twophoto.model.Post
import com.splunk.mint.Mint
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.HashMap


public class PostEntriesActivity : ActionBarActivity() {

    companion object {
        val POST_ENTRIES_KEY ="post_entries"
        val SELECTED_ENTRY_INDEX = "selected_entry_index"
    }

    var viewPager: ViewPager? = null
    var toolbar: Toolbar? = null
    val wallpapersTargetsMap = HashMap<Int, Target>()
    val downloadsTargetsMap = HashMap<Int, Target>()

    var postId = 0

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

            setAdapter(adapter)

            setCurrentItem(selectedItemIndex)

            setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //
                }

                override fun onPageSelected(position: Int) {
                    toolbar?.setTitle("${position + 1} из ${getAdapter().getCount()}")

                    with(getAdapter() as PostEntriesPagerAdapter) {
                        this.getViewForAtPosition(position - 1)?.viewWillHide()
                        this.getViewForAtPosition(position + 1)?.viewWillHide()
                        this.getViewForAtPosition(position)?.viewWillShow()
                    }
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
                    if(wallpapersTargetsMap.containsKey(currentItemIndex) == false) {
                        val target = object: Target {
                            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                try {
                                    WallpaperManager.getInstance(ctx).setBitmap(bitmap)

                                    Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_complete, Toast.LENGTH_LONG).show()
                                } catch(e: Exception) {
                                    Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_failed, Toast.LENGTH_LONG).show()
                                    Mint.logException(e)
                                }

                                wallpapersTargetsMap.remove(currentItemIndex)
                            }

                            override fun onBitmapFailed(errorDrawable: Drawable?) {
                                Toast.makeText(ctx, R.string.post_entries_action_set_wallpaper_failed, Toast.LENGTH_LONG).show()

                                wallpapersTargetsMap.remove(currentItemIndex)
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            }

                        }

                        wallpapersTargetsMap.put(currentItemIndex, target)

                        Picasso.with(ctx)
                                .load("http://${entry!!.big_img_url}")
                                .into(target)
                    }
                }

                R.id.menu_post_entries_action_download_picture -> {
                    if(downloadsTargetsMap.containsKey(currentItemIndex) == false) {
                        val target = object: Target {
                            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                if(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "2photo-$postId-${entry.id}", entry.description) != null) {
                                    Toast.makeText(ctx, R.string.post_entries_action_download_picture_complete, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(ctx, R.string.post_entries_action_download_picture_failed, Toast.LENGTH_LONG).show()
                                }

                                downloadsTargetsMap.remove(currentItemIndex)
                            }

                            override fun onBitmapFailed(errorDrawable: Drawable?) {
                                Toast.makeText(ctx, R.string.post_entries_action_download_picture_failed, Toast.LENGTH_LONG).show()

                                downloadsTargetsMap.remove(currentItemIndex)
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            }

                        }

                        Picasso.with(ctx)
                                .load("http://${entry!!.big_img_url}")
                                .into(target)

                        downloadsTargetsMap.put(currentItemIndex, target)
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
