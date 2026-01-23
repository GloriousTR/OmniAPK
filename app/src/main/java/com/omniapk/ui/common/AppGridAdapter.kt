package com.omniapk.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omniapk.R
import com.omniapk.data.model.AppInfo

/**
 * Adapter for horizontal app grid (64dp icons)
 * Used in featured sections like Aurora Store
 */
class AppGridAdapter(
    private val onAppClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppGridAdapter.AppGridViewHolder>(AppDiffCallback()) {
    
    class AppGridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val tvDownloads: TextView = view.findViewById(R.id.tvDownloads)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppGridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_grid, parent, false)
        return AppGridViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AppGridViewHolder, position: Int) {
        val app = getItem(position)
        
        holder.tvName.text = app.name
        holder.tvDownloads.text = getRandomDownloadCount()
        
        // Load icon
        if (app.icon != null) {
            holder.ivIcon.setImageDrawable(app.icon)
        } else if (!app.iconUrl.isNullOrEmpty()) {
            holder.ivIcon.load(app.iconUrl) {
                placeholder(R.drawable.ic_app_placeholder)
                error(R.drawable.ic_app_placeholder)
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_app_placeholder)
        }
        
        holder.itemView.setOnClickListener { onAppClick(app) }
    }
    
    private fun getRandomDownloadCount(): String {
        val counts = listOf("1 Mn+", "5 Mn+", "10 Mn+", "50 Mn+", "100 Mn+", "500 Mn+", "1 Mr+")
        return counts.random()
    }
    
    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.packageName == newItem.packageName
            
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem == newItem
    }
}
