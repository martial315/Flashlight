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


//add comment to code if debugging
//private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var cameraM:CameraManager
    private lateinit var powerBtn:ImageButton
    private lateinit var flashlightIntensity:SeekBar
    private lateinit var cameraPower : TextView
    private lateinit var tvIntensityText : TextView
    private lateinit var tvWarn : TextView
    private var isFlash = false
    private var intensityPossible = false
    private var helpProgress : Int = 1
    private var helpDefaultLevel : Int = 0
    private var helpMaxLvl : Int = 0
    var cameraFlashId :String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**set view Id*/
        powerBtn = findViewById(R.id.powerBtn)
        cameraM = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraPower = findViewById(R.id.tvIntensityLevel)
        tvIntensityText = findViewById(R.id.tvIntensityText)
        flashlightIntensity = findViewById(R.id.tvLightIntensity)
        tvWarn = findViewById(R.id.tvWarning)
        tvWarn.isVisible = false

        cameraFlashId = findFlash()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intensityPossible = findIntensity(cameraFlashId)


            if (intensityPossible){
                val cameraC = cameraM.getCameraCharacteristics(cameraFlashId)
                val defaultLevel = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL)!!
                val maxlevel = cameraC.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
                cameraPower.text = defaultLevel.toString()
                cameraPower.isVisible = true
                flashlightIntensity.isVisible = true
                tvIntensityText.isVisible = true

                flashlightIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean
                    ) {
                        helpProgress = progress
                        helpDefaultLevel = defaultLevel
                        helpMaxLvl = maxlevel!!

                        if(isFlash) {
                            changeIntensity(seekBar, cameraFlashId)
                        }
                        lightWarn(helpProgress, helpDefaultLevel)
                        cameraPower.text = "$helpProgress"
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                })
            }
            else {
                hideAdjustments()
            }
        }
        else {
            hideAdjustments()
        }

        powerBtn.setOnClickListener { flashlightOnOrOff(it, cameraFlashId) }
    }

    private fun findFlash(): String {
        for(camera in cameraM.cameraIdList) {
            var cameraC = cameraM.getCameraCharacteristics(camera)
            var flash = cameraC.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            if (flash != null && flash)
                return camera
        }
        return "neg"
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun changeIntensity(v: View?, cameraFlashId: String) {
                cameraM.turnOnTorchWithStrengthLevel(cameraFlashId, helpProgress)
                lightWarn(helpProgress, helpDefaultLevel)
                cameraPower.text = "$helpProgress"
    }
    private fun lightWarn(progress: Int, defaultLevel: Int) {
        if (progress > defaultLevel) {
            tvWarn.isVisible = true
        }
        else {
            tvWarn.isVisible = false
        }
    }
    private fun hideAdjustments() {
        flashlightIntensity.isVisible = false
        cameraPower.isVisible = false
        tvIntensityText.isVisible = false
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun findIntensity(cameraFlashId : String): Boolean {
        val cameraC = cameraM.getCameraCharacteristics(cameraFlashId)
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
    private fun flashlightOnOrOff(v: View?, cameraFlashId: String) {
            try {
                if (!isFlash) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        cameraM.turnOnTorchWithStrengthLevel(cameraFlashId, helpProgress)
                        isFlash = true
                        powerBtn.setImageResource(R.drawable.power_on)
                        textMessage("Flashlight is On", this)
                    }
                    else {
                        cameraM.setTorchMode(cameraFlashId, true)
                        isFlash = true
                        powerBtn.setImageResource(R.drawable.power_on)
                        textMessage("Flashlight is On", this)
                    }
                } else {
                    cameraM.setTorchMode(cameraFlashId, false)
                    isFlash = false
                    powerBtn.setImageResource(R.drawable.power_off)
                    textMessage("Flashlight is Off", this)
                }
            } catch (e: IllegalArgumentException) {
                textMessage("No valid camera. Please see support", this)
                //Log.i(TAG, "Camera ID: $cameraFlashId")
            }
        }
    override fun onStop() {
        super.onStop()
        try {
            // true sets the torch in OFF mode
            val cameraListId = cameraFlashId
            cameraM.setTorchMode(cameraListId, false)
            isFlash = false
            powerBtn.setImageResource(R.drawable.power_off)
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