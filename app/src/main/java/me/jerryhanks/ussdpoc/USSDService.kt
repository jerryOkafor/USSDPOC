package me.jerryhanks.ussdpoc

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import java.util.*


class USSDService : AccessibilityService() {

    private var overlay: View? = null
    private lateinit var windowManager: WindowManager
    private var overlayDrawn: Boolean = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "Event: $event")

        val info = event?.source


        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                !event.packageName.contains("com.android.phone")) {
            return
        }

        val className = event?.className
        if (className != null && className.contains("ProgressDialog")) {
            Log.d(TAG, "Has Progressbar")

            val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    },
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.OPAQUE)

            params.gravity = Gravity.CENTER
            overlay = LayoutInflater.from(this).inflate(R.layout.overlay, null)
            windowManager.addView(overlay, params)
            overlayDrawn = true
        }

        //check if the second event is fired.
        if (className != null && className.contains("AlertDialog")) {
            if (overlayDrawn) {
                windowManager.removeView(overlay)
                overlayDrawn = false
            }
        }

        val eventText = when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                event.text
            }
            else -> {
                Collections.singletonList(info?.text)
            }
        }

        formatAndLogUSSDResponse(eventText)

        //close dialog by performing global actions
        performGlobalAction(GLOBAL_ACTION_BACK)


    }

    private fun formatAndLogUSSDResponse(eventText: List<CharSequence>) {
        eventText.forEachIndexed { index, charSequence ->
            if (index == 0) {
                if (charSequence.isNullOrBlank()) return@forEachIndexed
                val resp = charSequence.split(";")
                resp.forEachIndexed { innerIndex, s ->
                    Log.d(TAG, "        Index $innerIndex : $s")
                }
            } else
                Log.d(TAG, "Index $index : $charSequence")
        }
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

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlay)

    }

    companion object {
        const val TAG = "USSDService"
    }
}


//            rootInActiveWindow.allChildren.forEach {
//                Log.d(TAG, "NodeInfo: $it | Text: ${it.text} | Is Dismissible: ${it.isDismissable}")
//                if (it.className.contains("ProgressBar") || it.className.contains("MMIDialogActivity")) {
//                    Log.d(TAG, "ProgressDialogBounds")
//                    val itToString = "ToString: $it"
//                    Log.d(TAG, itToString)
//
//                    val pattern = Pattern.compile(".*boundsInScreen:\\s+\\w+\\((\\d+),\\s+(\\d+)\\s+-\\s+(\\d+),\\s+(\\d+)\\).*")
//                    val matcher = pattern.matcher(itToString)
//                    if (matcher.matches()) {
//                        val result = matcher.toMatchResult()
//
//                        val rect = Rect()
//                        for (i in 1..result.groupCount()) { //us 1 to count to ignore the outer group
//                            Log.d(TAG, "          $i : ${result.group(i)}")
//                            when (i) {
//                                1 -> {
//                                    rect.left = result.group(i).toInt()
//                                }
//                                2 -> {
//                                    rect.top = result.group(i).toInt()
//                                }
//                                3 -> {
//                                    rect.right = result.group(i).toInt()
//                                }
//                                4 -> {
//                                    rect.bottom = result.group(i).toInt()
//                                }
//
//                            }
//                        }
//
//                        Log.d(TAG, "Rect: $rect")
//                        Log.d(TAG, "Center X: ${rect.centerX()} | Center Y: ${rect.centerY()}")
//                        params.x = rect.centerX()
//                        params.y = rect.centerY()
//                        params.gravity = Gravity.CENTER
//
//
//                        overlay = LayoutInflater.from(this).inflate(R.layout.overlay, null)
//                        windowManager.addView(overlay, params)
//                        overlayDraw = true
//
//                    }
//                }
//
//
//            }