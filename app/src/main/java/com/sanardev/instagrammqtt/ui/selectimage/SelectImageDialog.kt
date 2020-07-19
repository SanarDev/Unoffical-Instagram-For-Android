package com.sanardev.instagrammqtt.ui.selectimage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.snackbar.Snackbar
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.ItemSelectingImageBinding
import com.sanardev.instagrammqtt.databinding.LayoutSelectImageBinding
import com.sanardev.instagrammqtt.extensions.gone
import com.sanardev.instagrammqtt.extensions.visible
import com.sanardev.instagrammqtt.ui.fullscreen.FullScreenActivity
import com.sanardev.instagrammqtt.ui.playvideo.PlayVideoActivity
import com.sanardev.instagrammqtt.utils.DisplayUtils
import com.sanardev.instagrammqtt.utils.MediaUtils
import com.sanardev.instagrammqtt.utils.StorageUtils
import com.squareup.picasso.Picasso
import run.tripa.android.extensions.longToast
import run.tripa.android.extensions.sizeInKb
import run.tripa.android.extensions.toast
import run.tripa.android.extensions.vibration
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