package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.manager.PlayManager
import java.lang.RuntimeException

class PostsRecyclerview constructor(context: Context, attr: AttributeSet? = null) :
    RecyclerView(context, attr) {

    var mPostsRecyclerState: PostsRecyclerListener? = null
        set(value) {
            field = value
            adapter?.let {
                (it as PostsAdapter2).mListener = value
            }
        }
    private val mLayoutManager = layoutManager as LinearLayoutManager
    private val mAdapter: PostsAdapter2 by lazy {
        adapter!! as PostsAdapter2
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val positionFirstItem = mLayoutManager.findFirstVisibleItemPosition()
        val positionOfLastItem = mLayoutManager.findLastVisibleItemPosition()
        if (positionFirstItem == -1) {
            return
        }
        val totalItemCount = mLayoutManager.itemCount
        if (positionFirstItem >= totalItemCount - 3) {
            mPostsRecyclerState?.requestForLoadMore()
        }

        if (positionOfLastItem != -1) {
            if (mAdapter.currentMediaPosition != PlayManager.NONE &&
                positionFirstItem != mAdapter.currentMediaPosition &&
                positionOfLastItem != mAdapter.currentMediaPosition
            ) {
                mAdapter.currentMediaPosition = PlayManager.NONE
                mAdapter.mPlayManager.stopPlay()
                return
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter is PostsAdapter2) {
            adapter.mListener = mPostsRecyclerState
            super.setAdapter(adapter)
        } else {
            throw RuntimeException("Adapter must instance of PostsAdapter")
        }
    }
}