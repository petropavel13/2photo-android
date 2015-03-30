package com.github.petropavel13.twophoto

import android.app.Activity
import android.util.Log
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService
import com.octo.android.robospice.SpiceManager

/**
 * Created by petropavel on 27/03/15.
 */

abstract class SpiceActivity: Activity() {
    val spiceManager = SpiceManager(javaClass<Jackson2GoogleHttpClientSpiceService>())

    override fun onStart() {
        spiceManager.start(this)
        super.onStart()
    }

    override fun onStop() {
        spiceManager.shouldStop()
        super.onStop()
    }
}
