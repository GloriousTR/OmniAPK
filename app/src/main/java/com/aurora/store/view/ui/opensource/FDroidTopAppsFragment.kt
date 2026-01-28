/*
 * OmniAPK - F-Droid Top Apps Fragment
 * Shows top F-Droid apps from Room cache
 */

package com.aurora.store.view.ui.opensource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurora.store.R
import com.aurora.store.data.room.fdroid.FDroidAppDao
import com.aurora.store.data.room.fdroid.FDroidAppEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FDroidTopAppsFragment : Fragment() {

    @Inject
    lateinit var fdroidAppDao: FDroidAppDao

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressView: View
    private lateinit var emptyView: View
    private lateinit var emptyText: TextView
    private val adapter = FDroidAppAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fdroid_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        progressView = view.findViewById(R.id.progress_view)
        emptyView = view.findViewById(R.id.empty_view)
        emptyText = view.findViewById(R.id.empty_text)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadApps()
    }

    private fun loadApps() {
        progressView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apps = fdroidAppDao.getTopApps(50)
                updateUI(apps)
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun updateUI(apps: List<FDroidAppEntity>) {
        progressView.visibility = View.GONE
        if (apps.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyText.text = getString(R.string.fdroid_no_apps_synced)
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(apps.map { it.toFDroidApp() })
        }
    }
}

// Extension to convert Entity to FDroidApp
private fun FDroidAppEntity.toFDroidApp() = com.aurora.store.data.providers.FDroidApp(
    packageName = packageName,
    name = name,
    summary = summary,
    description = description,
    versionName = versionName,
    versionCode = versionCode,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    license = license,
    webSite = webSite,
    sourceCode = sourceCode,
    categories = categories,
    size = size,
    minSdkVersion = minSdkVersion,
    lastUpdated = lastUpdated,
    added = added,
    suggestedVersionCode = suggestedVersionCode,
    repoName = repoName,
    repoAddress = repoAddress
)
