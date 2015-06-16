package com.github.petropavel13.twophoto.model

import com.google.api.client.util.Key

/**
 * Created by petropavel on 25/03/15.
 */

abstract class LimitedResultsList<RESULTS_TYPE> {
    Key public var count: Int = 0
    Key public var next: String? = null
    Key public var previous: String? = null
    Key public var results: List<RESULTS_TYPE> = emptyList<RESULTS_TYPE>()
}