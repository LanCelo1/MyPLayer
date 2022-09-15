package uz.gita.myplayer.presentation.ui.screens

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.gita.myplayer.R
import uz.gita.myplayer.data.MusicData
import uz.gita.myplayer.data.StateEnum
import uz.gita.myplayer.databinding.ScreenMusicListBinding
import uz.gita.myplayer.presentation.ui.adapter.MyCursorAdapter
import uz.gita.myplayer.presentation.ui.service.MyService
import uz.gita.myplayer.utils.MyAppManager
import uz.gita.myplayer.utils.getMusicByPosition
import uz.gita.myplayer.utils.log
import uz.gita.myplayer.utils.songArt

class MusicListScreen : Fragment(R.layout.screen_music_list) {
    val binding: ScreenMusicListBinding by viewBinding(ScreenMusicListBinding::bind)
    val cursorAdapter = MyCursorAdapter()


    private fun loadMusic() {
        binding.apply {
            if (MyAppManager.cursor?.count!! > 1) {
                var data =
                    MyAppManager.cursor?.getMusicByPosition(MyAppManager.lastSelectedPosition)
                textMusicNameScreen.setText(data?.title)
                textArtistNameScreen.setText(data?.artist)
                imageMusic.setImageURI(requireContext().songArt(data?.album_id!!))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadMusic()
        setUpRecyclerView()
//        startMyService(StateEnum.PAUSE)
        setUpListeners()
        setUpObservers()
    }

    private fun setUpObservers() {
        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingMusic.observe(viewLifecycleOwner, isPlayMusicObserver)

    }

    private fun setUpListeners() {
        binding.apply {
            buttonManageScreen.setOnClickListener {
                startMyService(StateEnum.MANAGE, MyAppManager.process)
            }
            buttonNextScreen.setOnClickListener {
                startMyService(StateEnum.NEXT)
            }
            buttonPrevScreen.setOnClickListener {
                startMyService(StateEnum.PREV)
            }
            bottomPart.setOnClickListener {
                findNavController().navigate(R.id.action_musicListScreen_to_playScreen)
            }
        }

    }

    private fun setUpRecyclerView() {
        binding.apply {
            musicList.apply {
                layoutManager = LinearLayoutManager(this@MusicListScreen.context)
                cursorAdapter.cursor = MyAppManager.cursor
                adapter = cursorAdapter
                cursorAdapter.setSelectMusicListListener {
                    log(message = "lastSelectedPosition $it")
                    MyAppManager.lastSelectedPosition = it
                    startMyService(StateEnum.PLAY)
                    MyAppManager.process = 0
                }
            }
        }
    }

    private fun startMyService(state: StateEnum, progress: Int = 0) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("STATE", state)
        intent.putExtra("PROGRESS", progress)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(intent)
        } else {
            requireActivity().startService(intent)
        }
    }

    val playMusicObserver = Observer<MusicData> { data ->
        binding.apply {
            textMusicNameScreen.setText(data.title)
            textArtistNameScreen.setText(data.artist)
            if (data?.album_id != null && requireContext().songArt(data.album_id)!= null){
                imageMusic.setImageURI(requireContext().songArt(data.album_id))
            }else {
                imageMusic.setImageResource(R.drawable.ic_music_disk)
            }
        }
    }
    val isPlayMusicObserver = Observer<Boolean> { state ->
        binding.apply {
            if (state) {
                buttonManageScreen.setImageResource(R.drawable.ic_pause_btn)
            } else {
                buttonManageScreen.setImageResource(R.drawable.ic_play_btn)
            }
        }
    }

    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
            albumId)
    }
}