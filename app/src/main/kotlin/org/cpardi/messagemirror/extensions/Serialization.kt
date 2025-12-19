package org.cpardi.messagemirror.extensions

import android.content.Intent
import android.os.Parcel

fun ByteArray.toIntent(): Intent {
    val parcel = Parcel.obtain()
    try {
        parcel.unmarshall(this, 0, this.size)
        parcel.setDataPosition(0)
        return Intent.CREATOR.createFromParcel(parcel)
    } finally {
        parcel.recycle()
    }
}

fun Intent.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    try {
        this.writeToParcel(parcel, 0)
        val parcelBytes = parcel.marshall()
        return parcelBytes

    } finally {
        parcel.recycle()
    }
}
