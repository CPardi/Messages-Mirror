package org.cpardi.messagemirror.extensions

import android.net.Uri
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

fun Int.toLocalMsgId(): LocalMsgId = LocalMsgId("$this")
fun LocalMsgId.toUri(): Uri = Uri.parse("content://sms/${this.value}")
fun Uri.toLocalMsgId(): LocalMsgId = this.lastPathSegment?.toIntOrNull()?.toLocalMsgId()!!
fun Uri.toGlobalMsgId(deviceId: String): GlobalMsgId = GlobalMsgId("$deviceId;${this.lastPathSegment}")
