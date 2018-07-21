package me.jerryhanks.ussdpoc

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.util.*


class USSDService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "Event: $event")

        val info = event?.source

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.className.contains("AlertDialog"))
            return
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && info?.className == "android.widget.TextView")
            return
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && info?.text.isNullOrBlank())
            return

        val eventText = when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                event.text
            }
            else -> {
                Collections.singletonList(info?.text)
            }
        }
        val text = processUSSDText(eventText)

        if (text.isNullOrBlank()) return

        //close dialog by performing global actions
        performGlobalAction(GLOBAL_ACTION_BACK)


        //handle USSD response here.
        Log.d(TAG, "USSD Response: $text")
    }


    override fun onInterrupt() {
        Log.d(TAG, "USSD interrupted")

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "On Accessibility Service connected")
        val info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.android.phone")
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    private fun processUSSDText(eventText: List<CharSequence>): String? {
        for (s in eventText) {
            val text = s.toString()
            // Return text if text is the expected ussd response
            if (true) {
                return text
            }
        }
        return null
    }

    companion object {
        const val TAG = "USSDService"
    }
}
