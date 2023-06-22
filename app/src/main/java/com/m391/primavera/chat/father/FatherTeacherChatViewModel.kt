package com.m391.primavera.chat.father

import android.app.Application
import android.widget.TextView
import com.m391.primavera.App
import com.m391.primavera.utils.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FatherTeacherChatViewModel(app: Application) : BaseViewModel(app) {

    fun startTimer(textView: TextView) {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        updateTimer(textView)
    }

    fun stopTimer(textView: TextView) {
        isTimerRunning = false
        timerRunnable?.let { textView.removeCallbacks(it) }
        timerRunnable = null
    }

    private fun updateTimer(textView: TextView) {
        if (isTimerRunning) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime
            val formattedTime = getFormattedTime(elapsedTime)
            textView.text = formattedTime
            timerRunnable = Runnable { updateTimer(textView) }
            textView.postDelayed(timerRunnable, 1000) // Update every second (1000 milliseconds)
        }
    }

    private var startTime: Long = 0
    private var isTimerRunning: Boolean = false
    private var timerRunnable: Runnable? = null
    private fun getFormattedTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}