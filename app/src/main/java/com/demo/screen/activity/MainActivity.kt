package com.demo.screen.activity

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.demo.screen.R
import com.demo.screen.activity.Constant.FLOAT_REQUEST_CODE
import com.demo.screen.event.RecordEvent
import com.demo.screen.service.FloatWindowService
import com.demo.screen.service.RecordService
import com.demo.screen.utils.PermissionDispatcher
import com.demo.screen.utils.TempFileUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

class MainActivity : AppCompatActivity(), RecordService.RecordListener {


    /** 屏幕录制控制 */
    private var mRecordService: RecordService.RecordBinder? = null
    /** 悬浮窗控制service*/
    private lateinit var floatService: FloatWindowService
    private val mRecordServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {

        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val myBinder = binder as FloatWindowService.RecordBinder
            floatService = myBinder.getService()
        }
    }


    /** 服务绑定回调 */
    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mRecordService = null
            Log.d("record", "服务解绑")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mRecordService = service as RecordService.RecordBinder
            mRecordService!!.setListener(this@MainActivity)
            Log.d("record", "服务绑定")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {

        //初始化EventBus
        EventBus.getDefault().register(this)

        bindRecordService()

        //显示悬浮框
        val intent = Intent(this, FloatWindowService::class.java)
        bindService(intent, mRecordServiceConn, Context.BIND_AUTO_CREATE)

        btnShow.setOnClickListener {
            requestFloatPermission()
        }
    }


    private fun startRecord(it: RecordService.RecordBinder) {
        Log.d("record","开始录屏")
        PermissionDispatcher.checkAndRequest(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            grantedMethod = {
                TempFileUtil.createDir()
                it.requestPermission()
            },
            deniedMethod = {
                ToastUtils.showLong("我们需要必要的权限才能继续工作，请在设置中打开")
            },
            rationaleMethod = {
                ToastUtils.showLong("我们需要必要的权限才能继续工作，请在设置中打开")
            })
    }


    /**
     * 绑定录屏的Service
     */
    private fun bindRecordService() {

        bindService(Intent(this, RecordService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
    }


    @Subscribe
    fun onRecordEvent(event: RecordEvent) {
        mRecordService?.let {
            startRecord(it)
        }
    }


    /**
     * 请求悬浮窗权限
     */
    private fun requestFloatPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, FLOAT_REQUEST_CODE)
        } else {
            floatService.showFloatView()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mRecordService?.onRequestResult(requestCode, resultCode, data)
    }


    override fun onRecordStatusChanged(recordStatus: Int, msg: String) {

    }

    override fun requestRecordPermission(intent: Intent) {
        startActivity(Intent(this, PermissionActivity::class.java))
    }

    override fun onActivity() = this

    override fun onRecordEnd(file: File) {
        Logger.d("录屏文件已保存到${file.absolutePath}")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
