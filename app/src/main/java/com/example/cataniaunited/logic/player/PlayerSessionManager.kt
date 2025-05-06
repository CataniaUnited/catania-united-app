package com.example.cataniaunited.logic.player

import android.content.Context
import android.util.Log
import com.example.cataniaunited.MainApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayerSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getPlayerId(): String {
        return try {
            (context.applicationContext as MainApplication).getPlayerId()
        } catch (e: Exception) {
            Log.e("GameBoardLogic", "PlayerID Error", e);
            throw IllegalStateException("No player Id set")
        }
    }
}