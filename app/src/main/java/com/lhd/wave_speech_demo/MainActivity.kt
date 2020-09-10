package com.lhd.wave_speech_demo

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.base.baselibrary.activity.BaseActivity
import com.lhd.wave_speech_demo.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val listPermission = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    fun grantPermission(onAllow: () -> Unit) {
        doRequestPermission(listPermission, onAllow)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }
}