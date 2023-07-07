package com.m391.primavera.user.father.home.switjha.teacher


import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentSelectSubjectsBinding
import com.m391.primavera.databinding.FragmentSelectYearsBinding

class SelectYearsFragment : BottomSheetDialogFragment() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var binding: FragmentSelectYearsBinding
    private val viewModel: CreateTeacherViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_years, container, false)
        binding.lifecycleOwner = this
        binding.createViewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.selectedAcademicYears.value?.forEach { year ->
            when (year) {
                getString(R.string.first_primary) -> binding.firstPrimary.isChecked = true
                getString(R.string.second_primary) -> binding.secondPrimary.isChecked = true
                getString(R.string.third_primary) -> binding.thirdPrimary.isChecked = true
                getString(R.string.fourth_primary) -> binding.fourthPrimary.isChecked = true
                getString(R.string.fifth_primary) -> binding.fifthPrimary.isChecked = true
                getString(R.string.sixth_primary) -> binding.sixthPrimary.isChecked = true
                getString(R.string.first_preparatory) -> binding.firstPreparatory.isChecked = true
                getString(R.string.second_preparatory) -> binding.secondPreparatory.isChecked = true
                getString(R.string.third_preparatory) -> binding.thirdPreparatory.isChecked = true
                getString(R.string.first_secondary) -> binding.firstSecondary.isChecked = true
                getString(R.string.second_secondary) -> binding.secondSecondary.isChecked = true
                getString(R.string.third_secondary) -> binding.thirdSecondary.isChecked = true
            }
        }
        checkBoxWatcher(binding.firstPrimary)
        checkBoxWatcher(binding.secondPrimary)
        checkBoxWatcher(binding.thirdPrimary)
        checkBoxWatcher(binding.fourthPrimary)
        checkBoxWatcher(binding.fifthPrimary)
        checkBoxWatcher(binding.sixthPrimary)
        checkBoxWatcher(binding.firstPreparatory)
        checkBoxWatcher(binding.secondPreparatory)
        checkBoxWatcher(binding.thirdPreparatory)
        checkBoxWatcher(binding.firstSecondary)
        checkBoxWatcher(binding.secondSecondary)
        checkBoxWatcher(binding.thirdSecondary)
        binding.arrowBtn.setOnClickListener {
            this@SelectYearsFragment.dismiss()
        }
    }

    private fun checkBoxWatcher(checkBox: CheckBox) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> viewModel.addYear(checkBox.text.toString())
                false -> viewModel.removeYear(checkBox.text.toString())
            }
        }
    }
}