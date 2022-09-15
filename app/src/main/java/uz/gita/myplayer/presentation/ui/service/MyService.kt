package uz.gita.myplayer.presentation.ui.service

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import uz.gita.myplayer.MainActivity
import uz.gita.myplayer.R
import uz.gita.myplayer.data.MusicData
import uz.gita.myplayer.data.StateEnum
import uz.gita.myplayer.utils.MyAppManager
import uz.gita.myplayer.utils.getMusicByPosition
import uz.gita.myplayer.utils.log
import uz.gita.myplayer.utils.toFormattedString
import java.io.File

class MyService : Service() {
    val CHANNEL_ID = "DEMO"
    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private val scope  = CoroutineScope(Dispatchers.IO + Job())
    private var job : Job? = null

    override fun onBind(intent: Intent?): IBinder?  = null
    override fun onCreate() {
        super.onCreate()
        _mediaPlayer = MediaPlayer()
        createNotificationChannel()
        startMyForegroundService()

    }

    private fun createRemoteView(): RemoteViews {
        val view = RemoteViews(this.packageName, R.layout.remote_view)
        val musicData = MyAppManager.cursor?.getMusicByPosition(MyAppManager.lastSelectedPosition)
        view.setTextViewText(R.id.textMusicName, musicData?.title)
        view.setTextViewText(R.id.textArtistName, musicData?.artist)

        if (mediaPlayer.isPlaying) {
            view.setImageViewResource(R.id.buttonManage, R.drawable.ic_pause_bnt_light)
        } else {
            view.setImageViewResource(R.id.buttonManage, R.drawable.ic_play_btn)
        }
        view.setOnClickPendingIntent(R.id.buttonNext, createPendingIntent(StateEnum.NEXT))
        view.setOnClickPendingIntent(R.id.buttonPrev, createPendingIntent(StateEnum.PREV))
        view.setOnClickPendingIntent(R.id.buttonManage, createPendingIntent(StateEnum.MANAGE,MyAppManager.process))
        view.setOnClickPendingIntent(R.id.buttonCancel, createPendingIntent(StateEnum.CANCEL))
        return view
    }

    private fun createPendingIntent(state: StateEnum,process: Int = 0): PendingIntent {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("STATE", state)
        intent.putExtra("PROGRESS",process)
        return PendingIntent.getService(this,
            state.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private fun startMyForegroundService() {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setCustomContentView(createRemoteView())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentPendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("DEMO", CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startMyForegroundService()
        val command = intent?.extras?.getSerializable("STATE") as StateEnum
        val progress = intent?.extras?.getInt("PROGRESS",0)
        doCommand(command,if (progress==0) MyAppManager.process else progress!!)
        return START_NOT_STICKY
    }

    private fun doCommand(command: StateEnum,process: Int) {
        val musicData: MusicData =
            MyAppManager.cursor?.getMusicByPosition(MyAppManager.lastSelectedPosition)!!
        when (command) {
            StateEnum.MANAGE -> {
                if (mediaPlayer.isPlaying) doCommand(StateEnum.PAUSE,process) else doCommand(StateEnum.PLAY,process)
            }
            StateEnum.PLAY -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                _mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(musicData.data ?: "")))
                mediaPlayer.start()
                mediaPlayer.seekTo(process)
                MyAppManager.process = process
                mediaPlayer.setOnCompletionListener { doCommand(StateEnum.NEXT,0) }
                MyAppManager.playMusicLiveData.value = musicData
                MyAppManager.isPlayingMusic.value = true

                MyAppManager.fullTime = musicData.duration
                MyAppManager.currentTime = process.toLong()
                job?.let { it.cancel() }
                job = scope.launch {
                    changeProgress().collectLatest {
                        MyAppManager.currentTime = it
                        MyAppManager.currentTimeListener.postValue(it)
                    }
                }
                startMyForegroundService()
            }
            StateEnum.PAUSE -> {
                mediaPlayer.pause()
                job?.cancel()
                mediaPlayer.seekTo(process)
                MyAppManager.process = process
                MyAppManager.currentTime = process.toLong()
                MyAppManager.isPlayingMusic.value = false
                startMyForegroundService()
            }
            StateEnum.PREV -> {
                if (MyAppManager.lastSelectedPosition == 0){
                    MyAppManager.lastSelectedPosition = MyAppManager.cursor?.count!! - 1
                }else {
                    MyAppManager.lastSelectedPosition --
                }
                MyAppManager.currentTime = 0
                doCommand(StateEnum.PLAY,0)
            }
            StateEnum.NEXT -> {
                if (MyAppManager.lastSelectedPosition == MyAppManager.cursor?.count!!-1){
                    MyAppManager.lastSelectedPosition = 0
                }else {
                    MyAppManager.lastSelectedPosition ++
                }
                MyAppManager.currentTime = 0
                doCommand(StateEnum.PLAY,0)
            }
            StateEnum.CANCEL -> {
                MyAppManager.isPlayingMusic.value = false
                mediaPlayer.stop()
                stopSelf()
            }
        }
    }

    fun changeProgress() : Flow<Long> = flow {
        for (i in MyAppManager.currentTime .. MyAppManager.fullTime step 250){
            emit(i)
            delay(250)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        log(message = "OnDestroy")
    }
}