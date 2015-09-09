package com.github.petropavel13.twophoto

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

/**
 * Created by petropavel on 08/09/15.
 */

val eventsBus = Bus(ThreadEnforcer.MAIN)