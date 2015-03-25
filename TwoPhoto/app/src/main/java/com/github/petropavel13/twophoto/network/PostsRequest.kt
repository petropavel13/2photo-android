package com.github.petropavel13.twophoto.network

import android.net.Uri
import com.github.petropavel13.twophoto.BuilderForApiUri
import com.github.petropavel13.twophoto.model.LimitedResultsList
import com.github.petropavel13.twophoto.model.Post
import com.google.api.client.http.GenericUrl
import com.google.api.client.json.jackson2.JacksonFactory
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest

/**
 * Created by petropavel on 25/03/15.
 */

class LimitedPostsList: LimitedResultsList<Post>()

class PostsRequest(val limit: Int, val page: Int = 1): GoogleHttpClientSpiceRequest<LimitedPostsList>(javaClass<LimitedPostsList>()) {
    override fun loadDataFromNetwork(): LimitedPostsList? {
        val uri = BuilderForApiUri()
                .appendPath("posts/")
                .appendQueryParameter("limit", limit.toString())
                .appendQueryParameter("page", page.toString())
                .build().toString()

        val request = getHttpRequestFactory()
                .buildGetRequest(GenericUrl(uri))
                .setParser(JacksonFactory.getDefaultInstance().createJsonObjectParser())

        return request.execute().parseAs(getResultType())
    }
}
