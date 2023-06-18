package com.xihh.base.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import com.xihh.base.android.appContext
import com.xihh.base.util.Looper
import com.xihh.base.util.logf
import com.xihh.base.util.logx
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class AudioPlayer private constructor(
    viewModel: ViewModel,
    playWhenPrepared: Boolean = false,
    private val playMutex: Boolean = true,
) : Closeable, OnPreparedListener, OnCompletionListener {

    private val scope =
        CoroutineScope(Job() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logf { "AudioPlayer: CoroutineExceptionHandler=${throwable.stackTraceToString()}" }
        })

    private val proxy = MediaPlayer()

    private var dataSource: Any? = null

    private val _playerStateFlow = MutableStateFlow(STATE_IDLE)
    val playerStateFlow = _playerStateFlow.asStateFlow()

    private val _progressFlow = MutableStateFlow<Pair<Int, Int>?>(null)
    val progressFlow = _progressFlow.asStateFlow()

    private var prepareDeferred: CompletableDeferred<Unit>? = null

    private val playWhenPrepared = AtomicBoolean(playWhenPrepared)

    private val looper = Looper {
        _progressFlow.update { position to duration }
    }

    private val lockPrepare = Any()
    private val lockDataSource = Any()

    init {
        viewModel.addCloseable(this)
        val attribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attribute.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE)
        }

        proxy.setAudioAttributes(attribute.build())
        proxy.setOnPreparedListener(this)
        proxy.setOnCompletionListener(this)
        scope.launch {
            _playerStateFlow.stateIn(scope).collectLatest {
                logx { "AudioPlayer: playerStateFlow=$it " }
                when (it) {
                    STATE_PLAY -> looper.start()
                    else -> {
                        looper.pause()
                    }
                }
            }
        }
        proxy.setOnErrorListener { mp, what, extra ->
            logx { "AudioPlayer: setOnErrorListener what=$what  extra=$extra" }
            reset()
            false
        }
        proxy.setOnInfoListener { mp, what, extra ->
            logx { "AudioPlayer: setOnInfoListener what=$what  extra=$extra" }
            false
        }
    }

    fun prepare(path: String) {
        if (setDataSource(appContext, path)) {
            proxy.prepareAsync()
        }
    }

    fun prepare(context: Context, uri: Uri) {
        if (setDataSource(context, uri)) {
            proxy.prepareAsync()
        }
    }

    /**
     * @return 是否需要调用mediaPlayer的prepare方法
     **/
    private fun setDataSource(context: Context, data: Any): Boolean {
        _playerStateFlow.update { STATE_PREPARING }
        if (isClosed) {
            return false
        }
        when (dataSource) {
            data -> {
                scope.launch {
                    logx { "AudioPlayer: setDataSource 已经准备过了相同的音频源，不需要再准备，直接更新一下状态返回" }
                    delay(10)// TODO: 这里需要delay一下才能让流收集者观察到状态变化，暂时没有好的改进方法
                    onPrepared(proxy)
                }
                return false
            }
            null -> {

            }
            else -> {
                // ---------- 播放器准备过音频源，需要重置一下，才能重新设置dataSource，不然会崩溃 ----------
                reset()
            }
        }
        return try {
            synchronized(lockDataSource) {
                if (data is String) {
                    proxy.setDataSource(data)
                } else if (data is Uri) {
                    proxy.setDataSource(context, data)
                }
                dataSource = data
            }
            true
        } catch (e: Exception) {
            logf { "AudioPlayer: setDataSource   准备播放源失败:${e.stackTraceToString()}" }
            false
        }
    }

    /**
     * 这个方法会挂起，首先判断当前播放器是否准备好播放，没有的话就同步准备
     **/
    suspend fun start() {
        when {
            isClosed -> return
            isPreparing -> {
                synchronized(lockPrepare) {
                    prepareDeferred = CompletableDeferred()
                }
                prepareDeferred?.await()
                prepareDeferred = null
            }
            _playerStateFlow.value == STATE_IDLE -> {
                withContext(scope.coroutineContext) {
                    val data = dataSource
                    if (data != null) {
                        setDataSource(appContext, data)
                        proxy.prepare()
                    }
                }
            }
        }
        if (playMutex) {
            stopAll()
        }
        proxy.start()
        _playerStateFlow.update { STATE_PLAY }
    }

    /**
     * 不检查状态直接播放
     **/
    private fun startNow() {
        if (isClosed) {
            return
        }

        if (playMutex) {
            stopAll()
        }
        proxy.start()
        _playerStateFlow.update { STATE_PLAY }
    }

    fun pause() {
        when {
            isClosed || _playerStateFlow.value < STATE_PLAY -> return
        }
        proxy.pause()
        _playerStateFlow.update { STATE_PAUSE }
    }

    fun stop() {
        when {
            isClosed || _playerStateFlow.value < STATE_PAUSE -> return
        }
        proxy.pause()
        proxy.seekTo(0)
        _progressFlow.update { 0 to duration }
        _playerStateFlow.update { STATE_STOP }
    }

    /**
     * 调用这个方法之后，需要重新调用[prepare]才能播放
     **/
    fun reset() {
        if (isClosed) {
            return
        }
        synchronized(lockDataSource) {
            dataSource = null
        }
        _playerStateFlow.update { STATE_IDLE }
        proxy.reset()
    }

    /**
     * 调用这个方法释放播放器资源
     **/
    override fun close() {
        if (isClosed) {
            return
        }
        synchronized(poolLock) {
            audioPlayerPool.remove(this)
        }
        synchronized(lockDataSource) {
            dataSource = null
        }
        looper.close()
        proxy.release()
        _playerStateFlow.update { STATE_CLOSED }
        scope.cancel()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        _playerStateFlow.update { STATE_PREPARED }
        _progressFlow.update { position to duration }
        synchronized(lockPrepare) {
            prepareDeferred?.complete(Unit)
        }
        if (playWhenPrepared.get()) {
            startNow()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        _playerStateFlow.update { STATE_STOP }
    }

    private val isClosed get() = _playerStateFlow.value == STATE_CLOSED
    private val isPreparing get() = _playerStateFlow.value == STATE_PREPARING
    val isPrepared get() = _playerStateFlow.value > STATE_PREPARING
    val isPlaying get() = _playerStateFlow.value == STATE_PLAY

    val position get() = proxy.currentPosition
    val duration get() = proxy.duration
    val progress get() = proxy.currentPosition / proxy.duration

    companion object {

        private val poolLock = Any()

        @JvmStatic
        private val audioPlayerPool = ArrayList<AudioPlayer>()

        fun create(viewModel: ViewModel, playWhenPrepared: Boolean = false): AudioPlayer {
            val audioPlayer = AudioPlayer(viewModel, playWhenPrepared)
            synchronized(poolLock) {
                audioPlayerPool.add(audioPlayer)
            }
            return audioPlayer
        }

        fun stopAll() {
            synchronized(poolLock) {
                audioPlayerPool.forEach {
                    it.stop()
                }
            }
        }

        const val STATE_CLOSED = 0

        /**
         * 没有载入资源，也没有准备
         **/
        const val STATE_IDLE = 1
        const val STATE_PREPARING = 2
        const val STATE_PREPARED = 3
        const val STATE_STOP = 4
        const val STATE_PAUSE = 5
        const val STATE_PLAY = 6

        private const val MEDIA_ERROR = 100
        const val MEDIA_ERROR_ACCESS_TOKEN_EXPIRED = -1020
        const val MEDIA_ERROR_CONNECTION_LOST = -1005
        const val MEDIA_ERROR_IO = -1004
        const val MEDIA_ERROR_MALFORMED = -1007
        const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200
        const val MEDIA_ERROR_SERVER_DIED = 100
        const val MEDIA_ERROR_SYSTEM = Int.MIN_VALUE
        const val MEDIA_ERROR_TIMED_OUT = -110
        const val MEDIA_ERROR_TRANSCODING_CODEC_ALLOCATION_ERROR = -6011
        const val MEDIA_ERROR_TRANSCODING_DRM_CONTENTS_IS_ALREADY_PLAYING = -6012
        const val MEDIA_ERROR_TRANSCODING_LACK_OF_RESOURCE = -6013
        const val MEDIA_ERROR_TRANSCODING_UNSPECIFIED_ERROR = -6100
        const val MEDIA_ERROR_UNKNOWN = 1
        const val MEDIA_ERROR_UNSUPPORTED = -1010
        const val MEDIA_ErrDrmDevCertRevoked = -59
        const val MEDIA_ErrDrmLicenseExpired = 301
        const val MEDIA_ErrDrmLicenseNotFound = 300
        const val MEDIA_ErrDrmLicenseNotValidYet = 302
        const val MEDIA_ErrDrmRightsAcquisitionFailed = -49
        const val MEDIA_ErrDrmServerDeviceLimitReached = -64
        const val MEDIA_ErrDrmServerDomainRequired = -60
        const val MEDIA_ErrDrmServerInternalError = -58
        const val MEDIA_ErrDrmServerNotAMember = -61
        const val MEDIA_ErrDrmServerProtocolVersionMismatch = -63
        const val MEDIA_ErrDrmServerUnknownAccountId = -62
        private const val MEDIA_INFO = 200
        const val MEDIA_INFO_BAD_INTERLEAVING = 800
        const val MEDIA_INFO_BUFFERING_END = 702
        const val MEDIA_INFO_BUFFERING_START = 701
        const val MEDIA_INFO_BUFFERING_TOAST = 777
        const val MEDIA_INFO_CODEC_TYPE_HEVC = 10970
        const val MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803
        const val MEDIA_INFO_METADATA_UPDATE = 802
        const val MEDIA_INFO_NETWORK_BANDWIDTH = 703
        const val MEDIA_INFO_NOT_SEEKABLE = 801
        const val MEDIA_INFO_NO_AUDIO = 10972
        const val MEDIA_INFO_NO_VIDEO = 10973
        const val MEDIA_INFO_STARTED_AS_NEXT = 2
        const val MEDIA_INFO_SUBTITLE_TIMED_OUT = 902
        const val MEDIA_INFO_TIMED_TEXT_ERROR = 900
        const val MEDIA_INFO_UNKNOWN = 1
        const val MEDIA_INFO_UNSUPPORTED_AUDIO = 10950
        const val MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901
        const val MEDIA_INFO_UNSUPPORTED_TICKPLAY = 10953
        const val MEDIA_INFO_UNSUPPORTED_VIDEO = 10951
        const val MEDIA_INFO_VIDEO_RENDERING_START = 3
        const val MEDIA_INFO_VIDEO_TRACK_LAGGING = 700
    }
}
