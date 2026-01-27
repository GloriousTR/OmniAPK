/*
 * OmniAPK
 * Copyright (C) 2024
 *
 * Adapter for F-Droid repository list
 */

package com.aurora.store.view.ui.opensource

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aurora.store.R
import com.aurora.store.data.model.FDroidRepo

class RepoListAdapter : ListAdapter<FDroidRepo, RepoListAdapter.RepoViewHolder>(RepoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fdroid_repo, parent, false)
        return RepoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RepoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.repo_name)
        private val descText: TextView = itemView.findViewById(R.id.repo_description)
        private val urlText: TextView = itemView.findViewById(R.id.repo_url)
        private val icon: ImageView = itemView.findViewById(R.id.repo_icon)

        fun bind(repo: FDroidRepo) {
            nameText.text = repo.name
            descText.text = repo.description
            urlText.text = repo.address
            
            // Set icon based on repo type
            when {
                repo.id.contains("fdroid") -> icon.setImageResource(R.drawable.ic_open_source)
                repo.id.contains("guardian") -> icon.setImageResource(R.drawable.ic_shield)
                repo.id.contains("izzy") -> icon.setImageResource(R.drawable.ic_open_source)
                else -> icon.setImageResource(R.drawable.ic_open_source)
            }
        }
    }

    class RepoDiffCallback : DiffUtil.ItemCallback<FDroidRepo>() {
        override fun areItemsTheSame(oldItem: FDroidRepo, newItem: FDroidRepo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FDroidRepo, newItem: FDroidRepo): Boolean {
            return oldItem == newItem
        }
    }
}
