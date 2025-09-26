package com.linkiing.fdsmeshlibdemo.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding> : Fragment() {
    protected lateinit var binding: B
    protected lateinit var mView: View
    protected lateinit var mActivity: FragmentActivity
    protected lateinit var mContext: Context

    /**
     * 初始化UI绑定类
     * @return xxxBind
     */
    protected abstract fun initBind(inflater: LayoutInflater, container: ViewGroup?): B

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = initBind(inflater, container)
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            mActivity = requireActivity()
            mContext = requireContext()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    protected fun goActivity(cls: Class<*>, isFinish: Boolean) {
        startActivity(Intent(mContext, cls))
        if (isFinish) {
            mActivity.finish()
        }
    }

    protected fun goActivityBundle(cls: Class<*>, isFinish:Boolean,bundle:Bundle){
        val intent= Intent(mContext, cls)
        intent.putExtras(bundle)
        startActivity(intent)
        if (isFinish){
            mActivity.finish()
        }
    }
}