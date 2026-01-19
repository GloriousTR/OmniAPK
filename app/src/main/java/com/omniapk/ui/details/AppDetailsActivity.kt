package com.omniapk.ui.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.omniapk.databinding.ActivityAppDetailsBinding
import com.omniapk.utils.InstallMethod
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAppDetailsBinding
    private val viewModel: AppDetailsViewModel by viewModels()
    private var appName: String? = null
    private var packageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appName = intent.getStringExtra("appName")
        packageName = intent.getStringExtra("packageName")
        val versionName = intent.getStringExtra("versionName")

        binding.tvAppName.text = appName
        binding.tvPackageName.text = packageName
        binding.tvVersion.text = "Version: $versionName"

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnInstall.setOnClickListener {
            // For now, we simulate a download URL or use a placeholder because scraping is hard to get right blindly.
            // In a real scenario, we would have fetched the details.
            // Let's assume we are downloading a test APK for demonstration if URL is null
            val testUrl = "https://example.com/test.apk" 
            val fileName = "$appName.apk"
            
            // Trigger download flow
            viewModel.unkownAppDownloadLogic(testUrl, fileName)
            
            // Note: Since we don't have a real download monitor yet, we'll simulate the "Ready to Install" state after a delay or success
            // But wait, the ViewModel manages status. 
        }
    }

    private fun setupObservers() {
        viewModel.status.observe(this) { status ->
            binding.tvStatus.text = status
            
            if (status.contains("Download started")) {
                 // In a real app, we'd wait for completion.
                 // Here, let's enable a "Finish Install" or "Choose Method" dialog for manual triggering 
                 // or simulate completion.
                 showInstallMethodDialog()
            }
        }

        viewModel.isProcessing.observe(this) { processing ->
            binding.progressBar.visibility = if (processing) View.VISIBLE else View.GONE
            binding.btnInstall.isEnabled = !processing
        }
    }

    private fun showInstallMethodDialog() {
        val methods = arrayOf("Standard", "Root", "Shizuku")
        AlertDialog.Builder(this)
            .setTitle("Choose Install Method")
            .setItems(methods) { _, which ->
                val method = when (which) {
                    0 -> InstallMethod.STANDARD
                    1 -> InstallMethod.ROOT
                    2 -> InstallMethod.SHIZUKU
                    else -> InstallMethod.STANDARD
                }
                val fileName = "$appName.apk"
                viewModel.installApk(fileName, method)
            }
            .show()
    }
}
