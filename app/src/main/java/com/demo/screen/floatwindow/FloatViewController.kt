package cn.nineton.record.ui.floatwindow

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ScreenUtils
import com.demo.screen.BaseApplication
import com.demo.screen.R
import com.demo.screen.event.RecordEvent
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.window_start_record_small.view.*
import org.greenrobot.eventbus.EventBus

/**
 * *******************************
 * 猿代码: Wss
 * Email: wusongsong@nineton.cn
 * 时间轴：2019-07-31 16:57
 * *******************************
 *
 * 描述：
 *
 */
class FloatViewController {


    private var mScreenWidth: Int = 0
    private var mScreenHeight: Int = 0
    private var mWindowManager: WindowManager

    private lateinit var mWindowLayoutParams: WindowManager.LayoutParams

    private lateinit var mSmallBallContainer: ViewGroup
    private lateinit var mRecordIv: ImageView
    private lateinit var mRecordStart: TextView


    constructor() {
        mWindowManager = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initScreenSize()
        initView()
    }


    private fun getContext(): Context {
        return BaseApplication.context
    }

    private fun initView() {
        mSmallBallContainer = View.inflate(getContext(), R.layout.window_start_record_small, null) as ViewGroup
        mRecordIv = mSmallBallContainer.ivRecordIcon
        mRecordStart = mSmallBallContainer.tvStart

        mRecordIv.setOnClickListener {
            onStartRecord()
        }

    }

    /**
     * 开始录屏
     */
    private fun onStartRecord() {
        Logger.d("悬浮窗点开始录屏")
        EventBus.getDefault().post(RecordEvent())
    }

    fun initFloatWindow() {
        setSmallLayoutParams()

        mSmallBallContainer.scaleX = 1f
        mSmallBallContainer.scaleY = 1f

        mWindowManager.addView(mSmallBallContainer, mWindowLayoutParams)
    }


    private fun initScreenSize() {
        mScreenWidth = ScreenUtils.getScreenWidth()
        mScreenHeight = ScreenUtils.getScreenHeight()
    }

    private fun setSmallLayoutParams() {
        if (::mWindowLayoutParams.isInitialized.not()) {
            val x = mScreenWidth
            val y = mScreenHeight / 3 * 2
            val flags = getFloatWindowFlags()
            val type = getFloatWindowType()
            val size = ViewGroup.LayoutParams.WRAP_CONTENT
            mWindowLayoutParams = WindowManager.LayoutParams(size, size, type, flags, PixelFormat.TRANSLUCENT)
            mWindowLayoutParams.gravity = Gravity.TOP or Gravity.START
            mWindowLayoutParams.x = x
            mWindowLayoutParams.y = y
        }
    }

    /**
     * 移除所有悬浮窗视图
     */
    fun removeAllView() {
        mSmallBallContainer.visibility = View.INVISIBLE

        mWindowManager.removeView(mSmallBallContainer)

    }


    private fun getFloatWindowFlags(): Int {
        return WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    }


    private fun getFloatWindowType(): Int {
        val type: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE
        }
        return type
    }


}