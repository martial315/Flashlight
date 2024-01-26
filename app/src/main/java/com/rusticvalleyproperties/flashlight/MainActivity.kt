package com.rusticvalleyproperties.flashlight

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {
    private lateinit var cameraM :CameraManager
    private lateinit var powerBtn:ImageButton
    var isFlash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**set view Id*/
        powerBtn = findViewById(R.id.powerBtn)
        cameraM = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        powerBtn.setOnClickListener { flashlightOnOrOff(it) }
    }


    private fun flashlightOnOrOff(v: View?) {
        try {
            if (!isFlash) {
                val cameraListId = cameraM.cameraIdList[0]
                cameraM.setTorchMode(cameraListId, true)
                isFlash = true
                powerBtn.setImageResource(R.drawable.power_on)
                textMessage("Flashlight is On", this)
            } else {
                val cameraListId = cameraM.cameraIdList[0]
                cameraM.setTorchMode(cameraListId, false)
                isFlash = false
                powerBtn.setImageResource(R.drawable.power_off)
                textMessage("Flashlight is Off", this)
            }
        }
        catch (e: IllegalArgumentException) {
            textMessage("No valid camera. Please see support", this)
        }
    }

    override fun finish() {
        super.finish()
        try {
            // true sets the torch in OFF mode
            val cameraListId = cameraM.cameraIdList[0]
            cameraM.setTorchMode(cameraListId, false)
            textMessage("Flashlight is Off", this)
        }
        catch (e: IllegalArgumentException) {
            textMessage("No valid camera. Please see support", this)
        }
    }

    private fun textMessage(s: String, c:Context) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show()
    }
}