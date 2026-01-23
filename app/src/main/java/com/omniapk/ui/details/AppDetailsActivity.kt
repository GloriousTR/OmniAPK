package com.omniapk.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.omniapk.R // Ensure R is imported
import com.omniapk.databinding.ActivityAppDetailsBinding
import com.omniapk.data.model.AppInfo
import com.omniapk.utils.InstallMethod
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAppDetailsBinding
    private val viewModel: AppDetailsViewModel by viewModels()
    private var appName: String? = null
    private var packageName: String? = null
    
    // Mock data for demo purposes, in real app this comes from ViewModel/Repository
    private var screenshots = listOf(
        "https://via.placeholder.com/300x600.png?text=Screenshot+1", 
        "https://via.placeholder.com/300x600.png?text=Screenshot+2",
        "https://via.placeholder.com/300x600.png?text=Screenshot+3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        appName = intent.getStringExtra("appName")
        packageName = intent.getStringExtra("packageName")
        val versionName = intent.getStringExtra("versionName")
        val description = intent.getStringExtra("description")
        // In a real scenario, we'd fetch full details using packageName.
        // For now, populate with Intent data + Defaults.

        setupUI(appName, packageName, versionName, description)
        setupScreenshots()
        setupListeners()
        setupObservers()
    }

    private fun setupUI(name: String?, pkg: String?, version: String?, desc: String?) {
        binding.tvAppName.text = name ?: "Unknown App"
        binding.tvDeveloper.text = pkg ?: "" // Using package as developer for now
        binding.tvVersion.text = "v${version ?: "1.0"}"
        
        binding.tvDescription.text = desc ?: "No description available."
        
        binding.tvRating.text = "4.5 \u2605" // Mock
        binding.tvDownloads.text = "1M+" // Mock
        binding.tvSize.text = "45 MB" // Mock
        
        // Load Icon (Placeholder or if passed via intent logic, usually we need URL)
        binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon) 
    }

    private fun setupScreenshots() {
        val adapter = ScreenshotsAdapter(screenshots)
        binding.rvScreenshots.adapter = adapter
    }

    private fun setupListeners() {
        binding.tvReadMore.setOnClickListener {
            if (binding.tvDescription.maxLines == 4) {
                binding.tvDescription.maxLines = Int.MAX_VALUE
                binding.tvReadMore.text = "Show Less"
            } else {
                binding.tvDescription.maxLines = 4
                binding.tvReadMore.text = "Read More"
            }
        }

        binding.btnInstall.setOnClickListener {
            // Default action: Install standard
            showInstallMethodDialog()
        }

        binding.btnManualDownload.setOnClickListener { view ->
             showManualDownloadMenu(view)
        }
    }
    
    private fun showManualDownloadMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("APKMirror Versions")
        popup.menu.add("APKPure Versions")
        popup.menu.add("Play Store")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "APKMirror Versions" -> openBrowser("https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=${appName?.replace(" ", "+") ?: packageName}")
                "APKPure Versions" -> openBrowser("https://apkpure.com/search?q=$packageName")
                "Play Store" -> openBrowser("https://play.google.com/store/apps/details?id=$packageName")
            }
            true
        }
        popup.show()
    }
    
    private fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Browser not found", Toast.LENGTH_SHORT).show()
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
            binding.progressBar.isVisible = processing
        }
    }

    private fun showInstallMethodDialog() {
        val methods = arrayOf("Standard", "Root", "Shizuku", "Advanced (Split APK)")
        AlertDialog.Builder(this)
            .setTitle("Select Install Method")
            .setItems(methods) { _, which ->
                val method = when (which) {
                    0 -> InstallMethod.STANDARD
                    1 -> InstallMethod.ROOT
                    2 -> InstallMethod.SHIZUKU
                    3 -> { 
                        Toast.makeText(this, "Split APK Installer implementing...", Toast.LENGTH_SHORT).show()
                        return@setItems
                    } 
                    else -> InstallMethod.STANDARD
                }
                // Simulate install flow for now
                val fileName = "$appName.apk"
                viewModel.installApk(fileName, method) // This expects a file on disk, which we might not have yet in this UI flow without real download.
            }
            .show()
    }
}
