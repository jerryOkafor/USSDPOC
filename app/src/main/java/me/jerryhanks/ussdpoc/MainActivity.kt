package me.jerryhanks.ussdpoc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    private var USSDString = "*123${Uri.encode("#")}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnCallUSDD.setOnClickListener {
            val ussdString = edUSSD.text.toString()
            if (ussdString.isBlank()) {
                edUSSDTextInputLayout.error = "Missing USSD string"
                USSDString = Uri.parse(ussdString).toString()
            } else {
                edUSSDTextInputLayout.error = ""
            }

            dialUSSD()
        }

//        val USSDString = "*123${Uri.encode("#")}"
//        val send = Intent("android.intent.action.ussd.SEND", Uri.parse("tel:$USSDString"))
//        startService(send)
//
//
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action.equals("android.intent.action.ussd.RECEIVE")) {
//                    val text = intent?.getStringExtra(Intent.EXTRA_TEXT)
//                    // blah blah blah...
//                    Log.d(TAG, "Response: $text")
//                }
//            }
//
//        }
//
//        val filter = android.content.IntentFilter()
//        filter.addAction("android.intent.action.ussd.RECEIVE")
//        this.registerReceiver(receiver, filter)
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_CALL_PHONE)
    private fun dialUSSD() {
        Snackbar.make(currentFocus, "Starting Dialer", Snackbar.LENGTH_LONG).show()
        val perms = arrayOf(Manifest.permission.CALL_PHONE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            val telephoneManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Running Android 8")
                Toast.makeText(this@MainActivity, "Running Android 8", Toast.LENGTH_LONG).show()
                telephoneManager.sendUssdRequest(USSDString, object : TelephonyManager.UssdResponseCallback() {
                    override fun onReceiveUssdResponse(telephonyManager: TelephonyManager?,
                                                       request: String?, response: CharSequence?) {
                        Log.d(TAG, "Response: ${response.toString()}")
                        Toast.makeText(this@MainActivity, response.toString(), Toast.LENGTH_LONG).show()

                    }

                    override fun onReceiveUssdResponseFailed(telephonyManager: TelephonyManager?,
                                                             request: String?, failureCode: Int) {
                        Log.d(TAG, "Request Failed: $failureCode")

                    }
                }, Handler())
            } else {
                //use intent for the mean time.
                val USSDIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$USSDString"))
                startActivity(USSDIntent)
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.call_phone_rational),
                    RC_CALL_PHONE, *perms)
        }


    }

    override fun onStart() {
        super.onStart()
        if (ensureAccessibility())
            ensureCanDrawOverlay()

    }

    private fun ensureCanDrawOverlay(): Boolean {
        val uri = Uri.parse("package:$packageName")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //the draw overlay permission is not available, open the
            //settings screen the permissions
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, RC_DRAW_OVER_OTHER_APPS)
            } else {
                Log.w(TAG, "No matching activity found!")
            }
            Log.d(TAG, "Can draw over other Apps: false")
            return false
        }

        Log.d(TAG, "Can draw over other Apps: true")
        return true
    }

    private fun ensureAccessibility(): Boolean {
        val isAccessibilityEnabled = Utils.isAccessibilityServiceEnabled(this, USSDService::class)
        return if (!isAccessibilityEnabled) {
            //show thw setting activity
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, RC_CHECK_ACCESSIBILITY)
            false
        } else {
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_CHECK_ACCESSIBILITY -> {
                if (ensureAccessibility())
                    ensureCanDrawOverlay()

            }
            RC_DRAW_OVER_OTHER_APPS -> {
                ensureCanDrawOverlay()
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    companion object {
        const val RC_CALL_PHONE = 123
        const val RC_CHECK_ACCESSIBILITY = 1234
        const val RC_DRAW_OVER_OTHER_APPS = 132
        const val TAG = "Main"
    }
}
