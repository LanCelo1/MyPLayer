package uz.gita.myplayer.utils

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import uz.gita.myplayer.data.MusicData

object MyAppManager {
    var cursor : Cursor? = null
    var lastSelectedPosition = 0
    var isPlayingMusic= MutableLiveData<Boolean>()
    var playMusicLiveData  = MutableLiveData<MusicData>()

    var currentTime = 0L
    var fullTime = 0L
    var currentTimeListener =  MutableLiveData<Long>()
    var postSeekBarchangeListener =  MutableLiveData<Int>()
    var process : Int = 0
}