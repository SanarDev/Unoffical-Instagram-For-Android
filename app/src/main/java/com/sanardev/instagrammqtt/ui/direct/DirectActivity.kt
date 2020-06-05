package com.sanardev.instagrammqtt.ui.direct

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.databinding.ActivityDirectBinding
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider

class DirectActivity : BaseActivity<ActivityDirectBinding, DirectViewModel>() {

    private lateinit var emojiPopup: EmojiPopup

    override fun layoutRes(): Int {
        return R.layout.activity_direct
    }

    override fun getViewModelClass(): Class<DirectViewModel> {
        return DirectViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)

        emojiPopup =
            EmojiPopup.Builder.fromRootView(binding.layoutParent)
                .setOnEmojiPopupDismissListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
                }.setOnEmojiPopupShownListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
                }.build(binding.edtTextChat);

        binding.edtTextChat.setOnClickListener {
            emojiPopup.dismiss()
        }

    }

    fun onEmojiClick(v: View) {
        if (emojiPopup.isShowing) {
            emojiPopup.dismiss()
        } else {
            emojiPopup.toggle()
        }
    }

}