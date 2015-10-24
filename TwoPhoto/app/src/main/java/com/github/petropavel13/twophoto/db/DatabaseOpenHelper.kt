package com.github.petropavel13.twophoto.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.github.petropavel13.twophoto.model.*
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

/**
 * Created by petropavel on 07/05/15.
 */

class DatabaseOpenHelper(ctx: Context): OrmLiteSqliteOpenHelper(ctx, "2photo-db", null, 1) {
    override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
        //
    }

    override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
        TableUtils.createTableIfNotExists(connectionSource, Post.Artist::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Post.Author::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Post.Category::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Post.Entry::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Post.Tag::class.java)
        TableUtils.createTableIfNotExists(connectionSource, PostDetail.Comment::class.java)
        TableUtils.createTableIfNotExists(connectionSource, PostDetail::class.java)
        TableUtils.createTableIfNotExists(connectionSource, PostCategory::class.java)
        TableUtils.createTableIfNotExists(connectionSource, PostTag::class.java)
        TableUtils.createTableIfNotExists(connectionSource, PostArtist::class.java)
    }

    fun artistsDao(): Dao<Post.Artist, Int> = getDao(Post.Artist::class.java)
    fun postArtistDao(): Dao<PostArtist, Int> = getDao(PostArtist::class.java)
    fun authorsDao(): Dao<Post.Author, Int> = getDao(Post.Author::class.java)
    fun categoriesDao(): Dao<Post.Category, Int> = getDao(Post.Category::class.java)
    fun postCategoryDao(): Dao<PostCategory, Int> = getDao(PostCategory::class.java)
    fun entriesDao(): Dao<Post.Entry, Int> = getDao(Post.Entry::class.java)
    fun tagsDao(): Dao<Post.Tag, Int> = getDao(Post.Tag::class.java)
    fun postTagsDao(): Dao<PostTag, Int> = getDao(PostTag::class.java)
    fun commentsDao(): Dao<PostDetail.Comment, Int> = getDao(PostDetail.Comment::class.java)
    fun postsDao(): Dao<PostDetail, Int> = getDao(PostDetail::class.java)
}
