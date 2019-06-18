package com.renchen.motrack_basic

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.CalendarContract
import kotlinx.android.synthetic.main.activity_start.*
import kotlin.math.cos
import kotlin.math.sin

class StartActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        nextActivity_button.setOnClickListener {
            val operatingIntent = Intent(this, OperatingActivity::class.java)
            startActivity(operatingIntent)
        }

    }

}