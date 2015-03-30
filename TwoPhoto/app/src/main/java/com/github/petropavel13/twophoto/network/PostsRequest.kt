package com.github.petropavel13.twophoto.network

import android.net.Uri
import com.github.petropavel13.twophoto.BuilderForApiUri
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

class PostsRequest(val limit: Int, val page: Int = 1): GoogleHttpClientSpiceRequest<LimitedPostsList>(javaClass<LimitedPostsList>()) {
    val url = BuilderForApiUri()
            .appendPath("posts/")
            .appendQueryParameter("limit", limit.toString())
            .appendQueryParameter("page", page.toString())
            .build().toString()

    override fun loadDataFromNetwork(): LimitedPostsList? {
        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(url))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}

class PostRequest(val postId: Int): GoogleHttpClientSpiceRequest<PostDetail>(javaClass<PostDetail>()) {
    val url = BuilderForApiUri()
            .appendPath("posts/$postId/")
            .build().toString()

    override fun loadDataFromNetwork(): PostDetail? {
        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(url))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}
