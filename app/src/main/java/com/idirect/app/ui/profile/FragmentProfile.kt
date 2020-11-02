package com.idirect.app.ui.profile

import com.idirect.app.R
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentProfileBinding

class FragmentProfile : BaseFragment<FragmentProfileBinding,ProfileViewModel>() {
    companion object{
        const val NAME_TAG = "fragment_profile"
    }
    override fun getViewModelClass(): Class<ProfileViewModel> {
        return ProfileViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_profile
    }

    override fun getNameTag(): String {
        return NAME_TAG
    }


}