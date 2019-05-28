package com.renchen.motrack_basic

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_operating.*

class OperatingActivity: Activity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer : Sensor ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    private fun requestPermission() {
        val fineLocationCheck : Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationCheck : Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorValues =
            "X-axis : " + event.values[0] + "\t" +
            "Y-axis : " + event.values[1] + "\t" +
            "Z-axis : " + event.values[2]

        if (event.sensor == mAccelerometer) {
            Acc_text.text = sensorValues
            positionTrack(event.values)
        }

    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this, mAccelerometer)
    }

    private fun positionTrack(value : FloatArray) {

        if (value[2] >= 10.5) {
            Acc_text.text = "GO GO GO"
        }

    }

}
