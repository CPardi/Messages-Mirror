package org.cpardi.messagemirror.extensions

import java.net.URI

val URI.baseURI: String
    get() = "${this.scheme}://${this.authority}"
