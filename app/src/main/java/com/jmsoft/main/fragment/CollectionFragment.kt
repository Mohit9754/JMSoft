package com.jmsoft.main.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jmsoft.R
import com.jmsoft.databinding.FragmentCollectionBinding


class CollectionFragment : Fragment() {
  lateinit var activity: Activity
  lateinit var binding:FragmentCollectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        return binding.root
    }

}