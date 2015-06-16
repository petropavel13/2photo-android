package com.github.petropavel13.twophoto.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by petropavel on 15/06/15.
 */

DatabaseTable(tableName = "posts_categories")
class PostCategory {
    DatabaseField(generatedId = true)
    var id: Int = 0

    DatabaseField(foreign = true, canBeNull = false)
    var post = Post()

    DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    var category = Post.Category()

    constructor() {}

    constructor(post: PostDetail, category: Post.Category) {
        this.post = post
        this.category = category
    }
}