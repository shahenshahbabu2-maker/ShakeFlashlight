package com.example.shakeflashlight

import android.app.*
import android.content.*
import android.hardware.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.*
import kotlin.math.sqrt

class ShakeService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var flashOn = false
    private var lastShake = 0L
    private var lastToggle = 0L
    private var acceleration = 0f

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, "shake_flashlight")
            .setContentTitle("Shake Flashlight")
            .setContentText("Shake detection is active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
        startForeground(7, notification)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
            cameraManager?.getCameraCharacteristics(id)
                ?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x=event.values[0]; val y=event.values[1]; val z=event.values[2]
        val g = sqrt(x*x + y*y + z*z)
        acceleration = acceleration * 0.85f + g * 0.15f
        val delta = kotlin.math.abs(g - acceleration)
        val threshold = 7.0f
        val now = System.currentTimeMillis()

        if (delta > threshold && now - lastShake > 180) {
            if (lastShake > 0 && now - lastShake < 650 && now - lastToggle > 700) {
                toggleFlash()
                lastToggle = now
            }
            lastShake = now
        }
    }

    private fun toggleFlash() {
        try {
            cameraId?.let {
                flashOn = !flashOn
                cameraManager?.setTorchMode(it, flashOn)
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26)
                    v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                else @Suppress("DEPRECATION") v.vibrate(70)
            }
        } catch (_: Exception) {}
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        try { cameraId?.let { cameraManager?.setTorchMode(it, false) } } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "shake_flashlight", "Shake Flashlight",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
