package com.m391.primavera.authentication.information

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.m391.primavera.authentication.information.father.FatherInformationFragment
import com.m391.primavera.authentication.information.teacher.TeacherInformationFragment

class InformationViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FatherInformationFragment()
            }
            1 -> {
                TeacherInformationFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}