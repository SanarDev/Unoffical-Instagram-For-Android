package com.idirect.app.core

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.idirect.app.BR
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.idirect.app.R
import com.idirect.app.databinding.ActivityMainBinding
import com.idirect.app.di.DaggerViewModelFactory
import com.idirect.app.utils.DisplayUtils
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


abstract class BaseActivity<B:ViewDataBinding,VM: BaseViewModel> : AppCompatActivity(),
    HasSupportFragmentInjector {

    private val keyboardLayoutListener = OnGlobalLayoutListener {
        val heightDiff = rootLayout!!.rootView.height - rootLayout!!.height
        val contentViewTop =
            window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
        val broadcastManager =
            LocalBroadcastManager.getInstance(this@BaseActivity)
        if (heightDiff <= DisplayUtils.getScreenHeight() / 5) {
            if(isKeyboardShow){
                isKeyboardShow = false
                onHideKeyboard()
            }
            val intent = Intent("KeyboardWillHide")
            broadcastManager.sendBroadcast(intent)
        } else {
            val keyboardHeight = heightDiff - contentViewTop
            if(!isKeyboardShow){
                isKeyboardShow = true
                onShowKeyboard(keyboardHeight)
            }
            val intent = Intent("KeyboardWillShow")
            intent.putExtra("KeyboardHeight", keyboardHeight)
            broadcastManager.sendBroadcast(intent)
        }
    }

    private var keyboardListenersAttached = false
    private var isKeyboardShow = false
    private var rootLayout: ViewGroup? = null
    lateinit var binding: B
    lateinit var viewModel: VM

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    internal lateinit var viewModelFactory: DaggerViewModelFactory

    override fun supportFragmentInjector() = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes())
        viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModelClass())
        binding.setVariable(BR.viewModel, viewModel)
    }


    protected open fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }
        rootLayout = binding.root as ViewGroup
        rootLayout!!.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (keyboardListenersAttached) {
            rootLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(keyboardLayoutListener)
        }
    }

    override fun onPause() {
        super.onPause()
        BaseApplication.isAppInOnStop = true
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.isAppInOnStop = false
    }

    abstract fun layoutRes(): Int
    abstract fun getViewModelClass(): Class<VM>
    protected open fun onShowKeyboard(keyboardHeight: Int) {}
    protected open fun onHideKeyboard() {}
 }