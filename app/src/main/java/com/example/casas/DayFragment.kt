package com.example.casas

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment

class DayFragment : Fragment() {

    companion object {
        private const val ARG_DAY = "day"

        fun newInstance(day: String): DayFragment {
            val fragment = DayFragment()
            val bundle = Bundle()
            bundle.putString(ARG_DAY, day)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var day: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        day = arguments?.getString(ARG_DAY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            text = "Schedule for $day"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        return textView
    }
}
