package com.github.petropavel13.twophoto.extensions

import android.widget.HeaderViewListAdapter
import android.widget.ListAdapter
import android.widget.ListView

/**
 * Created by petropavel on 27/03/15.
 */

fun ListView.getRealAdapter<T>(): T? {
    val adapter = getAdapter()

    if (adapter is HeaderViewListAdapter) {
        return adapter.getWrappedAdapter() as? T
    }

    return adapter as? T
}