package com.github.petropavel13.twophoto.extensions

import android.database.sqlite.SQLiteException
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.model.PostArtist
import com.github.petropavel13.twophoto.model.PostCategory
import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.model.PostTag
import com.j256.ormlite.misc.TransactionManager

/**
 * Created by petropavel on 11/06/15.
 */

throws(SQLiteException::class)
fun PostDetail.createInDatabase(dbHelper: DatabaseOpenHelper) {
    val post = this

    TransactionManager.callInTransaction(dbHelper.getConnectionSource(), {
        with(dbHelper.tagsDao()) {
            tags.forEach { createIfNotExists(it) }
        }

        with(dbHelper.categoriesDao()) {
            categories.forEach { createIfNotExists(it) }
        }

        with(dbHelper.artistsDao()) {
            artists.forEach { createIfNotExists(it) }
        }

        dbHelper.authorsDao().createIfNotExists(author)

        dbHelper.postsDao().create(post)

        with(dbHelper.entriesDao()) {
            entries.forEach {
                it.dbPost = post
                create(it)
            }
        }

        with(dbHelper.commentsDao()) {
            comments.forEach {
                it.dbPost = post
                it.saveInDatabase(dbHelper, comments)
            }
        }

        with(dbHelper.postTagsDao()) {
            tags.forEach { create(PostTag(post, it)) }
        }

        with(dbHelper.postCategoryDao()) {
            categories.forEach { create(PostCategory(post, it)) }
        }

        with(dbHelper.postArtistDao()) {
            artists.forEach { create(PostArtist(post, it)) }
        }
    })
}

throws(SQLiteException::class)
fun PostDetail.deleteFromDatabase(dbHelper: DatabaseOpenHelper) {
    TransactionManager.callInTransaction(dbHelper.getConnectionSource(), {
        dbHelper.postArtistDao().deleteIds(artists.map { it.id })

        dbHelper.postCategoryDao().deleteIds(categories.map { it.id })

        dbHelper.postTagsDao().deleteIds(tags.map { it.id })

        dbHelper.commentsDao().deleteIds(dbComments!!.map { it.id })

        dbHelper.postsDao().delete(this)
    })
}