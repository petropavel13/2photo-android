package com.github.petropavel13.twophoto.sources

import com.github.petropavel13.twophoto.model.PostDetail
import com.github.petropavel13.twophoto.network.LimitedPostsList
import com.github.petropavel13.twophoto.network.PostsFilters

/**
 * Created by petropavel on 08/05/15.
 */


abstract class DataSource<RL, RD, F> {
    public interface ResponseListener<R> {
        fun onResponse(result: R?)
        fun onError(exception: Exception)
    }

    abstract fun requestList(listener: DataSource.ResponseListener<RL>, filters: F, limit: Int = 0, offset: Int = 0)

    abstract fun requestDetail(listener: DataSource.ResponseListener<RD>, forId: Int)

    abstract fun stopRequest()

    abstract fun retryUnfinishedRequest()
}

abstract class PostsDataSource: DataSource<LimitedPostsList, PostDetail, PostsFilters>()