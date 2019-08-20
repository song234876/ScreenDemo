package com.demo.screen

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * *******************************
 * 猿代码: Wss
 * Email: wusongsong@nineton.cn
 * 时间轴：2019-08-19 15:53
 * *******************************
 *
 * 描述：
 *
 */
class BaseApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        context = this
    }


    //伴生对象
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}