package com.example.parkedcarfinder

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class DetailFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    @SuppressLint("StringFormatMatches")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailTextView = view.findViewById<TextView>(R.id.fragment_detail_view)
        if (detailTextView != null) {
            detailTextView.text = getString(R.string.parking_location, "0")
        } else {
            Log.e("DetailFragment", "TextView not found")
        }

        val locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        locationViewModel.location.observe(viewLifecycleOwner) { location ->
            updateText(location)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun updateText(location: String) {
        view?.findViewById<TextView>(R.id.fragment_detail_view)?.text = getString(R.string.parking_location, location)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


