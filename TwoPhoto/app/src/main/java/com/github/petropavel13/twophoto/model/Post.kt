package com.github.petropavel13.twophoto.model

import android.os.Parcel
import android.os.Parcelable
import com.google.api.client.util.Key

/**
 * Created by petropavel on 23/03/15.
 */

open class Post() {
    [Key] var entries = emptyList<Entry>()
    [Key] var artists = emptyList<Artist>()
    [Key] var tags: List<Tag> = emptyList<Tag>()
    [Key] var categories = emptyList<Category>()
    [Key] var author = Author()
    [Key] var number_of_comments: Int = 0
    [Key] var id: Int = 0
    [Key] var title: String = ""
    [Key] var description: String = ""
    [Key] var link: String = ""
    [Key] var date: String = ""
    [Key] var rating: Int = 0
    [Key] var color: String = ""
    [Key] var face_image_url: String = ""

    open public class Author(): Parcelable {
        [Key] public var id: Int = 0
        [Key] public var name: String = ""
        [Key] public var avatar_url: String = ""

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

    public class Entry(): Parcelable {
        [Key] public var id: Int = 0
        [Key] public var big_img_url: String = ""
        [Key] public var medium_img_url: String = ""
        [Key] public var description: String = ""
        [Key] public var rating: Int = 0
        [Key] public var order: Int = 0

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