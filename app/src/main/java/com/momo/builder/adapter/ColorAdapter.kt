package com.momo.builder.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.momo.builder.databinding.ItemColorBinding
import com.momo.builder.u.show

class ColorAdapter(
    private val ctx: Context,
    chooseLabel: Boolean,
    private val choosedColor: String,
    private val clickColor: (color: String) -> Unit
) : Adapter<ColorAdapter.ColorView>() {
    private val colorList = arrayListOf<String>()

    init {
        colorList.clear()
        if (chooseLabel) {
            colorList.add("#F36E9D")
            colorList.add("#F3EDEF")
            colorList.add("#E6F2FF")
            colorList.add("#F1EFFF")
            colorList.add("#F8FFE6")
            colorList.add("#FFF2E6")
        } else {
            colorList.add("#575757")
            colorList.add("#E20E0E")
            colorList.add("#F7B21A")
            colorList.add("#1CDD4F")
            colorList.add("#5E68F2")
            colorList.add("#9B24EF")
        }
    }

    inner class ColorView(view: ItemColorBinding) : ViewHolder(view.root) {
        val ivColor = view.ivColor
        val ivSel = view.iconSel

        init {
            view.root.setOnClickListener {
                clickColor.invoke(colorList[layoutPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorView {
        return ColorView(ItemColorBinding.inflate(LayoutInflater.from(ctx), parent, false))
    }

    override fun getItemCount(): Int = colorList.size

    override fun onBindViewHolder(holder: ColorView, position: Int) {
        val color = colorList[position]
        holder.ivSel.show(color == choosedColor)
        holder.ivColor.setBackgroundColor(Color.parseColor(color))
    }
}