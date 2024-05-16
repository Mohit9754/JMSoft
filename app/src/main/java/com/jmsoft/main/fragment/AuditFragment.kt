package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.databinding.FragmentAuditBinding
import com.jmsoft.main.adapter.ExpectedAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuditFragment : Fragment() {

    val binding by lazy { FragmentAuditBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        lifecycleScope.launch(Dispatchers.Main) {

            init()
        }

        return binding.root

    }

    private fun setExpectedRecyclerView(){

        val AdapterExpected = ExpectedAdapter(requireContext(), arrayListOf())

        binding.rvExpected?.also {
            it.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
            it.adapter = AdapterExpected
        }

    }

    private fun setScannedRecyclerView(){

        val AdapterScanned = ExpectedAdapter(requireContext(), arrayListOf())

        binding.rvScanned?.also {
            it.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
            it.adapter = AdapterScanned
        }

    }
    private fun setUnKnownRecyclerView() {

        val AdapterUnKnown = ExpectedAdapter(requireContext(), arrayListOf())

        binding.rvUnknown?.also {
            it.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
            it.adapter = AdapterUnKnown
        }

    }

    private suspend fun init(){

        val expectedJob = lifecycleScope.launch(Dispatchers.Main) {
            setExpectedRecyclerView()
        }

        val scannedJob = lifecycleScope.launch(Dispatchers.Main) {
            setScannedRecyclerView()
        }

        val unKnownJob = lifecycleScope.launch(Dispatchers.Main) {
            setUnKnownRecyclerView()
        }

        expectedJob.join()
        scannedJob.join()
        unKnownJob.join()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

}