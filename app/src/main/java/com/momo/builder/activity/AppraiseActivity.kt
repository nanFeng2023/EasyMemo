package com.momo.builder.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.momo.builder.R
import com.momo.builder.baseactivity.BaseViewBinding
import com.momo.builder.databinding.ActivityAppraiseBinding
import com.momo.builder.u.Constant

class AppraiseActivity : BaseViewBinding<ActivityAppraiseBinding>() {
    private lateinit var optionView: List<AppCompatTextView>
    private var optionText = ""
    override fun initViewBinding(): ActivityAppraiseBinding {
        return ActivityAppraiseBinding.inflate(layoutInflater)
    }

    override fun onView() {
        optionView = arrayListOf(
            binding.option1,
            binding.option2,
            binding.option3,
            binding.option4,
            binding.option5
        )
    }

    override fun setClickListener() {
        binding.ivReturn.setOnClickListener { finish() }
        binding.option1.setOnClickListener {
            resetAndSetOption(0)
        }
        binding.option2.setOnClickListener {
            resetAndSetOption(1)
        }
        binding.option3.setOnClickListener {
            resetAndSetOption(2)
        }
        binding.option4.setOnClickListener {
            resetAndSetOption(3)
        }
        binding.option5.setOnClickListener {
            resetAndSetOption(4)
        }
        binding.atvSubmit.setOnClickListener {
            val text = binding.aetInput.text.toString()
            runCatching {
                val gmail = Constant.EMAIL
                val addresses = arrayOf(gmail)
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:$gmail")
                intent.putExtra(Intent.EXTRA_EMAIL, addresses)
                intent.putExtra(Intent.EXTRA_SUBJECT, optionText)
                if (text.isNotEmpty()) {
                    intent.putExtra(Intent.EXTRA_TEXT, text)
                }
                val chooserIntent = Intent.createChooser(intent, "Select email")
                if (chooserIntent != null) {
                    startActivity(chooserIntent)
                } else {
                    Toast.makeText(this, "Please set up a Mail account", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(this, "Please set up a Mail account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetAndSetOption(index: Int) {
        binding.atvSubmit.isEnabled = true
        binding.atvSubmit.setBackgroundResource(R.mipmap.ic_rate_bg2)
        for (view in optionView) {
            view.setBackgroundResource(R.drawable.shape_round15)
            view.setTextColor(Color.BLACK)
        }
        val textView = optionView[index]
        textView.setBackgroundResource(R.drawable.shape_round15_2)
        textView.setTextColor(Color.parseColor("#1C259C"))
        optionText = textView.text.toString()
    }

}