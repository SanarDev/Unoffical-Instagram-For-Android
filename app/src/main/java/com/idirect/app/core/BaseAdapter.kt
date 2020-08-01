package com.idirect.app.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.idirect.app.BR

abstract class BaseAdapter : RecyclerView.Adapter<BaseAdapter.BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding:ViewDataBinding = DataBindingUtil.inflate(layoutInflater,viewType,parent,false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getObjForPosition(holder,position)
        holder.bind(obj)
    }

    class BaseViewHolder (var binding:ViewDataBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(obj:Any){
            binding.setVariable(BR.obj,obj)
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition(position)
    }

    abstract fun getObjForPosition(holder: BaseViewHolder, position: Int): Any
    protected abstract fun getLayoutIdForPosition(position: Int): Int
}