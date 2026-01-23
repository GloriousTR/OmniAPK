package com.omniapk.data.model

/**
 * Featured section with horizontal app list
 * Used for "Senin için" tab
 */
data class FeaturedSection(
    val title: String,
    val apps: List<AppInfo>,
    val showMoreLink: String? = null
)

/**
 * Top chart with filter options
 * Used for "Üst sıralar" tab
 */
data class TopChart(
    val filter: String,
    val apps: List<AppInfo>
)
