package com.m391.primavera

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.m391.primavera.base.Constants
import com.m391.primavera.base.setupLinearRecycler
import com.m391.primavera.databinding.ActivityChatBinding
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.cancelButton.visibility = View.INVISIBLE
        binding.time.visibility = View.INVISIBLE
        setupRecyclerView()
        binding.sendButton.setOnClickListener {
            if (it.tag == getString(R.string.voice_message)) {
                requestRecordAudioPermission()
            } else {
                onSendVoiceClick(binding.sendButton)
            }
        }
        binding.cancelButton.setOnClickListener {
            onCancelClick()
        }
    }

    private fun setupRecyclerView() {
        val adapter = ChatAdapter {

        }
        binding.recyclerView.setupLinearRecycler(adapter)
    }

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) Toast.makeText(applicationContext, "Done", Toast.LENGTH_SHORT).show()
        else Toast.makeText(applicationContext, "NotDone", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestRecordAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        val isGranted =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            recordAudioPermissionLauncher.launch(permission)
        } else {
            onRecordClick(binding.sendButton)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun onRecordClick(it: ImageButton) {
        Animation.showTextViewWithAnimation(binding.time)
        viewModel.startTimer(binding.time)
        it.tag = getString(R.string.send_voice)
        Animation.showButtonWithAnimation(binding.cancelButton)
        Animation.animateImageChange(binding.sendButton, R.drawable.ic_baseline_add_task_24)
        it.background = ContextCompat.getDrawable(applicationContext, R.drawable.add_task_button)
        startRecordAudio()
    }

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecordAudio() {
        mediaRecorder = MediaRecorder()
        output =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + "/tempRecordings-" + Date().time + ".mp3"
        mediaRecorder!!.setOutputFile(output)
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
        } catch (e: IOException) {
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
        state = true
    }

    private fun sendRecording() {
        try {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            lifecycleScope.launch {
                if (viewModel.sendAudioMessage(output!!) == Constants.SUCCESS) {
                    Toast.makeText(applicationContext, "Audio Sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Audio Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun cancelRecording() {
        try {
            mediaRecorder!!.stop()
            if (File(output!!).exists()) {
                File(output!!).delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onCancelClick() {
        Animation.hideButtonWithAnimation(binding.cancelButton)
        Animation.hideTextViewWithAnimation(binding.time)
        viewModel.stopTimer(binding.time)
        binding.sendButton.background =
            ContextCompat.getDrawable(applicationContext, R.drawable.rounded_button)
        binding.sendButton.tag = getString(R.string.voice_message)
        Animation.animateImageChange(binding.sendButton, R.drawable.ic_baseline_mic_24)
        cancelRecording()
    }

    private fun onSendVoiceClick(it: ImageButton) {
        Animation.hideButtonWithAnimation(binding.cancelButton)
        Animation.hideTextViewWithAnimation(binding.time)
        viewModel.stopTimer(binding.time)
        it.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_button)
        it.tag = getString(R.string.voice_message)
        Animation.animateImageChange(binding.sendButton, R.drawable.ic_baseline_mic_24)
        sendRecording()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStreamMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStreamMessage()
        }
    }

}