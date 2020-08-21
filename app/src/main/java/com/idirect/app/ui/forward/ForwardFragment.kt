package com.idirect.app.ui.forward

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idirect.app.BR
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.databinding.FragmentForwardBinding
import com.idirect.app.databinding.ItemDirectShareBinding
import com.idirect.app.datasource.model.Recipients
import com.idirect.app.di.DaggerViewModelFactory
import com.idirect.app.extentions.dpToPx
import com.idirect.app.extentions.vibration
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiPopup
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class ForwardFragment : BottomSheetDialogFragment(),View.OnClickListener {

    @Inject
    lateinit var mGlide: RequestManager
    @Inject
    internal lateinit var viewModelFactory: DaggerViewModelFactory
    private var _emojiPopup: EmojiPopup?=null
    private val emojiPopup: EmojiPopup get() = _emojiPopup!!
    private var _adapter: UsersAdapter?=null
    private val adapter: UsersAdapter get() = _adapter!!
    private var _binding: FragmentForwardBinding?=null
    private val binding: FragmentForwardBinding get() = _binding!!
    private lateinit var viewModel: ForwardViewModel
    private lateinit var forwardBundle:ForwardBundle

    private var selectedUsers = ArrayList<String>().toMutableList()
    private var mForwardListener:ForwardListener?=null

    fun setBundle(forwardBundle:ForwardBundle): ForwardFragment {
        this@ForwardFragment.forwardBundle = forwardBundle
        return this
    }
    fun setListener(forwardListener: ForwardListener?): ForwardFragment {
        this@ForwardFragment.mForwardListener = forwardListener
        return this
    }
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _adapter = null
        _emojiPopup?.releaseMemory()
        _emojiPopup = null
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forward, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ForwardViewModel::class.java)
        binding.setVariable(BR.viewModel,viewModel)

        viewModel.recipients.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                adapter.items = it.data!!.recipients
                adapter.notifyDataSetChanged()
            }
        })
        val user = viewModel.getUserData()
        mGlide.load(user.profilePicUrl).into(binding.imgProfile)
        _adapter = UsersAdapter(null)
        binding.recyclerviewThreads.adapter = adapter


        _emojiPopup =
            EmojiPopup.Builder.fromRootView(binding.root)
                .setOnEmojiPopupDismissListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
                }.setOnEmojiPopupShownListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
                }.build(binding.edtMessage);

        binding.btnEmoji.setOnClickListener(this)
        binding.edtMessage.setOnClickListener(this)
        binding.btnSearchClose.setOnClickListener(this)
        binding.btnFab.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchWord = s.toString()
                if(s!!.isEmpty()){
                    binding.btnSearchClose.visibility = View.GONE
                }else{
                    binding.btnSearchClose.visibility = View.VISIBLE
                }
            }
        })
        setSelectedCount()
    }

    override fun onDismiss(dialog: DialogInterface) {
        mForwardListener?.onDismiss()
        super.onDismiss(dialog)
    }
    fun setSelectedCount(){
        if(selectedUsers.isEmpty()){
            binding.txtSelectedCount.visibility = View.GONE
        }else{
            binding.txtSelectedCount.visibility = View.VISIBLE
            binding.txtSelectedCount.text = selectedUsers.size.toString()
        }
    }
    inner class UsersAdapter(var items: List<Recipients>?) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items!![position]
            val dataBinding = holder.binding as ItemDirectShareBinding
            if (item.user != null) {
                mGlide.load(item.user.profilePicUrl).into(dataBinding.imgProfile)
                dataBinding.txtUsername.text = item.user.username
            } else {
                if (item.thread.users != null && item.thread.users.isNotEmpty()) {
                    mGlide.load(item.thread.users[0].profilePicUrl).into(dataBinding.imgProfile)
                    dataBinding.txtUsername.text = item.thread.users[0].username
                }
            }
            val id = if (item.user != null) {
                "[[${item.user}]]"
            } else {
                item.thread.threadId
            }
            if(selectedUsers.contains(id)){
                dataBinding.imgCheck.visibility = View.VISIBLE
            }else{
                dataBinding.imgCheck.visibility = View.GONE
            }
            dataBinding.root.setOnClickListener {
                val id = if (item.user != null) {
                    "[[${item.user}]]"
                } else {
                    item.thread.threadId
                }
                if(selectedUsers.contains(id)){
                    selectedUsers.remove(id)
                }else{
                    requireContext().vibration(50)
                    selectedUsers.add(id)
                }
                setSelectedCount()
                notifyItemChanged(position)
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.item_direct_share
        }

        override fun getItemCount(): Int {
            return if (items == null) 0 else items!!.size
        }

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            binding.btnEmoji.id ->{
                if(emojiPopup.isShowing){
                    emojiPopup.dismiss()
                }else{
                    emojiPopup.toggle()
                }
            }
            binding.edtMessage.id ->{
                emojiPopup.dismiss()
            }
            binding.btnSearchClose.id ->{
                binding.edtSearch.setText("")
            }
            binding.btnFab.id ->{
                viewModel.shareMediaTo(forwardBundle,selectedUsers)
                dismiss()
            }
            binding.btnBack.id ->{
                dismiss()
            }
        }
    }
}