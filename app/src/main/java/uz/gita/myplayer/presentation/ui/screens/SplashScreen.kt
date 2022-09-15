package uz.gita.myplayer.presentation.ui.screens

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.myplayer.R
import uz.gita.myplayer.utils.MyAppManager
import uz.gita.myplayer.utils.checkPermissions
import uz.gita.myplayer.utils.getMusicCursor

class SplashScreen : Fragment(R.layout.screen_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().checkPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requireContext().getMusicCursor().onEach {
                    MyAppManager.cursor = it
                    delay(2000)
                    findNavController().navigate(R.id.action_splashScreen_to_musicListScreen)
                }.launchIn(lifecycleScope)
        }
    }
}