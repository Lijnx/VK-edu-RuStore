package com.fthertz.sigmastore

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AppInfo(
    val id: Int,
    val app_name: String,
    val icon_url: String?,
    val icon_working: Boolean,
    val description: String,
    val category: String,
    val developer: String,
    val age_rating: String,
    val screenshots: List<String>,
    val is_real_apk: Boolean,
    val apk_file: String
)

object AppRepository {
    fun loadApps(context: Context): List<AppInfo> {
        val json = context.assets.open("AppsDataset.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<AppInfo>>() {}.type
        return Gson().fromJson(json, type)
    }
}