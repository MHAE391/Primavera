package com.m391.primavera.authentication.information

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.m391.primavera.R
import com.m391.primavera.databinding.ActivityInformationBinding

class InformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInformationBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var informationViewPagerAdapter: InformationViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        tabLayout = binding.informationTaps
        viewPager = binding.informationViewPager
        informationViewPagerAdapter = InformationViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = informationViewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.father)
                1 -> tab.text = getString(R.string.teacher)
                else -> tab.text = getString(R.string.fragment)
            }
        }.attach()

    }
}