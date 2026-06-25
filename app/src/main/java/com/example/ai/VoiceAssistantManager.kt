package com.example.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceAssistantManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "VoiceAssistant"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onInitCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language US is not supported or missing data")
            } else {
                isInitialized = true
                Log.d(TAG, "TextToSpeech successfully initialized")
                onInitCallback?.invoke()
            }
        } else {
            Log.e(TAG, "Initialization of TextToSpeech failed")
        }
    }

    fun setOnInitCallback(callback: () -> Unit) {
        onInitCallback = callback
        if (isInitialized) {
            callback()
        }
    }

    fun speak(text: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        if (isInitialized && tts != null) {
            onStart()
            // Set listener for callbacks if supported (simplified standard flow)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId")
            
            // To ensure UI reflects completed speech even if tts listener fails,
            // we simulate speech completion after a duration proportional to text length
            val durationMs = (text.split(" ").size * 450L).coerceAtLeast(1500L)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onDone()
            }, durationMs)
        } else {
            Log.w(TAG, "TTS not initialized yet. Queueing fallback...")
            onDone()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    /**
     * Parses custom delivery commands spoken by the driver and returns the corresponding response.
     */
    fun processVoiceCommand(command: String, remainingParcels: Int, nextCustomerName: String, nextAddress: String): CommandResult {
        val cleanCmd = command.lowercase(Locale.ROOT).trim()
        
        return when {
            cleanCmd.contains("next parcel") || cleanCmd.contains("show next") -> {
                CommandResult(
                    responseText = "The next parcel is for $nextCustomerName at $nextAddress. Navigate to begin.",
                    actionType = ActionType.SHOW_NEXT
                )
            }
            cleanCmd.contains("call") || cleanCmd.contains("phone") -> {
                CommandResult(
                    responseText = "Calling customer $nextCustomerName now.",
                    actionType = ActionType.CALL_CUSTOMER
                )
            }
            cleanCmd.contains("navigate") || cleanCmd.contains("go to") || cleanCmd.contains("direction") -> {
                CommandResult(
                    responseText = "Opening maps and routing to $nextCustomerName at $nextAddress.",
                    actionType = ActionType.NAVIGATE
                )
            }
            cleanCmd.contains("remaining") || cleanCmd.contains("how many") || cleanCmd.contains("parcels left") -> {
                CommandResult(
                    responseText = "You have $remainingParcels parcels remaining to deliver on your optimized route.",
                    actionType = ActionType.CHECK_REMAINING
                )
            }
            else -> {
                CommandResult(
                    responseText = "Sorry, I recognized: '$command'. Please try saying: 'Show next parcel', 'Navigate to next delivery', 'Call customer', or 'How many parcels are remaining?'.",
                    actionType = ActionType.UNKNOWN
                )
            }
        }
    }
}

data class CommandResult(
    val responseText: String,
    val actionType: ActionType
)

enum class ActionType {
    SHOW_NEXT,
    CALL_CUSTOMER,
    NAVIGATE,
    CHECK_REMAINING,
    UNKNOWN
}
