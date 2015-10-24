package com.github.petropavel13.twophoto.sources

import android.os.AsyncTask
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.model.*
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsFilters
import com.j256.ormlite.field.DatabaseField

/**
 * Created by petropavel on 08/05/15.
 */

class ORMLitePostsDataSource(private val databaseOpenHelper: DatabaseOpenHelper): PostsDataSource() {
    private abstract class DBTaskList : AsyncTask<Void, Void, List<PostDetail>>()
    private abstract class DBTaskDetail : AsyncTask<Void, Void, PostDetail>()

    private var unfinishedTaskList: DBTaskList? = null
    private var unfinishedTaskDetail: DBTaskDetail? = null

    override fun requestList(listener: DataSource.ResponseListener<LimitedPostsList>, filters: PostsFilters, limit: Int, offset: Int) {
        unfinishedTaskList = object: DBTaskList() {
            private var hasMore = false
            private var exception: Exception? = null

            override fun doInBackground(vararg p0: Void?): List<PostDetail>? {
                try {
                    with(databaseOpenHelper.postsDao()) {
                        hasMore = queryBuilder().countOf() > offset + limit

                        return queryBuilder()
                                .limit(limit.toLong())
                                .offset(offset.toLong())
                                .query()
                    }
                } catch(e: Exception) {
                    exception = e
                    return null
                }
            }

            override fun onPostExecute(result: List<PostDetail>?) {
                super.onPostExecute(result)

                unfinishedTaskList = null

                if (result != null) {
                    with(LimitedPostsList()) {
                        next = if (hasMore) "has more" else null
                        count = result.count()
                        results = result

                        listener.onResponse(this)
                    }
                } else {
                    listener.onError(exception!!)
                }
            }
        }

        unfinishedTaskList?.execute()
    }

    override fun requestDetail(listener: DataSource.ResponseListener<PostDetail>, forId: Int) {
        unfinishedTaskDetail = object: DBTaskDetail() {
            private var exception: Exception? = null

            override fun doInBackground(vararg p0: Void?): PostDetail? {
                try {
                    with(databaseOpenHelper.postsDao().queryForId(forId)) {
                        if (this == null) {
                            return this
                        }

                        entries = dbEntries?.toList() ?: emptyList<Post.Entry>()
                        comments = dbComments?.toList() ?: emptyList<PostDetail.Comment>()

                        fun postColumnForClass(cls: Class<*>) = with(cls.getDeclaredField("post")) {
                            with(getAnnotation(DatabaseField::class.java)?.columnName) {
                                if(isNullOrEmpty()) "${name}_id" else this
                            }
                        }

                        artists = databaseOpenHelper.postArtistDao().queryForEq(postColumnForClass(PostArtist::class.java), this).map { it.artist }

                        tags = databaseOpenHelper.postTagsDao().queryForEq(postColumnForClass(PostTag::class.java), this).map { it.tag }

                        categories = databaseOpenHelper.postCategoryDao().queryForEq(postColumnForClass(PostCategory::class.java), this).map { it.category }

                        return this
                    }
                } catch(e: Exception) {
                    exception = e
                    return null
                }
            }

            override fun onPostExecute(result: PostDetail?) {
                super.onPostExecute(result)

                unfinishedTaskDetail = null

                if (exception != null) {
                    listener.onError(exception!!)
                } else {
                    listener.onResponse(result)
                }
            }
        }

        unfinishedTaskDetail?.execute()
    }

    override fun stopRequest() {
        unfinishedTaskList?.cancel(true)
        unfinishedTaskDetail?.cancel(true)
    }

    override fun retryUnfinishedRequest() {
        unfinishedTaskList?.execute()
        unfinishedTaskDetail?.execute()
    }
}