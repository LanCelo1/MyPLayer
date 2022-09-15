package uz.gita.myplayer.presentation.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.processNextEventInCurrentThread
import uz.gita.myplayer.R
import uz.gita.myplayer.data.MusicData
import uz.gita.myplayer.data.StateEnum
import uz.gita.myplayer.databinding.ScreenPlayBinding
import uz.gita.myplayer.presentation.ui.service.MyService
import uz.gita.myplayer.utils.*

class PlayScreen : Fragment(R.layout.screen_play) {
    val binding: ScreenPlayBinding by viewBinding(ScreenPlayBinding::bind)
    var isPlaying: Boolean = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadMusic()
        setUpObservers()
        setUpListeners()
    }

    private fun loadMusic() {
        binding.apply {
            if (MyAppManager.cursor?.count!! > 1) {
                var data =
                    MyAppManager.cursor?.getMusicByPosition(MyAppManager.lastSelectedPosition)
                textArtistName.setText(data?.title)
                textArtistName.setText(data?.artist)
                if (data?.album_id != null && requireContext().songArt(data.album_id)!= null){
                    subImageview.setImageURI(requireContext().songArt(data.album_id))
                }else {
                    subImageview.setImageResource(R.drawable.ic_music_disk)
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.apply {
            buttonManage.setOnClickListener {
                startMyService(StateEnum.MANAGE, MyAppManager.process.toInt())
            }
            buttonNext.setOnClickListener {
                startMyService(StateEnum.NEXT)
            }
            buttonPrev.setOnClickListener {
                startMyService(StateEnum.PREV)
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    MyAppManager.process = progress
                    if (fromUser) {
//                        seekBar.progress = progress
//                        currentTime.text = progress.toLong().toFormattedString()
                            MyAppManager.currentTimeListener.value = progress.toLong()
//                        if (isPlaying) startMyService(StateEnum.PLAY, progress)
//                        else startMyService(StateEnum.PAUSE, progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    if (isPlaying) startMyService(StateEnum.PLAY, MyAppManager.process)
                    else startMyService(StateEnum.PAUSE, MyAppManager.process)
                }
            })
        }
    }

    private fun setUpObservers() {
        MyAppManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        MyAppManager.isPlayingMusic.observe(viewLifecycleOwner, isPlayingObserver)
        MyAppManager.currentTimeListener.observe(viewLifecycleOwner, currentTimeObserver)
    }

    val currentTimeObserver = Observer<Long> {
//        binding.seekBar.progress = (MyAppManager.currentTime * 100 / MyAppManager.fullTime).toInt()
        binding.seekBar.progress = it.toInt()
        binding.currentTime.setText(it.toFormattedString())
//        binding.seekBar.progress = it.toInt()
//        binding.currentTime.text = MyAppManager.currentTime.toFormattedString()
    }
    val isPlayingObserver = Observer<Boolean> {
        isPlaying = it
        binding.apply {
            if (it) {
                buttonManage.setImageResource(R.drawable.ic_pause_btn)
            } else {
                buttonManage.setImageResource(R.drawable.ic_play_btn)
            }
        }
    }
    val playMusicObserver = Observer<MusicData> {
        binding.apply {
            textArtistName.setText(it.artist)
            textMusicName.setText(it.title)
            if (it?.album_id != null && requireContext().songArt(it.album_id)!= null){
                subImageview.setImageURI(requireContext().songArt(it.album_id))
            }else {
                subImageview.setImageResource(R.drawable.ic_music_disk)
            }
            currentTime.setText(MyAppManager.currentTime.toFormattedString())
            totalTime.setText(it.duration.toFormattedString())
//            seekBar.progress = (MyAppManager.currentTime * 100 / MyAppManager.fullTime).toInt()
//            seekBar.progress = (MyAppManager.currentTime * 100 / MyAppManager.fullTime).toInt()
            seekBar.max = it.duration.toInt()
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

}