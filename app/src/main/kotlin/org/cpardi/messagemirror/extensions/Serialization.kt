package org.cpardi.messagemirror.extensions

import android.content.Intent
import android.os.Parcel
import android.util.Base64

fun String.toIntent(): Intent {
    val parcel = Parcel.obtain()
    try {
        val bytes = Base64.decode(this, Base64.NO_WRAP)
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return Intent.CREATOR.createFromParcel(parcel)
    } finally {
        parcel.recycle()
    }
}

fun Intent.serialise(): String {
    val parcel = Parcel.obtain()
    try {
        this.writeToParcel(parcel, 0)
        val parcelBytes = parcel.marshall()
        val base64String = Base64.encodeToString(parcelBytes, Base64.NO_WRAP)
        return base64String

    } finally {
        parcel.recycle()
    }
}
