package com.github.petropavel13.twophoto

import android.app.ActivityManager
import android.content.Context
import android.net.Uri

/**
 * Created by petropavel on 25/03/15.
 */

fun BuilderForApiUri() = Uri.Builder()
            .scheme("http")
            .authority("198.49.66.155")
            .appendPath("v1")

fun getMemoryClass(ctx: Context) = (ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.getMemoryClass() ?: 16