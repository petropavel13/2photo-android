package com.github.petropavel13.twophoto.sources

import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostRequest
import com.github.petropavel13.twophoto.network.PostsFilters
import com.github.petropavel13.twophoto.network.PostsRequest
import com.octo.android.robospice.SpiceManager
import com.octo.android.robospice.persistence.exception.SpiceException
import com.octo.android.robospice.request.listener.RequestListener
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

/**
 * Created by petropavel on 08/05/15.
 */

class SpicePostsDataSource(private val spiceManager: SpiceManager, private val pageSize: Int = -1): PostsDataSource() {
    private var unfinishedRequestNetwork: PostsRequest? = null

    private var listListener: WeakReference<DataSource.ResponseListener<LimitedPostsList>> by Delegates.notNull()
    private var detailListener: WeakReference<DataSource.ResponseListener<PostDetail>> by Delegates.notNull()

    private val spicePostsListener = object: RequestListener<LimitedPostsList> {
        override fun onRequestSuccess(result: LimitedPostsList) {
            unfinishedRequestNetwork = null

            listListener.get()?.onResponse(result)
        }

        override fun onRequestFailure(spiceException: SpiceException) {
            unfinishedRequestNetwork = null

            listListener.get()?.onError(spiceException)
        }
    }

    private val spicePostListener = object: RequestListener<PostDetail> {
        override fun onRequestSuccess(result: PostDetail) {
            unfinishedRequestNetwork = null

            detailListener.get()?.onResponse(result)
        }

        override fun onRequestFailure(spiceException: SpiceException) {
            unfinishedRequestNetwork = null

            detailListener.get()?.onError(spiceException)
        }
    }

    override fun requestList(listener: DataSource.ResponseListener<LimitedPostsList>,
                             filters: PostsFilters,
                             limit: Int,
                             offset: Int) {
        if (unfinishedRequestNetwork != null) {
            spiceManager.cancel(unfinishedRequestNetwork)
        }

        this.listListener = WeakReference(listener)

        spiceManager.execute(PostsRequest(limit = limit, page = (offset / pageSize) + 1, postsFilters = filters), spicePostsListener)
    }

    override fun requestDetail(listener: DataSource.ResponseListener<PostDetail>, forId: Int) {
        if(unfinishedRequestNetwork != null) {
            spiceManager.cancel(unfinishedRequestNetwork)
        }

        this.detailListener = WeakReference(listener)

        spiceManager.execute(PostRequest(forId), spicePostListener)
    }

    override fun stopRequest() {
        if (unfinishedRequestNetwork != null) {
            spiceManager.cancel(unfinishedRequestNetwork)
        }
    }

    override fun retryUnfinishedRequest() {
        if (unfinishedRequestNetwork != null) {
            spiceManager.execute(unfinishedRequestNetwork, spicePostsListener)
        }
    }
}