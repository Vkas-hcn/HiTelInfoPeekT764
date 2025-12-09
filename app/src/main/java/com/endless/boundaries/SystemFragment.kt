package com.endless.boundaries

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.endless.boundaries.databinding.FragmentSystemBinding
import androidx.core.graphics.toColorInt

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSystemInfo()
    }

    private fun loadSystemInfo() {
        // Set header info
        binding.tvAndroidVersion.text = "Android ${Build.VERSION.RELEASE} - ${Build.VERSION.CODENAME}"
        binding.tvUiVersion.text = Build.DISPLAY
        binding.tvReleaseDate.text = "Released :\n${Build.TIME}"

        // Get system info
        val systemInfoList = mutableListOf<Pair<String, String>>()
        
        systemInfoList.add(Pair("SDK Version", Build.VERSION.SDK_INT.toString()))
        systemInfoList.add(Pair("Android Version", Build.VERSION.RELEASE))
        systemInfoList.add(Pair("Security Patch", Build.VERSION.SECURITY_PATCH))
        systemInfoList.add(Pair("Build ID", Build.ID))
        systemInfoList.add(Pair("Display", Build.DISPLAY))
        systemInfoList.add(Pair("Fingerprint", Build.FINGERPRINT))
        systemInfoList.add(Pair("Host", Build.HOST))
        systemInfoList.add(Pair("Tags", Build.TAGS))
        systemInfoList.add(Pair("Type", Build.TYPE))
        systemInfoList.add(Pair("User", Build.USER))
        systemInfoList.add(Pair("Bootloader", Build.BOOTLOADER))
        systemInfoList.add(Pair("Kernel", System.getProperty("os.version") ?: "Unknown"))

        // Add info items to layout
        systemInfoList.forEachIndexed { index, pair ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_device_info, binding.llSystemInfoList, false)
            
            itemView.findViewById<android.widget.TextView>(R.id.tvLabel).text = pair.first
            itemView.findViewById<android.widget.TextView>(R.id.tvValue).text = pair.second
            
            binding.llSystemInfoList.addView(itemView)
            
            // Add divider if not last item
            if (index < systemInfoList.size - 1) {
                val divider = View(context)
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                )
                layoutParams.setMargins(dpToPx(16), 0, dpToPx(16), 0)
                divider.layoutParams = layoutParams
                divider.setBackgroundColor("#FFF0F0F0".toColorInt())
                binding.llSystemInfoList.addView(divider)
            }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
