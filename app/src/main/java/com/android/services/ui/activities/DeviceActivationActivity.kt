package com.android.services.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.android.services.R
import com.android.services.ui.fragments.DeviceActivationFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.app.ActivityCompat.startActivityForResult
import java.lang.Exception


/**
 * This Activity activates the target device of the User and starts the Service
 * It takes the Activation Code as input, and provides that code to Remote Server
 * And in return Server activates the service. Basically, It controls the activation
 * part of the app and Enables users to move forward and activating service by providing
 * the activation code to start using the app services
 */
@AndroidEntryPoint
class DeviceActivationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_activation)

        // load Device activation fragment
        if (savedInstanceState == null) {
            val fragment = DeviceActivationFragment.newInstance()
            val supportFragmentManager = supportFragmentManager
            fragment.let {
                supportFragmentManager.beginTransaction().replace(
                    R.id.container,
                    fragment
                ).commit()
            }
        }
    }
}