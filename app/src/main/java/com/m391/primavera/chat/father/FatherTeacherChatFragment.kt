package com.m391.primavera.chat.father

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivityViewModel
import com.m391.primavera.databinding.FragmentFatherTeacherChatBinding
import com.m391.primavera.utils.*
import com.m391.primavera.utils.Animation.animateImageChange
import com.m391.primavera.utils.Animation.hideButtonWithAnimation
import com.m391.primavera.utils.Animation.hideTextViewWithAnimation
import com.m391.primavera.utils.Animation.showButtonWithAnimation
import com.m391.primavera.utils.Animation.showTextViewWithAnimation
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.IMAGE_MESSAGE
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEXT_MESSAGE
import com.m391.primavera.utils.Constants.TYPE
import com.m391.primavera.utils.Constants.VOICE_MESSAGE
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File
import java.io.IOException
import java.util.*

class FatherTeacherChatFragment : BaseFragment() {

    private lateinit var binding: FragmentFatherTeacherChatBinding
    override val viewModel: ChatActivityViewModel by activityViewModels()
    private val fatherTeacherChatViewModel: FatherTeacherChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (activity?.intent!!.extras!!.getString(TYPE)) {
            TEACHER -> {
                val teacherUid = activity?.intent!!.extras!!.getString(Constants.TEACHER_UID)
                val teacherFirstName = activity?.intent!!.extras!!.getString(FATHER_FIRST_NAME)
                viewModel.setChatSenderAndReceiver(
                    teacherUid!!,
                    teacherFirstName!!
                )
            }

            FATHER -> {
                val fatherUid = activity?.intent!!.extras!!.getString(Constants.FATHER_UID)
                val fatherFirstName = activity?.intent!!.extras!!.getString(FATHER_FIRST_NAME)
                viewModel.setChatSenderAndReceiver(
                    fatherUid!!,
                    fatherFirstName!!
                )
            }

            CHILD -> {
                findNavController().navigate(R.id.action_fatherTeacherChatFragment_to_fatherChildChatFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_father_teacher_chat, container, false
        )
        binding.cancelVoiceMessage.visibility = View.INVISIBLE
        binding.timer.visibility = View.INVISIBLE
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupRecyclerView()

        return binding.root
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

                getString(R.string.text_message) -> {
                    lifecycleScope.launch {
                        if (viewModel.sendTextMessage() == SUCCESS) {
                            Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT)
                                .show()
                            viewModel.sendFCM(TEXT_MESSAGE)
                        } else Toast.makeText(
                            requireContext(), "Message Failed", Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        binding.textMessage.addTextChangedListener(textWatcher)
        binding.cancelVoiceMessage.setOnClickListener { onCancelClick() }
        binding.sendImage.setOnClickListener {
            chooseSendingPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.arrowBtn.setOnClickListener {
            activity?.finish()
        }
        setSendMessageClick()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun onRecordClick(it: ImageButton) {
        binding.textMessage.isEnabled = false
        binding.sendImage.isEnabled = false
        showTextViewWithAnimation(binding.timer)
        fatherTeacherChatViewModel.startTimer(binding.timer)
        it.tag = getString(R.string.send_voice)
        showButtonWithAnimation(binding.cancelVoiceMessage)
        animateImageChange(binding.sendMessage, R.drawable.ic_baseline_add_task_24)
        it.background = ContextCompat.getDrawable(requireContext(), R.drawable.add_task_button)
        startRecordAudio()
    }

    private fun onSendVoiceClick(it: ImageButton) {
        hideButtonWithAnimation(binding.cancelVoiceMessage)
        hideTextViewWithAnimation(binding.timer)
        fatherTeacherChatViewModel.stopTimer(binding.timer)
        it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
        it.tag = getString(R.string.voice_message)
        animateImageChange(binding.sendMessage, R.drawable.ic_baseline_mic_24)
        binding.textMessage.isEnabled = true
        binding.sendImage.isEnabled = true
        sendRecording()
    }

    private fun onCancelClick() {
        hideButtonWithAnimation(binding.cancelVoiceMessage)
        hideTextViewWithAnimation(binding.timer)
        fatherTeacherChatViewModel.stopTimer(binding.textMessage)
        binding.textMessage.isEnabled = true
        binding.sendImage.isEnabled = true
        binding.sendMessage.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
        binding.sendMessage.tag = getString(R.string.voice_message)
        animateImageChange(binding.sendMessage, R.drawable.ic_baseline_mic_24)
        cancelRecording()
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if ((text.isNullOrEmpty() || text.isBlank()) && binding.sendMessage.tag == getString(R.string.text_message)) {
                animateImageChange(binding.sendMessage, R.drawable.ic_baseline_mic_24)
                binding.sendMessage.tag = getString(R.string.voice_message)
            } else if (!text.isNullOrEmpty() && text.isNotBlank() && binding.sendMessage.tag == getString(
                    R.string.voice_message
                )
            ) {
                animateImageChange(binding.sendMessage, R.drawable.ic_baseline_send_24)
                binding.sendMessage.tag = getString(R.string.text_message)
            }
        }

        override fun afterTextChanged(text: Editable?) {}
    }

    private fun setupRecyclerView() {
        val adapter = ChatAdapter {}
        binding.usersRecyclerView.setupLinearRecycler(adapter)
    }

    private val chooseSendingPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    if (viewModel.sendImageMessage(uri.toString()) == SUCCESS) {
                        Toast.makeText(requireContext(), "Image Sent", Toast.LENGTH_SHORT).show()
                        viewModel.sendFCM(IMAGE_MESSAGE)
                    } else Toast.makeText(requireContext(), "Send Image Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeMessagesStream(viewLifecycleOwner)
        }
        MediaPlayerManager.stopAudio()
        if (mediaRecorder != null) onCancelClick()
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

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecordAudio() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else MediaRecorder()
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
                if (viewModel.sendAudioMessage(output!!) == SUCCESS) {
                    Toast.makeText(requireContext(), "Audio Sent", Toast.LENGTH_SHORT).show()
                    viewModel.sendFCM(VOICE_MESSAGE)
                } else {
                    Toast.makeText(requireContext(), "Audio Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        mediaRecorder = null
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
        mediaRecorder = null
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openMessagesStream(viewLifecycleOwner)
        }
    }
}