package com.renchen.motrack_basic

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_start.*

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