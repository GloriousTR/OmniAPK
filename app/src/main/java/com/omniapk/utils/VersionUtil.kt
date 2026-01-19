package com.omniapk.utils

object VersionUtil {
    /**
     * Compares two version strings.
     * Returns 1 if version1 > version2
     * Returns -1 if version1 < version2
     * Returns 0 if equal
     */
    fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
        
        val length = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until length) {
            val part1 = if (i < v1Parts.size) v1Parts[i] else 0
            val part2 = if (i < v2Parts.size) v2Parts[i] else 0
            
            if (part1 > part2) return 1
            if (part1 < part2) return -1
        }
        
        return 0
    }
}
