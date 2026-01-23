package com.omniapk.utils.install

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import android.widget.Toast

class SplitInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Install Successful!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e("SplitInstall", "Install failed: $status, $message")
                Toast.makeText(context, "Install Failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }
}
