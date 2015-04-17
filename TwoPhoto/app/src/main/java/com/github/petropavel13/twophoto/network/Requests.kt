package com.github.petropavel13.twophoto.network

import android.net.Uri
import com.github.petropavel13.twophoto.BuilderForApiUri
import com.github.petropavel13.twophoto.model.AuthorDetail
import com.github.petropavel13.twophoto.model.LimitedResultsList
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.model.PostDetail
import com.google.api.client.http.GenericUrl
import com.google.api.client.json.jackson2.JacksonFactory
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest

/**
 * Created by petropavel on 25/03/15.
 */

class LimitedPostsList: LimitedResultsList<Post>()

class PostsRequest(limit: Int,
                   page: Int = 1,
                   postsFilters: PostsFilters = PostsFilters()): GoogleHttpClientSpiceRequest<LimitedPostsList>(javaClass<LimitedPostsList>()) {

    private val urlString: String

    init {
        var builder = BuilderForApiUri()
                .appendEncodedPath("posts/")
                .appendQueryParameter("limit", limit.toString())
                .appendQueryParameter("page", page.toString())

        if (postsFilters.artistId != null)
            builder = builder.appendQueryParameter("artists", postsFilters.artistId.toString())

        if (postsFilters.authorId != null)
            builder = builder.appendQueryParameter("author", postsFilters.authorId.toString())

        if (postsFilters.tagId != null)
            builder = builder.appendQueryParameter("tags", postsFilters.tagId.toString())

        if (postsFilters.categoryId != null)
            builder = builder.appendQueryParameter("categories", postsFilters.categoryId.toString())

        urlString = builder.build().toString()
    }

    override fun loadDataFromNetwork(): LimitedPostsList? {
        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(urlString))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}

class PostRequest(postId: Int): GoogleHttpClientSpiceRequest<PostDetail>(javaClass<PostDetail>()) {
    val url = BuilderForApiUri()
            .appendEncodedPath("posts/$postId/")
            .build().toString()

    override fun loadDataFromNetwork(): PostDetail? {
        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(url))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}

class AuthorRequest(authorId: Int): GoogleHttpClientSpiceRequest<AuthorDetail>(javaClass<AuthorDetail>()) {
    val url = BuilderForApiUri()
            .appendEncodedPath("authors/$authorId/")
            .build().toString()

    override fun loadDataFromNetwork(): AuthorDetail? {
        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(url))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}
