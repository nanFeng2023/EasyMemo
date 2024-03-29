package com.momo.builder.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.momo.builder.R
import com.momo.builder.bean.FuncBean
import com.momo.builder.databinding.ItemFuncBinding

class EditFuncAdapter(
    private val ctx:Context,
    private var fontColor:String,
    private val clickItem:(index:Int)->Unit
):Adapter<EditFuncAdapter.FuncView>() {
    private val list= arrayListOf(
        FuncBean(R.drawable.icon_func1,R.drawable.icon_func7),
        FuncBean(R.drawable.icon_func2,R.drawable.icon_func8),
        FuncBean(R.drawable.icon_func3,R.drawable.icon_func9),
        FuncBean(R.drawable.icon_func4,R.drawable.icon_func4),
        FuncBean(R.drawable.icon_func5,R.drawable.icon_func10),
//        FuncBean(R.drawable.icon_func6,R.drawable.icon_func6),
    )

    private val chooseList= arrayListOf<String>()

    fun setFontColor(color:String){
        fontColor=color
        notifyDataSetChanged()
    }

    inner class FuncView(view:ItemFuncBinding):ViewHolder(view.root){
        val iconFunc=view.iconFunc
        init {
            view.root.setOnClickListener {
                val s = layoutPosition.toString()
                if (chooseList.contains(s)){
                    chooseList.remove(s)
                }else{
                    chooseList.add(s)
                }
                notifyDataSetChanged()
                clickItem.invoke(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuncView {
        return FuncView(ItemFuncBinding.inflate(LayoutInflater.from(ctx),parent,false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: FuncView, position: Int) {
        val funcBean = list[position]
        holder.iconFunc.setImageResource(
            if (chooseList.contains(position.toString())) funcBean.selIcon
            else funcBean.unsIcon
        )
        if (position==3){
            holder.iconFunc.setImageResource(
                when(fontColor){
                    "#575757"->R.drawable.color1
                    "#E20E0E"->R.drawable.color2
                    "#F7B21A"->R.drawable.color3
                    "#1CDD4F"->R.drawable.color4
                    "#5E68F2"->R.drawable.color5
                    "#9B24EF"->R.drawable.color6
                    else->R.drawable.icon_func4
                }
            )
        }
    }
}