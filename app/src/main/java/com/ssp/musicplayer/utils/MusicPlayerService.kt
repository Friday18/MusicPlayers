package com.ssp.musicplayer.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ssp.musicplayer.CHANNEL_ID
import com.ssp.musicplayer.R
import com.ssp.musicplayer.data.Track
import com.ssp.musicplayer.data.songs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val PREV = "prev_song"
const val NEXT = "next_song"
const val PLAY_PAUSE = "play_pause"

class MusicPlayerService : Service() {

    val binder = MusicBinder()

    private var mediaPlayer = MediaPlayer()

    private var currentTrack = MutableStateFlow<Track>(Track())

    private var musicList = mutableListOf(Track())

    private val maxDuration = MutableStateFlow(0f)

    private val currentDuration = MutableStateFlow(0f)

    private val scope = CoroutineScope(Dispatchers.Main)

    private var job: Job? = null

    private var isPlaying = MutableStateFlow(false)

    inner class MusicBinder : Binder() {
        fun getService() = this@MusicPlayerService

        fun setMusicList(list: List<Track>) {
            this@MusicPlayerService.musicList = list.toMutableList()
        }

        fun currentDuration() = this@MusicPlayerService.currentDuration

        fun maxDuration() = this@MusicPlayerService.maxDuration

        fun isPlaying() = this@MusicPlayerService.isPlaying

        fun getCurrentTrack() = this@MusicPlayerService.currentTrack
    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (intent.action) {
                PREV -> {
                    prev()
                }

                PLAY_PAUSE -> {
                    playPause()
                }

                NEXT -> {
                    next()
                }

                else -> {
                    currentTrack
                        .update { songs.get(0) }
                    play(currentTrack.value)
                }
            }
        }


        return START_STICKY
    }

    fun updateDuration() {
        job = scope.launch {
            if (mediaPlayer.isPlaying.not()) return@launch

            maxDuration.update { mediaPlayer.duration.toFloat() }
            while (true) {
                currentDuration.update { mediaPlayer.currentPosition.toFloat() }
                delay(1000)
            }
        }

    }

    fun playPause() {
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
        else mediaPlayer.start()
        sendNotification(track = currentTrack.value)
    }

    fun next() {
        job?.cancel()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()
        val index = musicList.indexOf(currentTrack.value)
        val nextIndex = index.plus(1).mod(musicList.size)
        val nextItem = musicList[nextIndex]

        currentTrack.update { nextItem }
        mediaPlayer.apply {

            setDataSource(
                this@MusicPlayerService, getRawURI(
                    nextItem.id
                )
            )
            prepareAsync()
            setOnPreparedListener {
                mediaPlayer.start()
                sendNotification(currentTrack.value)
                updateDuration()
            }
        }
    }

    fun prev() {
        job?.cancel()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()

        val index = musicList.indexOf(currentTrack.value)
        val prevIndex = if (index < 0) musicList.size.minus(1) else index.minus(1)
        val prevItem = musicList[prevIndex]
        currentTrack.update { prevItem }
        mediaPlayer.apply {

            setDataSource(
                this@MusicPlayerService,
                getRawURI(currentTrack.value.id)
            )
            prepareAsync()
            setOnPreparedListener {
                mediaPlayer.start()
                sendNotification(currentTrack.value)
                updateDuration()
            }
        }
    }

    private fun play(track: Track) {

        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(
            this, getRawURI(
                track.id
            )
        )
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            sendNotification(track)
            updateDuration()
        }

    }


    fun getRawURI(id: Int) = Uri.parse("android.resource://${packageName}/${id}")

    private fun sendNotification(track: Track) {

        val session = MediaSessionCompat(this, "music")
        isPlaying.update { mediaPlayer.isPlaying }
        val style =
            androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2).setMediaSession(session.sessionToken)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(style)
            .setContentTitle(track.name)
            .setContentText(track.desc)
            .addAction(R.drawable.previous, "prev", createPrevPendingIntent())
            .addAction(R.drawable.next, "next", createNextPendingIntent())
            .addAction(
                if (mediaPlayer.isPlaying) R.drawable.pause else R.drawable.play,
                "play_pause",
                createPlayPausePendingIntent()
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ktm250)).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }


    }

    fun createPrevPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = PREV
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun createNextPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = NEXT
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun createPlayPausePendingIntent(): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = PLAY_PAUSE
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


}