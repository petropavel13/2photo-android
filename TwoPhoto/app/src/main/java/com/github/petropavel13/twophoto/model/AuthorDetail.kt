package com.github.petropavel13.twophoto.model

import com.google.api.client.util.Key

/**
 * Created by petropavel on 16/04/15.
 */

class AuthorDetail: Post.Author() {
    @Key public var number_of_comments: Int = 0
    @Key public var number_of_posts: Int = 0
    @Key public var reg_date: String = ""
    @Key public var last_visit: String = ""
    @Key public var country: String? = ""
    @Key public var city: String? = ""
    @Key public var site: String? = ""
    @Key public var skype: String? = ""
    @Key public var email: String? = ""
    @Key public var carma: Int = 0
    @Key public var description: String? = ""
}