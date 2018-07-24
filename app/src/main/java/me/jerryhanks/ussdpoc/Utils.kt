package me.jerryhanks.ussdpoc

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import kotlin.reflect.KClass


/**
 * @author Po10cio on 21/07/2018.
 *
 **/

object Utils {
    @JvmStatic
    fun isAccessibilityServiceEnabled(context: Context, accessibilityClass: KClass<*>): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityClass.java)

        val enabledServiceSettings = Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false

        val colonSplitter = enabledServiceSettings.split(":").listIterator()

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)

            if (enabledService != null && enabledService == expectedComponentName)
                return true
        }

        return false

    }
}