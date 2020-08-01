package com.idirect.app.ui.selectimage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.databinding.ItemSelectingImageBinding
import com.idirect.app.databinding.LayoutSelectImageBinding
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.ui.fullscreen.FullScreenActivity
import com.idirect.app.ui.playvideo.PlayVideoActivity
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.MediaUtils
import com.idirect.app.extentions.sizeInKb
import com.idirect.app.extentions.toast
import com.idirect.app.extentions.vibration
import java.io.File


class SelectImageDialog(var resultFunction:(List<String>) -> Unit) : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private var selectedItem = ArrayList<String>().toMutableList()
    val adapter = ImageAdapter(emptyList())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: LayoutSelectImageBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.layout_select_image, container, false)
        dataSourceFactory =
            DefaultDataSourceFactory(context!!, Util.getUserAgent(context!!, "Instagram"))
        adapter.items = MediaUtils.loadImagesfromSDCard2(context!!).reversed()
        binding.recyclerviewImages.adapter = adapter
        binding.btnDone.setOnClickListener {
            if(selectedItem.isEmpty()){
                context!!.toast(getString(R.string.you_must_select_media))
                return@setOnClickListener
            }
            resultFunction.invoke(selectedItem)
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    inner class ImageAdapter(var items: List<String>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val file = File(item)
            val mimeType = MediaUtils.getMimeType(file.path)
            val dataBinding = holder.binding as ItemSelectingImageBinding
            dataBinding.txtName.text = file.name
            dataBinding.txtMimeType.text = mimeType?.toUpperCase()
            dataBinding.txtFilesize.text =
                String.format(getString(R.string.size_kb), file.sizeInKb.toInt())
            val standardOverrideSize = DisplayUtils.getScreenWidth() / 2
            if (mimeType != null && mimeType.contains("video")) {
                val interval: Long = 0 * 1000
                val options = RequestOptions().frame(interval)
                Glide.with(context!!).asBitmap()
                    .load(item)
                    .override(standardOverrideSize,standardOverrideSize)
                    .centerCrop()
                    .apply(options)
                    .into(dataBinding.imgPic)
                dataBinding.btnOpen.setImageResource(R.drawable.ic_play)
            } else {
                Glide.with(context!!).load(file)
                    .override(standardOverrideSize, standardOverrideSize)
                    .centerCrop().into(dataBinding.imgPic)
                dataBinding.btnOpen.setImageResource(R.drawable.ic_open_in_new)
            }

            if(selectedItem.contains(item)){
                visible(dataBinding.txtSelecteNumber)
                for(index in selectedItem.indices){
                    if(selectedItem[index] == item){
                        dataBinding.txtSelecteNumber.text = (index + 1).toString()
                    }
                }
            }else{
                gone(dataBinding.txtSelecteNumber)
            }
            dataBinding.root.setOnClickListener {
                if(selectedItem.contains(item)){
                    selectedItem.remove(item)
                }else{
                    context!!.vibration(50)
                    selectedItem.add(item)
                }
                notifyDataSetChanged()
            }
            dataBinding.btnOpen.setOnClickListener {
                if (mimeType != null && mimeType.contains("video")) {
                    PlayVideoActivity.playFile(activity!!,item)
                }else{
                    FullScreenActivity.openFile(activity!!,item)
                }
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.item_selecting_image
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }
}