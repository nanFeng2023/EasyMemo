package com.momo.builder.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.momo.builder.R
import com.momo.builder.activity.AppraiseActivity
import com.momo.builder.databinding.FragmentAppraiseBinding

class BottomAppraiseDialog : BottomSheetDialogFragment() {
    private lateinit var mBinding: FragmentAppraiseBinding
    private var starLevel = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CusBottomDialogTheme)
    }

//    override fun onStart() {
//        super.onStart()
//        dialog?.run {
//            runCatching {
//                val viewGroup = requireView().parent as ViewGroup
//                viewGroup.setBackgroundResource(android.R.color.transparent)
//            }
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val viewGroup = mBinding.root.parent as ViewGroup
//        viewGroup.setBackgroundResource(android.R.color.transparent)

        mBinding.atvRate.setOnClickListener {
            if (starLevel <= 3) {
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        AppraiseActivity::class.java
                    )
                )
                dismiss()
            } else {
                //不模拟界面，只测试流程
//                val manager = FakeReviewManager(requireContext())
                val manager = ReviewManagerFactory.create(requireContext())
                val requestReviewFlow = manager.requestReviewFlow()
                requestReviewFlow.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val reviewInfo = it.result
                        val flow =
                            manager.launchReviewFlow(requireActivity(), reviewInfo)
                        flow.addOnCompleteListener {
                            Log.d("-----", "appraise end")
                            dismiss()
                        }
                    } else {
                        Log.d("-----", "google appraise launch fail")
                    }
                }
            }
        }
        mBinding.ivStar1.setOnClickListener {
            starLevel = 1
            updateStar()
        }
        mBinding.ivStar2.setOnClickListener {
            starLevel = 2
            updateStar()
        }
        mBinding.ivStar3.setOnClickListener {
            starLevel = 3
            updateStar()
        }
        mBinding.ivStar4.setOnClickListener {
            starLevel = 4
            updateStar()
        }
        mBinding.ivStar5.setOnClickListener {
            starLevel = 5
            updateStar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appraise, container, false)
        mBinding = FragmentAppraiseBinding.bind(view)
        return view
    }

    private fun updateStar() {
        if (starLevel > 0) {
            val starList = arrayListOf(
                mBinding.ivStar1,
                mBinding.ivStar2,
                mBinding.ivStar3,
                mBinding.ivStar4,
                mBinding.ivStar5
            )
            for (i in starList.indices) {
                starList[i].setImageResource(R.mipmap.ic_star_black)
            }
            for (i in 0 until starLevel) {
                starList[i].setImageResource(R.mipmap.ic_start_style2)
            }
            mBinding.atvRate.isEnabled = true
            mBinding.atvRate.setBackgroundResource(R.mipmap.ic_rate_bg2)
            mBinding.llcBestAppraise.visibility = View.INVISIBLE
            if (starLevel <= 3) {
                mBinding.atvText1.text = "Oh，we’re sorry…"
                mBinding.atvText2.text = "Your feedback is welcome."
                mBinding.atvRate.text = "Rate"
                mBinding.ivEmote.setImageResource(R.mipmap.ic_emoj_dejected)
            } else {
                mBinding.atvText1.text = "Much appreciated!"
                mBinding.atvText2.text = "Your support is our biggest motivation!"
                mBinding.atvRate.text = "Rate on google play"
                mBinding.ivEmote.setImageResource(R.mipmap.ic_emoj_appraise)
            }
        }
    }


}