package com.github.petropavel13.twophoto.model

import com.google.api.client.util.Key
import com.j256.ormlite.dao.ForeignCollection
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by petropavel on 27/03/15.
 */

@DatabaseTable(tableName = "posts")
class PostDetail: Post() {
    @Key
    var comments = emptyList<Comment>()

    @ForeignCollectionField(eager = true)
    var dbComments: ForeignCollection<Comment>? = null

    @ForeignCollectionField(eager = true, orderColumnName = "order")
    var dbEntries: ForeignCollection<Post.Entry>? = null


    @DatabaseTable(tableName = "comments")
    public class Comment {
        @Key
        public var author: Post.Author = Post.Author()

        @DatabaseField(foreign = true, canBeNull = false)
        public var dbAuthor: Post.Author = Post.Author()

        @DatabaseField(foreign = true, canBeNull = false)
        public var dbPost: PostDetail = PostDetail()

        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var message: String = ""

        @Key @DatabaseField(canBeNull = false)
        public var date: String = ""

        @Key
        public var reply_to: Int? = null

        @DatabaseField(foreign = true)
        public var dbReplyTo: PostDetail.Comment? = null

        @Key @DatabaseField(canBeNull = false)
        public var rating: Int = 0
    }
}