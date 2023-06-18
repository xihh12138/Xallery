package com.xihh.base.media

import android.content.Context
import android.media.*
import android.media.AudioManager.AudioPlaybackCallback
import android.media.AudioManager.AudioRecordingCallback
import android.os.Build
import androidx.annotation.RequiresApi
import com.xihh.base.android.appContext
import com.xihh.base.util.logx

class SpeakListener {

    /**
     * 麦克风监听
     **/
    private val audioRecordingCallback = @RequiresApi(Build.VERSION_CODES.N)
    object : AudioRecordingCallback() {
        override fun onRecordingConfigChanged(configs: MutableList<AudioRecordingConfiguration>?) {
            super.onRecordingConfigChanged(configs)
            logx { "SpeakListener: onRecordingConfigChanged   configs=${configs?.joinToString()}" }
        }
    }

    /**
     * 听筒/扬声器监听
     **/
    private val audioPlaybackCallback = @RequiresApi(Build.VERSION_CODES.O)
    object : AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
            super.onPlaybackConfigChanged(configs)
            logx { "SpeakListener: onPlaybackConfigChanged   configs=${configs?.joinToString()}" }
        }
    }

    /**
     * 媒体设备监听
     **/
    private val audioDeviceCallback = @RequiresApi(Build.VERSION_CODES.M)
    object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesAdded(addedDevices)
            logx { "SpeakListener: onAudioDevicesAdded   addedDevices=${addedDevices?.joinToString()}" }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesRemoved(removedDevices)
            logx { "SpeakListener: onAudioDevicesRemoved   removedDevices=${removedDevices?.joinToString()}" }
        }
    }

    fun init() {
        val audioManager = (appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        audioManager.registerAudioRecordingCallback(audioRecordingCallback, null)
        audioManager.registerAudioPlaybackCallback(audioPlaybackCallback, null)
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }
}