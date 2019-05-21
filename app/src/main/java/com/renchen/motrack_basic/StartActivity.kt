package com.renchen.motrack_basic

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        nextActivity_button.setOnClickListener {
            val operatingIntent = Intent(this, OperatingActivity::class.java)
            startActivity(operatingIntent)
        }

    }

}