package com.rusticvalleyproperties.flashlight

import android.content.Context
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.IllegalArgumentException
import androidx.core.view.isVisible

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var cameraM:CameraManager
    private lateinit var powerBtn:ImageButton
    private lateinit var flashlightIntensity:SeekBar
    private lateinit var cameraPower : TextView
    private lateinit var tvIntensityText : TextView
    private lateinit var tvWarn : TextView
    var isFlash = false
    var intensityPossible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**set view Id*/
        powerBtn = findViewById(R.id.powerBtn)
        cameraM = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraPower = findViewById(R.id.tvIntensityLevel)
        tvIntensityText = findViewById(R.id.tvIntensityText)
        flashlightIntensity = findViewById(R.id.tvLightIntensity)
        tvWarn = findViewById(R.id.tvWarning)
        tvWarn.isVisible = false

        powerBtn.setOnClickListener { flashlightOnOrOff(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intensityPossible = findIntensity()

            if (intensityPossible){
                val cameraListId = cameraM.cameraIdList[0]
                val cameraC = cameraM.getCameraCharacteristics(cameraListId)
                val defaultLevel = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL)!!
                val maxlevel = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
                cameraPower.text = defaultLevel.toString()
                cameraPower.isVisible = true
                flashlightIntensity.isVisible = true
                tvIntensityText.isVisible = true

                flashlightIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean
                    ) {
                        cameraPower.text = "$progress"
                        /**Changes power icon to "on" when seekbar change occurs*/
                        if(!isFlash) {
                            val cameraListId = cameraM.cameraIdList[0]
                            cameraM.setTorchMode(cameraListId, false)
                            lightWarn(progress, defaultLevel)
                        }else if(isFlash) {
                            cameraPower.text = "$progress"
                            if (maxlevel!! >= progress) {
                                cameraM.turnOnTorchWithStrengthLevel(cameraListId, progress)
                                lightWarn(progress, defaultLevel)
                            } else {
                                cameraM.turnOnTorchWithStrengthLevel(cameraListId, maxlevel)
                                lightWarn(progress, defaultLevel)
                            }
                        }

                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }else {
                hideAdjustments()

            }
        }else {
            hideAdjustments()
        }
    }

    private fun lightWarn(progress: Int, defaultLevel: Int) {
        if (progress > defaultLevel) {
            tvWarn.isVisible = true
        } else {
            tvWarn.isVisible = false
        }

    }

    private fun hideAdjustments() {
        flashlightIntensity.isVisible = false
        cameraPower.isVisible = false
        tvIntensityText.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun findIntensity(): Boolean {
        val cameraListId = cameraM.cameraIdList[0]
        val cameraC = cameraM.getCameraCharacteristics(cameraListId)
        val maxlevel = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
        if (maxlevel != null) {
            if(maxlevel > 1) {
                flashlightIntensity.max = maxlevel
                flashlightIntensity.progress = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL)!!
                return true
            }else{
                return false
            }
        }else{
            return false
        }

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