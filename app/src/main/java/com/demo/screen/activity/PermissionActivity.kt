package com.demo.screen.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.demo.screen.activity.Constant.RECORD_REQUEST_CODE
import com.demo.screen.service.RecordService

class PermissionActivity : Activity() {


    private var projectionManager: MediaProjectionManager? = null
    /** 屏幕录制控制 */
    private var mRecordService: RecordService.RecordBinder? = null


    /** 服务绑定回调 */
    private var mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mRecordService = null
            Log.d("record","服务解绑")

        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mRecordService = service as RecordService.RecordBinder
            Log.d("record","服务绑定")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = Intent(this, RecordService::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)

        val captureIntent = projectionManager?.createScreenCaptureIntent()
        startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mRecordService?.onRequestResult(requestCode, resultCode, data)
        finish()
    }
}
