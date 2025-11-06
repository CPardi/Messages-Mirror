package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Polymorphic class representing the various events occurring on either the SMS host or Mirror. */
@Serializable
sealed class EventDto {
    /** Represents when an SMS message is received by the SMS host */
    @Serializable
    data class SmsReceive(val address: String, val subject: String, val status: Int, val body: String, val date: Long) : EventDto()

    /** Represents when an SMS message is send by a Mirror */
    @Serializable
    data class SmsSend(val addresses: Array<String>, val subject: String, val status: Int, val body: String, val date: Long) : EventDto()

    companion object {
        /** Enables polymorphic serialisation for this class */
        val Serializer: Json
            get() = Json {
                serializersModule = SerializersModule {
                    polymorphic(EventDto::class) {
                        subclass(SmsReceive::class)
                        subclass(SmsSend::class)
                    }
                };
                classDiscriminator = "type";
                ignoreUnknownKeys = true
            }
    }
}
