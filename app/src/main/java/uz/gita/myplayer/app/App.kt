package uz.gita.myplayer.app

import android.app.Application
import timber.log.Timber
import uz.gita.myplayer.BuildConfig


class App() : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}