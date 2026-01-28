/*
 * OmniAPK - F-Droid App Adapter
 * Copyright (C) 2024
 */

package com.aurora.store.view.ui.opensource

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.aurora.extensions.navigate
import com.aurora.store.R
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.data.providers.FDroidApp
import com.google.android.material.button.MaterialButton

class FDroidAppAdapter(
    private val onItemClick: ((FDroidApp) -> Unit)? = null
) : ListAdapter<FDroidApp, FDroidAppAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fdroid_app, parent, false)
        return AppViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder(
        itemView: View,
        private val onItemClick: ((FDroidApp) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.app_icon)
        private val nameText: TextView = itemView.findViewById(R.id.app_name)
        private val summaryText: TextView = itemView.findViewById(R.id.app_summary)
        private val versionText: TextView = itemView.findViewById(R.id.app_version)
        private val repoText: TextView = itemView.findViewById(R.id.app_repo)
        private val downloadBtn: MaterialButton = itemView.findViewById(R.id.btn_download)

        fun bind(app: FDroidApp) {
            nameText.text = app.name
            summaryText.text = app.summary
            versionText.text = app.versionName
            repoText.text = app.repoName

            // Load icon with Coil 3
            if (app.iconUrl.isNotEmpty()) {
                iconView.load(app.iconUrl) {
                    placeholder(R.drawable.ic_app_placeholder)
                    transformations(RoundedCornersTransformation(12f))
                }
            } else {
                iconView.setImageResource(R.drawable.ic_app_placeholder)
            }

            // Download button
            downloadBtn.setOnClickListener {
                if (app.downloadUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.downloadUrl))
                    itemView.context.startActivity(intent)
                }
            }

            // Item click - navigate to F-Droid app details screen
            itemView.setOnClickListener {
                if (onItemClick != null) {
                    onItemClick.invoke(app)
                } else {
                    // Fallback: Navigate to F-Droid app details screen
                    itemView.context.navigate(Screen.FDroidAppDetails(app.packageName))
                }
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<FDroidApp>() {
        override fun areItemsTheSame(oldItem: FDroidApp, newItem: FDroidApp): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: FDroidApp, newItem: FDroidApp): Boolean {
            return oldItem == newItem
        }
    }
}
