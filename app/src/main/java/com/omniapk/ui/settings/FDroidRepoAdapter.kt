package com.omniapk.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omniapk.R
import com.omniapk.data.model.FDroidRepo

class FDroidRepoAdapter(
    private var repos: List<FDroidRepo>,
    private val onRepoToggle: (FDroidRepo, Boolean) -> Unit
) : RecyclerView.Adapter<FDroidRepoAdapter.RepoViewHolder>() {

    class RepoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRepoName)
        val tvDescription: TextView = view.findViewById(R.id.tvRepoDescription)
        val switchEnabled: Switch = view.findViewById(R.id.switchRepoEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fdroid_repo, parent, false)
        return RepoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repos[position]
        holder.tvName.text = repo.name
        holder.tvDescription.text = repo.description
        holder.switchEnabled.isChecked = repo.enabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onRepoToggle(repo, isChecked)
        }
    }

    override fun getItemCount(): Int = repos.size

    fun updateRepos(newRepos: List<FDroidRepo>) {
        repos = newRepos
        notifyDataSetChanged()
    }
}
