package org.cpardi.messagemirror.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.dialogs.ShareMirrorSettingsDialog
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.helpers.MirrorConfig
import org.cpardi.messagemirror.models.DeviceMode
import org.fossify.commons.compose.extensions.getActivity
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isPackageInstalled
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.models.RadioItem
import org.fossify.messages.databinding.ViewMirrorSettingsBinding
import org.fossify.messages.extensions.toArrayList
import java.net.URI
import java.security.SecureRandom

class MirrorSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMirrorSettingsBinding.inflate(LayoutInflater.from(context), this)
    private val config: MirrorConfig = context.mirrorConfig
    private val barcodeLauncher = (context as? ComponentActivity)?.registerForActivityResult(ScanContract()) { result -> handleBarcodeContent(result, context) }

    init {
        (context as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onHostResume()
            }
        })
    }

    private fun onHostResume() {
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

        context.updateTextColors(binding.mirrorSettingsRoot)
        binding.mirrorSettingsSectionLabel.setTextColor(context.getProperPrimaryColor())

        arrayOf(
            binding.mirrorSettingsGenerateTopicButton,
            binding.mirrorSettingsCopyTopicButton,
            binding.mirrorSettingsGenerateKeyButton,
            binding.mirrorSettingsCopyKeyButton,
        ).forEach {
            val textColor = context.getProperTextColor()
            it.setTextColor(textColor)
            it.compoundDrawables.forEach { cd ->
                cd?.applyColorFilter(textColor)
            }
        }
    }

    private fun setupEnableMirrorSwitch() = binding.apply {
        val enabled = config.enabled
        mirrorSettingsEnable.isChecked = enabled
        setSettingsEnabled(enabled)

        mirrorSettingsEnableHolder.setOnClickListener {
            mirrorSettingsEnable.toggle()
            val isChecked = mirrorSettingsEnable.isChecked
            setSettingsEnabled(isChecked)
            config.enabled = isChecked
        }
    }

    private fun setupDeviceMode() = binding.apply {
        val currentMode = config.mode
        mirrorSettingsMode.text = currentMode.description()

        mirrorSettingsModeHolder.setOnClickListener {
            val items = DeviceMode.entries.map { id -> RadioItem(id.value, id.description()) }.toArrayList()
            val currentMode = config.mode
            RadioGroupDialog(context.getActivity(), items, currentMode.value) { selected ->
                val mode = DeviceMode.fromInt(selected as Int)
                mirrorSettingsMode.text = mode.description()
                config.mode = mode
            }
        }
    }

    private fun setupTopic() = binding.apply {
        mirrorSettingsTopicEdittext.setText(config.topicUrl.toString())

        mirrorSettingsTopicEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // This is intentionally empty
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                config.topicUrl = URI(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) { // This is intentionally empty
            }
        })
    }

    private fun setupGenerateTopic() = binding.apply {
        mirrorSettingsGenerateTopicButton.setOnClickListener {
            val randomPassword = generateRandomPassword(length = 16)
            mirrorSettingsTopicEdittext.setText(randomPassword)
        }
    }

    private fun setupCopyTopic() = binding.apply {
        mirrorSettingsCopyTopicButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = mirrorSettingsTopicEdittext.text.toString()
            val clip = ClipData.newPlainText("Topic", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Topic copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupEncryptionKey() = binding.apply {
        mirrorSettingsKeyEdittext.setText(config.encryptionKey)

        mirrorSettingsKeyEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // This is intentionally empty
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                config.encryptionKey = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) { // This is intentionally empty
            }
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
            ShareMirrorSettingsDialog(context, "${config.topicUrl};${config.encryptionKey}")
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
        binding.mirrorSettingsKeyEdittext.setText(topicAndKey[1])
    }

    private fun setSettingsEnabled(isEnabled: Boolean) = binding.apply {
        mirrorSettingsEnable.isChecked = isEnabled
        mirrorSettingsControlsHolder.isEnabled = isEnabled
        mirrorSettingsControlsHolder.isVisible = isEnabled
        mirrorSettingsNtfyWarning.isVisible = !context.isPackageInstalled(Constants.PACKAGE_NTFY)
    }

    private fun generateRandomPassword(length: Int): String {
        val charset = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val secureRandom = SecureRandom()
        return (1..length)
            .map { charset[secureRandom.nextInt(charset.size)] }
            .joinToString("")
    }
}
