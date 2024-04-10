package com.momo.builder.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chinalwb.are.android.inner.Html
import com.momo.builder.R
import com.momo.builder.activity.MemoListActivity
import com.momo.builder.bean.MemoBean
import com.momo.builder.databinding.ItemEmeoBinding
import com.momo.builder.u.MemoU
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoListAdapter(
    private val memoType: String,
    private val ctx: Context,
    private val clickItem: (bean: MemoBean) -> Unit
) : Adapter<MemoListAdapter.MemoView>() {
    private val list = arrayListOf<MemoBean>()

    lateinit var updateTodoCount: () -> Unit

    init {
        selectGoalData(MemoU.list)
    }

    fun setList(list: ArrayList<MemoBean>) {
        this.list.clear()
        selectGoalData(list)
        notifyDataSetChanged()
    }

    fun searchList(list: ArrayList<MemoBean>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    private fun selectGoalData(li: ArrayList<MemoBean>) {
        when (memoType) {
            MemoListActivity.NOTES -> {
                val mList = ArrayList<MemoBean>()
                for (b in li) {
                    if (b.type == 0) {
                        mList.add(b)
                    }
                }
                if (mList.isNotEmpty())
                    list.addAll(mList)
            }

            MemoListActivity.TODO -> {
                val mList = ArrayList<MemoBean>()
                for (b in li) {
                    if (b.type == 1 && !b.isDone) {
                        mList.add(b)
                    }
                }
                if (mList.isNotEmpty())
                    list.addAll(mList)
            }

            MemoListActivity.DONE -> {
                val mList = ArrayList<MemoBean>()
                for (b in li) {
                    if (b.isDone) {
                        mList.add(b)
                    }
                }
                if (mList.isNotEmpty())
                    list.addAll(mList)
                list.sortByDescending { it.time }
            }

            else -> list.addAll(li)
        }
    }

    inner class MemoView(view: ItemEmeoBinding) : ViewHolder(view.root) {
        val itemBg = view.itemBg
        val tvTime = view.itemTime
        val tvContent = view.itemContent
        val tvTitle = view.itemTitle
        val ivSelect = view.itemSelect

        init {
            view.root.setOnClickListener {
                clickItem.invoke(list[layoutPosition])
            }
            ivSelect.setOnClickListener {
                val memo = list[adapterPosition]
                memo.isDone = !memo.isDone
                if (memo.isDone) {
                    memo.time = System.currentTimeMillis()
                    ivSelect.setImageResource(R.mipmap.ic_selected)
                } else {
                    ivSelect.setImageResource(R.mipmap.ic_select)
                }
                ivSelect.postDelayed({
                    val mLi = arrayListOf<MemoBean>()
                    mLi.addAll(list)
                    setList(mLi)
                    updateTodoCount.invoke()
                }, 800)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoView =
        MemoView(ItemEmeoBinding.inflate(LayoutInflater.from(ctx), parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MemoView, position: Int) {
        val memoBean = list[position]
        holder.itemBg.setBackgroundColor(Color.parseColor(memoBean.labelColor))
        holder.tvTime.text =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(memoBean.time))
        holder.tvContent.text = Html.fromHtml(memoBean.htmlContent)
        holder.tvTitle.text = memoBean.title
        if (memoBean.labelColor == "#F36E9D") {
            holder.tvTitle.setTextColor(Color.WHITE)
            holder.tvTime.setTextColor(Color.WHITE)
            holder.tvContent.setTextColor(Color.WHITE)
        } else {
            holder.tvTitle.setTextColor(ctx.getColor(R.color.html_color))
            holder.tvTime.setTextColor(ctx.getColor(R.color.html_color))
            holder.tvContent.setTextColor(ctx.getColor(R.color.html_color))
        }
        if (memoBean.type == 0) {
            holder.ivSelect.visibility = View.GONE
        } else {
            holder.ivSelect.visibility = View.VISIBLE
        }
        if (memoBean.isDone) {
            holder.ivSelect.setImageResource(R.mipmap.ic_selected)
        } else {
            holder.ivSelect.setImageResource(R.mipmap.ic_select)
        }
    }
}