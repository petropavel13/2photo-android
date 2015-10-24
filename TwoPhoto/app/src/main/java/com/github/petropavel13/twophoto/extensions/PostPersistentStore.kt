package com.github.petropavel13.twophoto.extensions

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Environment
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.memory.PooledByteBuffer
import com.facebook.imagepipeline.request.ImageRequest
import com.github.petropavel13.twophoto.db.DatabaseOpenHelper
import com.github.petropavel13.twophoto.model.PostArtist
import com.github.petropavel13.twophoto.model.PostCategory
import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.model.PostTag
import com.j256.ormlite.misc.TransactionManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by petropavel on 11/06/15.
 */

@Throws(SQLiteException::class)
fun PostDetail.createInDatabase(dbHelper: DatabaseOpenHelper) {
    val post = this

    TransactionManager.callInTransaction(dbHelper.connectionSource, {
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

class ImageSaver(val imageFile: File): BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
    var exception: Throwable? = null
    private set

    companion object {
        private val MAX_BUFFER_SIZE = 1024 * 128 // 128 KB
    }
    override fun onFailureImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>) {
        exception = dataSource.failureCause
    }

    override fun onNewResultImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>) {
        if (dataSource.isFinished) {
            if(dataSource.hasResult()) {
                val imageReference = dataSource.result

                var fos: FileOutputStream? = null

                try {
                    val image = imageReference.get()

                    val total = image.size()
                    val realBufferSize = if(total < MAX_BUFFER_SIZE) total else MAX_BUFFER_SIZE

                    var bytesForRead = realBufferSize

                    val ba = ByteArray(realBufferSize)

                    fos = FileOutputStream(imageFile)

                    var offset = 0

                    while(bytesForRead > 0) {
                        image.read(offset, ba, 0, bytesForRead)
                        fos.write(ba, 0, bytesForRead)

                        offset += realBufferSize

                        val estimated = total - offset

                        bytesForRead = if(estimated > realBufferSize) realBufferSize else estimated
                    }
                } catch(fnfe: FileNotFoundException) {
                    exception = fnfe
                } catch(ioe: IOException) {
                    exception = ioe
                } finally {
                    fos?.close()

                    imageReference.close()
                }
            }
        }
    }
}

private fun PostDetail.postFolderPath(ctx: Context) = ctx.getExternalFilesDir(null).path + "/post_${id}"

//throws(PostSaveException::class)
fun PostDetail.savePostImages(ctx: Context) {
    when(Environment.getExternalStorageState()) {
        Environment.MEDIA_MOUNTED -> {
            val postFolder = File(postFolderPath(ctx))

            if(postFolder.exists() == false) {
                if(postFolder.mkdir() == false) {
//                    throw PostSaveException("Failed to create post directory in ${postFolder.path}")
                }
            }

            with(Fresco.getImagePipeline()) {
                val faceImageUri = Uri.parse(face_image_url)

                val faceImageFile = File(postFolder, faceImageUri.lastPathSegment)

                if (faceImageFile.exists()) {
                    if (faceImageFile.delete() == false) {
                        // throw
                    }
                }

                val faceImageSaver = ImageSaver(faceImageFile)

                fetchEncodedImage(ImageRequest.fromUri(faceImageUri), null)
                        .subscribe(faceImageSaver, CallerThreadExecutor.getInstance())

                if (faceImageSaver.exception != null) {
                    // throw
                }

                face_image_url = faceImageFile.toURI().toURL().toString()

                entries.forEach {
                    val mediumUri = Uri.parse(it.medium_img_url)
                    val bigUri = Uri.parse(it.big_img_url)

                    val mediumImageFile = File(postFolder, mediumUri.lastPathSegment)
                    val bigImageFile = File(postFolder, bigUri.lastPathSegment)

                    if (mediumImageFile.exists()) {
                        if (mediumImageFile.delete() == false) {
                            // throw
                        }
                    }

                    if (bigImageFile.exists()) {
                        if (bigImageFile.delete() == false) {
                            // throw
                        }
                    }

                    val mediumSaver = ImageSaver(mediumImageFile)
                    val bigSaver = ImageSaver(bigImageFile)


                    fetchEncodedImage(ImageRequest.fromUri(mediumUri), null)
                            .subscribe(mediumSaver, CallerThreadExecutor.getInstance())

                    fetchEncodedImage(ImageRequest.fromUri(bigUri), null)
                            .subscribe(bigSaver, CallerThreadExecutor.getInstance())


                    if (mediumSaver.exception != null) {
                        // throw
                    }

                    it.medium_img_url = mediumImageFile.toURI().toURL().toString()

                    if (bigSaver.exception != null) {
                        // throw
                    }

                    it.big_img_url = bigImageFile.toURI().toURL().toString()
                }
            }
        }
        else -> {
            // throw
        }
    }
}


@Throws(SQLiteException::class)
fun PostDetail.deleteFromDatabase(dbHelper: DatabaseOpenHelper) {
    TransactionManager.callInTransaction(dbHelper.connectionSource, {
        dbHelper.postArtistDao().deleteIds(artists.map { it.id })

        dbHelper.postCategoryDao().deleteIds(categories.map { it.id })

        dbHelper.postTagsDao().deleteIds(tags.map { it.id })

        if(dbComments != null) {
            dbHelper.commentsDao().deleteIds(dbComments!!.map { it.id })
        }

        dbHelper.postsDao().delete(this)
    })
}

fun PostDetail.deletePostImages(ctx: Context) {
    when(Environment.getExternalStorageState()) {
        Environment.MEDIA_MOUNTED -> {
            val postFolder = File(postFolderPath(ctx))

            if(postFolder.exists()) {
                if(postFolder.deleteRecursively() == false) {
                    // throw
                }
            }
        }
        else -> {
            // throw
        }
    }
}