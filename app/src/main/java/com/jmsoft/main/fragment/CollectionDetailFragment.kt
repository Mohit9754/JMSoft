package com.jmsoft.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jmsoft.R
import com.jmsoft.databinding.FragmentCatalogBinding
import com.jmsoft.databinding.FragmentCollectionDetailBinding

class CollectionDetailFragment : Fragment(),View.OnClickListener {

    lateinit var binding:FragmentCollectionDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCollectionDetailBinding.inflate(layoutInflater)

        init()


        return binding.root
    }


    private fun init(){

    }

    override fun onClick(v: View?) {

    }

}