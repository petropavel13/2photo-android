package com.github.petropavel13.twophoto.model

import android.os.Parcel
import android.os.Parcelable
import com.google.api.client.util.Key
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by petropavel on 23/03/15.
 */

open class Post() {
    @Key
    var entries = emptyList<Entry>()

    @Key
    var artists = emptyList<Artist>()

    @Key
    var tags = emptyList<Tag>()

    @Key
    var categories = emptyList<Category>()

    @Key @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    var author = Author()

    @Key @DatabaseField(canBeNull = false)
    var number_of_comments: Int = 0

    @Key @DatabaseField(id = true)
    var id: Int = 0

    @Key @DatabaseField(canBeNull = false)
    var title: String = ""

    @Key @DatabaseField
    var description: String? = null

    @Key @DatabaseField
    var link: String? = null

    @Key @DatabaseField(canBeNull = false)
    var date: String = ""

    @Key @DatabaseField(canBeNull = false)
    var rating: Int = 0

    @Key @DatabaseField(canBeNull = false)
    var color: String = ""

    @Key @DatabaseField(canBeNull = false)
    var face_image_url: String = ""


    @DatabaseTable(tableName = "authors")
    open public class Author(): Parcelable {
        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var name: String = ""

        @Key @DatabaseField(canBeNull = false)
        public var avatar_url: String = ""

        companion object {
            val CREATOR: Parcelable.Creator<Author> = object: Parcelable.Creator<Post.Author> {
                override fun createFromParcel(src: Parcel): Author? {
                    val me = Author()
                    me.id = src.readInt()
                    me.name = src.readString()
                    me.avatar_url= src.readString()

                    return me
                }

                override fun newArray(size: Int) = Array(size, { Author() })
            }
        }

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            with(dest) {
                writeInt(id)
                writeString(name)
                writeString(avatar_url)
            }
        }
    }


    @DatabaseTable(tableName = "entries")
    public class Entry(): Parcelable {
        @DatabaseField(foreign = true, canBeNull = false)
        public var dbPost: PostDetail = PostDetail()

        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var big_img_url: String = ""

        @Key @DatabaseField(canBeNull = false)
        public var medium_img_url: String = ""

        @Key @DatabaseField
        public var description: String? = null

        @Key @DatabaseField(canBeNull = false)
        public var rating: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var order: Int = 0

        companion object {
            val CREATOR: Parcelable.Creator<Entry> = object: Parcelable.Creator<Post.Entry> {
                override fun createFromParcel(src: Parcel): Entry? {
                    val entry = Entry()
                    entry.id = src.readInt()
                    entry.big_img_url = src.readString()
                    entry.medium_img_url = src.readString()
                    entry.description = src.readString()
                    entry.rating = src.readInt()
                    entry.order = src.readInt()

                    return entry
                }

                override fun newArray(size: Int) = Array(size, { Entry() })
            }
        }

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            with(dest) {
                writeInt(id)
                writeString(big_img_url)
                writeString(medium_img_url)
                writeString(description)
                writeInt(rating)
                writeInt(order)
            }
        }
    }


    @DatabaseTable(tableName = "artists")
    public class Artist() {
        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var name: String = ""

        @Key @DatabaseField(canBeNull = false)
        public var avatar_url: String = ""
    }


    @DatabaseTable(tableName = "categories")
    public class Category() {
        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var title: String = ""
    }


    @DatabaseTable(tableName = "tags")
    public class Tag() {
        @Key @DatabaseField(id = true)
        public var id: Int = 0

        @Key @DatabaseField(canBeNull = false)
        public var title: String = ""
    }
}