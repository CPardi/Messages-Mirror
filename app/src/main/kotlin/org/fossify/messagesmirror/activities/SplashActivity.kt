package org.fossify.messagesmirror.activities

import android.content.Intent
import org.fossify.commons.activities.BaseSplashActivity
import org.fossify.messages.activities.MainActivity

/**
 * Code in org.fossify.commons references classes using the applicationId (Defined in gradle). As the applicationId has been updated, but not the namespace, this
 * class cannot be found. Clone of existing activity in the expected namespace, so that the required fields can be found.
 *
 * @see org.fossify.messages.activities.SplashActivity
 */
class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
