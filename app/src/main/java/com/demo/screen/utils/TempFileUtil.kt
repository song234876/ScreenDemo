package com.demo.screen.utils

import android.os.Environment
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils

/**
 * *******************************
 * 猿代码: Lxw
 * Email: longxuewei@nineton.cn
 * 时间轴：2019-04-18 11:34
 * *******************************
 *
 * 描述：
 *
 */
object TempFileUtil {


    /**
     * 创建目录
     */
    fun createDir() {
        FileUtils.createOrExistsDir(getVideoDir())
    }


    /**
     * 获取视频保存目录
     */
    fun getVideoDir(): String = "${Environment.getExternalStorageDirectory()}/ScreenDemo"
}