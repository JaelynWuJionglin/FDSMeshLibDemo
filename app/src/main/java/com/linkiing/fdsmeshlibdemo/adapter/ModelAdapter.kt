package com.linkiing.fdsmeshlibdemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.bean.ModelInfo

/**
 * 设备与组列表
 */
class ModelAdapter(private var context: Context, private var modelList: MutableList<ModelInfo> )  : RecyclerView.Adapter<ModelAdapter.MyHolder>() {

    private var onItemClickListener: (Int, Int) -> Unit = {_: Int, _: Int ->}


    fun setOnItemClickListener(onItemClickListener: (Int,Int) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    fun updateModeList(modelList: MutableList<ModelInfo>){
        this.modelList=modelList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_model_item, parent, false)
        return MyHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val mode = modelList[position]
        holder.tv_name.text = mode.name

        //长按事件
//        holder.itemView.setOnLongClickListener {
//            onItemClickListener(position,mode.address)
//            false
//        }
        //点击事件
        holder.itemView.setOnClickListener{
            onItemClickListener(position,mode.address)
        }
        if(position==modelList.size-1){
            holder.tv_line.visibility=View.GONE
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_line = itemView.findViewById<View>(R.id.tv_line)
    }
}