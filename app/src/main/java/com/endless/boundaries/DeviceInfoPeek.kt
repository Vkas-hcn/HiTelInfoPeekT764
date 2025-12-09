package com.endless.boundaries

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.endless.boundaries.databinding.PeekDeviceInfoBinding

class DeviceInfoPeek : AppCompatActivity() {

    private val binding by lazy {
        PeekDeviceInfoBinding.inflate(layoutInflater)
    }

    private val tabTitles = listOf("Device", "System", "CPU", "Battery")
    private val tabViews = mutableListOf<View>()
    private val indicatorViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupImmersiveStatusBar()
        setupToolbar()
        setupTabs()
        setupViewPager()

        // Set initial tab from intent
        val initialTab = intent.getIntExtra("INITIAL_TAB", 0)
        binding.viewPager.setCurrentItem(initialTab, false)
    }

    private fun setupImmersiveStatusBar() {
        // Set status bar color to match background
        window.statusBarColor = Color.parseColor("#F9F9F9")
        
        // Set status bar icons to dark mode (for light background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add padding to top bar for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        tabTitles.forEachIndexed { index, title ->
            val tabLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPadding(0, 0, 0, dpToPx(8))
            }

            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = title
                textSize = 14f
                setTextColor(Color.parseColor("#FF787979"))
                gravity = Gravity.CENTER
            }

            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(5)
                ).apply {
                    topMargin = dpToPx(8)
                }
                background = ContextCompat.getDrawable(this@DeviceInfoPeek, R.drawable.bg_tab_indicator)
                visibility = View.INVISIBLE
            }

            tabLayout.addView(textView)
            tabLayout.addView(indicator)

            tabLayout.setOnClickListener {
                binding.viewPager.currentItem = index
            }

            binding.llTabContainer.addView(tabLayout)
            tabViews.add(textView)
            indicatorViews.add(indicator)
        }

        // Select first tab
        selectTab(0)
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> DeviceFragment()
                    1 -> SystemFragment()
                    2 -> CpuFragment()
                    3 -> BatteryFragment()
                    else -> DeviceFragment()
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectTab(position)
                updateTitle(position)
            }
        })
    }

    private fun selectTab(position: Int) {
        tabViews.forEachIndexed { index, view ->
            val textView = view as TextView
            if (index == position) {
                textView.setTextColor(Color.parseColor("#FF1E1F20"))
                indicatorViews[index].visibility = View.VISIBLE
                
                // Measure text width for indicator
                textView.post {
                    val indicatorWidth = textView.width
                    val indicatorParams = indicatorViews[index].layoutParams as LinearLayout.LayoutParams
                    indicatorParams.width = indicatorWidth
                    indicatorViews[index].layoutParams = indicatorParams
                }
            } else {
                textView.setTextColor(Color.parseColor("#FF787979"))
                indicatorViews[index].visibility = View.INVISIBLE
            }
        }
    }

    private fun updateTitle(position: Int) {
        binding.tvTitle.text = tabTitles[position]
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
