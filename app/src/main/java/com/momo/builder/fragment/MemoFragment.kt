package com.momo.builder.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.momo.builder.R
import com.momo.builder.activity.EditActivity
import com.momo.builder.activity.MemoListActivity
import com.momo.builder.adapter.MemoListAdapter
import com.momo.builder.databinding.FragmentMemoBinding
import com.momo.builder.u.EventReportU
import com.momo.builder.u.MemoU

class MemoFragment : Fragment() {
    private var mTabType: String = ""
    private lateinit var binding: FragmentMemoBinding
    var memoListAdapter: MemoListAdapter? = null
    private var mMemoListActivity: MemoListActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTabType = requireArguments().getString("tabType") ?: ""
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMemoListActivity = requireActivity() as MemoListActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memo, container, false)
        binding = FragmentMemoBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() {
        memoListAdapter = MemoListAdapter(mTabType, requireContext()) {
            if (!it.isDone) {
                if (mTabType == "Notes")
                    EventReportU.reportCustomEvent(EventReportU.home_note)
                mMemoListActivity?.toEdit(it)
            }
        }
        memoListAdapter!!.updateTodoCount = {
            if (mTabType == "Todo") {
                mMemoListActivity?.initTodoCount()
            }
        }
        binding.rvMemo.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoListAdapter
//            smoothScrollToPosition(0)
        }
        binding.rvMemo.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                val manager = recyclerView.layoutManager as LinearLayoutManager
//                if (!recyclerView.canScrollVertically(1)) {
//                    mMemoListActivity?.binding?.layoutContent?.viewPager?.isUserInputEnabled = true
//                } else mMemoListActivity?.binding?.layoutContent?.viewPager?.isUserInputEnabled =
//                    manager.findFirstVisibleItemPosition() != 0
            }
        })
    }

    fun setData() {
        memoListAdapter?.setList(MemoU.list)
    }

    companion object {
        @JvmStatic
        fun newInstance(tabType: String): MemoFragment {
            val fragment = MemoFragment()
            val bundle = Bundle()
            bundle.putString("tabType", tabType)
            fragment.arguments = bundle
            return fragment
        }
    }
}