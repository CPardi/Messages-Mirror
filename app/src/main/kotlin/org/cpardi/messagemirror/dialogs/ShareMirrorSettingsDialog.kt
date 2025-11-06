package org.cpardi.messagemirror.dialogs

import android.app.AlertDialog
import android.content.Context
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class ShareMirrorSettingsDialog(activity: Context, text: String) {
    init {
        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix: BitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
        val barcodeEncoder = BarcodeEncoder()
        val qrBitmap = barcodeEncoder.createBitmap(bitMatrix)

        val imageView = ImageView(activity)
        imageView.setImageBitmap(qrBitmap)
        AlertDialog.Builder(activity)
            .setTitle("Share Mirror Settings")
            .setMessage("""To sync settings, choose "Scan Settings" on your other device and scan this QR code.""")
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
    }
}
