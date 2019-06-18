package com.renchen.motrack_basic

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.opengl.ETC1.getHeight
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Half.EPSILON
import android.util.Half.toFloat
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_operating.*
import kotlinx.android.synthetic.main.activity_start.*
import java.lang.Math.pow
import java.lang.Math.toDegrees
import java.lang.ref.WeakReference
import java.security.KeyStore
import java.util.*
import javax.xml.xpath.XPathVariableResolver
import kotlin.concurrent.schedule
import kotlin.math.*

/*
This class is an activity while operating position tracking.

1. the magnetometer and accelerometer
*/

class OperatingActivity: Activity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)    // Use to catch the x, y, and z acceleration value
    private val magnetometerReading = FloatArray(3)     // Use to catch the x, y, and z magnetic filed value
    private val gyroscopeReading = FloatArray(3)        // Use to catch the x, y, and z gyroscope value

    private val rotationMatrix = FloatArray(9)          // Rotation matrix based on current readings from accelerometer and magnetometer
    private val orientationAngles = FloatArray(3)       /* Express the updated rotation matrix as three orientation angles
                                                                Azimuth(0) -> degrees of rotation about -z axis
                                                                              (0-N, 90-E, 180-S, 270-W)
                                                                Pitch(1)   -> degrees of rotation about x axis
                                                                Roll(2)    -> degrees of rotation about y axis
                                                             */

    private var startCheck: Boolean = true                   // Use to check weather the button is first click or not
    private var startTime:Long = System.currentTimeMillis()  // T.0
    private var startTheta = -PI/2                           // theta.0
    private var startAzimuth = 0.0f
    private val stepLength = 0.8f
    private var stepCheck:Boolean = true

    private final val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4)
    private var timestamp: Long = 0

    /*
        Under parameter is for draw picture
    */
    private var height: Int = 0
    private var width: Int = 0
    private var bitmap: Bitmap
    private var canvas: Canvas
    private var startX: Float
    private var startY: Float
    private var paint: Paint = Paint()
    private var paintPoint = Paint()

    init {
        height = Resources.getSystem().displayMetrics.heightPixels
        width = Resources.getSystem().displayMetrics.widthPixels

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        paint.setColor(Color.YELLOW)
        paint.strokeWidth = 3f

        paintPoint.setColor(Color.RED)
        paintPoint.strokeWidth = 15f

        startX = width/(2f)
        startY = height/(2f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operating)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Start position tracking while first click @startAndEnd_button button
        startAndEnd_button.setOnClickListener {
            if (startCheck) {
                startAndEnd_button.text = "End"
                startCheck = false
                startTime = System.currentTimeMillis()    // Assign T.0
                startAzimuth = (toDegrees(orientationAngles[0].toDouble()) + 360).toFloat() % 360
            } else {
                startCheck = true
                startAndEnd_button.isClickable = false
                startAndEnd_button.visibility = View.GONE
            }
        }

        canvas.drawPoint(startX, startY, paint)
        // set bitmap as background to ImageView
        view_image.background = BitmapDrawable(getResources(), bitmap)
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        // Get readings from accelerometer and magnetometer.
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
        }
        // Update device's orientation
        updateOrientationAngles()

        if (!startCheck) {
            if (timestamp != 0L) {
                var dt = (event.timestamp - timestamp) * NS2S

                startTheta -= gyroscopeReading[2] * dt           // theta = theta.0 + wt     // rad

                timestamp = event.timestamp

                if (accelerometerReading[2] >= (9.8 + 3.0) && stepCheck) {
                    stepCheck = false

                    var x = startX + stepLength * cos(startTheta) * 50  //1 pixel = 0.02 m
                    var y = startY + stepLength * sin(startTheta) * 50

                    canvas.drawLine(startX, startY, x.toFloat(), y.toFloat(), paint)
                    canvas.drawPoint(x.toFloat(), y.toFloat(), paintPoint)
                    // set bitmap as background to ImageView
                    view_image.background = BitmapDrawable(getResources(), bitmap)

                    startX = x.toFloat()
                    startY = y.toFloat()

                    Handler().postDelayed({ stepCheck = true }, 500)
                }

                Log.d("dt.", dt.toString())
                Log.d("theta", startTheta.toString())

            }
            timestamp = event.timestamp

            Log.d("gyro.", gyroscopeReading[2].toString())
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
        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
                gyroscope -> mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this)
    }

}
