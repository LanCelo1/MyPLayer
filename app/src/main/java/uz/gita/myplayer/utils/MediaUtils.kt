package uz.gita.myplayer.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import uz.gita.myplayer.data.MusicData


private val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.DATA,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.ALBUM_ID
)

fun Context.getMusicCursor(): Flow<Cursor> = flow{
    val cursor : Cursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        MediaStore.Audio.Media.IS_MUSIC +"!=0",
        null,
        null
    ) ?: return@flow
    emit(cursor)
}.flowOn(Dispatchers.IO)


fun Cursor.getMusicByPosition(pos : Int) : MusicData {
    MyAppManager.cursor?.moveToPosition(pos)

    val music =  MusicData(
        this.getInt(0),
        this.getString(1),
        this.getString(2),
        this.getString(3),
        this.getLong(4),
        this.getLong(5)
    )
    log(message = "$music")
    return music
}

fun SeekBar.setChangeProgress(block : (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let {
                block.invoke(it.progress)
            }
        }

    })

}
fun Context.songArt(albumId: Long): Uri? {
    try {
        val sArtworkUri: Uri = Uri
            .parse("content://media/external/audio/albumart")
        val uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        val pfd: ParcelFileDescriptor? = this.contentResolver
            .openFileDescriptor(uri, "r")
        if (pfd != null) {
            return uri
        }
    } catch (e: Exception) {
        Log.d("TTT",e.message.toString())
    }
    return null
}