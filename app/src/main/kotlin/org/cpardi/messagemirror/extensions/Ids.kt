package org.cpardi.messagemirror.extensions

import android.net.Uri
import android.provider.Telephony
import androidx.core.net.toUri
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

fun LocalMsgId.toUri(): Uri = "${if (this.isMMS) "${Telephony.Mms.CONTENT_URI}/outbox" else Telephony.Sms.CONTENT_URI}/${this.id}".toUri()
fun Uri.toLocalMsgId(): LocalMsgId = LocalMsgId(this.lastPathSegment?.toLong()!!, this.host == "mms")
fun LocalMsgId.toGlobalMsgId(deviceId: String): GlobalMsgId = GlobalMsgId("$deviceId;${this.id};${if (this.isMMS) "mms" else "sms"}")
