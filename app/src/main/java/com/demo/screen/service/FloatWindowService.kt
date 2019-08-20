package com.demo.screen.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import cn.nineton.record.ui.floatwindow.FloatViewController

/**
 * *******************************
 * 猿代码: Wss
 * Email: wusongsong@nineton.cn
 * 时间轴：2019-08-19 15:37
 * *******************************
 *
 * 描述：
 *
 */
class FloatWindowService: Service() {

    private lateinit var floatViewController: FloatViewController

    override fun onBind(intent: Intent): IBinder {
        return RecordBinder()
    }


    fun showFloatView(){
        if (::floatViewController.isInitialized.not()){
            floatViewController = FloatViewController()
        }
        floatViewController.initFloatWindow()
    }


    inner class RecordBinder: Binder(){
        fun getService(): FloatWindowService {
            return this@FloatWindowService
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::floatViewController.isInitialized){
            floatViewController.removeAllView()
        }
        return super.onUnbind(intent)
    }
}