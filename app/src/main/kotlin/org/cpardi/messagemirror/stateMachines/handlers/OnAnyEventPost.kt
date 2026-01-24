package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.models.EventDto
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.helpers.refreshMessages

private val TAG: String = OnAnyEventPost::class.qualifiedName!!

class OnAnyEventPost(val context: Context) {
    fun handle(send: EventDto): MessageMapState? {
        refreshMessages()
        refreshConversations()
        Log.d(TAG, "Updated messages and conversations after event ${send::class.simpleName}")
        return null
    }
}
