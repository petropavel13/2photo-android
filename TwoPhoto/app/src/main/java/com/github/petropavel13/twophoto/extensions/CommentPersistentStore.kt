package com.github.petropavel13.twophoto.extensions

import android.database.sqlite.SQLiteException
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.model.PostDetail
import com.j256.ormlite.misc.TransactionManager

/**
 * Created by petropavel on 15/06/15.
 */

throws(SQLiteException::class)
fun PostDetail.Comment.saveInDatabase(dbHelper: DatabaseOpenHelper, allComments: Collection<PostDetail.Comment>) {
    val comment = this

    TransactionManager.callInTransaction(dbHelper.getConnectionSource(), {
        dbHelper.authorsDao().createIfNotExists(author)

        dbAuthor = author

        if (reply_to != null && reply_to != 0) {
            dbReplyTo = allComments.first { it.id == reply_to }
        }

        dbHelper.commentsDao().create(comment)
    })
}
