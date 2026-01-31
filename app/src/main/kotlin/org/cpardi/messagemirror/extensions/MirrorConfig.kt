package org.cpardi.messagemirror.extensions

import org.cpardi.messagemirror.helpers.MirrorConfig
import org.cpardi.messagemirror.models.DeviceMode

val MirrorConfig.isEnabled: Boolean
    get() = this.mode != DeviceMode.None
