package com.github.petropavel13.twophoto

import android.net.Uri

/**
 * Created by petropavel on 25/03/15.
 */

fun BuilderForApiUri(): Uri.Builder {
    return Uri.Builder()
            .scheme("http")
            .authority("198.49.66.155")
            .appendPath("v1")
}