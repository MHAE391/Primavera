package com.m391.primavera.user.father.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentFatherProfileBinding
import com.m391.primavera.user.father.teacher.TeacherLocationFragment
import com.m391.primavera.utils.BaseFragment
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Binding
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class FatherProfileFragment : BaseFragment() {

    override val viewModel: FatherProfileViewModel by activityViewModels()
    private val args: FatherProfileFragmentArgs by navArgs()
    private lateinit var binding: FragmentFatherProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_father_profile, container, false)
        return binding.root
    }

    private val chooseFatherPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Binding.loadImage(binding.profileImage, uri.toString(), uri.toString())
                viewModel.setNewImage(uri.toString())
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        if (args.from != "Home") {
            binding.editProfileImage.visibility = View.GONE
            binding.editProfileLocation.visibility = View.GONE
            binding.addChild.visibility = View.GONE
            binding.update.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.openStream(viewLifecycleOwner, args.fatherUid)
        }
        binding.editProfileImage.setOnClickListener {
            chooseFatherPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.fatherLocation.setOnClickListener {
            val fragment = FatherShowLocationFragment()
            fragment.show(parentFragmentManager, "FatherShowLocation")
        }
        binding.editProfileLocation.setOnClickListener {
            val fragment = FatherEditLocationFragment()
            fragment.show(parentFragmentManager, "FatherEditLocation")
        }
        binding.update.setOnClickListener {
            lifecycleScope.launch {
                viewModel.updateFatherInfo()
            }
        }
        binding.addChild.setOnClickListener {
            findNavController().navigate(FatherProfileFragmentDirections.actionFatherProfileFragmentToAddNewChildFragment())
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.closeStream(viewLifecycleOwner, args.fatherUid)
        }
    }
}