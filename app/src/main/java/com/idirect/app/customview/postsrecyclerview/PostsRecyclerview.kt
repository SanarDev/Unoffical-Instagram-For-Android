package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idirect.app.manager.PlayManager
import java.lang.RuntimeException

class PostsRecyclerview constructor(context: Context,attr:AttributeSet?=null) : RecyclerView(context,attr) {

    var mPostsRecyclerState:PostsRecyclerState?=null

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val mLayoutManager = layoutManager as LinearLayoutManager
        val mAdapter = adapter as PostsAdapter

        val positionFirstItem = mLayoutManager.findFirstCompletelyVisibleItemPosition()
        if (positionFirstItem != -1 &&
            mAdapter.currentMediaPosition != PlayManager.NONE &&
            positionFirstItem != mAdapter.currentMediaPosition
        ) {
            mAdapter.currentMediaPosition = PlayManager.NONE
            mAdapter.mPlayManager.stopPlay()
            return
        }
        val totalItemCount = mLayoutManager.itemCount
        if (mLayoutManager != null && mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
            mPostsRecyclerState?.requestForLoadMore()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter is PostsAdapter) {
            super.setAdapter(adapter)
        } else {
            throw RuntimeException("Adapter must instance of PostsAdapter")
        }
    }
}