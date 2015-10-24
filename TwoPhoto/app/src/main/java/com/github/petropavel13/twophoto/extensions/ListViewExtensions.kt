package com.github.petropavel13.twophoto.extensions

import android.widget.AdapterView
import android.widget.WrapperListAdapter

/**
 * Created by petropavel on 27/03/15.
 */

fun <T> AdapterView<*>.getRealAdapter(): T? {
    val adapter = getAdapter()

    if (adapter is WrapperListAdapter) {
        return adapter.getWrappedAdapter() as? T
    }

    return adapter as? T
}