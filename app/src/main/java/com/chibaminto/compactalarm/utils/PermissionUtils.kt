package com.chibaminto.compactalarm.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.chibaminto.compactalarm.R

class PermissionUtils(private val activity: AppCompatActivity) {

    private var notificationPermissionRequested = false

    private val notificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (notificationPermissionRequested) {
            if (isGranted) {

            } else {
                showPermissionDialog()
            }
            notificationPermissionRequested = false
        }
    }

    fun checkAndRequestAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.alarm_and_reminder_setup))
                    .setMessage(activity.getString(R.string.permission_required_message))
                    .setPositiveButton(activity.getString(R.string.open_settings_button)) { dialog, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(activity.getString(R.string.cancel_button)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.permission_required_title))
            .setMessage(activity.getString(R.string.permission_required_message))
            .setPositiveButton(activity.getString(R.string.grant_button)) { dialog, which ->
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(R.string.cancel_button)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                if (!notificationPermissionRequested) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    notificationPermissionRequested = true
                }
            }
        }
    }
}