package com.demo.screen.service

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import com.blankj.utilcode.util.ScreenUtils
import com.demo.screen.activity.Constant.RECORD_REQUEST_CODE
import com.demo.screen.utils.TempFileUtil
import com.orhanobut.logger.Logger
import java.io.File


/**
 * *******************************
 * 猿代码: Lxw
 * Email: longxuewei@nineton.cn
 * 时间轴：2019-04-11 10:48
 * *******************************
 *
 * 描述：录屏Service的，此Service，主要和[HomeFragment]进行绑定切相互调用，录屏的逻辑大部分在此类中实现。
 *
 */
class RecordService : Service() {

    /** 录制监听 */
    private var mClient: RecordListener? = null

    /** 申请录屏实例 */
    private var mMediaProjectionManager: MediaProjectionManager? = null

    /** 录制实例 */
    private var mMediaRecorder: MediaRecorder? = null

    private var mMediaProjection: MediaProjection? = null
    private var mCurrentRecordOutputFile: File? = null


    /**
     * 返回Binder供Activity调用
     */
    override fun onBind(intent: Intent?): IBinder {
        return RecordBinder()
    }


    /**
     * 开始录制
     */
    private fun startRecord() {
        mMediaProjection?.let { prepareRecorder(it) }
    }


    /**
     * 停止录制
     */
    private fun stopRecord() {

        mMediaRecorder?.setOnErrorListener(null)
        mMediaRecorder?.setOnInfoListener(null)
        mMediaRecorder?.setPreviewDisplay(null)

        mMediaRecorder?.stop()
        mMediaRecorder?.release()
        mMediaRecorder = null
        mClient?.onRecordEnd(mCurrentRecordOutputFile!!)
        Logger.d("停止录制...")
    }


    /**
     * 在录制之前，需要向用户请求权限
     */
    fun requestPermission() {
        mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mMediaProjectionManager!!.createScreenCaptureIntent()
        mClient?.requestRecordPermission(intent)
    }


    /**
     * 请求结果接收: 权限请求用户做出回应--->由Activity转发到此。
     * 如果用户同意，将做下一步准备
     */
    private fun onRequestResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.d("录屏权限返回")
        when (requestCode) {
            //录屏权限返回
            RECORD_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        Logger.d("录屏权限请求成功")
                        mMediaProjectionManager?.let {
                            mMediaProjection = it.getMediaProjection(resultCode, data!!)
                            startRecord()
                        }
                    }
                    RESULT_CANCELED -> {
                        Logger.d("录屏权限请求失败")
                    }
                }
            }
        }

    }


    /**
     * 准备录制所需要的参数格式等信息
     */
    private fun prepareRecorder(mediaProjection: MediaProjection) {
        mCurrentRecordOutputFile = File(TempFileUtil.getVideoDir(), "${System.currentTimeMillis()}.mp4")

        val recordWidth = 1080
        val recordHeight = 1920


        try {
            mMediaRecorder = MediaRecorder().apply {

                this.setOnErrorListener { _, what, extra -> Logger.d("MediaRecord出现错误:  $what...$extra") }

                //音频来源
                this.setAudioSource(MediaRecorder.AudioSource.MIC)
                //视频源
                this.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                //输出格式
                this.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                //输出文件
                this.setOutputFile(mCurrentRecordOutputFile!!.absolutePath)

                this.setVideoSize(recordWidth, recordHeight)

                //视频编码格式
                this.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                //音频编码格式
                this.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                //比特率
                this.setVideoEncodingBitRate((ScreenUtils.getScreenWidth() * ScreenUtils.getScreenHeight() * 8))
                //帧率
                this.setVideoFrameRate(60)
                //设置录屏尺寸
                this.setVideoSize(recordWidth, recordHeight)

                //开始准备
                this.prepare()
            }

            val virtualDisplay = mediaProjection.createVirtualDisplay(
                "Record",
                recordWidth,
                recordHeight,
                ScreenUtils.getScreenDensityDpi(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder!!.surface,
                null,
                null
            )
            try {
                mMediaRecorder?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            mMediaRecorder?.release()
            mMediaRecorder = null
            return
        }

    }

    /**
     * 录制监听接口 用于 Service 调用 Client
     */
    interface RecordListener {
        /**
         * 录制状态改变
         * [isRecording]: 是否正在录制
         * [msg]：附带信息，录制结束提示，保存位置，取消授权，提示等
         */
        fun onRecordStatusChanged(recordStatus: Int, msg: String)

        /**
         * 请求录屏权限
         */
        fun requestRecordPermission(intent: Intent)

        /**
         * Service需要客户端
         */
        fun onActivity(): Activity

        /**
         * 录制结束，回传录制生成的文件
         */
        fun onRecordEnd(file: File)

    }


    /**
     * 暴露给Activity调用, 用于 Client 调用 Service
     */
    internal open inner class RecordBinder : Binder() {


        /**
         * 请求权限
         */
        fun requestPermission() = this@RecordService.requestPermission()


        /**
         * 开始录制
         */
        fun startRecord() = this@RecordService.startRecord()


        /**
         * 停止录制
         */
        fun stopRecord() = this@RecordService.stopRecord()

        /**
         * 请求结果接收
         */
        fun onRequestResult(requestCode: Int, resultCode: Int, data: Intent?) =
            this@RecordService.onRequestResult(requestCode, resultCode, data)


        /**
         * 设置录制的回调监听
         */
        fun setListener(recordListener: RecordListener) {
            this@RecordService.mClient = recordListener
        }
    }
}