package com.omniapk.ui.common

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omniapk.R
import com.omniapk.data.model.FeaturedSection
import com.omniapk.ui.details.AppDetailsActivity

/**
 * Adapter for displaying featured sections with horizontal app lists
 * Like Aurora Store "Senin için" tab
 */
class FeaturedSectionAdapter : RecyclerView.Adapter<FeaturedSectionAdapter.SectionViewHolder>() {
    
    private var sections: List<FeaturedSection> = emptyList()
    
    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSectionTitle)
        val ivArrow: ImageView = view.findViewById(R.id.ivArrow)
        val rvApps: RecyclerView = view.findViewById(R.id.rvApps)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_section, parent, false)
        return SectionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        
        holder.tvTitle.text = section.title
        
        // Setup horizontal RecyclerView for apps
        holder.rvApps.layoutManager = LinearLayoutManager(
            holder.itemView.context, 
            LinearLayoutManager.HORIZONTAL, 
            false
        )
        
        val appGridAdapter = AppGridAdapter { app ->
            val intent = Intent(holder.itemView.context, AppDetailsActivity::class.java).apply {
                putExtra("appName", app.name)
                putExtra("packageName", app.packageName)
                putExtra("versionName", app.versionName)
                putExtra("description", app.description)
                putExtra("source", app.source)
            }
            holder.itemView.context.startActivity(intent)
        }
        
        holder.rvApps.adapter = appGridAdapter
        appGridAdapter.submitList(section.apps)
        
        // Arrow click - show more
        holder.ivArrow.setOnClickListener {
            // TODO: Navigate to category details
        }
    }
    
    override fun getItemCount(): Int = sections.size
    
    fun submitList(newSections: List<FeaturedSection>) {
        sections = newSections
        notifyDataSetChanged()
    }
}
