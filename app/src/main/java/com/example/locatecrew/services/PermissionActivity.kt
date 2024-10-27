package com.example.locatecrew.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            broadcastPermissionGranted()
        } else {
            broadcastPermissionDenied()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            broadcastPermissionGranted()
            finish()
        }
    }

    private fun broadcastPermissionGranted() {
        val intent = Intent(LocationService.ACTION_REQUEST_PERMISSION)
        intent.putExtra("isGranted", true)
        sendBroadcast(intent)
    }

    private fun broadcastPermissionDenied() {
        val intent = Intent(LocationService.ACTION_REQUEST_PERMISSION)
        intent.putExtra("isGranted", false)
        sendBroadcast(intent)
    }
}