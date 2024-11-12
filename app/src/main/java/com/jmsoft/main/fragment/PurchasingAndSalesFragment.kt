package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentPurchasingAndSalesBinding
import com.jmsoft.main.activity.DashboardActivity

class PurchasingAndSalesFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentPurchasingAndSalesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentPurchasingAndSalesBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    // Set Tab And ViewPager
    private fun setTabAndViewPager() {

        Utils.purchaseAndSalesBinding.setBinding(binding)

        val fragments: List<Fragment> =
            listOf(PurchasingFragment(), SalesFragment())

        binding.viewPager?.adapter = object : FragmentStateAdapter(this) {

            override fun getItemCount() = fragments.size

            override fun createFragment(position: Int) = fragments[position]

        }

        binding.viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

//                fragments = listOf(PurchasingFragment(), SalesFragment())

                binding.llSearchSection?.visibility = if (position == 0) View.GONE else View.VISIBLE
                binding.llAdd?.visibility = if (position == 0) View.VISIBLE else View.GONE
            }
        })

        val tabText = resources.getStringArray(R.array.arrPurchasingAndSales)

        // Link TabLayout with ViewPager
        binding.viewPager?.let {
            binding.tabLayout?.let { it1 ->
                TabLayoutMediator(it1, it) { tab, position ->
                    tab.text = tabText[position]
                }.attach()
            }
        }

    }

    private fun init() {

        setTabAndViewPager()

        binding.mcvBackBtn?.setOnClickListener(this)
        binding.mcvAdd?.setOnClickListener(this)

    }

    override fun onClick(view: View?) {

        when (view) {

            binding.mcvBackBtn -> {
                (requireActivity() as DashboardActivity).navController?.popBackStack()
            }

            binding.mcvAdd -> {
                (requireActivity() as DashboardActivity).navController?.navigate(R.id.addPurchasing)
            }

        }
    }

}