package com.m391.primavera.authentication.information.teacher

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m391.primavera.R
import com.m391.primavera.databinding.FragmentSelectSubjectsBinding

class SelectSubjectsFragment : BottomSheetDialogFragment() {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var binding: FragmentSelectSubjectsBinding
    private val viewModel: TeacherInformationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_subjects, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val layout = binding.bottom
        layout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
    }

    override fun onStart() {
        super.onStart()
        viewModel.selectedAcademicSubjects.value?.forEach { subject ->
            when (subject) {
                getString(R.string.arabic) -> binding.arabic.isChecked = true
                getString(R.string.biology) -> binding.biology.isChecked = true
                getString(R.string.french) -> binding.french.isChecked = true
                getString(R.string.english) -> binding.english.isChecked = true
                getString(R.string.chemistry) -> binding.chemistry.isChecked = true
                getString(R.string.geography) -> binding.geography.isChecked = true
                getString(R.string.german) -> binding.german.isChecked = true
                getString(R.string.mathematics) -> binding.mathematics.isChecked = true
                getString(R.string.geology) -> binding.geology.isChecked = true
                getString(R.string.history) -> binding.history.isChecked = true
                getString(R.string.physics) -> binding.physics.isChecked = true
                getString(R.string.philosophy) -> binding.philosophy.isChecked = true
            }
        }
        checkBoxWatcher(binding.arabic)
        checkBoxWatcher(binding.biology)
        checkBoxWatcher(binding.french)
        checkBoxWatcher(binding.english)
        checkBoxWatcher(binding.chemistry)
        checkBoxWatcher(binding.geography)
        checkBoxWatcher(binding.german)
        checkBoxWatcher(binding.mathematics)
        checkBoxWatcher(binding.geology)
        checkBoxWatcher(binding.history)
        checkBoxWatcher(binding.physics)
        checkBoxWatcher(binding.philosophy)
        binding.arrowBtn.setOnClickListener {
            this@SelectSubjectsFragment.dismiss()
        }
    }

    private fun checkBoxWatcher(checkBox: CheckBox) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> viewModel.addSubject(checkBox.text.toString())
                false -> viewModel.removeSubject(checkBox.text.toString())
            }
        }
    }

}