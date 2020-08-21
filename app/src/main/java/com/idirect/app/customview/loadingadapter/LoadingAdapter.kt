package com.idirect.app.customview.loadingadapter

import androidx.recyclerview.widget.RecyclerView
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.datasource.model.event.LoadingEvent

abstract class LoadingAdapter(var items:MutableList<Any> = ArrayList<Any>().toMutableList()) : BaseAdapter() {


    fun setLoading(isLoading: Boolean,recyclerView: RecyclerView?=null) {
        if (isLoading) {
            items.add(LoadingEvent())
            notifyItemInserted(items.size - 1)
            recyclerView?.scrollToPosition(items.size - 1)
        } else {
            for (i in items.indices) {
                if (items[i] is LoadingEvent) {
                    items.removeAt(i)
                    notifyItemRemoved(i)
                }
            }
        }
    }

    override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
        val item = items!![position]
        if(item is LoadingEvent){
            return item
        }
        return objForPosition(holder,position)
    }

    override fun getLayoutIdForPosition(position: Int): Int {
        val item = items!![position]
        if(item is LoadingEvent){
            return R.layout.layout_loading
        }
        return layoutIdForPosition(position)
    }

    override fun getItemCount(): Int {
        return if(items == null)0 else items!!.size
    }

    abstract fun objForPosition(holder: BaseViewHolder, position: Int):Any
    abstract fun layoutIdForPosition(position: Int):Int
}