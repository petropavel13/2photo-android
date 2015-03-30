package com.github.petropavel13.twophoto.extensions

import android.widget.AdapterView
import android.widget.HeaderViewListAdapter

/**
 * Created by petropavel on 27/03/15.
 */

fun AdapterView<*>.getRealAdapter<T>(): T? {
    val adapter = getAdapter()

    if (adapter is HeaderViewListAdapter) {
        return adapter.getWrappedAdapter() as? T
    }

    return adapter as? T
}