package com.github.petropavel13.twophoto.network

import android.os.Bundle

/**
 * Created by petropavel on 17/04/15.
 */

public class PostsFilters {
    val artistId: Int?
    val authorId: Int?
    val tagId: Int?
    val categoryId: Int?

    constructor(artistId: Int? = null,
                authorId: Int? = null,
                tagId: Int? = null,
                categoryId: Int? = null) {
        this.artistId = artistId
        this.authorId = authorId
        this.tagId = tagId
        this.categoryId = categoryId
    }

    constructor(bundle: Bundle) {
        artistId = if (bundle.containsKey(ARTIST_ID_KEY)) bundle.getInt(ARTIST_ID_KEY) else null
        authorId = if (bundle.containsKey(AUTHOR_ID_KEY)) bundle.getInt(AUTHOR_ID_KEY) else null
        tagId = if (bundle.containsKey(TAG_ID_KEY)) bundle.getInt(TAG_ID_KEY) else null
        categoryId = if (bundle.containsKey(CATEGORY_ID_KEY)) bundle.getInt(CATEGORY_ID_KEY) else null
    }

    companion object {
        private val ARTIST_ID_KEY = "artist_id"
        private val AUTHOR_ID_KEY = "author_id"
        private val TAG_ID_KEY = "tag_id"
        private val CATEGORY_ID_KEY = "category_id"
    }

    public val bundle: Bundle
        get() {
            val b = Bundle()

            if (artistId != null)
                b.putInt(ARTIST_ID_KEY, artistId)

            if (authorId != null)
                b.putInt(AUTHOR_ID_KEY, authorId)

            if (tagId != null)
                b.putInt(TAG_ID_KEY, tagId)

            if (categoryId != null)
                b.putInt(CATEGORY_ID_KEY, categoryId)

            return b
        }
}
