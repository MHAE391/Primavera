package com.m391.primavera.chat.child

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivityViewModel
import com.m391.primavera.chat.father.ChatAdapter
import com.m391.primavera.databinding.FragmentFatherChildChatBinding
import com.m391.primavera.utils.*
import com.m391.primavera.utils.MediaPlayerManager.stopAudio
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class FatherChildChatFragment : BaseFragment() {

    override val viewModel: FatherChildViewModel by viewModels()

    private lateinit var binding: FragmentFatherChildChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val childUid = activity?.intent!!.extras!!.getString(Constants.CHILD_UID)
        val childName = activity?.intent!!.extras!!.getString(Constants.CHILD_NAME)
        viewModel.setChatSenderAndReceiver(
            childUid!!, childName!!
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_child_chat, container, false)
        binding.cancelVoiceMessage.visibility = View.INVISIBLE
        binding.timer.visibility = View.INVISIBLE
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        setupRecyclerView()
        setSendMessageClick()
        binding.arrowBtn.setOnClickListener {
            activity?.finish()
        }
        binding.cancelVoiceMessage.setOnClickListener { onCancelClick() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openMessagesStream(viewLifecycleOwner)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeMessagesStream(viewLifecycleOwner)
        }
        stopAudio()
        if (mediaRecorder != null) onCancelClick()
    }

    private fun setupRecyclerView() {
        val adapter = ChildChatAdapter {}
        binding.usersRecyclerView.setupLinearRecycler(adapter)
    }

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecordAudio() {
        mediaRecorder = MediaRecorder(requireContext())
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
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
        state = true
    }

    private fun sendRecording() {
        try {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            lifecycleScope.launch {
                if (viewModel.sendAudioMessage(output!!) == Constants.SUCCESS) {
                    viewModel.sendFcm()
                    Toast.makeText(requireContext(), "Audio Sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Audio Failed", Toast.LENGTH_SHORT).show()
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

    private fun checkRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestRecordAudioPermission() {
        PermissionX.init(this).permissions(Manifest.permission.RECORD_AUDIO)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    Constants.PERMISSION_LIST,
                    Constants.PERMISSION_OK,
                    Constants.PERMISSION_CANCEL
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    Constants.PERMISSION_SETTING,
                    Constants.PERMISSION_OK,
                    Constants.PERMISSION_CANCEL
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    onRecordClick(binding.sendMessage)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun onRecordClick(it: ImageButton) {
        Animation.showTextViewWithAnimation(binding.timer)
        viewModel.startTimer(binding.timer)
        it.tag = getString(R.string.send_voice)
        Animation.showButtonWithAnimation(binding.cancelVoiceMessage)
        Animation.animateImageChange(binding.sendMessage, R.drawable.ic_baseline_add_task_24)
        it.background = ContextCompat.getDrawable(requireContext(), R.drawable.add_task_button)
        startRecordAudio()
    }

    private fun onSendVoiceClick(it: ImageButton) {
        Animation.hideButtonWithAnimation(binding.cancelVoiceMessage)
        Animation.hideTextViewWithAnimation(binding.timer)
        viewModel.stopTimer(binding.timer)
        it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
        it.tag = getString(R.string.voice_message)
        Animation.animateImageChange(binding.sendMessage, R.drawable.ic_baseline_mic_24)
        sendRecording()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setSendMessageClick() {
        binding.sendMessage.setOnClickListener {
            when (it.tag) {
                getString(R.string.voice_message) -> {
                    if (checkRecordAudioPermission()) onRecordClick(binding.sendMessage)
                    else requestRecordAudioPermission()
                }

                getString(R.string.send_voice) -> {
                    onSendVoiceClick(binding.sendMessage)
                }

            }
        }
    }

    private fun onCancelClick() {
        Animation.hideButtonWithAnimation(binding.cancelVoiceMessage)
        Animation.hideTextViewWithAnimation(binding.timer)
        viewModel.stopTimer(binding.timer)
        binding.sendMessage.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
        binding.sendMessage.tag = getString(R.string.voice_message)
        Animation.animateImageChange(binding.sendMessage, R.drawable.ic_baseline_mic_24)
        cancelRecording()
    }

}