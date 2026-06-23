package com.linkiing.fdsmeshlibdemo.view.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import com.linkiing.fdsmeshlibdemo.R
import com.linkiing.fdsmeshlibdemo.databinding.StuPaBottomDialogLayoutBinding
import androidx.core.graphics.drawable.toDrawable
import com.godox.sdk.api.FDSMeshApi

class StuPaBottomMenuDialog(private val context: Context) :
    BaseFullDialog<StuPaBottomDialogLayoutBinding>(context) {
    private var listener: (Int) -> Unit = {}
    private var adapter: ArrayAdapter<String>? = null
    private val dataList = mutableListOf<String>()

    init {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.bomToTopDialogAnim) //设置窗口弹出动画
        window?.decorView?.setPadding(0, 0, 0, 0)
        window?.setGravity(Gravity.BOTTOM)
    }

    /**
     * 设置按钮的显示内容和监听
     * @param listener
     */
    fun setOnDialogListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    /**
     * showDialog
     */
    fun showDialog() {
        if (!isShowing) {
            updateData()
            super.show()
        }
    }

    /**
     * dismissDialog
     */
    fun dismissDialog() {
        if (isShowing) {
            super.dismiss()
        }
    }

    override fun initBind(): StuPaBottomDialogLayoutBinding {
        return StuPaBottomDialogLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        binding.tvCancel.setOnClickListener {
            dismissDialog()
        }

        updateDataList()
        adapter = ArrayAdapter(context, R.layout.item_custom_text, dataList)
        binding.listView.setAdapter(adapter)

        binding.listView.setOnItemClickListener { parent, view, position, id ->
            if (position >= 0 && position < dataList.size) {
                when (val itemStr = dataList[position]) {
                    "NOT_PA" -> {
                        listener(0)
                    }

                    "NEED_PA" -> {
                        listener(1)
                    }

                    else -> {
                        val strPa = itemStr.removePrefix("FM_TYPE_")
                        val isPa = try {
                            Integer.parseInt(strPa)
                        } catch (n: NumberFormatException) {
                            n.printStackTrace()
                            -1
                        }
                        listener(isPa)
                    }
                }
            }
            dismissDialog()
        }
    }

    private fun updateDataList() {
        dataList.clear()
        dataList.add("NOT_PA") //非PA
        dataList.add("NEED_PA") //需要开启PA指令
        for (node in FDSMeshApi.instance.getFDSNodes()) {
            if (node.isPA != 0 && node.isPA != 1 ) {
                //其他类型
                val fmType = "FM_TYPE_${node.isPA}"
                if (!dataList.contains(fmType)) {
                    dataList.add(fmType)
                }
            }
        }
    }

    private fun updateData() {
        updateDataList()
        adapter?.notifyDataSetChanged()
    }
}