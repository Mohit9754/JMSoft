package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentPurchasingBinding
import com.jmsoft.main.adapter.AdapterPurchasing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PurchasingFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentPurchasingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentPurchasingBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    // Set Purchasing RecyclerView
    private suspend fun setPurchasingRecyclerView() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllPurchase()
        }

        val purchasingList = result.await()

        if (purchasingList.isNotEmpty()) {

            binding.llEmptyPurchasing?.visibility = View.GONE
            binding.mcvPurchasingList?.visibility = View.VISIBLE

            val adapterPurchasing = AdapterPurchasing(requireActivity(),purchasingList,binding,lifecycleScope)

            binding.rvPurchasing?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvPurchasing?.adapter = adapterPurchasing

        }
        else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding.llEmptyPurchasing?.visibility = View.VISIBLE
            binding.mcvPurchasingList?.visibility = View.GONE
        }
    }

    private fun init() {

        lifecycleScope.launch {
            setPurchasingRecyclerView()
        }

    }

    override fun onClick(view: View?) {


    }

}