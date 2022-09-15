package uz.gita.myplayer.utils

import android.content.Context
import android.widget.Toast
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun log(tag : String = "TTT", message : String){
    Timber.tag(tag).d(message)
}

fun toast(context : Context,message: String){
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}

fun Long.toFormattedString(): String =
    String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(
                this
            )
        ),
        TimeUnit.MILLISECONDS.toSeconds(
            this
        ) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    )