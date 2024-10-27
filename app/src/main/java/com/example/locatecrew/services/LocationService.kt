package com.example.locatecrew.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.locatecrew.R
import com.example.locatecrew.data.firebase.FirebaseRepository
import com.example.locatecrew.data.store.LoggedInUserManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FOREGROUND_SERVICE_TYPE_LOCATION = 2

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "onCreate() called")
        createNotificationChannel()
        startForeground()
        initLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand() called")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startForeground() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.cloneFilter()
            val notificationId = System.currentTimeMillis().toInt()
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.location_service_running))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
                .build()

            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        }
    }

    private fun initLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                println()
                println()
                println("IIIIIIII AMMMMMMMMM HHHHHEEEEEEEEEEERRRRRRRRRRRRRRRRRRREEEEEEEE")
                println()
                println()
                println()
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        serviceScope.launch {
                            val loggedInUser = LoggedInUserManager.getLoggedInUsername()
                            if (loggedInUser != null) {
                                firebaseRepository.updateUserLocation(loggedInUser, location.latitude, location.longitude)
                            }
                        }
                    }
                }
            }
        }

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            val permissionIntent = Intent(this, PermissionActivity::class.java)
            permissionIntent.action = ACTION_REQUEST_PERMISSION
            permissionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(permissionIntent)
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val CHANNEL_ID = "LocationServiceChannel"
        const val ACTION_REQUEST_PERMISSION = "com.example.locatecrew.REQUEST_PERMISSION"
    }
}