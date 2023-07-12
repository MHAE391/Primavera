package com.m391.primavera.user.father.home.child

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentHeartRateBinding
import com.m391.primavera.user.father.home.FatherHomeViewModel
import com.m391.primavera.utils.BaseBottomSheetFragment
import com.m391.primavera.utils.setupLinearRecycler
import kotlinx.coroutines.launch

class HeartRateFragment : BaseBottomSheetFragment() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private val healthHistoryViewModel: HealthHistoryViewModel by activityViewModels()

    private lateinit var binding: FragmentHeartRateBinding
    override val viewModel: FatherHomeViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_heart_rate, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = healthHistoryViewModel
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    private fun setupBottomSheet(view: View) {
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels * 4 / 5
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.isDraggable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
        binding.arrowBtn.setOnClickListener {
            this@HeartRateFragment.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            healthHistoryViewModel.openHeartRateHistoryStream(
                viewModel.currentChildInformation.value!!.watchUID,
                viewLifecycleOwner
            )
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            healthHistoryViewModel.closeHeartRateHistoryStream(
                viewModel.currentChildInformation.value!!.watchUID,
                viewLifecycleOwner
            )
        }
    }

    private fun setupRecyclerView() {
        val adapter = HealthAdapter {}
        binding.usersRecyclerView.setupLinearRecycler(adapter)
    }
}