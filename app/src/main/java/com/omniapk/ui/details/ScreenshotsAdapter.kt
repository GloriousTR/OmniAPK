package com.omniapk.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omniapk.databinding.ItemScreenshotBinding

class ScreenshotsAdapter(private val screenshots: List<String>) :
    RecyclerView.Adapter<ScreenshotsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemScreenshotBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScreenshotBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val url = screenshots[position]
        viewHolder.binding.ivScreenshot.load(url) {
            crossfade(true)
        }
    }

    override fun getItemCount() = screenshots.size
}
