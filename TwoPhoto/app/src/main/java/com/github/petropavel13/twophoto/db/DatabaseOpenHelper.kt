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
        TableUtils.createTableIfNotExists(connectionSource, javaClass<Post.Artist>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<Post.Author>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<Post.Category>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<Post.Entry>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<Post.Tag>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<PostDetail.Comment>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<PostDetail>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<PostCategory>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<PostTag>())
        TableUtils.createTableIfNotExists(connectionSource, javaClass<PostArtist>())
    }

    fun artistsDao(): Dao<Post.Artist, Int> = getDao(javaClass<Post.Artist>())
    fun postArtistDao(): Dao<PostArtist, Int> = getDao(javaClass<PostArtist>())
    fun authorsDao(): Dao<Post.Author, Int> = getDao(javaClass<Post.Author>())
    fun categoriesDao(): Dao<Post.Category, Int> = getDao(javaClass<Post.Category>())
    fun postCategoryDao(): Dao<PostCategory, Int> = getDao(javaClass<PostCategory>())
    fun entriesDao(): Dao<Post.Entry, Int> = getDao(javaClass<Post.Entry>())
    fun tagsDao(): Dao<Post.Tag, Int> = getDao(javaClass<Post.Tag>())
    fun postTagsDao(): Dao<PostTag, Int> = getDao(javaClass<PostTag>())
    fun commentsDao(): Dao<PostDetail.Comment, Int> = getDao(javaClass<PostDetail.Comment>())
    fun postsDao(): Dao<PostDetail, Int> = getDao(javaClass<PostDetail>())
}
