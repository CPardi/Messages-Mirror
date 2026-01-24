package org.cpardi.messagemirror.extensions

import android.net.Uri
import android.provider.Telephony.Sms
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

fun Int.toLocalMsgId(): LocalMsgId = LocalMsgId("$this")
fun LocalMsgId.toUri(): Uri = Uri.parse("${Sms.CONTENT_URI}/${this.value}")
fun Uri.toLocalMsgId(): LocalMsgId = this.lastPathSegment?.toIntOrNull()?.toLocalMsgId()!!
fun LocalMsgId.toGlobalMsgId(deviceId: String): GlobalMsgId = GlobalMsgId("$deviceId;${this.value}")
