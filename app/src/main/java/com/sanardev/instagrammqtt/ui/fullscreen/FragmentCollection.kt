package com.sanardev.instagrammqtt.ui.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseFragment
import com.sanardev.instagrammqtt.databinding.FragmentCollectionBinding

class FragmentCollection constructor(var url:String): BaseFragment<FragmentCollectionBinding,FragmentCollectionViewModel>() {
    override fun getViewModelClass(): Class<FragmentCollectionViewModel> {
        return FragmentCollectionViewModel::class.java
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_collection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(context!!).load(url).into(binding.photoView)
    }

}