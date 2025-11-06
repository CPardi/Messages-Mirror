package org.cpardi.messagemirror.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.dialogs.ShareMirrorSettingsDialog
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver
import org.fossify.commons.compose.extensions.getActivity
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.isPackageInstalled
import org.fossify.commons.models.RadioItem
import org.fossify.messages.databinding.ViewMirrorSettingsBinding
import org.fossify.messages.extensions.toArrayList
import java.security.SecureRandom

class MirrorSettings @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMirrorSettingsBinding.inflate(LayoutInflater.from(context), this)
    private val prefs: SharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    private val editPrefs: SharedPreferences.Editor = prefs.edit()
    private val barcodeLauncher =
        (context as? ComponentActivity)?.registerForActivityResult(ScanContract()) { result ->
            handleBarcodeContent(
                result,
                context
            )
        }

    companion object {
        const val SETTINGS_NAME = "mirror_settings"
        const val MODE_NAME = "mode"
        const val ENABLE_NAME = "mirror_enabled"
        const val TOPIC_NAME = "topic_name"
        const val ENCRYPTION_KEY_NAME = "encryption_key"
    }

    enum class DeviceMode(val value: Int) {
        SmsHost(1),
        Mirror(2);

        fun description(): String = when (this) {
            SmsHost -> "SMS Host"
            Mirror -> "Mirror"
        }

        companion object {
            fun fromInt(value: Int): DeviceMode = entries.find { it.value == value }
                ?: throw IllegalArgumentException("Invalid Mode value: $value")
        }
    }

    init {
        setupEnableMirrorSwitch()
        setupDeviceMode()
        setupTopic()
        setupGenerateTopic()
        setupCopyTopic()
        setupEncryptionKey()
        setupGenerateKey()
        setupCopyKey()
        setupShare()
        setupScan()
    }

    private fun setupEnableMirrorSwitch() = binding.apply {
        val isEnabled = prefs.getBoolean(ENABLE_NAME, false)
        mirrorSettingsEnable.isChecked = isEnabled
        setSettingsEnabled(isEnabled)

        mirrorSettingsEnableHolder.setOnClickListener {
            mirrorSettingsEnable.toggle()
            val isChecked = mirrorSettingsEnable.isChecked
            setSettingsEnabled(isChecked)
            editPrefs.putBoolean(ENABLE_NAME, isChecked).apply()
        }
    }

    private fun setupDeviceMode() = binding.apply {
        val currentMode = DeviceMode.fromInt(prefs.getInt(MODE_NAME, DeviceMode.SmsHost.value))
        mirrorSettingsMode.text = currentMode.description()

        mirrorSettingsModeHolder.setOnClickListener {
            val items = DeviceMode.entries.map { id -> RadioItem(id.value, id.description()) }.toArrayList()
            val currentMode = DeviceMode.fromInt(prefs.getInt(MODE_NAME, DeviceMode.SmsHost.value))
            RadioGroupDialog(context.getActivity(), items, currentMode.value) { selected ->
                val mode = DeviceMode.fromInt(selected as Int)
                mirrorSettingsMode.text = mode.description()
                editPrefs.putInt(MODE_NAME, selected).apply()
            }
        }
    }

    private fun setupTopic() = binding.apply {
        mirrorSettingsTopicEdittext.setText(prefs.getString(TOPIC_NAME, "") ?: "")

        mirrorSettingsTopicEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editPrefs.putString(TOPIC_NAME, s?.toString() ?: "").apply()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupGenerateTopic() = binding.apply {
        mirrorSettingsGenerateButton.setOnClickListener {
            val randomPassword = generateRandomPassword(16)
            mirrorSettingsTopicEdittext.setText(randomPassword)
        }
    }

    private fun setupCopyTopic() = binding.apply {
        mirrorSettingsCopyButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = mirrorSettingsTopicEdittext.text.toString()
            val clip = ClipData.newPlainText("Topic", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Topic copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupEncryptionKey() = binding.apply {
        mirrorSettingsKeyEdittext.setText(prefs.getString(ENCRYPTION_KEY_NAME, "") ?: "")

        mirrorSettingsKeyEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editPrefs.putString(ENCRYPTION_KEY_NAME, s?.toString() ?: "").apply()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupGenerateKey() = binding.apply {
        mirrorSettingsGenerateKeyButton.setOnClickListener {
            val keyBytes = CryptoHelper.generateAESKey()
            val keyBase64 = Base64.encodeToString(keyBytes.encoded, Base64.NO_WRAP)
            mirrorSettingsKeyEdittext.setText(keyBase64)
        }
    }

    private fun setupCopyKey() = binding.apply {
        mirrorSettingsCopyKeyButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = mirrorSettingsKeyEdittext.text.toString()
            val clip = ClipData.newPlainText("Key", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Encryption key copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupShare() = binding.apply {
        mirrorSettingsShareHolder.setOnClickListener {
            val topic = prefs.getString(TOPIC_NAME, "") ?: ""
            val key = prefs.getString(ENCRYPTION_KEY_NAME, "") ?: ""
            ShareMirrorSettingsDialog(context, "$topic;$key")
        }
    }

    private fun setupScan() = binding.apply {
        mirrorSettingsScanHolder.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan QR code to import settings")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setBarcodeImageEnabled(true)
            }
            barcodeLauncher?.launch(options)
        }
    }

    private fun handleBarcodeContent(
        result: ScanIntentResult?,
        context: ComponentActivity
    ) {
        val contents = result?.contents
        if (contents == null)
            return

        val topicAndKey = contents.split(';')
        if (topicAndKey.size != 2) {
            Toast.makeText(
                context,
                "Invalid QR code format. Please scan a valid mirror settings QR code.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        binding.mirrorSettingsTopicEdittext.setText(topicAndKey[0])
        editPrefs.putString(ENCRYPTION_KEY_NAME, topicAndKey[1]).apply()
    }

    private fun setSettingsEnabled(isEnabled: Boolean) = binding.apply {
        mirrorSettingsEnable.isChecked = isEnabled
        mirrorSettingsControlsHolder.isEnabled = isEnabled
        mirrorSettingsControlsHolder.isVisible = isEnabled
        mirrorSettingsNtfyWarning.isVisible = !context.isPackageInstalled(ForwardingSmsReceiver.NTFY_PACKAGE)
    }

    private fun generateRandomPassword(length: Int): String {
        val charset = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val secureRandom = SecureRandom()
        return (1..length)
            .map { charset[secureRandom.nextInt(charset.size)] }
            .joinToString("")
    }
}
