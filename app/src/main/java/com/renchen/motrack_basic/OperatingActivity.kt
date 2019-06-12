package com.renchen.motrack_basic

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_operating.*
import java.security.KeyStore
import java.util.*
import kotlin.concurrent.schedule

/*
This class is an activity while operating position tracking.

1. the magnetometer and accelerometer
*/

class OperatingActivity: Activity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)    // Use to catch the x, y, and z acceleration value
    private val magnetometerReading = FloatArray(3)     // Use to catch the x, y, and z magnetic filed value

    private val rotationMatrix = FloatArray(9)          // Rotation matrix based on current readings from accelerometer and magnetometer
    private val orientationAngles = FloatArray(3)       /* Express the updated rotation matrix as three orientation angles
                                                                Azimuth(0) -> degrees of rotation about -z axis
                                                                              (0-N, 90-E, 180-S, 270-W)
                                                                Pitch(1)   -> degrees of rotation about x axis
                                                                Roll(2)    -> degrees of rotation about y axis
                                                             */

    private var startTime:Long = System.currentTimeMillis()
    private var startCheck: Boolean = true                   // Use to check weather the button is first click or not

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Start position tracking while first click @startAndEnd_button button
        startAndEnd_button.setOnClickListener {
            if (startCheck) {
                startAndEnd_button.text = "End"
                startCheck = false
                startTime = System.currentTimeMillis()    // Use to calculate the time difference during tacking
            } else {
                startCheck = true
                startAndEnd_button.isClickable = false
                test_Text.text = "DONE"
            }
        }
    }

    private fun requestPermission() {
        val fineLocationCheck : Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationCheck : Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        // Get readings from accelerometer and magnetometer.
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            if(!startCheck) {
                startTracking(startTime)
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // Update device's orientation
        updateOrientationAngles()
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    private fun startTracking(startTime: Long) {
            if(accelerometerReading[2] >= (9.8 + 0.6)) {
                var timeDifference: Long = System.currentTimeMillis() - startTime
                var displacement: Float = accelerometerReading[0] * timeDifference
                test_Text.text = "WOW"
                Timer().schedule(1000) {

                }
            } else {
                test_Text.text = "OWO"
            }
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            accelerometer -> mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            magneticField -> mSensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this)
    }


}
