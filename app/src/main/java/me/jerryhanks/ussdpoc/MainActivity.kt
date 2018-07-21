package me.jerryhanks.ussdpoc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            dialUSSD()
        }
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_CALL_PHONE)
    private fun dialUSSD() {
        val perms = arrayOf(Manifest.permission.CALL_PHONE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            val telephoneManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val USSDString = "*123${Uri.encode("#")}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephoneManager.sendUssdRequest(USSDString, object : TelephonyManager.UssdResponseCallback() {
                    override fun onReceiveUssdResponse(telephonyManager: TelephonyManager?,
                                                       request: String?, response: CharSequence?) {
                        Log.d(TAG, response.toString())

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

    companion object {
        const val RC_CALL_PHONE = 123
        const val TAG = "Main"
    }
}
