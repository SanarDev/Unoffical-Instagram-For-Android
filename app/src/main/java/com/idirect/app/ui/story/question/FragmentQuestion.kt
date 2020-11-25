package com.idirect.app.ui.story.question

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.R
import com.idirect.app.databinding.FragmentQuestionBinding
import com.idirect.app.extensions.color
import com.idirect.app.extentions.isColorDark
import com.idirect.app.utils.BitmapUtils
import com.sanardev.instagramapijava.model.story.QuestionSticker
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FragmentQuestion(
    val questionSticker: QuestionSticker,
    val callback:ActionListener
) : DialogFragment() {

    companion object{
        const val QUESTION = "question"
    }
    @Inject
    lateinit var mHandler:Handler
    private lateinit var binding: FragmentQuestionBinding
    private lateinit var mGlide:RequestManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        val drawable = BitmapDrawable(resources, BitmapUtils.fastblur(BitmapUtils.takeScreenShot(requireParentFragment().view),100))
        dialog!!.window!!.apply {
            setBackgroundDrawable(drawable)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_question,container,false)
        mGlide = Glide.with(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mGlide.load(questionSticker.profilePicUrl).into(binding.imgProfile)
        binding.txtQuestion.text = questionSticker.question
        binding.cardView.setCardBackgroundColor(Color.parseColor(questionSticker.backgroundColor))
        binding.txtQuestion.setTextColor(Color.parseColor(questionSticker.textColor))
        binding.edtAnswer.setTextColor(Color.parseColor(questionSticker.textColor))
        binding.edtAnswer.setHintTextColor(Color.parseColor(questionSticker.textColor))

        if(isColorDark(Color.parseColor(questionSticker.backgroundColor))){
            binding.btnSend.setTextColor(color(R.color.bg_blue_apply))
            binding.btnSend.setBackgroundResource(R.drawable.bg_question_send_light_button)
        }else{
            binding.btnSend.setTextColor(Color.WHITE)
            binding.btnSend.setBackgroundResource(R.drawable.bg_question_send_dark_button)
        }

        binding.edtAnswer.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.isNullOrBlank()){
                    binding.btnSend.visibility = View.GONE
                }else{
                    binding.btnSend.visibility = View.VISIBLE
                }
            }
        })
        binding.btnSend.setOnClickListener {
            callback.onSendResponse(binding.edtAnswer.text.toString())
            binding.edtAnswer.isEnabled = false
            binding.btnSend.setText(R.string.question_response_sent)
            if(isColorDark(Color.parseColor(questionSticker.backgroundColor))){
                binding.btnSend.setTextColor(color(R.color.positive_tally))
            }else{
                binding.btnSend.setBackgroundResource(R.drawable.bg_question_send_success)
            }
            mHandler.postDelayed({
                dismiss()
            },1000)
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback.onDismiss()
    }
}