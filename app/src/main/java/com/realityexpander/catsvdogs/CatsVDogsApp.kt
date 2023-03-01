package com.realityexpander.catsvdogs

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CatsVDogsApp: Application() {

    companion object {
        lateinit var userId: String
    }

    fun getUserId(context: Context): String {
        return context.getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("userId", null)
            ?: run {
                val id = java.util.UUID.randomUUID().toString()
                getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .putString("userId", id)
                    .apply()
                id
            }
    }
}
