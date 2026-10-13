package com.example.shakeflashlight

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var toggle: Switch
    private lateinit var sensitivity: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 60, 36, 36)
            setBackgroundColor(android.graphics.Color.rgb(8,11,18))
        }

        val title = TextView(this).apply {
            text = "🔦  Shake Flashlight"
            textSize = 30f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0,0,0,16)
        }
        val subtitle = TextView(this).apply {
            text = "Shake your phone to turn the flashlight ON/OFF"
            textSize = 16f
            setTextColor(android.graphics.Color.LTGRAY)
            setPadding(0,0,0,35)
        }

        toggle = Switch(this).apply {
            text = "Shake Detection"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            isChecked = getPreferences(0).getBoolean("enabled", true)
        }

        val label = TextView(this).apply {
            text = "Shake sensitivity"
            textSize = 17f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0,35,0,5)
        }

        sensitivity = SeekBar(this).apply {
            max = 100
            progress = getPreferences(0).getInt("sensitivity", 65)
        }

        val info = TextView(this).apply {
            text = "\nTip: A quick double shake works best. Keep the phone away from accidental movement.\n\nThe service can continue detecting shakes while the app is not on screen."
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(toggle)
        layout.addView(label)
        layout.addView(sensitivity)
        layout.addView(info)
        setContentView(layout)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        toggle.setOnCheckedChangeListener { _, checked ->
            getPreferences(0).edit().putBoolean("enabled", checked).apply()
            if (checked) startShakeService() else stopService(Intent(this, ShakeService::class.java))
        }

        sensitivity.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                getPreferences(0).edit().putInt("sensitivity", p).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        if (toggle.isChecked) startShakeService()
    }

    private fun startShakeService() {
        ContextCompat.startForegroundService(this, Intent(this, ShakeService::class.java))
    }
}
