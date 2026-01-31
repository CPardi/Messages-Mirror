package org.cpardi.messagemirror.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.messages.R

private const val WIDTH: Int = 600
private const val HEIGHT = 600

class ShareMirrorSettingsDialog(activity: Context, text: String) {
    init {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.dialog_share_mirror_settings, null)

        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix: BitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
        val barcodeEncoder = BarcodeEncoder()
        val qrBitmap = barcodeEncoder.createBitmap(bitMatrix)

        val qrImageView = view.findViewById<ImageView>(R.id.share_mirror_settings_qr_code)
        qrImageView.setImageBitmap(qrBitmap)

        val titleView = view.findViewById<TextView>(R.id.share_mirror_settings_title)
        val messageView = view.findViewById<TextView>(R.id.share_mirror_settings_message)

        val primaryColor = activity.getProperPrimaryColor()
        val textColor = activity.getProperTextColor()

        titleView.setTextColor(primaryColor)
        messageView.setTextColor(textColor)

        AlertDialog.Builder(activity)
            .setView(view)
            .setPositiveButton("Close", null)
            .show()
    }
}
