package com.github.petropavel13.twophoto.model

import com.google.api.client.util.Key
import java.util.ArrayList

/**
 * Created by petropavel on 23/03/15.
 */

class Post() {
    [Key] var entries: List<Post.Entry> = emptyList<Post.Entry>()
    [Key] var artists: List<Artist> = emptyList<Post.Artist>()
    [Key] var tags: List<Tag> = emptyList<Post.Tag>()
    [Key] var categories: List<Category> = emptyList<Post.Category>()
    [Key] var author: Post.Author = Author()
    [Key] var number_of_comments: Int = 0
    [Key] var id: Int = 0
    [Key] var title: String = ""
    [Key] var description: String = ""
    [Key] var link: String = ""
    [Key] var date: String = ""
    [Key] var rating: Int = 0
    [Key] var color: String = ""
    [Key] var face_image_url: String = ""

    public class Author() {
        [Key] public var id: Int = 0
        [Key] public var name: String = ""
        [Key] public var avatar_url: String = ""
    }

    public class Entry() {
        [Key] public var id: Int = 0
        [Key] public var big_img_url: String = ""
        [Key] public var medium_img_url: String = ""
        [Key] public var description: String = ""
        [Key] public var rating: Int = 0
        [Key] public var order: Int = 0
    }

    public class Artist() {
        [Key] public var id: Int = 0
        [Key] public var name: String = ""
        [Key] public var avatar_url: String = ""
    }

    public class Category() {
        [Key] public var id: Int = 0
        [Key] public var title: String = ""
    }

    public class Tag() {
        [Key] public var id: Int = 0
        [Key] public var title: String = ""
    }
}