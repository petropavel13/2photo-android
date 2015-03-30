package com.github.petropavel13.twophoto.model

import com.google.api.client.util.Key

/**
 * Created by petropavel on 27/03/15.
 */

class PostDetail: Post() {
    [Key] var comments = emptyList<Comment>()

    public class Comment {
        [Key] public var author: Post.Author = Post.Author()
        [Key] public var id: Int = 0
        [Key] public var message: String = ""
        [Key] public var date: String = ""
        [Key] public var reply_to: Int? = 0
        [Key] public var rating: Int = 0
    }
}