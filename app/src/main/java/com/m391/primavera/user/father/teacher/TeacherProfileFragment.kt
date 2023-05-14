package com.m391.primavera.user.father.teacher

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentTeacherProfileBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEACHER_UID
import com.m391.primavera.utils.Constants.TYPE
import kotlinx.coroutines.launch
import java.io.Serializable

class TeacherProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentTeacherProfileBinding
    override val viewModel: TeacherProfileViewModel by viewModels()
    private val args: TeacherProfileFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_teacher_profile, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.teacherUid)
        }
        binding.chat.setOnClickListener {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra(TYPE, TEACHER)
            intent.putExtra(TEACHER_UID, viewModel.teacherData.value!!.teacherId)
            intent.putExtra(FATHER_FIRST_NAME, viewModel.teacherData.value!!.firstName)
            startActivity(intent)
        }
    }


    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.teacherUid)
        }
    }
}
