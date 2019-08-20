package com.demo.screen.utils

import android.annotation.SuppressLint
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils

/**
 *
 * 描述：权限调度工具
 *
 * 函数：
 */
object PermissionDispatcher {

    /**
     * 检测和请求权限，当执行需要权限的方法时，调用此函数，进行权限检测然后再执行对应的方法
     *
     * [permissionName]：需要检测或申请的权限的名字参考[PermissionUtils.isGranted]方法
     * [grantedMethod]：权限请求成功或者已经有对应的权限然后需要执行的方法
     * [deniedMethod]：权限被拒绝需要执行的方法
     * [rationaleMethod]：说明理由的执行方法
     */
    @SuppressLint("WrongConstant")
    fun checkAndRequest(vararg permissionName: String, grantedMethod: () -> Unit = {}, deniedMethod: () -> Unit = {}, rationaleMethod: (shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) -> Unit = {}) {
        if (!PermissionUtils.isGranted(*permissionName)) {
            val needPermissionGroup = getNeedPermissionGroup(*permissionName)
            if (needPermissionGroup.isNotEmpty()) {
                PermissionUtils.permission(*needPermissionGroup).callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        grantedMethod.invoke()
                    }

                    override fun onDenied() {
                        deniedMethod.invoke()
                    }
                }).rationale {
                    rationaleMethod.invoke(it)
                }.request()
            }
        } else {
            grantedMethod.invoke()
        }
    }


    /**
     * 通过用户传入的权限去拿到对应的组进行申请
     * [permissionName]：需要的权限
     * @return 对应的 权限组
     */
    private fun getNeedPermissionGroup(vararg permissionName: String): Array<String> {
        val calendarGroup = PermissionConstants.getPermissions(PermissionConstants.CALENDAR).intersect(permissionName.toList()).isNotEmpty()
        val cameraGroup = PermissionConstants.getPermissions(PermissionConstants.CAMERA).intersect(permissionName.toList()).isNotEmpty()
        val contactsGroup = PermissionConstants.getPermissions(PermissionConstants.CONTACTS).intersect(permissionName.toList()).isNotEmpty()
        val locationGroup = PermissionConstants.getPermissions(PermissionConstants.LOCATION).intersect(permissionName.toList()).isNotEmpty()
        val microphoneGroup = PermissionConstants.getPermissions(PermissionConstants.MICROPHONE).intersect(permissionName.toList()).isNotEmpty()
        val phoneGroup = PermissionConstants.getPermissions(PermissionConstants.PHONE).intersect(permissionName.toList()).isNotEmpty()
        val sensorsGroup = PermissionConstants.getPermissions(PermissionConstants.SENSORS).intersect(permissionName.toList()).isNotEmpty()
        val smsGroup = PermissionConstants.getPermissions(PermissionConstants.SMS).intersect(permissionName.toList()).isNotEmpty()
        val storageGroup = PermissionConstants.getPermissions(PermissionConstants.STORAGE).intersect(permissionName.toList()).isNotEmpty()

        val needRequestPermission = mutableListOf<String>()
        if (calendarGroup) {
            needRequestPermission.add(PermissionConstants.CALENDAR)
        }
        if (cameraGroup) {
            needRequestPermission.add(PermissionConstants.CAMERA)
        }
        if (contactsGroup) {
            needRequestPermission.add(PermissionConstants.CONTACTS)
        }
        if (locationGroup) {
            needRequestPermission.add(PermissionConstants.LOCATION)
        }
        if (microphoneGroup) {
            needRequestPermission.add(PermissionConstants.MICROPHONE)
        }
        if (phoneGroup) {
            needRequestPermission.add(PermissionConstants.PHONE)
        }
        if (sensorsGroup) {
            needRequestPermission.add(PermissionConstants.SENSORS)
        }
        if (smsGroup) {
            needRequestPermission.add(PermissionConstants.SMS)
        }
        if (storageGroup) {
            needRequestPermission.add(PermissionConstants.STORAGE)
        }
        return needRequestPermission.toTypedArray()
    }
}