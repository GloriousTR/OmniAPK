package com.omniapk.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omniapk.data.model.AppInfo
import com.omniapk.databinding.ItemAppBinding

class AppsAdapter(
    private val onItemClick: (AppInfo) -> Unit = {}
) : ListAdapter<AppInfo, AppsAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder(
        private val binding: ItemAppBinding,
        private val onItemClick: (AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo) {
            binding.tvAppName.text = appInfo.name
            binding.tvPackageName.text = appInfo.packageName
            binding.ivAppIcon.setImageDrawable(appInfo.icon) // Note: This might need Coil for URLs in search results
            
            itemView.setOnClickListener { onItemClick(appInfo) }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) = oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) = oldItem == newItem
    }
}
