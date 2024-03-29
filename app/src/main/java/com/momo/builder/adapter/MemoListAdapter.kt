package com.momo.builder.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chinalwb.are.android.inner.Html
import com.momo.builder.bean.MemoBean
import com.momo.builder.databinding.ItemEmeoBinding
import com.momo.builder.u.MemoU
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoListAdapter(
    private val ctx: Context,
    private val clickItem: (bean: MemoBean) -> Unit
) : Adapter<MemoListAdapter.MemoView>() {
    private val list = arrayListOf<MemoBean>()

    init {
        list.addAll(MemoU.list)
    }

    fun setList(list: ArrayList<MemoBean>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class MemoView(view: ItemEmeoBinding) : ViewHolder(view.root) {
        val itemBg = view.itemBg
        val tvTime = view.itemTime
        val tvContent = view.itemContent
        val tvTitle = view.itemTitle

        init {
            view.root.setOnClickListener {
                clickItem.invoke(list[layoutPosition])
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

    }
}