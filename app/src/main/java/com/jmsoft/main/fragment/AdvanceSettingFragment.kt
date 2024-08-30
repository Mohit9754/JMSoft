package com.jmsoft.main.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants.Companion.frequencyData
import com.jmsoft.basic.UtilityTools.Constants.Companion.frequencyIndex
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentAdvanceSettingBinding
import com.jmsoft.databinding.FragmentAuditBinding
import com.jmsoft.databinding.FragmentSettingBinding
import com.jmsoft.main.activity.DashboardActivity

class AdvanceSettingFragment : Fragment(),View.OnClickListener {

    private val fValues = intArrayOf(
        0x01,
        0x02,
        0x04,
        0x08,
        0x16,
        0x32,
        0x33,
        0x34,
        0x35,
        0x36,
        0x37,
        0x80,
        0x3B
    )

    private val binding by lazy { FragmentAdvanceSettingBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        init()

        return binding.root

    }

    private fun setFrequencySpinner() {

        val frequencyArray = resources.getStringArray(R.array.arrayFrequency)

        val spinnerAdapter = ArrayAdapter(requireActivity(), R.layout.item_spinner, frequencyArray)
        spinnerAdapter.setDropDownViewResource(R.layout.item_custom_spinner_list)

        binding.spFrequency?.adapter = spinnerAdapter

        // Get the stored index from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences(frequencyData, Context.MODE_PRIVATE)
        val savedIndex = sharedPreferences.getInt(frequencyIndex, 0) // Default to 0 if no value is found

        // Set the spinner to the saved index
        binding.spFrequency?.setSelection(savedIndex)

        binding.spFrequency?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val sharedPreferences = requireActivity().getSharedPreferences(frequencyData, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putInt(frequencyIndex, position)
                editor.apply() // or editor.commit() to save synchronously

                Utils.E("Frequency is ${fValues[position]}")

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected (optional)
            }
        }

    }

    private fun init() {

        setFrequencySpinner()

        binding.mcvBackBtn?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

    }

}