package com.m391.primavera.user.father.teacher

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.authentication.information.teacher.SelectTeacherLocationFragment
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.databinding.FragmentTeacherProfileBinding
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.Constants.CHATS
import com.m391.primavera.utils.Constants.CHILD
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEACHER_UID
import com.m391.primavera.utils.Constants.TYPE
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.Serializable

class TeacherProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentTeacherProfileBinding
    override val viewModel: TeacherProfileViewModel by activityViewModels()
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

        if (args.from == CHATS) {
            binding.chat.text = getString(R.string.back_to_chat)
        }
        binding.chat.setOnClickListener {
            if (args.from != CHATS) {
                val intent = Intent(activity, ChatActivity::class.java)
                lifecycleScope.launch {
                    viewModel.createConversation()
                }
                intent.putExtra(TYPE, TEACHER)
                intent.putExtra(TEACHER_UID, viewModel.teacherData.value!!.teacherId)
                intent.putExtra(FATHER_FIRST_NAME, viewModel.teacherData.value!!.firstName)
                startActivity(intent)
            } else {
                viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }
        }
        binding.teacherLocation.setOnClickListener {
            val fragment = TeacherLocationFragment()
            fragment.show(parentFragmentManager, "ShowLocation")
        }
        binding.teacherRate.setOnClickListener {
            showRatingDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.teacherUid)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.teacherUid)
        }
    }

    private fun showRatingDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rateBar)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toDouble()
            lifecycleScope.launch {
                viewModel.rateTeacher(rating)
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}
