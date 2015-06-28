package com.github.petropavel13.twophoto

import android.net.Uri

/**
 * Created by petropavel on 25/03/15.
 */

fun BuilderForApiUri() = Uri.Builder()
            .scheme("http")
            .authority("192.227.236.251")
            .appendPath("v1")