package com.omniapk.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appName = intent.getStringExtra("appName")
        packageName = intent.getStringExtra("packageName")
        val versionName = intent.getStringExtra("versionName")
        val description = intent.getStringExtra("description")
        source = intent.getStringExtra("source")

        binding.tvAppName.text = appName
        binding.tvPackageName.text = packageName
        binding.tvVersion.text = "Versiyon: $versionName"
        
        if (!description.isNullOrEmpty()) {
            binding.tvDescription.text = description
            binding.tvDescription.visibility = View.VISIBLE
        }

        setupDownloadButtons()
        setupObservers()
    }

    private fun setupDownloadButtons() {
        // Show/hide buttons based on source
        when (source) {
            "F-Droid" -> {
                binding.btnDownloadFDroid.visibility = View.VISIBLE
                binding.btnDownloadApkMirror.visibility = View.GONE
                binding.btnDownloadApkPure.visibility = View.GONE
            }
            else -> {
                binding.btnDownloadFDroid.visibility = View.GONE
                binding.btnDownloadApkMirror.visibility = View.VISIBLE
                binding.btnDownloadApkPure.visibility = View.VISIBLE
            }
        }
        
        // APKMirror button
        binding.btnDownloadApkMirror.setOnClickListener {
            val searchQuery = appName?.replace(" ", "+") ?: packageName
            val url = "https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=$searchQuery"
            openBrowser(url)
        }
        
        // APKPure button
        binding.btnDownloadApkPure.setOnClickListener {
            val url = "https://apkpure.com/search?q=$packageName"
            openBrowser(url)
        }
        
        // F-Droid button
        binding.btnDownloadFDroid.setOnClickListener {
            val url = "https://f-droid.org/en/packages/$packageName/"
            openBrowser(url)
        }
    }
    
    private fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tarayıcı açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.status.observe(this) { status ->
            binding.tvStatus.text = status
            
            if (status.contains("Download started")) {
                 showInstallMethodDialog()
            }
        }

        viewModel.isProcessing.observe(this) { processing ->
            binding.progressBar.visibility = if (processing) View.VISIBLE else View.GONE
        }
    }

    private fun showInstallMethodDialog() {
        val methods = arrayOf("Standard", "Root", "Shizuku")
        AlertDialog.Builder(this)
            .setTitle("Kurulum Yöntemi Seçin")
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
